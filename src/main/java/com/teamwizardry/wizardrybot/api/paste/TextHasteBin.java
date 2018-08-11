package com.teamwizardry.wizardrybot.api.paste;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.annotation.Nullable;
import java.net.URL;

public class TextHasteBin implements TextLink {

	@Override
	public boolean test(URL url) {
		String string = url.toString();
		return string.contains("hastebin.com");
	}

	@Nullable
	@Override
	public String getText(URL url) {
		String string = url.toString();
		if (!string.contains("raw")) {
			string = string.replace(".com/", ".com/raw/");
		}
		try {
			HttpResponse<String> response = Unirest.get(string).asString();
			return response.getBody();

		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
}
