package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

public class ModuleMemeGen extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"meme", "genmeme", "creatememe", "makememe"};
	}

	@Override
	public String getName() {
		return "Meme Generator";
	}

	@Override
	public String getDescription() {
		return "Will create a meme just the way you want";
	}

	@Override
	public String getUsage() {
		return "This command has several ways you can use it" + "\n" +
				"hey albus, meme <list <meme to search for>>" + "\n" +
				"hey albus, meme <id> <top text> [bottom text] but both need to be within `[ ]`. Bottom text is optional (see examples)" + "\n" +
				"hey albus, meme <id> <any amount of boxes you want in the meme>. A box is where you specify everything about the text and its formatting" + "\n" +
				"{ } This is a box." + "\n" +
				"{loc=top; text=slaps roof of car; color=magenta}" + "\n" +
				"{loc=upper left; text=slaps roof of car; color=azure; outline_color=brown; outline_width=8; font=arial; italics=true; bold=true; caps=false; font_size=30}" + "\n" +
				"The two required parameters in a box are `loc` and `text`. All else has a default." + "\n" +
				"DO NOT WORRY ABOUT FORMATTING. You can add as many spaces and junk as you want in the boxes. I handle them properly so you can't easily fuck shit up." + "\n";
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	@Override
	public String getExample() {
		return "hey albus, meme 61579 [one does not simply] [make a meme]" + "\n" +
				"hey albus, meme 143318362 {loc=center right; text=slaps roof of car; color=white}" + "\n" +
				"hey albus, meme 143318362 {loc=bottomleft; text=slaps roof of car; color=royal blue, size = 234}" + "\n" +
				"hey albus, meme 143318362 {loc=upper right; text=slaps roof of car; color=OldLace} {loc=center; text=this bad boy can hold so many features; outline color=red}" + "\n" +
				"hey albus, meme 143318362 {loc=center; text=slaps roof of car; color=magenta, outline width = 42}" + "\n\n" +
				"hey albus, meme 97630774\n" +
				" [loc = center left; text = test1; x += 150; y -= 100] \n" +
				" [loc = center right; text = test2; x -= 150; y -= 100] \n" +
				" [link = https://i.imgur.com/IQvYr96.jpg; height = 300; loc = bottom left; x += 100] \n" +
				" [link = https://i.imgur.com/jRuwgxf.jpg; height = 300; loc = bottom right; x -= 50]";
	}

	private void incorrectCommand(Message message) {
		message.getChannel().sendMessage("Incorrect command usage");
		message.getChannel().sendMessage("Usage: `" + getUsage() + "`");
		message.getChannel().sendMessage("Example: `" + getExample() + "`");
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		String[] args = command.getCommandArguments().split(" ");

		Statistics.INSTANCE.addToStat("attempted_meme_generations");
		if (args.length <= 0) {
			incorrectCommand(message);
			return;
		}

		if (args[0].equalsIgnoreCase("list") && args.length > 1) {
			String memeSearchQuery = command.getCommandArguments().replace("list", "").trim();

			try {
				Document document = Jsoup.connect("https://imgflip.com/memesearch?q=" + URLEncoder.encode(memeSearchQuery, "UTF-8")).get();

				Element templatesElement = document.getElementById("memeTemplates");

				Elements memeBoxes = templatesElement.getElementsByClass("mt-box");

				StringBuilder builder = new StringBuilder();
				primary:
				for (Element memeBox : memeBoxes) {
					if (builder.length() > 1700) break;

					Elements titleElements = memeBox.getElementsByClass("mt-title");
					Elements imgElements = memeBox.getElementsByClass("mt-img-wrap");

					String url = null;
					String txt = null;
					String id = null;
					for (Element titleElement : titleElements) {
						txt = titleElement.text();
						String[] splitURL = titleElement.select("a").attr("href").split("/");
						id = splitURL.length > 1 ? splitURL[splitURL.length - 2] : null;

						if (!StringUtils.isNumeric(id)) continue primary;
					}

					for (Element imgElement : imgElements) {
						url = "https:" + imgElement.select("img").attr("src");
					}

					if (url == null || url.isEmpty() || txt == null || txt.isEmpty() || id == null || id.isEmpty())
						continue;

					builder.append(id)
							.append(" - ")
							.append("[").append(txt).append("](").append(url).append(")")
							.append("\n");
				}

				if (builder.toString().isEmpty()) {
					message.getChannel().sendMessage("No memes related to \"`" + memeSearchQuery + "`\" found. Try a different search.");

				} else {
					EmbedBuilder embed = new EmbedBuilder().setTitle("Memes Matching \"`" + memeSearchQuery + "`\"")
							.setColor(Color.WHITE)
							.setDescription(builder.toString());
					message.getChannel().sendMessage(embed);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args.length > 1) {
			String id = args[0].trim();

			if (!StringUtils.isNumeric(id)) {
				message.getChannel().sendMessage("Invalid meme id. Try again.");
				return;
			}

			String[] boxes = StringUtils.substringsBetween(message.getContent(), "[", "]");
			if (boxes == null || boxes.length <= 0) {

				incorrectCommand(message);

			} else {
				boolean hasParams = false;
				Set<String> corrections = new HashSet<>();
				HashMap<String, HashMap<String, String>> paramsMap = new HashMap<>();

				for (String box : boxes) {
					if (!box.contains(";")) {
						break;
					}

					HashMap<String, String> map = new HashMap<>();
					paramsMap.put(box, map);

					String[] params = box.split(";");

					for (String param : params) {
						if (!param.contains("=")) {
							corrections.add("Missing equals `=` in param `" + param + "`");
							return;
						}

						String[] keyValue = param.split("=");

						String key = keyValue[0];
						if (key.contains("loc") || key.contains("pos")) key = "loc";
						else if (key.contains("xt")) key = "text";
						else if (key.contains("link") || key.contains("img") || key.contains("image")) key = "url";

						map.put(key.trim().toLowerCase(Locale.getDefault()).replace(" ", "_"), keyValue[1].trim());
					}

					hasParams = !paramsMap.isEmpty();
				}


				// ---------- HAS PARAMS ---------- //
				if (hasParams) {
					for (String correction : corrections) {
						message.getChannel().sendMessage(correction);
					}
					if (!corrections.isEmpty()) {
						incorrectCommand(message);
					}

					Vec2d imgDims = null;
					String imgurURL = null;

					try {


						// --- Download image data --- //
						{
							HttpResponse<JsonNode> response = Unirest.post("https://api.imgflip.com/caption_image")
									.field("username", "imgflip_hubot")
									.field("password", "imgflip_hubot")
									.field("template_id", id)
									.field("max_font_size", 0)
									.field("text0", " ")
									.asJson();

							if (response == null) {
								message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
							} else {
								JsonElement element = new JsonParser().parse(response.getBody().toString());
								if (!element.isJsonObject()) {
									message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
									return;
								}

								JsonObject object = element.getAsJsonObject();

								if (object.has("success")) {
									if (!object.getAsJsonPrimitive("success").getAsBoolean()) {
										message.getChannel().sendMessage("Can't create meme! Response:");
										message.getChannel().sendMessage("```" + object.toString() + "```");
										return;
									}
								}

								if (object.has("data") && object.get("data").isJsonObject()) {
									JsonObject dataObject = object.getAsJsonObject("data");

									if (dataObject.has("url") && dataObject.get("url").isJsonPrimitive()) {

										JsonObject obj = ImgurUploader.uploadWithJson(dataObject.getAsJsonPrimitive("url").getAsString());

										if (obj == null) {
											message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
											throw new Exception();
										}

										{

											JsonObject data = obj.getAsJsonObject("data");
											if (data == null || !data.isJsonObject()) {
												message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
												throw new Exception();
											}

											imgDims = new Vec2d(data.getAsJsonPrimitive("width").getAsInt(), data.getAsJsonPrimitive("height").getAsInt());
											imgurURL = data.getAsJsonPrimitive("link").getAsString();
										}
									}
								}
							}
						}

						if (imgDims == null || imgurURL == null) {
							message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
							throw new Exception();
						}

						BufferedImage img = Utils.downloadURLAsImage(null, imgurURL);
						Graphics2D graphics = img.createGraphics();
						graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
						graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
								RenderingHints.VALUE_RENDER_QUALITY);

						// LOOP IMAGES
						for (int j = 0; j < boxes.length; j++) {
							String box = boxes[j];
							HashMap<String, String> params = paramsMap.get(box);

							if (!params.containsKey("url")) {
								continue;
							}

							Vec2d loc = new Vec2d(0, 0);
							int pasteImgWidth = -1, pasteImgHeight = -1, x = -1, y = -1, shiftX = 0, shiftY = 0;
							String pasteURL = null;
							for (Map.Entry<String, String> entry : params.entrySet()) {
								String value = entry.getValue();
								switch (entry.getKey()) {

									case "x_-":
									case "x-": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftX = -Integer.parseInt(value);
										break;
									}

									case "x_+":
									case "x+": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftX = Integer.parseInt(value);
										break;
									}

									case "y-":
									case "y_-": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftY = -Integer.parseInt(value);
										break;
									}

									case "y_+":
									case "y+": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftY = Integer.parseInt(value);
									}

									case "x": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										x = Integer.parseInt(value);
										break;
									}

									case "y": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										y = Integer.parseInt(value);
										break;
									}

									case "width":
									case "img width":
									case "image width": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Image width is not an integer `" + value + "`. Try another.");
											break;
										}

										pasteImgWidth = Integer.parseInt(value);
										break;
									}

									case "height":
									case "img height":
									case "image height": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Image height is not an integer `" + value + "`. Try another.");
											break;
										}

										pasteImgHeight = Integer.parseInt(value);
										break;
									}

									case "url": {
										pasteURL = value;
										break;
									}
								}
							}

							// --- DRAW --- //
							if (pasteURL == null) {
								message.getChannel().sendMessage("Couldn't find link in box `{" + box + "}`");
								continue;
							}

							BufferedImage pasteImg = Utils.downloadURLAsImage(message, pasteURL);

							if (pasteImg == null) continue;
							pasteImg = toBufferedImage(pasteImg.getScaledInstance(pasteImgWidth, pasteImgHeight, Image.SCALE_SMOOTH));

							Vec2d size = new Vec2d(pasteImg.getWidth(), pasteImg.getHeight());

							if (params.containsKey("loc")) {
								loc = getVecFromName(params.get("loc"), imgDims, size);
							} else {
								if (j == 0) {
									if (boxes.length == 1) {
										loc = getVecFromName("center", imgDims, size);
									} else {
										loc = getVecFromName("center left", imgDims, size);
									}
								} else if (j == 1) {

									if (boxes.length == 2) {
										loc = getVecFromName("center right", imgDims, size);
									} else {
										loc = getVecFromName("center", imgDims, size);
									}
								} else if (j == 2) {
									loc = getVecFromName("center right", imgDims, size);
								} else if (x == -1 && y == -1) {
									continue;
								}
							}
							if (x != -1) loc.x = x;
							if (y != -1) loc.y = y;

							loc.x += shiftX;
							loc.y += shiftY;

							graphics.translate(loc.x, loc.y);

							graphics.drawImage(pasteImg, 0, 0, null);

							graphics.translate(-loc.x, -loc.y);
						}

						// LOOP TEXT
						boxLoop1:
						for (int j = 0; j < boxes.length; j++) {
							String box = boxes[j];
							HashMap<String, String> params = paramsMap.get(box);

							if (!params.containsKey("text")) {
								continue;
							}

							Color color = Color.WHITE, outlineColor = Color.BLACK;
							Vec2d loc = new Vec2d(0, 0);
							int fontSize = 50, outlineWidth = 5, x = -1, y = -1, shiftX = 0, shiftY = 0;
							boolean bold = false, italic = false, caps = true;
							String font = null, text = null;
							for (Map.Entry<String, String> entry : params.entrySet()) {
								String value = entry.getValue();
								switch (entry.getKey()) {
									case "color": {
										Color c = ColorUtils.getColorFromName(value.replace(" ", ""));
										if (c == null) {
											message.getChannel().sendMessage("Invalid color name `" + value + "`. Try another.");
											break;
										}
										color = c;
										break;
									}

									case "x_-":
									case "x-": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftX = -Integer.parseInt(value);
										break;
									}

									case "x_+":
									case "x+": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftX = Integer.parseInt(value);
										break;
									}

									case "y-":
									case "y_-": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftY = -Integer.parseInt(value);
										break;
									}

									case "y_+":
									case "y+": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										shiftY = Integer.parseInt(value);
										break;
									}

									case "x": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("X is not an integer `" + value + "`. Try another.");
											break;
										}

										x = Integer.parseInt(value);
										break;
									}

									case "y": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Y is not an integer `" + value + "`. Try another.");
											break;
										}

										y = Integer.parseInt(value);
										break;
									}

									case "outlinecolor":
									case "outline_color": {
										Color outlineC = ColorUtils.getColorFromName(value);
										if (outlineColor == null) {
											message.getChannel().sendMessage("Invalid outline color name `" + value + "`. Try another.");
											break;
										}

										outlineColor = outlineC;
										break;
									}

									case "size":
									case "fontsize":
									case "font_size": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Font size is not an integer `" + value + "`. Try another.");
											break;
										}

										fontSize = Integer.parseInt(value);
										break;
									}

									case "outlinewidth":
									case "outline_width": {
										if (!StringUtils.isNumeric(value)) {
											message.getChannel().sendMessage("Font size is not an integer `" + value + "`. Try another.");
											break;
										}

										outlineWidth = Integer.parseInt(value);
										break;
									}

									case "caps": {
										caps = value.equals("true");
										break;
									}

									case "bold": {
										bold = value.equals("true");
										break;
									}

									case "italics":
									case "italic": {
										italic = value.equals("true");
										break;
									}

									case "font": {
										font = value;
										break;
									}
									case "text": {
										text = value;
										break;
									}
								}
							}

							// --- DRAW --- //
							if (text == null) {
								message.getChannel().sendMessage("Couldn't find text in box `{" + box + "}`");
								continue;
							}

							if (caps) {
								text = text.toUpperCase(Locale.getDefault());
							}

							if (outlineWidth >= 0)
								graphics.setStroke(new BasicStroke(outlineWidth));

							Font fontObj = new Font(font == null ? "Impact" : font, (bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0), fontSize);

							String[] split;

							if (text.contains("/n"))
								split = text.split("/n");
							else split = new String[]{text};
							for (int i = 0; i < split.length; i++) {
								String line = split[i].trim();

								int width = Integer.MAX_VALUE;
								while (fontSize >= 10 && width >= imgDims.x) {
									fontObj = new Font(font == null ? "Impact" : font, (bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0), --fontSize);
									width = graphics.getFontMetrics(fontObj).stringWidth(line);
								}

								Rectangle2D textBounds = graphics.getFontMetrics(fontObj).getStringBounds(line, graphics);

								if (params.containsKey("loc")) {
									loc = getVecFromName(params.get("loc"), imgDims, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));
								} else {
									if (j == 0) {
										loc = getVecFromName("top", imgDims, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));

									} else if (j == 1) {

										if (boxes.length == 2) {
											loc = getVecFromName("bottom", imgDims, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));
										} else {
											loc = getVecFromName("center", imgDims, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));
										}
									} else if (j == 2) {
										loc = getVecFromName("bottom", imgDims, new Vec2d(textBounds.getWidth(), textBounds.getHeight()));
									} else if (x == -1 && y == -1) {
										continue boxLoop1;
									}
								}
								if (x != -1) loc.x = x;
								if (y != -1) loc.y = y;

								loc.x += shiftX;
								loc.y += shiftY;

								graphics.translate(loc.x, loc.y + textBounds.getHeight() * i);

								GlyphVector vector = fontObj.createGlyphVector(graphics.getFontRenderContext(), line);
								Shape textShape = vector.getOutline();

								if (outlineWidth > 0 && outlineColor != null) {
									graphics.setColor(outlineColor);
									graphics.draw(textShape);
								}

								graphics.setColor(color);
								graphics.fill(textShape);

								graphics.translate(-loc.x, -loc.y - textBounds.getHeight() * i);
							}

							// --- URL --- //
						}


						graphics.dispose();

						File file = new File("downloads/meme_" + id + "_" + UUID.randomUUID() + ".jpeg");
						if (!file.exists()) file.createNewFile();
						ImageIO.write(img, "jpeg", file);

						message.getChannel().sendMessage(ImgurUploader.upload(file));

						Statistics.INSTANCE.addToStat("successful_meme_generations");

						file.delete();

					} catch (Exception e) {
						message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
						e.printStackTrace();
					}
				}


				// ---------- DOESNT HAVE PARAMS ---------- //
				if (!hasParams) {
					try {
						HttpResponse<JsonNode> response = null;

						if (boxes.length == 1) {

							response = Unirest.post("https://api.imgflip.com/caption_image")
									.field("username", "imgflip_hubot")
									.field("password", "imgflip_hubot")
									.field("template_id", id)
									.field("text0", boxes[0])
									.asJson();

						} else if (boxes.length == 2) {

							response = Unirest.post("https://api.imgflip.com/caption_image")
									.field("username", "imgflip_hubot")
									.field("password", "imgflip_hubot")
									.field("template_id", id)
									.field("text0", boxes[0])
									.field("text1", boxes[1])
									.asJson();

						}

						if (response == null) {
							message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
						} else {
							JsonElement element = new JsonParser().parse(response.getBody().toString());
							if (!element.isJsonObject()) {
								message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
								return;
							}

							JsonObject object = element.getAsJsonObject();

							if (object.has("success")) {
								if (!object.getAsJsonPrimitive("success").getAsBoolean()) {
									message.getChannel().sendMessage("Can't create meme! Response:");
									message.getChannel().sendMessage("```" + object.toString() + "```");
									return;
								}
							}

							if (object.has("data") && object.get("data").isJsonObject()) {
								JsonObject dataObject = object.getAsJsonObject("data");

								if (dataObject.has("url") && dataObject.get("url").isJsonPrimitive()) {
									String url = dataObject.getAsJsonPrimitive("url").getAsString();

									message.getChannel().sendMessage(ImgurUploader.upload(url));
									Statistics.INSTANCE.addToStat("successful_meme_generations");

								}
							}
						}

					} catch (UnirestException e) {
						message.getChannel().sendMessage("Something went wrong. Yell at my maker.");
						e.printStackTrace();
					}
				}
			}

		} else {
			incorrectCommand(message);
		}
	}

	@Nullable
	private Vec2d getVecFromName(String locName, Vec2d imgDims, Vec2d objDims) {
		locName = locName.toLowerCase(Locale.getDefault()).replace("corner", "").replace(" ", "").trim();

		switch (locName) {
			case "middle":
			case "center": {
				double x = (imgDims.x / 2.0) - (objDims.x / 2.0);
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "right":
			case "rightcenter":
			case "centerright": {
				double x = imgDims.x - objDims.x;
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "left":
			case "leftcenter":
			case "centerleft": {
				double x = 0;
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "up":
			case "topcenter":
			case "centertop":
			case "top": {
				double x = (imgDims.x / 2.0) + (objDims.x / 2.0);
				double y = objDims.y;

				return new Vec2d(x, y);
			}
			case "down":
			case "bottomcenter":
			case "centerbottom":
			case "bottom": {
				double x = (imgDims.x / 2.0) - (objDims.x / 2.0);
				double y = imgDims.y - objDims.y;

				return new Vec2d(x, y);
			}
			case "topleft":
			case "upperleft": {
				double x = 0;
				double y = objDims.y;

				return new Vec2d(x, y);
			}

			case "topright":
			case "upperright": {
				double x = imgDims.x - objDims.x;
				double y = objDims.y;

				return new Vec2d(x, y);
			}

			case "lowerleft":
			case "bottomleft": {
				double x = 0;
				double y = imgDims.y - objDims.y;

				return new Vec2d(x, y);
			}

			case "lowerright":
			case "bottomright": {
				double x = imgDims.x - objDims.x;
				double y = imgDims.y - objDims.y;

				return new Vec2d(x, y);
			}
		}

		return null;
	}
}
