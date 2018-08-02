package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.common.base.Splitter;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

public class ModuleConvertText extends Module {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public int getPriority() {
		return -1;
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

					if (txt.contains("Time: ") && txt.contains("at net.minecraft")) {
						message.getChannel().sendMessage("SUMMARY:");
						message.getChannel().sendMessage("```" + StringUtils.substringBetween(txt, "Time: ", "at net.minecraft") + "```");
					} else if (txt.length() > 1500) {
						List<String> splits = Splitter.fixedLength(1500).splitToList(txt);
						for (String string : splits)
							message.getChannel().sendMessage("```" + string + "```");
					} else {
						message.getChannel().sendMessage("```" + txt + "```");
					}
					Statistics.INSTANCE.addToStat("text_files_summarized");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
