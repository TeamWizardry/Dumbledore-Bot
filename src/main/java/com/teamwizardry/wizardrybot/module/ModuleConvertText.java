package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.common.base.Splitter;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import com.teamwizardry.wizardrybot.api.Utils;
import com.teamwizardry.wizardrybot.api.paste.TextLinkExtractor;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	public void onMessage(DiscordApi api, Message message, Result result, Command command, boolean whatsapp) {
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

					String text = sb.toString();
					if (text.isEmpty()) continue;

					processText(message, text);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Set<URL> urls = Utils.findURLsInString(message.getContent());
		if (urls.isEmpty()) return;

		for (URL url : urls) {
			String text = TextLinkExtractor.getText(url);
			if (text == null || text.isEmpty()) continue;

			processText(message, text);
		}
	}

	private void processText(Message message, String text) {
		if (text.contains("Time: ") && text.contains("at net.minecraft")) {
			message.getChannel().sendMessage("SUMMARY:");
			message.getChannel().sendMessage("```" + StringUtils.substringBetween(text, "Time: ", "at net.minecraft") + "```");
		} else if (text.length() > 1500) {
			List<String> splits = new ArrayList<>(Splitter.fixedLength(1500).splitToList(text));
			while (splits.size() > 5) {
				splits.remove(0);
			}
			for (String string : splits)
				message.getChannel().sendMessage("```" + string + "```");
		} else {
			message.getChannel().sendMessage("```" + text + "```");
		}

		Statistics.INSTANCE.addToStat("text_files_summarized");

		if (text.contains("java.lang.NoClassDefFoundError: com/teamwizardry/librarianlib")) {
			message.getChannel().sendMessage("***Solution: UPDATE LIBRARIANLIB***");
			message.getChannel().sendMessage("***LibrarianLib Download Link: https://minecraft.curseforge.com/projects/librarianlib***");
			Statistics.INSTANCE.addToStat("liblib_update_solutions_given");
		}

		if (text.contains("---- Minecraft Crash Report ----")) {
			String versionTable = StringUtils.substringBetween(text,
					"-- System Details --",
					"Loaded coremods");

			new BufferedReader(new StringReader(versionTable)).lines().forEach(line -> {
				String keyword;
				if (line.contains("wizardry") && !line.contains("Electroblob")) {
					keyword = "wizardry";
				} else if (line.contains("librarianlib")) {
					keyword = "librarianlib";
				} else return;

				line = line.replace(" ", "");
				line = StringUtils.substringBetween(line, keyword + "|", "|None");

				if (line.contains("|")) {
					String[] sections = line.split("\\|");

					if (sections.length >= 2) {
						String builder = StringUtils.capitalize(keyword) + " Version: `" + sections[0] + "`\n" +
								StringUtils.capitalize(keyword) + " Jar Name: `" + sections[1] + "`\n";
						message.getChannel().sendMessage(builder);
					}
				}
			});
		}
	}
}
