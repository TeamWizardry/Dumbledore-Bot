package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AzureEyeball {

	private ImageColor color = null;
	private Metadata metadata = null;
	private Tags tags = null;
	private String description = "";
	private Faces faces = null;

	public AzureEyeball(JsonElement analyze, JsonElement describe, JsonElement face) {
		if (analyze.isJsonObject()) {
			JsonObject object = analyze.getAsJsonObject();

			metadata = new Metadata(object);
			color = new ImageColor(object);
			tags = new Tags(object);
			faces = new Faces(face);
			if (object.isJsonObject()) {
				JsonObject describeObject = describe.getAsJsonObject();
				description = new Description(describeObject).getDescription();
			}
		}
	}

	public ImageColor getColor() {
		return color;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Tags getTags() {
		return tags;
	}

	public String getDescription() {
		return description;
	}

	public Faces getFaces() {
		return faces;
	}

	public static class Emotion {

		private final String emotion;
		private final float confidence;

		public Emotion(String emotion, float confidence) {
			this.emotion = emotion;
			this.confidence = confidence;
		}

		public float getConfidence() {
			return confidence;
		}

		public String getEmotion() {
			return emotion;
		}
	}

	public static class Face {

		private String faceID;
		private double age;
		private String gender;
		private Vec2d corner1;
		private Vec2d corner2;
		private float smile;
		private float baldness;
		private float moustache;
		private float beard;
		private float sideburns;
		private String glasses;
		private String[] hairColor;
		private Emotion[] emotions;
		private String exposureLevel;
		private boolean lipMakeup;
		private boolean eyeMakeup;

		public Face(String faceID, double age, String gender, int x, int y, int width, int height, float smile, float baldness, float moustache, float beard, float sideburns, String glasses, String[] hairColor, Emotion[] emotions, String exposureLevel, boolean lipMakeup, boolean eyeMakeup) {
			this.faceID = faceID;
			this.age = age;
			this.gender = gender;
			this.smile = smile;
			this.baldness = baldness;
			this.moustache = moustache;
			this.beard = beard;
			this.sideburns = sideburns;
			this.glasses = glasses;
			this.hairColor = hairColor;
			this.emotions = emotions;
			this.exposureLevel = exposureLevel;
			this.lipMakeup = lipMakeup;
			this.eyeMakeup = eyeMakeup;
			corner1 = new Vec2d(x, y);
			corner2 = new Vec2d(x + width, y + height);
		}

		public Vec2d getCorner2() {
			return corner2;
		}

		public Vec2d getCorner1() {
			return corner1;
		}

		public String getGender() {
			return gender;
		}

		public String getExposureLevel() {
			return exposureLevel;
		}

		public Emotion[] getEmotions() {
			return emotions;
		}

		public String[] getHairColor() {
			return hairColor;
		}

		public String getGlasses() {
			return glasses;
		}

		public float getSideburns() {
			return sideburns;
		}

		public float getBeard() {
			return beard;
		}

		public float getMoustache() {
			return moustache;
		}

		public float getBaldness() {
			return baldness;
		}

		public float getSmile() {
			return smile;
		}

		public String getFaceID() {
			return faceID;
		}

		public double getAge() {
			return age;
		}

		public boolean isLipMakeup() {
			return lipMakeup;
		}

		public boolean isEyeMakeup() {
			return eyeMakeup;
		}
	}

	public class Faces {

		private HashSet<Face> faces = new HashSet<>();

		Faces(JsonElement element) {
			if (element.isJsonArray()) {
				JsonArray elements = element.getAsJsonArray();
				for (JsonElement faces : elements) {
					if (!faces.isJsonObject()) continue;
					JsonObject face = faces.getAsJsonObject();
					FaceBuilder builder = new FaceBuilder();

					if (face.has("faceRectangle") && face.get("faceRectangle").isJsonObject()) {
						JsonObject faceRectangle = face.getAsJsonObject("faceRectangle");
						if (faceRectangle.has("left") && faceRectangle.get("left").isJsonPrimitive()) {
							builder.setX(faceRectangle.getAsJsonPrimitive("left").getAsInt());
						}
						if (faceRectangle.has("top") && faceRectangle.get("top").isJsonPrimitive()) {
							builder.setY(faceRectangle.getAsJsonPrimitive("top").getAsInt());
						}
						if (faceRectangle.has("width") && faceRectangle.get("width").isJsonPrimitive()) {
							builder.setWidth(faceRectangle.getAsJsonPrimitive("width").getAsInt());
						}
						if (faceRectangle.has("height") && faceRectangle.get("height").isJsonPrimitive()) {
							builder.setHeight(faceRectangle.getAsJsonPrimitive("height").getAsInt());
						}
					}

					if (face.has("faceAttributes") && face.get("faceAttributes").isJsonObject()) {
						JsonObject faceAttributes = face.getAsJsonObject("faceAttributes");

						if (faceAttributes.has("age") && faceAttributes.get("age").isJsonPrimitive()) {
							builder.setAge(faceAttributes.getAsJsonPrimitive("age").getAsDouble());
						}

						if (faceAttributes.has("gender") && faceAttributes.get("gender").isJsonPrimitive()) {
							builder.setGender(faceAttributes.getAsJsonPrimitive("gender").getAsString());
						} else builder.setGender("¯\\\\_(ツ)_/¯");

						if (faceAttributes.has("facialHair") && faceAttributes.get("facialHair").isJsonObject()) {
							JsonObject facialHair = faceAttributes.getAsJsonObject("facialHair");
							if (facialHair.has("moustache") && facialHair.get("moustache").isJsonPrimitive()) {
								builder.setMoustache(facialHair.getAsJsonPrimitive("moustache").getAsFloat());
							}
							if (facialHair.has("beard") && facialHair.get("beard").isJsonPrimitive()) {
								builder.setBeard(facialHair.getAsJsonPrimitive("beard").getAsFloat());
							}
							if (facialHair.has("sideburns") && facialHair.get("sideburns").isJsonPrimitive()) {
								builder.setSideburns(facialHair.getAsJsonPrimitive("sideburns").getAsFloat());
							}
						}

						if (faceAttributes.has("smile") && faceAttributes.get("smile").isJsonPrimitive()) {
							builder.setSmile(faceAttributes.getAsJsonPrimitive("smile").getAsFloat());
						}

						if (faceAttributes.has("emotion") && faceAttributes.get("emotion").isJsonObject()) {
							JsonObject emotions = faceAttributes.getAsJsonObject("emotion");

							ArrayList<Emotion> emotionList = new ArrayList<>();
							Set<Map.Entry<String, JsonElement>> entries = emotions.entrySet();
							for (Map.Entry<String, JsonElement> entry : entries) {
								emotionList.add(new Emotion(entry.getKey(), entry.getValue().getAsFloat()));
							}

							Emotion[] emotionArray = new Emotion[emotionList.size()];
							for (int i = 0; i < emotionList.size(); i++) {
								emotionArray[i] = emotionList.get(i);
							}
							builder.setEmotions(emotionArray);
						}

						if (faceAttributes.has("makeup") && faceAttributes.get("makeup").isJsonObject()) {
							JsonObject makeup = faceAttributes.getAsJsonObject("makeup");
							if (makeup.has("eyeMakeup") && makeup.get("eyeMakeup").isJsonPrimitive()) {
								builder.setEyeMakeup(makeup.getAsJsonPrimitive("eyeMakeup").getAsBoolean());
							}
							if (makeup.has("lipMakeup") && makeup.get("lipMakeup").isJsonPrimitive()) {
								builder.setLipMakeup(makeup.getAsJsonPrimitive("lipMakeup").getAsBoolean());
							}
						}

						if (faceAttributes.has("glasses") && faceAttributes.get("glasses").isJsonPrimitive()) {
							builder.setGlasses(faceAttributes.getAsJsonPrimitive("glasses").getAsString());
						}

						if (faceAttributes.has("hair") && faceAttributes.get("hair").isJsonObject()) {
							JsonObject hair = faceAttributes.getAsJsonObject("hair");
							if (hair.has("bald") && hair.get("bald").isJsonPrimitive()) {
								builder.setBaldness(hair.getAsJsonPrimitive("bald").getAsFloat());
							}

							if (hair.has("hairColor") && hair.get("hairColor").isJsonArray()) {
								JsonArray hairColors = hair.getAsJsonArray("hairColor");
								ArrayList<String> colors = new ArrayList<>();
								for (JsonElement hairColorElement : hairColors) {
									if (!hairColorElement.isJsonObject()) continue;
									JsonObject hairColor = hairColorElement.getAsJsonObject();
									if (hairColor.has("confidence") && hairColor.get("confidence").isJsonPrimitive()) {
										if (hairColor.getAsJsonPrimitive("confidence").getAsFloat() >= 0.8) {
											colors.add(hairColor.getAsJsonPrimitive("color").getAsString());
										}
									}
								}
								String[] colorArray = new String[colors.size()];
								for (int i = 0; i < colors.size(); i++) {
									colorArray[i] = colors.get(i);
								}
								builder.setHairColor(colorArray);
							}
						}
					}

					this.faces.add(builder.createFace());
				}
			}
		}

		public HashSet<Face> getFaces() {
			return faces;
		}
	}

	public class Description {

		private String description = "";

		Description(JsonObject object) {
			if (object.has("description") && object.get("description").isJsonObject()) {
				JsonObject description = object.getAsJsonObject("description");
				if (description.has("captions") && description.get("captions").isJsonArray()) {

					JsonObject bestCaption = null;

					JsonArray captions = description.getAsJsonArray("captions");
					for (JsonElement captionElement : captions) {
						if (!captionElement.isJsonObject()) continue;
						JsonObject caption = captionElement.getAsJsonObject();

						if (bestCaption == null) bestCaption = caption;
						if (caption.has("confidence") && caption.get("confidence").isJsonPrimitive()) {
							if (caption.getAsJsonPrimitive("confidence").getAsFloat() > bestCaption.getAsJsonPrimitive("confidence").getAsFloat()) {
								bestCaption = caption;
							}
						}
					}

					if (bestCaption != null) {
						this.description = bestCaption.getAsJsonPrimitive("text").getAsString();
					}
				}
			}
		}

		public String getDescription() {
			return description;
		}
	}

	public class Tags {

		private ArrayList<String> tags = new ArrayList<>();
		private String readableTags = "";

		Tags(JsonObject object) {
			if (object.has("tags") && object.get("tags").isJsonArray()) {
				JsonArray tags = object.getAsJsonArray("tags");

				for (JsonElement tagElement : tags) {
					if (!tagElement.isJsonObject()) continue;
					JsonObject tag = tagElement.getAsJsonObject();
					if (tag.has("confidence")
							&& tag.get("confidence").isJsonPrimitive()
							&& tag.getAsJsonPrimitive("confidence").getAsFloat() >= 0.8) {
						if (tag.has("name") && tag.get("name").isJsonPrimitive())
							this.tags.add(tag.getAsJsonPrimitive("name").getAsString());
					}
				}
			}

			StringBuilder builder = new StringBuilder();
			for (String tag : tags) {
				builder.append(tag).append(", ");
			}
			readableTags = builder.toString();
		}

		public String getReadableTags() {
			return readableTags;
		}
	}

	public class ImageColor {

		@Nullable
		private Color dominantColorForeground = Color.WHITE;
		@Nullable
		private Color dominantColorBackground = Color.WHITE;
		@Nullable
		private Color accentColor = Color.WHITE;
		@Nullable
		private Color[] dominantColors = new Color[]{Color.WHITE};

		ImageColor(JsonObject object) {
			if (object.has("color") && object.get("color").isJsonObject()) {
				JsonObject color = object.getAsJsonObject("color");
				if (color.has("dominantColorForeground") && color.get("dominantColorForeground").isJsonPrimitive()) {
					dominantColorBackground = new ColorUtils().getColorFromName(color.getAsJsonPrimitive("dominantColorForeground").getAsString());
				}
				if (color.has("dominantColorBackground") && color.get("dominantColorBackground").isJsonPrimitive()) {
					dominantColorBackground = new ColorUtils().getColorFromName(color.getAsJsonPrimitive("dominantColorBackground").getAsString());
				}
				if (color.has("accentColor") && color.get("accentColor").isJsonPrimitive()) {
					//int hex = color.getAsJsonPrimitive("accentColor").getAsInt();
					//accentColor = Color.decode(hex + "");
				}
				if (color.has("dominantColors") && color.get("dominantColors").isJsonArray()) {
					ArrayList<Color> colors = new ArrayList<>();
					JsonArray dominantColorsArray = color.getAsJsonArray("dominantColors");
					for (JsonElement colorElement : dominantColorsArray) {
						if (colorElement.isJsonPrimitive()) {
							colors.add(new ColorUtils().getColorFromName(colorElement.getAsJsonPrimitive().getAsString()));
						}
					}
					dominantColors = new Color[colors.size()];
					for (int i = 0; i < colors.size(); i++) {
						dominantColors[i] = colors.get(i);
					}
				}
			}
		}

		public Color[] getDominantColors() {
			return dominantColors;
		}

		public Color getAccentColor() {
			return accentColor;
		}

		public Color getDominantColorBackground() {
			return dominantColorBackground;
		}

		public Color getDominantColorForeground() {
			return dominantColorForeground;
		}
	}

	public class Metadata {

		private int width = 0;
		private int height = 0;
		private String format = "<NULL>";

		Metadata(JsonObject object) {
			if (object.has("metadata") && object.get("metadata").isJsonObject()) {
				JsonObject metadata = object.getAsJsonObject("metadata");
				if (metadata.has("width") && metadata.get("width").isJsonPrimitive()
						&& metadata.has("height") && metadata.get("height").isJsonPrimitive()
						&& metadata.has("format") && metadata.get("format").isJsonPrimitive()) {
					width = metadata.getAsJsonPrimitive("width").getAsInt();
					height = metadata.getAsJsonPrimitive("height").getAsInt();
					format = metadata.getAsJsonPrimitive("format").getAsString();
				}
			}
		}

		public String getFormat() {
			return format;
		}

		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}
	}
}
