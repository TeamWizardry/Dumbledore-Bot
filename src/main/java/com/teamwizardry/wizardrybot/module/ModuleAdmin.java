package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

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
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if ((String.valueOf(message.getAuthor().getId())).equals("136826665069314048")) {

			String[] args = command.getCommandArguments().split(" ");

			if (args.length == 0) {
				message.getChannel().sendMessage("No args defined");
			} else if (args[0].equalsIgnoreCase("serverlist")) {
				StringBuilder builder = new StringBuilder("Servers I'm in:").append("\n");
				int count = 0;
				for (Server server : api.getServers()) {
					builder.append(server.getName())
							.append(" - ")
							.append(server.getMembers().size())
							.append("\n");
					count += server.getMembers().size();
				}

				builder.append("\nTotal Servers: ")
						.append(api.getServers().size())
						.append("\nTotal Members: ")
						.append(count);

				message.getChannel().sendMessage(builder.toString());
			}
		}
	}
}
