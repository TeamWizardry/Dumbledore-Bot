package com.teamwizardry.wizardrybot.api;

import com.teamwizardry.wizardrybot.api.imgur.ImgurUploader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedField;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;
import java.util.function.Consumer;

public class WhatsappDiscordAdapter {

	public static String convertMessage(Message message) {
		StringBuilder builder = new StringBuilder();
		if (!message.getContent().isEmpty()) {
			lines(message.getContent(), line -> {
				builder.append(adaptFormatting(line)).append("\n\n");
			});
		}

		if (!message.getEmbeds().isEmpty()) {

			for (Embed embed : message.getEmbeds()) {
				builder.append("/----------------------------------------------------\\").append("\n");

				String title = stripFormatting(embed.getTitle().orElse(""));
				String description = embed.getDescription().orElse("");

				if (!title.isEmpty()) {
					lines(title, line -> {
						builder.append("| ").append("*").append(stripFormatting(line)).append("*").append("\n");
					});
				}

				if (!description.isEmpty()) {
					lines(description, line -> {
						builder.append("| | ").append(adaptFormatting(line)).append("\n");
					});
				}

				if (!title.isEmpty() && (description.isEmpty() || !embed.getFields().isEmpty()))
					builder.append("|=======================>").append("\n").append("| \n");
				else if (!title.isEmpty() && !embed.getFields().isEmpty()) {
					builder.append("|=======================>").append("\n").append("| \n");
				}

				for (EmbedField field : embed.getFields()) {
					if (field.getName().isEmpty()) continue;

					builder.append("| ").append("*").append(stripFormatting(field.getName())).append("*").append("\n");

					if (field.getValue().isEmpty()) continue;

					lines(field.getValue(), line -> {
						builder.append("| | ").append(adaptFormatting(line)).append("\n");
					});
				}

				builder.append("\\----------------------------------------------------/");
			}
		}

		if (!message.getAttachments().isEmpty()) {
			builder.append("\n");
			for (MessageAttachment attachment : message.getAttachments()) {
				if (attachment.isImage()) {
					attachment.downloadAsImage().whenComplete((bufferedImage, throwable) -> {
						File file = new File("downloads/whatsapp-adapter-" + UUID.randomUUID() + "." + FilenameUtils.getExtension(attachment.getFileName()));
						try {
							ImageIO.write(bufferedImage, FilenameUtils.getExtension(attachment.getFileName()), file);
							String url = ImgurUploader.upload(file);

							if (url != null && !url.isEmpty())
								builder.append(url).append("\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			}
		}

		return builder.toString();
	}

	private static String adaptFormatting(String discordFormat) {
		discordFormat = discordFormat.replace("**", "_").replace("~~", "~")
				.replace("Demoniaque", "Saad").replace("demoniaque", "saad")
				.trim();

		if (discordFormat.contains("```")) {
			String blocks = StringUtils.substringBetween(discordFormat, "```");

			StringBuilder blockBuilder = new StringBuilder();
			lines(blocks, line -> blockBuilder.append("> ").append(line).append("\n"));
			discordFormat = discordFormat.replace(blocks, blockBuilder.toString());
		}

		return discordFormat.replace("`", "\"");
	}

	private static String stripFormatting(String discordFormat) {
		return discordFormat.replace("*", "").replace("~", "")
				.replace("Demoniaque", "Saad").replace("demoniaque", "saad")
				.trim();
	}

	private static void lines(String string, Consumer<String> lineConsumer) {
		new BufferedReader(new StringReader(string)).lines().forEach(lineConsumer);
	}
}
