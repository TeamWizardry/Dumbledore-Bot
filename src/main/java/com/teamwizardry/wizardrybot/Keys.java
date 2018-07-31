package com.teamwizardry.wizardrybot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;

public final class Keys {

	public static String WIZARDRY_DIALOGFLOW_API;
	public static String DIALOGFLOW_API;
	public static String BING_SPELL_CHECK_API;
	public static String MERRIAM_DICTIONARY_KEY;
	public static String PASSWORD;
	public static String CLOUDINARY_NAME;
	public static String CLOUDINARY_KEY;
	public static String CLOUDINARY_SECRET;

	public static boolean authorize(String KEY) {
		try {
			HttpResponse<String> response = Unirest.get("https://wizardry-discord-bot.appspot.com/?key=" + KEY).asString();

			try {
				new JSONObject(response.getBody());
			} catch (JSONException ex) {
				System.out.println("Kindly fuck off pls.");
				return false;
			}

			JsonElement element = new JsonParser().parse(response.getBody());

			if (element == null || !element.isJsonObject()) {
				System.out.println("Kindly fuck off pls.");
			} else {
				JsonObject object = element.getAsJsonObject();

				Keys.DIALOGFLOW_API = object.getAsJsonPrimitive("dialogflow").getAsString();
				Keys.BING_SPELL_CHECK_API = object.getAsJsonPrimitive("bing_spell_check").getAsString();
				Keys.MERRIAM_DICTIONARY_KEY = object.getAsJsonPrimitive("merriam_dictionary").getAsString();
				Keys.PASSWORD = object.getAsJsonPrimitive("password").getAsString();
				Keys.CLOUDINARY_NAME = object.getAsJsonPrimitive("cloudinary_name").getAsString();
				Keys.CLOUDINARY_KEY = object.getAsJsonPrimitive("cloudinary_key").getAsString();
				Keys.CLOUDINARY_SECRET = object.getAsJsonPrimitive("cloudinary_secret").getAsString();
				Keys.WIZARDRY_DIALOGFLOW_API = object.getAsJsonPrimitive("wizardry_dialogflow_api").getAsString();

				return true;
			}
		} catch (UnirestException ignored) {
			ignored.printStackTrace();
		}

		return false;
	}
}
