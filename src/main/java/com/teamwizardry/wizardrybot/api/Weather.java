package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teamwizardry.stickytape.utils.JsonLib;

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
			snow,
			seaLevel,
			groundLevel;
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

		temperature = JsonLib.parse(object).getDouble("main/temp");
		temperatureHighest = JsonLib.parse(object).getDouble("main/temp_max");
		temperatureLowest = JsonLib.parse(object).getDouble("main/temp_min");
		pressure = JsonLib.parse(object).getDouble("main/pressure");
		humidity = JsonLib.parse(object).getDouble("main/humidity");
		windSpeed = JsonLib.parse(object).getDouble("wind/speed");
		windDegrees = JsonLib.parse(object).getDouble("wind/degrees");
		cloudsPercentage = JsonLib.parse(object).getDouble("clouds/all");
		rain = JsonLib.parse(object).getDouble("rain/3h");
		snow = JsonLib.parse(object).getDouble("snow/3h");
		seaLevel = JsonLib.parse(object).getDouble("main/sea_level");
		groundLevel = JsonLib.parse(object).getDouble("main/grnd_level");
		dateTime = JsonLib.parse(object).getLong("dt");
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

	public double getSeaLevel() {
		return seaLevel;
	}

	public double getGroundLevel() {
		return groundLevel;
	}

	public long getDateTime() {
		return dateTime;
	}

	public Set<Pair> getDescriptions() {
		return descriptions;
	}
}
