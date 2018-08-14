package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.Map;

public class ModuleAdmin extends Module implements ICommandModule {

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Admin";
	}

	@Override
	public String getDescription() {
		return "Run admin commands";
	}

	@Override
	public String getUsage() {
		return "hey albus, admin <command>";
	}

	@Override
	public String getExample() {
		return "'hey albus, admin serverlist";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"admin"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		if ((String.valueOf(message.getAuthor().getId())).equals("136826665069314048")) {

			String[] args = command.getArguments().split(" ");

			if (args.length == 0) {
				message.getChannel().sendMessage("No args defined");
			} else if (args[0].equalsIgnoreCase("serverlist")) {
				int longestName = 0;
				for (Server server : api.getServers()) {
					if (server.getName().length() > longestName) longestName = server.getName().length();
				}

				StringBuilder builder = new StringBuilder("Servers I'm in:").append("\n```");
				int count = 0;
				for (Server server : api.getServers()) {
					int subtract = longestName - server.getName().length();

					builder.append(server.getName())
							.append(": ")
							.append(StringUtils.repeat(" ", subtract))
							.append(server.getMembers().size())
							.append("\n");
					count += server.getMembers().size();
				}

				builder.append("\nTotal Servers: ")
						.append(api.getServers().size())
						.append("\nTotal Members: ")
						.append(count)
						.append("```");

				message.getChannel().sendMessage(builder.toString());

			} else if (args[0].equalsIgnoreCase("stats")) {
				int longestName = 0;
				for (Map.Entry<String, JsonElement> element : Statistics.INSTANCE.getStats().entrySet()) {
					if (element.getKey().length() > longestName) longestName = element.getKey().length();
				}

				StringBuilder builder = new StringBuilder("Statistics:").append("\n```");
				for (Map.Entry<String, JsonElement> entry : Statistics.INSTANCE.getStats().entrySet()) {
					String key = StringUtils.capitalize(entry.getKey().replace("_", " "));
					int subtract = longestName - key.length();

					builder.append(key)
							.append(": ")
							.append(StringUtils.repeat(" ", subtract))
							.append(entry.getValue().getAsInt()).append("\n");
				}
				builder.append("```");
				message.getChannel().sendMessage(builder.toString());
			}
		}

		return true;
	}
}
