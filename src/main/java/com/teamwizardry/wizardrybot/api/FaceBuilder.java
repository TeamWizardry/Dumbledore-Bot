package com.teamwizardry.wizardrybot.api;

public class FaceBuilder {
	private String faceID;
	private double age;
	private String gender;
	private int x;
	private int y;
	private int width;
	private int height;
	private float smile;
	private float baldness;
	private float moustache;
	private float beard;
	private float sideburns;
	private String glasses;
	private String[] hairColor = new String[0];
	private AzureEyeball.Emotion[] emotions = new AzureEyeball.Emotion[0];
	private String exposureLevel;
	private boolean lipMakeup;
	private boolean eyeMakeup;

	public FaceBuilder setFaceID(String faceID) {
		this.faceID = faceID;
		return this;
	}

	public FaceBuilder setAge(double age) {
		this.age = age;
		return this;
	}

	public FaceBuilder setGender(String gender) {
		this.gender = gender;
		return this;
	}

	public FaceBuilder setX(int x) {
		this.x = x;
		return this;
	}

	public FaceBuilder setY(int y) {
		this.y = y;
		return this;
	}

	public FaceBuilder setWidth(int width) {
		this.width = width;
		return this;
	}

	public FaceBuilder setHeight(int height) {
		this.height = height;
		return this;
	}

	public FaceBuilder setSmile(float smile) {
		this.smile = smile;
		return this;
	}

	public FaceBuilder setBaldness(float baldness) {
		this.baldness = baldness;
		return this;
	}

	public FaceBuilder setMoustache(float moustache) {
		this.moustache = moustache;
		return this;
	}

	public FaceBuilder setBeard(float beard) {
		this.beard = beard;
		return this;
	}

	public FaceBuilder setSideburns(float sideburns) {
		this.sideburns = sideburns;
		return this;
	}

	public FaceBuilder setGlasses(String glasses) {
		this.glasses = glasses;
		return this;
	}

	public FaceBuilder setHairColor(String[] hairColor) {
		this.hairColor = hairColor;
		return this;
	}

	public FaceBuilder setEmotions(AzureEyeball.Emotion[] emotions) {
		this.emotions = emotions;
		return this;
	}

	public FaceBuilder setExposureLevel(String exposureLevel) {
		this.exposureLevel = exposureLevel;
		return this;
	}

	public FaceBuilder setLipMakeup(boolean lipMakeup) {
		this.lipMakeup = lipMakeup;
		return this;
	}

	public FaceBuilder setEyeMakeup(boolean eyeMakeup) {
		this.eyeMakeup = eyeMakeup;
		return this;
	}

	public AzureEyeball.Face createFace() {
		return new AzureEyeball.Face(faceID, age, gender, x, y, width, height, smile, baldness, moustache, beard, sideburns, glasses, hairColor, emotions, exposureLevel, lipMakeup, eyeMakeup);
	}
}