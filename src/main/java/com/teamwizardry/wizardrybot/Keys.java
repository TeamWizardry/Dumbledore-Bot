package com.teamwizardry.wizardrybot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public final class Keys {

	public static String WIZARDRY_DIALOGFLOW_API;
	public static String DIALOGFLOW_API;
	public static String BING_SPELL_CHECK_API;
	public static String MERRIAM_DICTIONARY_KEY;
	public static String PASSWORD;
	public static String IMGUR;
	public static String YOUTUBE;

	public static boolean authorize(String KEY) {
		try {
			HttpResponse<String> response = Unirest.get("https://wizardry-discord-bot.appspot.com/?key=" + KEY).asString();

			JsonElement element = new JsonParser().parse(response.getBody());

			if (element == null || !element.isJsonObject()) {
				System.out.println("Kindly fuck off pls.");
			} else {
				JsonObject object = element.getAsJsonObject();

				Keys.DIALOGFLOW_API = object.getAsJsonPrimitive("dialogflow").getAsString();
				Keys.BING_SPELL_CHECK_API = object.getAsJsonPrimitive("bing_spell_check").getAsString();
				Keys.MERRIAM_DICTIONARY_KEY = object.getAsJsonPrimitive("merriam_dictionary").getAsString();
				Keys.PASSWORD = object.getAsJsonPrimitive("password").getAsString();
				Keys.WIZARDRY_DIALOGFLOW_API = object.getAsJsonPrimitive("wizardry_dialogflow_api").getAsString();
				Keys.IMGUR = object.getAsJsonPrimitive("imgur").getAsString();
				Keys.YOUTUBE = object.getAsJsonPrimitive("youtube").getAsString();
				return true;
			}
		} catch (Exception e) {
			System.out.println("Kindly fuck off please.");
			e.printStackTrace();
		}

		return false;
	}
}
