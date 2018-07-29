package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Image;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ModuleLookAt extends Module implements ICommandModule {

	@Override
	public String getName() {
		return "Look At";
	}

	@Override
	public String getDescription() {
		return "Albus will analyze a given image or image url and display what he thinks he's looking at.";
	}

	@Override
	public String getUsage() {
		return "hey albus, look at this image for me pls <image or link>";
	}

	@Override
	public String getExample() {
		return "'hey albus, look at this pic https://i.imgur.com/kctg7Ge.png'";
	}

	@Override
	public String getActionID() {
		return "input.look_at";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	public static BufferedImage resizeProportionally(BufferedImage bufferedImage, int scaledWidth, int scaledHeight) {
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, bufferedImage.getType());

		Graphics2D g2d = outputImage.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Dimension scaled = getScaledDimension(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()), new Dimension(scaledWidth, scaledHeight));
		g2d.drawImage(bufferedImage, (int) (scaledWidth / 2.0 - scaled.width / 2.0), (int) (scaledHeight / 2.0 - scaled.height / 2.0), scaled.width, scaled.height, null);
		g2d.dispose();

		return outputImage;
	}

	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

		double requiredWidth, requiredHeight;
		double targetRatio = boundary.getWidth() / boundary.getHeight();
		double sourceRatio = imgSize.getWidth() / imgSize.getHeight();
		if (sourceRatio >= targetRatio) { // source is wider than target in proportion
			requiredWidth = boundary.getWidth();
			requiredHeight = requiredWidth / sourceRatio;
		} else { // source is higher than target in proportion
			requiredHeight = boundary.getHeight();
			requiredWidth = requiredHeight * sourceRatio;
		}
		return new Dimension((int) requiredWidth, (int) requiredHeight);
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {

				long benchmark = System.currentTimeMillis();

				HashSet<String> urls = new HashSet<>();
				String inputURL = result.getStringParameter("url");
				if (inputURL.isEmpty()) {
					for (MessageAttachment attachment : message.getAttachments()) {
						Image image = ImageIO.read(attachment.getUrl());
						if (image != null) {
							urls.add(attachment.getUrl().toString());
						}
					}
				} else urls.add(inputURL);

				if (urls.isEmpty()) {
					message.getChannel().sendMessage("What image would you like me to look at? Rephrase your sentence.");
				} else {
					message.getChannel().sendMessage("Alright! Give me a minute...").get();
				}

				for (String url : urls) {
					if (!Domains.INSTANCE.isLinkWhitelisted(url)) {
						message.getChannel().sendMessage("Link `" + url + "` is not a whitelisted domain, sorry.");
						continue;
					}

					List<AnnotateImageRequest> requests = new ArrayList<>();

					File file = new File("tempFile.png");
					if (file.exists()) {
						file.delete();
					}

					message.getChannel().sendMessage("Downloading image...");

					URL urlObject = new URL(url);
					URLConnection urlConnection = urlObject.openConnection();
					urlConnection.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");

					BufferedImage img = ImageIO.read(urlConnection.getInputStream());
					if (img == null) {
						message.getChannel().sendMessage("Couldn't download image. Please try again with another link.");
						return;
					}

					img = resizeProportionally(img, 1920, 1080);
					ImageIO.write(img, "png", file);

					ByteString imgBytes = ByteString.readFrom(new FileInputStream(file));
					com.google.cloud.vision.v1.Image sourceImage = com.google.cloud.vision.v1.Image.newBuilder().setContent(imgBytes).build();

					AnnotateImageRequest request =
							AnnotateImageRequest.newBuilder()
									.addFeatures(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build())
									.addFeatures(Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build())
									.addFeatures(Feature.newBuilder().setType(Feature.Type.WEB_DETECTION).build())
									.addFeatures(Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build())
									.addFeatures(Feature.newBuilder().setType(Feature.Type.LOGO_DETECTION).build())
									.addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
									.setImage(sourceImage)
									.build();
					requests.add(request);

					message.getChannel().sendMessage("Uploading to google...");

					ImageAnnotatorClient client = ImageAnnotatorClient.create();

					BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
					List<AnnotateImageResponse> responses = response.getResponsesList();

					message.getChannel().sendMessage("Processing results...");

					for (AnnotateImageResponse res : responses) {

						StringBuilder logos = new StringBuilder();
						for (EntityAnnotation annotation : res.getLogoAnnotationsList()) {
							logos.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
						}

						StringBuilder text = new StringBuilder();
						HashSet<EntityAnnotation> deque = new HashSet<>(res.getTextAnnotationsList());
						for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
							deque.remove(annotation);
							String copy = text.toString().replace("\n", " ");
							if (copy.split(" ").length >= deque.size()) break;
							text.append(annotation.getDescription()).append("\n");
						}

						StringBuilder landmarks = new StringBuilder();
						for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
							landmarks.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
						}

						StringBuilder labels = new StringBuilder();
						for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
							labels.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
						}

						StringBuilder webLabels = new StringBuilder();
						WebDetection web = res.getWebDetection();
						for (WebDetection.WebEntity annotation : web.getWebEntitiesList()) {
							webLabels.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
						}

						for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
							Color randomColor = createRandomBrightColor(annotation.hashCode());
							Graphics2D graphics = img.createGraphics();
							graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
							graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
									RenderingHints.VALUE_RENDER_QUALITY);

							graphics.setStroke(new BasicStroke(5));
							graphics.setColor(Color.BLACK);

							Vertex prevV = null;
							for (Vertex vertex : annotation.getBoundingPoly().getVerticesList()) {
								if (prevV == null) prevV = vertex;
								graphics.drawLine(vertex.getX(), vertex.getY(), prevV.getX(), prevV.getY());
								prevV = vertex;
							}
							Vertex lastV = annotation.getBoundingPoly().getVerticesList().get(0);
							graphics.drawLine(lastV.getX(), lastV.getY(), prevV.getX(), prevV.getY());
							prevV = null;

							graphics.setStroke(new BasicStroke(3));
							graphics.setColor(randomColor);

							for (Vertex vertex : annotation.getBoundingPoly().getVerticesList()) {
								if (prevV == null) prevV = vertex;
								graphics.drawLine(vertex.getX(), vertex.getY(), prevV.getX(), prevV.getY());
								prevV = vertex;
							}
							lastV = annotation.getBoundingPoly().getVerticesList().get(0);
							graphics.drawLine(lastV.getX(), lastV.getY(), prevV.getX(), prevV.getY());
						}

						for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
							Color randomColor = createRandomBrightColor(annotation.hashCode());
							Graphics2D graphics = img.createGraphics();
							graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
							graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
									RenderingHints.VALUE_RENDER_QUALITY);
							for (FaceAnnotation.Landmark landmark : annotation.getLandmarksList()) {
								if (!landmark.hasPosition()) continue;

								Position pos = landmark.getPosition();

								graphics.setColor(Color.BLACK);
								Ellipse2D.Double circle = new Ellipse2D.Double((int) pos.getX() - 5, (int) pos.getY() - 5, 10, 10);
								graphics.fill(circle);

								graphics.setColor(randomColor);
								circle = new Ellipse2D.Double((int) pos.getX() - 3, (int) pos.getY() - 3, 6, 6);
								graphics.fill(circle);
							}
							graphics.dispose();
						}

						for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {

							Color randomColor = createRandomBrightColor(annotation.hashCode());
							Graphics2D graphics = img.createGraphics();
							graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
							graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
									RenderingHints.VALUE_RENDER_QUALITY);

							StringBuilder description = new StringBuilder();
							if (annotation.getAngerLikelihoodValue() > 1) {
								description.append("| Anger").append("\n");
							}
							if (annotation.getJoyLikelihoodValue() > 1) {
								description.append("| Joy").append("\n");
							}
							if (annotation.getSurpriseLikelihoodValue() > 1) {
								description.append("| Surprised").append("\n");
							}
							if (annotation.getHeadwearLikelihoodValue() > 1) {
								description.append("| Head Wear").append("\n");
							}
							if (annotation.getBlurredLikelihoodValue() > 1) {
								description.append("| Blurred").append("\n");
							}
							if (annotation.getSorrowLikelihoodValue() > 1) {
								description.append("| Sorrow").append("\n");
							}

							Vertex lastV = annotation.getBoundingPoly().getVerticesList().get(annotation.getBoundingPoly().getVerticesList().size() - 1);

							renderText(img, description.toString(), lastV.getX(), lastV.getY() + 24, randomColor);
						}

						if (!file.exists()) file.createNewFile();
						ImageIO.write(img, "png", file);

						Map finalMap = null;
						if (!res.getFaceAnnotationsList().isEmpty()) {
							message.getChannel().sendMessage("Uploading...");
							finalMap = WizardryBot.cloudinary.uploader().upload(file, new HashMap());
						}

						EmbedBuilder builder = new EmbedBuilder()
								.setTitle("Analysis")
								.setThumbnail(url);

						if (finalMap != null) {
							builder.setImage((String) finalMap.get("url"))
									.setUrl((String) finalMap.get("url"));
						} else {
							builder.setImage(url).setUrl(url);
						}
						//.setTitle("Size: " + eyeball.getMetadata().getWidth() + "x" + eyeball.getMetadata().getHeight() + " Format: " + eyeball.getMetadata().getFormat().toLowerCase() + " Size: " + (Math.round(fileSize * 100.0) / 100.0) + " " + fileSizeType)
						if (!labels.toString().isEmpty())
							builder.addField("Descriptions", labels.toString(), false);
						if (!webLabels.toString().isEmpty())
							builder.addField("Labels", webLabels.toString(), false);
						if (!landmarks.toString().isEmpty())
							builder.addField("Landmarks", landmarks.toString(), false);
						if (!logos.toString().isEmpty())
							builder.addField("Logos", webLabels.toString(), false);
						if (!text.toString().isEmpty())
							builder.addField("Text", text.toString(), false);

						message.getChannel().sendMessage("Done! Took me " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - benchmark) + " seconds", builder);
						file.delete();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				message.getChannel().sendMessage("Woopsies. Something went wrong! :(").thenAccept(message1 -> {
					message1.getChannel().sendMessage("```" + e.getMessage() + "```");
				});
			}
		}));
	}

	private Color createRandomBrightColor(long seed) {
		Random random = new Random(seed);
		float h = random.nextFloat();
		float s = 1f;
		float b = 0.9f + ((1f - 0.9f) * random.nextFloat());
		return Color.getHSBColor(h, s, b);
	}

	private void renderText(BufferedImage image, String text, float x, float y, Color color) {
		BasicStroke outlineStroke = new BasicStroke(2.0f);
		Graphics2D graphics = image.createGraphics();

		int FONT_SIZE = 24;

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		graphics.translate(x, y);

		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			GlyphVector glyphVector = new Font("Arial", Font.PLAIN, FONT_SIZE).createGlyphVector(graphics.getFontRenderContext(), line);
			Shape textShape = glyphVector.getOutline();

			graphics.translate(0, i * FONT_SIZE);

			graphics.setColor(Color.BLACK);
			graphics.setStroke(outlineStroke);
			graphics.draw(textShape); // draw outline

			graphics.setColor(color);
			graphics.fill(textShape); // fill the shape

			graphics.translate(0, -i * FONT_SIZE);
		}
		graphics.dispose();
	}

}
