package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.RandUtil;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class ModuleGif extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Gif";
	}

	@Override
	public String getDescription() {
		return "Will return a gif from tenor.com from the keyword used";
	}

	@Override
	public String getUsage() {
		return "'hey albus, gif <thing>'";
	}

	@Override
	public String getExample() {
		return "'hey albus, gif excited";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"gif", "jif"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {

		try {
			HttpResponse<JsonNode> response = Unirest.get("https://api.tenor.com/v1/search?q=" + command.getCommandArguments().replace(" ", "_") + "&key=5TU1160NKPSY&limit=20").asJson();

			JsonObject object = (JsonObject) new JsonParser().parse(response.getBody().toString());

			if (object == null || object.isJsonNull()) {
				message.getChannel().sendMessage("Something went wrong. Sorry...");
				return;
			}

			if (object.has("results") && object.get("results").isJsonArray()) {
				JsonArray array = object.getAsJsonArray("results");

				JsonElement element = array.get(RandUtil.nextInt(array.size() - 1));
				if (element == null) return;
				if (element.isJsonObject()) {
					JsonObject resultObj = element.getAsJsonObject();

					if (resultObj.has("media") && resultObj.get("media").isJsonArray()) {
						JsonArray mediaArray = resultObj.getAsJsonArray("media");

						for (JsonElement mediaElem : mediaArray) {
							if (!mediaElem.isJsonObject()) continue;
							JsonObject mediaObj = mediaElem.getAsJsonObject();

							if (mediaObj.has("mediumgif") && mediaObj.get("mediumgif").isJsonObject()) {
								JsonObject gifObj = mediaObj.getAsJsonObject("mediumgif");

								if (gifObj.has("url") && gifObj.get("url").isJsonPrimitive()) {
									message.getChannel().sendMessage(gifObj.getAsJsonPrimitive("url").getAsString());
									message.delete();
								}
							}
						}
					}
				}
			}

			System.out.println(object.toString());
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		//	Statistics.INSTANCE.addToStat("gifs_provided");
	}
}
