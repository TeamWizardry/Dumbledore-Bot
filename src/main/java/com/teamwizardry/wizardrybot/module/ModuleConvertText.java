package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ModuleConvertText extends Module {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public String getName() {
		return "Text File Converter";
	}

	@Override
	public String getDescription() {
		return "Will convert text files to hastebin/pastebin links";
	}

	@Override
	public String getUsage() {
		return "Upload a text file";
	}

	@Override
	public String getExample() {
		return null;
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command) {
		if (message.getAttachments().isEmpty()) return;

		for (MessageAttachment attachment : message.getAttachments()) {
			if (attachment.isImage()) continue;
			if (attachment.getFileName().contains("txt") || attachment.getFileName().contains("log")) {

				try {
					HttpURLConnection urlcon = (HttpURLConnection) attachment.getUrl().openConnection();
					urlcon.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
					System.setProperty("http.agent", "Chrome");
					BufferedReader reader = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line).append("\n");
					}

					String txt = sb.toString();

					HttpResponse<String> response = Unirest
							.post("https://pastebin.com/api/api_post.php")
							.field("api_paste_code", txt)
							.field("api_dev_key", "78ed8f15e7325b20ea0752c3ac8aa1cc")
							.field("api_paste_private", 1)
							.field("api_paste_name", attachment.getFileName())
							.field("api_option", "paste")
							.asString();

					String node = response.getBody();

					message.getChannel().sendMessage(node.replace(".com/", ".com/raw/"));

					if (txt.contains("//")) {
						message.getChannel().sendMessage("SUMMARY:");
						message.getChannel().sendMessage("```" + StringUtils.substringBetween(txt, "Time: ", "at net.minecraft") + "```");
					} else if (txt.length() > 1500) {
						message.getChannel().sendMessage("```" + txt.substring(0, 1480) + "...```");
					} else {
						message.getChannel().sendMessage("```" + txt + "```");
					}

				} catch (IOException | UnirestException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
