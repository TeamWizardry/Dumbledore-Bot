package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.api.*;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

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
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (result.getParameters().containsKey("geo-city")) {

			String place = result.getStringParameter("geo-city");

			try {
				HttpResponse<JsonNode> response = Unirest.get("http://api.openweathermap.org/data/2.5/weather?q=" + place + "&units=metric&APPID=2552c8d4c97fdbc938d9ebc1d122bf72").asJson();

				JsonObject object = (JsonObject) new JsonParser().parse(response.getBody().toString());

				if (object == null || !object.isJsonObject()) {
					message.getChannel().sendMessage("Something went wrong while checking the weather. Sorry...");
					return true;
				}

				Weather weather = new Weather(object);

				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle(place.toUpperCase());
				builder.setColor(Color.ORANGE);
				String desc = "";
				for (Pair pair : weather.getDescriptions())
					desc = "**" + StringUtils.capitalize(pair.getKey().toLowerCase()) + "** / " + pair.getValue().toLowerCase() + "\n";

				builder.setDescription("**" + weather.getTemperature() + "C**. ↑" + weather.getTemperatureHighest() + " - ↓" + weather.getTemperatureLowest()
						+ "\n" + "**" + (weather.getTemperature() * 1.8 + 32) + "F**. ↑" + (weather.getTemperatureHighest() * 1.8 + 32) + "  ↓" + (weather.getTemperatureLowest() * 1.8 + 32));
				builder.addField("Description", desc, false);
				if ((int) weather.getHumidity() != 0)
					builder.addField("Humidity", (int) weather.getHumidity() + "%", false);
				if ((int) weather.getPressure() != 0)
					builder.addField("Pressure", (int) weather.getPressure() + " hPa", false);
				if ((int) weather.getCloudsPercentage() != 0)
					builder.addField("Cloudiness", (int) weather.getCloudsPercentage() + "%", false);
				if ((int) weather.getRain() != 0)
					builder.addField("Rain Volume", (int) weather.getRain() + " L", false);
				if ((int) weather.getSnow() != 0)
					builder.addField("Snow Volume", (int) weather.getSnow() + " L", false);
				if ((int) weather.getWindSpeed() != 0) {
					builder.addField("Wind Speed", (int) weather.getWindSpeed() + " m/s", false);
				}

				message.getChannel().sendMessage(builder);

			} catch (UnirestException e) {
				e.printStackTrace();
			}


		} else {
			message.getChannel().sendMessage("Which city or place would you like a forecast on? Restate your sentence.");
		}

		return true;
	}
}
