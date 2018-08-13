package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.WizardryBot;
import kotlin.Pair;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
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

	public static void sendWebhookMessage(Webhook webhook, String message, String username, String avatarURL) {
		webhook.getToken().ifPresent(token -> {
			JsonObject object = new JsonObject();
			object.addProperty("content", message);
			object.addProperty("username", username);
			object.addProperty("avatar_url", avatarURL);
			try {
				Unirest.post("https://discordapp.com/api/v6/webhooks/" + webhook.getIdAsString() + "/" + token)
						.header("Content-Type", "application/json")
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

	@Nullable
	public static User lookupUserFromHash(JsonObject object, DiscordApi api) {
		for (Server server : api.getServers()) {
			for (User user : server.getMembers()) {
				if (checkHashMatch(user.getIdAsString(), object)) {
					return user;
				}
			}
		}
		return null;
	}

	@Nullable
	public static User lookupUserFromHash(JsonObject object, Channel channel) {
		for (User user : channel.getApi().getServerChannelById(channel.getId()).get().getServer().getMembers()) {
			if (checkHashMatch(user.getIdAsString(), object)) {
				return user;
			}
		}
		return null;
	}

	@Nullable
	public static User lookupUserFromHash(String hash, String salt, Channel channel) {
		for (User user : channel.getApi().getServerChannelById(channel.getId()).get().getServer().getMembers()) {
			if (checkHashMatch(user.getIdAsString(), hash, salt)) {
				return user;
			}
		}
		return null;
	}

	@Nullable
	public static User lookupUserFromHash(String hash, String salt) {
		for (Server server : WizardryBot.API.getServers())
			for (User user : server.getMembers()) {
				if (checkHashMatch(user.getIdAsString(), hash, salt)) {
					return user;
				}
			}
		return null;
	}

	public static boolean checkHashMatch(String plainText, JsonObject object) {
		Pair<byte[], byte[]> hashSalt = getHashSalt(object);
		if (hashSalt == null) return false;
		return checkHashMatch(plainText, hashSalt);
	}

	public static boolean checkHashMatch(String plainText, Pair<byte[], byte[]> pair) {
		return checkHashMatch(plainText, pair.getFirst(), pair.getSecond());
	}

	public static boolean checkHashMatch(String plainText, String hash, String salt) {
		return checkHashMatch(plainText, salt.getBytes(), hash.getBytes());
	}

	public static boolean checkHashMatch(String plainText, byte[] hash, byte[] salt) {
		return Crypto.isExpectedPassword(plainText.toCharArray(), salt, hash);
	}

	@Nullable
	public static Pair<byte[], byte[]> getHashSalt(JsonObject object) {
		if (!object.has("salt") || !object.has("hash")) return null;

		byte[] salt = Base64.decodeBase64(object.getAsJsonPrimitive("salt").getAsString());
		byte[] hash = Base64.decodeBase64(object.getAsJsonPrimitive("hash").getAsString());

		return new Pair<>(hash, salt);
	}

	public static JsonObject encryptString(User user) {
		return encryptString(user.getIdAsString());
	}

	public static JsonObject encryptString(String string) {
		JsonObject object = new JsonObject();
		byte[] saltBytes = Crypto.getNextSalt();
		String salt = Base64.encodeBase64String(saltBytes);
		object.addProperty("salt", salt);
		object.addProperty("hash", Base64.encodeBase64String(Crypto.hash(string.toCharArray(), saltBytes)));

		return object;
	}

	public static String encrypt(String string) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(Keys.PASSWORD);
		return textEncryptor.encrypt(string);

	}

	public static String decrypt(String string) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(Keys.PASSWORD);
		return textEncryptor.decrypt(string);
	}

	public static byte[] getMACAddress() throws SocketException, UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);

		return networkInterface.getHardwareAddress();
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
		for (String word : string.split(" ")) {
			try {
				new URL(word);
				return word;
			} catch (MalformedURLException ignored) {
			}
		}
		return null;
	}
}
