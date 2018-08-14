package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class Weather {

	private double
			temperature,
			pressure,
			humidity,
			temperatureLowest,
			temperatureHighest,
			windSpeed,
			windDegrees,
			cloudsPercentage,
			rain,
			snow;
	private long dateTime;
	private Set<Pair> descriptions = new HashSet<>();

	public Weather(JsonObject object) {
		if (object.has("weather") && object.get("weather").isJsonArray()) {
			JsonArray weather = object.getAsJsonArray("weather");

			for (JsonElement element : weather) {
				if (!element.isJsonObject()) continue;
				JsonObject subWeather = element.getAsJsonObject();

				if (subWeather.has("main") && subWeather.has("description")) {
					descriptions.add(new Pair(subWeather.getAsJsonPrimitive("main").getAsString(), subWeather.getAsJsonPrimitive("description").getAsString()));
				}
			}
		}

		if (object.has("dt"))
			dateTime = object.getAsJsonPrimitive("dt").getAsLong();

		if (object.has("main")) {
			JsonObject main = object.getAsJsonObject("main");
			temperature = main.getAsJsonPrimitive("temp").getAsDouble();
			temperatureHighest = main.getAsJsonPrimitive("temp_max").getAsDouble();
			temperatureLowest = main.getAsJsonPrimitive("temp_min").getAsDouble();
			pressure = main.getAsJsonPrimitive("pressure").getAsDouble();
			humidity = main.getAsJsonPrimitive("humidity").getAsDouble();
		}

		if (object.has("wind")) {
			windSpeed = object.getAsJsonObject("wind").getAsJsonPrimitive("speed").getAsDouble();
			windDegrees = object.getAsJsonObject("wind").getAsJsonPrimitive("deg").getAsDouble();
		}

		if (object.has("clouds"))
			cloudsPercentage = object.getAsJsonObject("clouds").getAsJsonPrimitive("all").getAsDouble();

		if (object.has("rain")) {
			rain = object.getAsJsonObject("rain").getAsJsonPrimitive("3h").getAsDouble();
			snow = object.getAsJsonObject("snow").getAsJsonPrimitive("3h").getAsDouble();
		}
	}

	public double getTemperature() {
		return temperature;
	}

	public double getPressure() {
		return pressure;
	}

	public double getHumidity() {
		return humidity;
	}

	public double getTemperatureLowest() {
		return temperatureLowest;
	}

	public double getTemperatureHighest() {
		return temperatureHighest;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public double getWindDegrees() {
		return windDegrees;
	}

	public double getCloudsPercentage() {
		return cloudsPercentage;
	}

	public double getRain() {
		return rain;
	}

	public double getSnow() {
		return snow;
	}

	public long getDateTime() {
		return dateTime;
	}

	public Set<Pair> getDescriptions() {
		return descriptions;
	}
}
