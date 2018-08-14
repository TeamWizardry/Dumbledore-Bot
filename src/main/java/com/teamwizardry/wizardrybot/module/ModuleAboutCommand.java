package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;

public class ModuleAboutCommand extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "input.ask_command";
	}

	@Override
	public String getName() {
		return "Command Information";
	}

	@Override
	public String getDescription() {
		return "Get information about a specific command";
	}

	@Override
	public String getUsage() {
		return "hey albus, <question about command>";
	}

	@Override
	public String getExample() {
		return "'hey albus, what's the sanity check command?' or 'hey alby, what does the math command do?'";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	public static void sendCommandMessage(Message message, Module module) {
		EmbedBuilder embed = new EmbedBuilder().setTitle(module.getName() + ":").setColor(Color.BLUE)
				.setDescription("")
				.addField("Description", module.getDescription(), false)
				.addField("Usage", module.getUsage(), false)
				.addField("Example", module.getExample(), false);

		if (module instanceof ICommandModule) {
			if (((ICommandModule) module).getAliases().length > 0)
				embed.addField("Aliases", Arrays.toString(((ICommandModule) module).getAliases()), false);
		}
		message.getChannel().sendMessage(embed);
		Statistics.INSTANCE.addToStat("commands_questioned");

	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		Module askingAbout = null;
		String cmdName = result.getStringParameter("any").toLowerCase().trim();

		if (cmdName.isEmpty()) {
			message.getChannel().sendMessage("That's not a command you silly goof.");
			return true;
		}

		for (Module module : WizardryBot.modules) {
			if (module.getName().toLowerCase().contains(cmdName)) {
				askingAbout = module;
				break;
			} else if (module instanceof ICommandModule) {
				ICommandModule cmd = (ICommandModule) module;
				if (Arrays.asList(cmd.getAliases()).contains(cmdName)) {
					askingAbout = module;
					break;
				}
			}
		}
		if (askingAbout == null) {
			message.getChannel().sendMessage("That's not a command you silly goof.");
			return true;
		}

		sendCommandMessage(message, askingAbout);

		return true;
	}
}
