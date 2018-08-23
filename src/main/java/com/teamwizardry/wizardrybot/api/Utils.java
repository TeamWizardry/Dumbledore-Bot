package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.math.Vec2d;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Utils {

	public static List<BufferedImage> stupidVerboseImageSearch(Message message) {
		List<BufferedImage> images = new ArrayList<>();
		String matchedURL = Utils.findURLInString(message.getContent());

		if (matchedURL != null && !matchedURL.isEmpty()) {
			images.add(Utils.downloadURLAsImage(null, matchedURL));
		} else {
			for (MessageAttachment attachment : message.getAttachments()) {
				if (attachment.isImage()) {
					try {
						BufferedImage image = attachment.downloadAsImage().get();
						if (image != null)
							images.add(image);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
			if (images.isEmpty()) {

				try {
					List<Message> messageHistory = new ArrayList<>(message.getChannel().getMessages(20).get());

					Collections.reverse(messageHistory);

					for (Message msg : messageHistory) {
						String matchedURL1 = Utils.findURLInString(msg.getContent());
						if (matchedURL1 != null && !matchedURL1.isEmpty()) {
							BufferedImage img = Utils.downloadURLAsImage(null, matchedURL1);
							if (img != null) {
								images.add(img);
								break;
							}
						}

						for (MessageAttachment attachment : message.getAttachments()) {
							if (attachment.isImage()) {
								BufferedImage image = attachment.downloadAsImage().get();
								if (image != null)
									images.add(image);
							}
						}
					}

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		return images;
	}

	public static BufferedImage downloadURLAsImage(@Nullable Message message, String url) {
		if (!Domains.INSTANCE.isLinkWhitelisted(url)) {
			if (message != null)
				message.getChannel().sendMessage("Link `" + url + "` is not a whitelisted domain, sorry.");
			return null;
		}

		File file = new File("downloads/temp-" + UUID.randomUUID() + ".png");
		if (file.exists()) {
			file.delete();
		}

		try {
			URL urlObject = new URL(url);
			URLConnection urlConnection = urlObject.openConnection();
			urlConnection.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");

			BufferedImage img = ImageIO.read(urlConnection.getInputStream());
			if (img == null) {
				if (message != null)
					message.getChannel().sendMessage("Couldn't download image. Please try again with another link.");
				return null;
			}

			return img;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String imgToBase64String(final RenderedImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ImageIO.write(img, formatName, os);
			return Base64.encodeBase64String(os.toByteArray());
		} catch (final IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static void sendWebhookMessage(Webhook webhook, String message, String username, String avatarURL) {
		webhook.getToken().ifPresent(token -> {
			JsonObject object = new JsonObject();
			object.addProperty("content", message);
			object.addProperty("username", username);
			object.addProperty("avatar_url", avatarURL);
			try {
				Unirest.post("https://discordapp.com/api/v6/webhooks/" + webhook.getIdAsString() + "/" + token)
						.header("Context-Type", "application/json")
						.body(object.toString())
						.asString();
			} catch (UnirestException e) {
				e.printStackTrace();
			}
		});
	}

	public static String processMentions(Message message) {
		String string = message.getContent();
		string = string.replace("`", "")
				.replace("@everyone", "@\u200Beveryone")
				.replace("@here", "@\u200Bhere")
				.trim();

		if (string.startsWith("<WHATSAPP=")) {
			String handle = StringUtils.substringBetween(string, "<WHATSAPP=", ">");
			string = string.substring("<WHATSAPP=".length() + handle.length() + ">".length());
		}

		for (User id : message.getMentionedUsers()) {
			string = string.replace("<@" + id.getId() + ">", "@\u200B" + id.getName());
		}

		for (Role id : message.getMentionedRoles()) {
			string = string.replace("<@!" + id.getId() + ">", "@\u200B" + id.getName());
		}

		return string;
	}

	public static String processMentions(String string) {
		string = string.replace("`", "")
				.replace("@everyone", "@\u200Beveryone")
				.replace("@here", "@\u200Bhere")
				.trim();

		if (string.startsWith("<WHATSAPP=")) {
			String handle = StringUtils.substringBetween(string, "<WHATSAPP=", ">");
			string = string.substring("<WHATSAPP=".length() + handle.length() + ">".length());
		}

		try {
			String[] mentions = StringUtils.substringsBetween(string, "<@", ">");
			if (mentions != null)
				for (String id : mentions) {
					id = id.replace("!", "");
					User user = WizardryBot.API.getUserById(id).get();
					string = string.replace("<@" + id + ">", "@\u200B" + user.getName());
				}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return string;
	}

	public static User getUser(long id, DiscordApi api) {
		for (Server server : api.getServers()) {
			for (User user : server.getMembers()) {
				if (user.getId() == id) {
					return user;
				}
			}
		}
		return null;
	}

	@Nonnull
	public static Set<URL> findURLsInString(String string) {
		Set<URL> urls = new HashSet<>();
		for (String word : string.split(" ")) {
			try {
				urls.add(new URL(word));
			} catch (MalformedURLException ignored) {
			}
		}
		return urls;
	}

	@Nullable
	public static String findURLInString(String string) {
		if (string == null) return null;

		string = string.trim();
		if (string.isEmpty()) return null;

		if (string.contains(" ")) {
			for (String word : string.split(" ")) {
				try {
					new URL(word);
					return word;
				} catch (MalformedURLException ignored) {
				}
			}
		} else {
			try {
				new URL(string);
				return string;
			} catch (MalformedURLException ignored) {
			}
		}

		return null;
	}

	public static BufferedImage resize(BufferedImage bufferedImage, int scaledWidth, int scaledHeight) {
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, bufferedImage.getType());

		Graphics2D g2d = outputImage.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.drawImage(bufferedImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		return outputImage;
	}

	public static BufferedImage resizeProportionally(BufferedImage bufferedImage, int scaledWidth, int scaledHeight) {
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, bufferedImage.getType());

		Graphics2D g2d = outputImage.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Dimension scaled = getScaledDimension(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()), new Dimension(scaledWidth, scaledHeight));
		g2d.drawImage(bufferedImage, (int) (scaledWidth / 2.0 - scaled.width / 2.0), (int) (scaledHeight / 2.0 - scaled.height / 2.0), scaled.width, scaled.height, null);
		g2d.dispose();

		return outputImage;
	}

	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

		double requiredWidth, requiredHeight;
		double targetRatio = boundary.getWidth() / boundary.getHeight();
		double sourceRatio = imgSize.getWidth() / imgSize.getHeight();
		if (sourceRatio >= targetRatio) { // source is wider than target in proportion
			requiredWidth = boundary.getWidth();
			requiredHeight = requiredWidth / sourceRatio;
		} else { // source is higher than target in proportion
			requiredHeight = boundary.getHeight();
			requiredWidth = requiredHeight * sourceRatio;
		}
		return new Dimension((int) requiredWidth, (int) requiredHeight);
	}

	@Nullable
	public static Vec2d getVecFromName(String locName, Vec2d imgDims, Vec2d objDims) {
		locName = locName.toLowerCase().replace("corner", "").replace(" ", "").trim();

		switch (locName) {
			case "middle":
			case "center": {
				double x = (imgDims.x / 2.0) - (objDims.x / 2.0);
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "right":
			case "rightcenter":
			case "centerright": {
				double x = imgDims.x - objDims.x;
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "left":
			case "leftcenter":
			case "centerleft": {
				double x = 0;
				double y = (imgDims.y / 2.0) + (objDims.y / 2.0);

				return new Vec2d(x, y);
			}
			case "up":
			case "topcenter":
			case "centertop":
			case "top": {
				double x = (imgDims.x / 2.0) - (objDims.x / 2.0);
				double y = objDims.y;

				return new Vec2d(x, y);
			}
			case "down":
			case "bottomcenter":
			case "centerbottom":
			case "bottom": {
				double x = (imgDims.x / 2.0) - (objDims.x / 2.0);
				double y = imgDims.y - objDims.y / 2.0;

				return new Vec2d(x, y);
			}
			case "topleft":
			case "upperleft": {
				double x = 0;
				double y = objDims.y;

				return new Vec2d(x, y);
			}

			case "topright":
			case "upperright": {
				double x = imgDims.x - objDims.x;
				double y = objDims.y;

				return new Vec2d(x, y);
			}

			case "lowerleft":
			case "bottomleft": {
				double x = 0;
				double y = imgDims.y - objDims.y;

				return new Vec2d(x, y);
			}

			case "lowerright":
			case "bottomright": {
				double x = imgDims.x - objDims.x;
				double y = imgDims.y - objDims.y;

				return new Vec2d(x, y);
			}
		}

		return null;
	}

	public static String readableFileSize(long size) {
		if (size <= 0) return "0";
		final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public static Vec2d calculateCentroid(Set<Vec2d> room) {
		double centroidX = 0, centroidY = 0;

		for (Vec2d vec : room) {
			centroidX += vec.x;
			centroidY += vec.y;
		}
		return new Vec2d(centroidX / room.size(), centroidY / room.size());
	}
}
