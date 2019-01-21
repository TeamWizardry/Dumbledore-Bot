package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ModuleConvertText extends Module implements ICommandModule {

	@Override
	public String getName() {
		return "Log Reader";
	}

	@Override
	public String getDescription() {
		return "Read an uploaded text file either from a link or direct file upload";
	}

	@Override
	public String getUsage() {
		return "hey albus, read";
	}

	@Override
	public String getExample() {
		return "hey albus, read";
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"read", "read that file", "read file", "read that link", "read link", "read log"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message mainMessage, Command command, Result result, boolean whatsapp) {
		try {
			List<Message> messageHistory = new ArrayList<>(mainMessage.getChannel().getMessages(20).get());

			Collections.reverse(messageHistory);
			for (Message message : messageHistory) {

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
				if (urls.isEmpty()) return true;

				for (URL url : urls) {
					String text = TextLinkExtractor.getText(url);
					if (text == null || text.isEmpty()) continue;

					processText(message, text);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void processText(Message message, String text) {
		if (text.contains("A detailed walkthrough of the error, its code path and all known details is as follows:")) {
			message.getChannel().sendMessage("SUMMARY:");
			String chunk = StringUtils.substringBetween(text, "Description: ", "A detailed walkthrough of the error, its code path and all known details is as follows:");

			StringBuilder chunkBuilder = new StringBuilder();
			new BufferedReader(new StringReader(chunk)).lines().forEach(line -> {
				if (!line.contains("at sun") && !line.contains("at GradleStart") && !line.contains("at java") && !line.contains("at com.google")) {
					chunkBuilder.append(line).append("\n");
				}
			});

			if (!chunkBuilder.toString().isEmpty()) {
				message.getChannel().sendMessage(chunkBuilder.toString());
			}
		}

		Statistics.INSTANCE.addToStat("crash_reports_summarized");

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
