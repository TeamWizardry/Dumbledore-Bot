package com.teamwizardry.wizardrybot.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Statistics {

	public static Statistics INSTANCE = new Statistics();
	private File file = new File("stats.json");
	private JsonObject object;

	private Statistics() {
		object = getJson();
	}

	public void addToStat(String stat) {
		int count = 0;
		if (object.has(stat)) {
			JsonPrimitive prop = object.getAsJsonPrimitive(stat);
			count = prop.getAsInt();
		}
		object.addProperty(stat, count + 1);

		saveJson();
	}

	public JsonObject getStats() {
		return object;
	}

	private void saveJson() {
		try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(file.toPath()))) {
			Streams.write(object, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JsonObject getJson() {
		try {
			String txt = FileUtils.readFileToString(file, Charset.defaultCharset());
			return new Gson().fromJson(txt, JsonObject.class);
		} catch (IOException e) {
			return new JsonObject();
		}
	}
}
