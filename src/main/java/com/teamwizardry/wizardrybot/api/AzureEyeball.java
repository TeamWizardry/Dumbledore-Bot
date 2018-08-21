package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;

public class AzureEyeball {

	private ImageColor color = null;
	private Metadata metadata = null;
	private Tags tags = null;
	private Description description = null;
	private SimpleFaces faces = null;
	private Categories categories = null;

	public AzureEyeball(@Nullable JsonElement received) {
		if (received != null && received.isJsonObject()) {
			JsonObject object = received.getAsJsonObject();
			if (object.has("description") && object.has("faces") && object.has("categories") && object.has("color")) {

				metadata = new Metadata(object);
				color = new ImageColor(object);
				tags = new Tags(object);
				faces = new SimpleFaces(object);
				description = new Description(object);
				categories = new Categories(object);
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

	public Description getDescription() {
		return description;
	}

	public SimpleFaces getFaces() {
		return faces;
	}

	public Categories getCategories() {
		return categories;
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

	public static class Celebrity {
		private String name;
		private float confidence;
		private Vec2d corner1, corner2;

		public Celebrity(String name, float confidence, Vec2d corner1, Vec2d corner2) {
			this.name = name;
			this.confidence = confidence;
			this.corner1 = corner1;
			this.corner2 = corner2;
		}

		public String getName() {
			return name;
		}

		public float getConfidence() {
			return confidence;
		}

		public Vec2d getCorner1() {
			return corner1;
		}

		public Vec2d getCorner2() {
			return corner2;
		}
	}

	public static class LandMark {

		private String name;
		private float confidence;

		public LandMark(String name, float confidence) {
			this.name = name;
			this.confidence = confidence;
		}

		public String getName() {
			return name;
		}

		public float getConfidence() {
			return confidence;
		}
	}

	public static class Categories {

		private Set<Celebrity> celebrities = new HashSet<>();
		private Set<LandMark> landmarks = new HashSet<>();

		Categories(JsonObject object) {
			if (object.has("categories") && object.get("categories").isJsonArray()) {
				JsonArray categories = object.getAsJsonArray("categories");

				for (JsonElement element : categories) {
					if (!element.isJsonObject()) continue;
					JsonObject category = element.getAsJsonObject();

					if (category.has("detail")) {
						JsonObject detail = category.getAsJsonObject("detail");

						if (detail.has("celebrities") && detail.get("celebrities").isJsonArray()) {
							for (JsonElement element1 : detail.getAsJsonArray("celebrities")) {
								JsonObject celebrity = element1.getAsJsonObject();

								if (celebrity.has("faceRectangle") && celebrity.has("name") && celebrity.has("confidence")) {
									String name = celebrity.getAsJsonPrimitive("name").getAsString();
									float confidence = celebrity.getAsJsonPrimitive("confidence").getAsFloat();

									Vec2d corner1, corner2;
									if (celebrity.has("faceRectangle") && celebrity.get("faceRectangle").isJsonObject()) {
										JsonObject faceRectangle = celebrity.getAsJsonObject("faceRectangle");
										corner1 = new Vec2d(faceRectangle.getAsJsonPrimitive("left").getAsInt(), faceRectangle.getAsJsonPrimitive("top").getAsInt());
										corner2 = new Vec2d(faceRectangle.getAsJsonPrimitive("width").getAsInt(), faceRectangle.getAsJsonPrimitive("height").getAsInt());

										celebrities.add(new Celebrity(name, confidence, corner1, corner2));
									}
								}
							}
						}

						if (detail.has("landmarks") && detail.get("landmarks").isJsonArray()) {
							for (JsonElement element1 : detail.getAsJsonArray("landmarks")) {
								JsonObject landmark = element1.getAsJsonObject();

								if (landmark.has("name") && landmark.has("confidence")) {
									landmarks.add(new LandMark(landmark.getAsJsonPrimitive("name").getAsString(), landmark.getAsJsonPrimitive("confidence").getAsFloat()));
								}
							}
						}
					}
				}
			}
		}

		public Set<Celebrity> getCelebrities() {
			return celebrities;
		}

		public Set<LandMark> getLandmarks() {
			return landmarks;
		}
	}

	public class SimpleFace {
		private Vec2d corner1, corner2;
		private String gender;
		private int age;

		public SimpleFace(Vec2d corner1, Vec2d corner2, String gender, int age) {
			this.corner1 = corner1;
			this.corner2 = corner2;
			this.gender = gender;
			this.age = age;
		}

		public Vec2d getCorner1() {
			return corner1;
		}

		public Vec2d getCorner2() {
			return corner2;
		}

		public String getGender() {
			return gender;
		}

		public int getAge() {
			return age;
		}
	}

	public class SimpleFaces {

		private HashSet<SimpleFace> faces = new HashSet<>();

		SimpleFaces(JsonObject obj) {
			if (obj.has("faces") && obj.get("faces").isJsonArray()) {
				JsonArray elements = obj.getAsJsonArray("faces");
				for (JsonElement faces : elements) {
					if (!faces.isJsonObject()) continue;
					JsonObject face = faces.getAsJsonObject();

					Vec2d corner1 = null, corner2 = null;
					String gender = null;
					int age = -1;

					if (face.has("faceRectangle") && face.get("faceRectangle").isJsonObject()) {
						JsonObject faceRectangle = face.getAsJsonObject("faceRectangle");
						corner1 = new Vec2d(faceRectangle.getAsJsonPrimitive("left").getAsInt(), faceRectangle.getAsJsonPrimitive("top").getAsInt());
						corner2 = new Vec2d(faceRectangle.getAsJsonPrimitive("width").getAsInt(), faceRectangle.getAsJsonPrimitive("height").getAsInt());
					}

					if (face.has("gender") && face.get("gender").isJsonPrimitive()) {
						gender = face.getAsJsonPrimitive("gender").getAsString();
					}

					if (face.has("age") && face.get("age").isJsonPrimitive()) {
						age = face.getAsJsonPrimitive("age").getAsInt();
					}

					this.faces.add(new SimpleFace(corner1, corner2, gender, age));
				}
			}
		}

		public HashSet<SimpleFace> getFaces() {
			return faces;
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

		Faces(JsonObject obj) {
			if (obj.has("faces") && obj.get("faces").isJsonArray()) {
				JsonArray elements = obj.getAsJsonArray("faces");
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

		private HashMap<String, Float> captions = new HashMap<>();

		Description(JsonObject object) {
			if (object.has("description") && object.get("description").isJsonObject()) {
				JsonObject description = object.getAsJsonObject("description");
				if (description.has("captions") && description.get("captions").isJsonArray()) {


					JsonArray captions = description.getAsJsonArray("captions");
					for (JsonElement captionElement : captions) {
						if (!captionElement.isJsonObject()) continue;
						JsonObject caption = captionElement.getAsJsonObject();

						if (caption.has("confidence") && caption.has("text")) {
							this.captions.put(caption.getAsJsonPrimitive("text").getAsString(), caption.getAsJsonPrimitive("confidence").getAsFloat());
						}
					}
				}
			}
		}

		public HashMap<String, Float> getCaptions() {
			return captions;
		}
	}

	public class Tags {

		private HashMap<String, Float> tags = new HashMap<>();

		Tags(JsonObject object) {
			if (object.has("tags") && object.get("tags").isJsonArray()) {
				JsonArray tags = object.getAsJsonArray("tags");

				for (JsonElement tagElement : tags) {
					if (!tagElement.isJsonObject()) continue;
					JsonObject tag = tagElement.getAsJsonObject();

					if (tag.has("name") && tag.has("confidence")) {
						this.tags.put(tag.getAsJsonPrimitive("name").getAsString(), tag.getAsJsonPrimitive("confidence").getAsFloat());
					}
				}
			}
		}

		public HashMap<String, Float> getTags() {
			return tags;
		}
	}

	public class ImageColor {

		@Nonnull
		private Color dominantColorForeground = Color.WHITE;
		@Nonnull
		private Color dominantColorBackground = Color.WHITE;
		@Nonnull
		private Color accentColor = Color.WHITE;
		@Nonnull
		private Color[] dominantColors = new Color[]{Color.WHITE};

		ImageColor(JsonObject object) {
			if (object.has("color") && object.get("color").isJsonObject()) {
				JsonObject color = object.getAsJsonObject("color");
				if (color.has("dominantColorForeground") && color.get("dominantColorForeground").isJsonPrimitive()) {
					dominantColorBackground = ColorUtils.getColorFromName(color.getAsJsonPrimitive("dominantColorForeground").getAsString(), Color.WHITE);
				}
				if (color.has("dominantColorBackground") && color.get("dominantColorBackground").isJsonPrimitive()) {
					dominantColorBackground = ColorUtils.getColorFromName(color.getAsJsonPrimitive("dominantColorBackground").getAsString(), Color.WHITE);
				}
				if (color.has("accentColor") && color.get("accentColor").isJsonPrimitive()) {
					//	String hex = color.getAsJsonPrimitive("accentColor").getAsString();
					//	accentColor = Color.decode(hex);
				}
				if (color.has("dominantColors") && color.get("dominantColors").isJsonArray()) {
					ArrayList<Color> colors = new ArrayList<>();
					JsonArray dominantColorsArray = color.getAsJsonArray("dominantColors");
					for (JsonElement colorElement : dominantColorsArray) {
						if (colorElement.isJsonPrimitive()) {
							colors.add(ColorUtils.getColorFromName(colorElement.getAsJsonPrimitive().getAsString()));
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
