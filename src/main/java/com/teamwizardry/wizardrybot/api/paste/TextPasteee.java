package com.teamwizardry.wizardrybot.api.paste;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.annotation.Nullable;
import java.net.URL;

public class TextPasteee implements TextLink {

	@Override
	public boolean test(URL url) {
		String string = url.toString();
		return string.contains("paste.ee");
	}

	@Nullable
	@Override
	public String getText(URL url) {
		String string = url.toString();
		if (!string.contains("paste.ee/r/")) {
			string = string.replace("pastee.ee/p/", ".pastee.ee/r/")
					.replace("pastee.ee/d/", ".pastee.ee/r/");
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
