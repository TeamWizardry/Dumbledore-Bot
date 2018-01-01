package com.teamwizardry.wizardrybot.api;

import com.teamwizardry.wizardrybot.Keys;
import com.teamwizardry.wizardrybot.WizardryBot;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.channels.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.Role;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.net.*;

public class Utils {

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

		String[] mentions = StringUtils.substringsBetween(string, "<@", ">");
		if (mentions != null)
			for (String id : mentions) {
				id = id.replace("!", "");
				User user = WizardryBot.API.getUserById(id).get();
				string = string.replace("<@" + id + ">", "@\u200B" + user.getName());
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
