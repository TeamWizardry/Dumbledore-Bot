package com.teamwizardry.wizardrybot.api;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.WizardryBot;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.net.*;
import java.util.concurrent.ExecutionException;

public class Utils {

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
	public static User lookupUserFromHash(String hash, Channel channel) {
		for (User user : channel.getApi().getServerChannelById(channel.getId()).get().getServer().getMembers()) {
			if (BCrypt.checkpw(String.valueOf(user.getId()), hash)) {
				return user;
			}
		}
		return null;
	}

	@Nullable
	public static User lookupUserFromHash(String hash) {
		for (Server server : WizardryBot.API.getServers())
			for (User user : server.getMembers()) {
				if (BCrypt.checkpw(String.valueOf(user.getId()), hash)) {
					return user;
				}
			}
		return null;
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
