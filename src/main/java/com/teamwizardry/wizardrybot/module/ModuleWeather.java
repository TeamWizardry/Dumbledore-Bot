package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.joda.time.DateTime;

public class ModuleWeather extends Module implements ICommandModule {
	@Override
	public String getActionID() {
		return "input.weather";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "Weather";
	}

	@Override
	public String getDescription() {
		return "Albus will give you the current forecast in a specific place in the world.";
	}

	@Override
	public String getUsage() {
		return "hey albus, whats the weather in <city/place>";
	}

	@Override
	public String getExample() {
		return "hey albus, what's the weather in brooklyn?";
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (result.getParameters().containsKey("geo-city")) {

			String place = result.getStringParameter("geo-city");

			try {
				HttpResponse<JsonNode> response = Unirest.get("http://api.openweathermap.org/data/2.5/weather?q=" + place + "&APPID=2552c8d4c97fdbc938d9ebc1d122bf72").asJson();

				JsonObject object = (JsonObject) new JsonParser().parse(response.getBody().toString());

				if (object == null || object.isJsonNull()) {
					message.getChannel().sendMessage("Something went wrong while checking the weather. Sorry...");
					return;
				}

				Weather weather = new Weather(object);

				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle(place.toUpperCase());
				String desc = "";
				for (Pair pair : weather.getDescriptions()) desc = pair.getValue() + ", ";

				builder.setDescription(weather.getTemperature() + "C. ↑" + weather.getTemperatureHighest() + " - ↓" + weather.getTemperatureLowest());
				builder.addField("Description", desc, false);
				builder.addField("Clouds", weather.getCloudsPercentage() + "", false);
				builder.addField("Rain", weather.getRain() + "", false);
				builder.addField("Snow", weather.getSnow() + "", false);
				builder.addField("Ground Level", weather.getGroundLevel() + "", false);
				builder.addField("Sea Level", weather.getSeaLevel() + "", false);
				builder.addField("dt", new DateTime(weather.getDateTime()).toString() + "", false);

				message.getChannel().sendMessage(builder);

			} catch (UnirestException e) {
				e.printStackTrace();
			}


		} else {
			message.getChannel().sendMessage("Which city or place would you like a forecast on? Restate your sentence.");
		}
	}
}
