package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;


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

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {
		try {

			long benchmark = System.currentTimeMillis();

			Set<BufferedImage> images = new HashSet<>();
			String inputURL = result.getStringParameter("url");

			if (!inputURL.isEmpty()) {
				BufferedImage img = Utils.downloadURLAsImage(null, inputURL);
				if (img != null)
					images.add(img);
			} else {
				images.addAll(Utils.stupidVerboseImageSearch(message));
			}

			if (inputURL.isEmpty() && images.isEmpty()) {
				message.getChannel().sendMessage("What image would you like me to look at? Rephrase your sentence.");
			} else {
				message.getChannel().sendMessage("Alright! Give me a minute...").get();
			}

			for (BufferedImage img : images) {
				List<AnnotateImageRequest> requests = new ArrayList<>();

				File file = new File("downloads/" + UUID.randomUUID() + ".png");
				if (file.exists()) {
					file.delete();
				}

				img = Utils.resizeProportionally(img, 1920, 1080);
				ImageIO.write(img, "png", file);

				Graphics2D graphics = img.createGraphics();
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);


				ByteString imgBytes = ByteString.readFrom(new FileInputStream(file));
				Image sourceImage = Image.newBuilder().setContent(imgBytes).build();

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

				Credentials myCredentials = ServiceAccountCredentials.fromStream(
						new FileInputStream("C:\\vision-189116-445d469f591a.json"));
				ImageAnnotatorSettings imageAnnotatorSettings =
						ImageAnnotatorSettings.newBuilder()
								.setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
								.build();
				ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings);

				BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
				List<AnnotateImageResponse> responses = response.getResponsesList();

				for (AnnotateImageResponse res : responses) {

					StringBuilder logos = new StringBuilder();
					for (EntityAnnotation annotation : res.getLogoAnnotationsList()) {
						if (annotation.getDescription().isEmpty()) continue;
						logos.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
					}

					StringBuilder text = new StringBuilder();
					HashSet<EntityAnnotation> deque = new HashSet<>(res.getTextAnnotationsList());
					for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
						if (annotation.getDescription().isEmpty()) continue;
						deque.remove(annotation);
						String copy = text.toString().replace("\n", " ");
						if (copy.split(" ").length >= deque.size()) break;
						text.append(annotation.getDescription()).append("\n");
					}

					StringBuilder landmarks = new StringBuilder();
					for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
						if (annotation.getDescription().isEmpty()) continue;
						landmarks.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
					}

					StringBuilder labels = new StringBuilder();
					for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
						if (annotation.getDescription().isEmpty()) continue;
						labels.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
					}

					StringBuilder webLabels = new StringBuilder();
					WebDetection web = res.getWebDetection();
					for (WebDetection.WebEntity annotation : web.getWebEntitiesList()) {
						if (annotation.getDescription().isEmpty()) continue;
						webLabels.append("| **").append(annotation.getDescription()).append("**: ").append(annotation.getScore()).append("\n");
					}

					for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
						Color randomColor = createRandomBrightColor(annotation.hashCode());
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
					}

					AzureEyeball azure;
					// AZURE
					{
						JsonObject object = new JsonObject();
						object.addProperty("url", ImgurUploader.upload(file));
						HttpResponse<JsonNode> response1 = Unirest
								.post("https://microsoft-azure-microsoft-computer-vision-v1.p.mashape.com/analyze?details=Celebrities%2CLandmarks&visualfeatures=Categories%2CTags%2CColor%2CFaces%2CDescription")
								.header("X-Mashape-Key", "CHSZKNeIrAmshAcMpGUkVFPSrnTRp1aRW5KjsnbPB676iBivmt")
								.header("X-Mashape-Host", "microsoft-azure-microsoft-computer-vision-v1.p.mashape.com")
								.header("Content-Type", "application/json")
								.body(object.toString())
								.asJson();

						JsonElement element = new JsonParser().parse(response1.getBody().toString());
						azure = new AzureEyeball(element);
					}


					for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
						Color randomColor = createRandomBrightColor(annotation.hashCode());

						StringBuilder description = new StringBuilder();

						if (annotation.getDetectionConfidence() >= 0.5) {
							description.append("| Detection Confidence: ")
									.append((int) Math.round(annotation.getDetectionConfidence() * 100.0))
									.append("%")
									.append("\n");
						}

						AzureEyeball.SimpleFace closestFace = null;
						Set<Vec2d> googleCorners = new HashSet<>();
						Vec2d googleCentroid;
						Vec2d googleCorner1 = null;
						Vec2d googleCorner2 = null;
						for (Vertex vertex : annotation.getBoundingPoly().getVerticesList()) {
							googleCorners.add(new Vec2d(vertex.getX(), vertex.getY()));

							Vec2d v = new Vec2d(vertex.getX(), vertex.getY());
							if (googleCorner1 == null) googleCorner1 = v;
							if (googleCorner2 == null) googleCorner2 = v;

							if (v.length() > googleCorner2.length()) googleCorner2 = v;
							if (v.length() < googleCorner1.length()) googleCorner1 = v;

						}

						if (googleCorner1 != null && !googleCorners.isEmpty()) {
							googleCentroid = Utils.calculateCentroid(googleCorners);

							double shortestDist1 = Double.MAX_VALUE;
							for (AzureEyeball.SimpleFace face : azure.getFaces().getFaces()) {

								Set<Vec2d> azureCorners = new HashSet<>();
								azureCorners.add(face.getCorner1());
								azureCorners.add(face.getCorner2());
								Vec2d azureCentroid = Utils.calculateCentroid(azureCorners);

								double dist = azureCentroid.distance(googleCentroid);

								if (shortestDist1 > dist) {
									shortestDist1 = dist;
									closestFace = face;
								}
							}

							if (closestFace != null) {

								Set<Vec2d> azureCorners = new HashSet<>();
								azureCorners.add(closestFace.getCorner1());
								azureCorners.add(closestFace.getCorner2());
								Vec2d azureCentroid = Utils.calculateCentroid(azureCorners);

								double shortestDist2 = -1;
								AzureEyeball.Celebrity closestCeleb = null;
								for (AzureEyeball.Celebrity celebrity : azure.getCategories().getCelebrities()) {

									Set<Vec2d> celebrityCorners = new HashSet<>();
									celebrityCorners.add(celebrity.getCorner1());
									celebrityCorners.add(celebrity.getCorner2());
									Vec2d celebrityCentroid = Utils.calculateCentroid(celebrityCorners);

									if (shortestDist2 == -1) {
										closestCeleb = celebrity;
										shortestDist2 = celebrityCentroid.distance(azureCentroid);
										continue;
									}

									double dist2 = celebrityCentroid.distance(azureCentroid);
									if (shortestDist2 > dist2) {
										shortestDist2 = dist2;
										closestCeleb = celebrity;
									}
								}

								if (closestCeleb != null) {
									renderText(graphics, closestCeleb.getName().toLowerCase(), closestFace.getCorner1().add(100, 100), closestFace.getCorner1().add(100, 100), closestFace.getCorner2(), randomColor);
									//renderTextCentered(graphics, closestCeleb.getName().toUpperCase(), closestFace.getCorner1(), closestFace.getCorner2(), randomColor);
								}

								{

									Vec2d corner1 = closestFace.getCorner1();
									Vec2d corner2 = corner1.add(closestFace.getCorner2());
									Vec2d corner3 = new Vec2d(corner1.x, corner2.y);
									Vec2d corner4 = new Vec2d(corner2.x, corner1.y);

									graphics.setStroke(new BasicStroke(3));
									graphics.setColor(Color.BLACK);
									graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner3.x, (int) corner3.y);
									graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner3.x, (int) corner3.y);
									graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner4.x, (int) corner4.y);
									graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner4.x, (int) corner4.y);

									graphics.setStroke(new BasicStroke(1));
									graphics.setColor(randomColor);
									graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner3.x, (int) corner3.y);
									graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner3.x, (int) corner3.y);
									graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner4.x, (int) corner4.y);
									graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner4.x, (int) corner4.y);

									if (closestCeleb != null) {
										corner1 = closestCeleb.getCorner1();
										corner2 = corner1.add(closestCeleb.getCorner2());
										corner3 = new Vec2d(corner1.x, corner2.y);
										corner4 = new Vec2d(corner2.x, corner1.y);

										graphics.setStroke(new BasicStroke(3));
										graphics.setColor(randomColor);
										graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner3.x, (int) corner3.y);
										graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner3.x, (int) corner3.y);
										graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner4.x, (int) corner4.y);
										graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner4.x, (int) corner4.y);

										graphics.setStroke(new BasicStroke(1));
										graphics.setColor(Color.BLACK);
										graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner3.x, (int) corner3.y);
										graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner3.x, (int) corner3.y);
										graphics.drawLine((int) corner2.x, (int) corner2.y, (int) corner4.x, (int) corner4.y);
										graphics.drawLine((int) corner1.x, (int) corner1.y, (int) corner4.x, (int) corner4.y);
									}
								}
							}
						}

						if (closestFace != null) {
							description.append("| Age: ")
									.append(closestFace.getAge())
									.append("\n");

							description.append("| Gender: ")
									.append(closestFace.getGender())
									.append("\n");
						}

						if (annotation.getHeadwearLikelihoodValue() > 1) {
							description.append("| Head Wear: ")
									.append(StringUtils.capitalize(annotation.getHeadwearLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
									.append("\n");
						}
						if (annotation.getBlurredLikelihoodValue() > 1) {
							description.append("| Blurred: ")
									.append(StringUtils.capitalize(annotation.getBlurredLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
									.append("\n");
						}
						if (annotation.getUnderExposedLikelihoodValue() > 1) {
							description.append("| Under Exposed: ")
									.append(StringUtils.capitalize(annotation.getUnderExposedLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
									.append("\n");
						}

						if (annotation.getAngerLikelihoodValue() > 1
								|| annotation.getJoyLikelihoodValue() > 1
								|| annotation.getSurpriseLikelihoodValue() > 1
								|| annotation.getSorrowLikelihoodValue() > 1) {
							description.append("| Emotions:").append("\n");
							{
								if (annotation.getAngerLikelihoodValue() > 1) {
									description.append("| | Anger: ")
											.append(StringUtils.capitalize(annotation.getAngerLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
											.append("\n");
								}
								if (annotation.getJoyLikelihoodValue() > 1) {
									description.append("| | Joy: ")
											.append(StringUtils.capitalize(annotation.getJoyLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
											.append("\n");
								}
								if (annotation.getSurpriseLikelihoodValue() > 1) {
									description.append("| | Surprised: ")
											.append(StringUtils.capitalize(annotation.getSurpriseLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
											.append("\n");
								}
								if (annotation.getSorrowLikelihoodValue() > 1) {
									description.append("| | Sorrow: ")
											.append(StringUtils.capitalize(annotation.getSorrowLikelihood().getValueDescriptor().getName().replace("_", " ").toLowerCase()))
											.append("\n");
								}
							}
						}

						Vertex lastV = annotation.getBoundingPoly().getVerticesList().get(annotation.getBoundingPoly().getVerticesList().size() - 1);

						renderText(graphics, description.toString(), new Vec2d(lastV.getX(), lastV.getY() + 24), Vec2d.ZERO, new Vec2d(img.getWidth(), img.getHeight()), randomColor);
					}

					graphics.dispose();
					if (!file.exists()) file.createNewFile();
					ImageIO.write(img, "png", file);

					message.getChannel().sendMessage("Uploading...");

					//String stringURL = ImgurUploader.upload(file);
//
					//EmbedBuilder builder = new EmbedBuilder()
					//		.setTitle("Analysis");
//
					//if (stringURL != null && !stringURL.isEmpty()) {
					//	builder.setImage(stringURL)
					//			.setThumbnail(stringURL)
					//			.setUrl(stringURL);
					//} else {
					//	builder.setImage(img)
					//			.setThumbnail(img)
					//			.setUrl(inputURL);
					//}
					//if (azure != null) {
					//	{
					//		StringBuilder desc = new StringBuilder();
					//		if (!azure.getDescription().getCaptions().isEmpty()) {
					//			for (Map.Entry<String, Float> entry : azure.getDescription().getCaptions().entrySet()) {
					//				desc.append("| \"").append(entry.getKey()).append("\": ").append(entry.getValue()).append("\n");
					//			}
					//		}
					//		if (!desc.toString().isEmpty())
					//			builder.addField("Captions", desc.toString(), false);
					//	}
					//	{
					//		StringBuilder desc = new StringBuilder();
					//		if (!azure.getTags().getTags().isEmpty()) {
					//			for (Map.Entry<String, Float> entry : azure.getTags().getTags().entrySet()) {
					//				desc.append("| **").append(entry.getKey()).append("**: ").append(entry.getValue()).append("\n");
					//			}
					//		}
					//		if (!desc.toString().isEmpty())
					//			builder.addInlineField("Tags", desc.toString());
					//	}
					//	{
					//		builder.setColor(azure.getColor().getDominantColorForeground());
					//	}
					//	{
					//		builder.setTitle("Size: " + azure.getMetadata().getWidth() + "x" + azure.getMetadata().getHeight() + " Format: " + azure.getMetadata().getFormat().toLowerCase() + " Size: " + Utils.readableFileSize(file.length()));
					//	}
					//}
					//if (!labels.toString().isEmpty())
					//	builder.addInlineField("Entities", labels.toString());
					//if (!webLabels.toString().isEmpty())
					//	builder.addInlineField("Labels", webLabels.toString());
					//if (!landmarks.toString().isEmpty())
					//	builder.addInlineField("Landmarks", landmarks.toString());
					//if (!logos.toString().isEmpty())
					//	builder.addInlineField("Logos", webLabels.toString());
					//if (!text.toString().isEmpty())
					//	builder.addInlineField("Text", text.toString());
//
					//message.getChannel().sendMessage("Done! Took me " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - benchmark) + " seconds", builder);
					//file.delete();
					//Statistics.INSTANCE.addToStat("images_looked_at");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			message.getChannel().sendMessage("Woopsies. Something went wrong! :(").thenAccept(message1 -> {
				message1.getChannel().sendMessage("```" + e.getMessage() + "```");
			});
		}

		return true;
	}

	private Color createRandomBrightColor(long seed) {
		Random random = new Random(seed);
		float h = random.nextFloat();
		float s = 1f;
		float b = 0.9f + ((1f - 0.9f) * random.nextFloat());
		return Color.getHSBColor(h, s, b);
	}

	private void renderText(Graphics2D graphics, String text, Vec2d pos, Vec2d imgCorner, Vec2d imgSize, Color color) {
		int FONT_SIZE = 24;

		String[] lines = text.split("\n");
		Font font = new Font("TimesRoman", Font.PLAIN, FONT_SIZE);
		Vec2d size = Vec2d.ZERO;
		for (String line : lines) {
			Rectangle2D textBounds = graphics.getFontMetrics(font).getStringBounds(line, graphics);
			size.y += textBounds.getHeight();
			if (size.x < textBounds.getWidth()) {
				size.x = textBounds.getWidth();
			}
		}

		Vec2d total = pos.add(size);
		while (total.y > imgSize.y) {
			if (--pos.y <= imgCorner.y) {
				break;
			}
			total = pos.add(size);
		}

		while (total.x > imgSize.x) {
			if (--pos.x <= imgCorner.x) {
				break;
			}
			total = pos.add(size);
		}

		graphics.translate(pos.x, pos.y);

		BasicStroke outlineStroke1 = new BasicStroke(1.5f);
		BasicStroke outlineStroke2 = new BasicStroke(5.0f);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			Rectangle2D textBounds = graphics.getFontMetrics(font).getStringBounds(line, graphics);
			GlyphVector glyphVector = font.createGlyphVector(graphics.getFontRenderContext(), line);
			Shape textShape = glyphVector.getOutline();

			graphics.translate(0, i * textBounds.getHeight());

			graphics.setColor(Color.BLACK);
			graphics.setStroke(outlineStroke2);
			graphics.draw(textShape); // draw outline

			graphics.setColor(color);
			graphics.setStroke(outlineStroke1);
			graphics.draw(textShape); // draw outline

			graphics.setColor(Color.WHITE);
			graphics.fill(textShape); // fill the shape

			graphics.translate(0, -i * textBounds.getHeight());
		}
		graphics.translate(-pos.x, -pos.y);
	}

	private void renderTextCentered(Graphics2D graphics, String text, Vec2d imgCorner, Vec2d imgSize, Color color) {
		BasicStroke outlineStroke = new BasicStroke(5.0f);

		int FONT_SIZE = 20;

		String[] lines = text.split("\n");
		Font font = new Font("TimesRoman", Font.PLAIN, FONT_SIZE / lines.length);
		Vec2d size = Vec2d.ZERO;
		for (String line : lines) {
			Rectangle2D textBounds = graphics.getFontMetrics(font).getStringBounds(line, graphics);
			size.y += textBounds.getHeight();
			if (size.x < textBounds.getWidth()) {
				size.x = textBounds.getWidth();
			}
		}

		graphics.translate(imgCorner.x, imgCorner.y);
		graphics.translate((imgSize.x / 2) - (size.x / 2), (imgSize.y / 2) - (size.y / 2));

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			Rectangle2D textBounds = graphics.getFontMetrics(font).getStringBounds(line, graphics);
			GlyphVector glyphVector = font.createGlyphVector(graphics.getFontRenderContext(), line);
			Shape textShape = glyphVector.getOutline();

			graphics.translate(0, i * textBounds.getHeight());

			graphics.setColor(Color.BLACK);
			graphics.setStroke(outlineStroke);
			graphics.draw(textShape); // draw outline

			graphics.setColor(color);
			graphics.fill(textShape); // fill the shape

			graphics.translate(0, -i * textBounds.getHeight());
		}
		graphics.translate(-((imgSize.x / 2) - (size.x / 2)), -((imgSize.y / 2) - (size.y / 2)));
		graphics.translate(-imgCorner.x, -imgCorner.y);
	}
}
