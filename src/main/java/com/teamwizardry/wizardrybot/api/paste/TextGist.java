package com.teamwizardry.wizardrybot.api.paste;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Map;

public class TextGist implements TextLink {

	@Override
	public boolean test(URL url) {
		String string = url.toString();
		return string.contains("gist.github.com");
	}

	@Nullable
	@Override
	public String getText(URL url) {
		String string = url.toString();
		String[] chunk = string.split("/");
		String gistID = chunk[chunk.length - 1];
		if (gistID.isEmpty()) return null;

		try {
			HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/gists/" + gistID).asJson();
			JsonElement element = new JsonParser().parse(response.getBody().toString());
			if (!element.isJsonObject()) return null;
			JsonObject object = element.getAsJsonObject();

			if (object.has("files") && object.get("files").isJsonObject()) {

				JsonObject files = object.getAsJsonObject("files");

				StringBuilder text = new StringBuilder();
				for (Map.Entry<String, JsonElement> entry : files.entrySet()) {
					if (entry.getValue().isJsonObject()) {
						JsonObject body = entry.getValue().getAsJsonObject();

						if (body.has("raw_url") && body.get("raw_url").isJsonPrimitive()) {
							String raw = body.getAsJsonPrimitive("raw_url").getAsString();

							HttpResponse<String> rawRes = Unirest.get(raw).asString();
							text.append(rawRes.getBody()).append(StringUtils.repeat("\n", 10));
						}
					}
				}

				return text.toString();
			}

		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
}
