package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
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

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		Module askingAbout = null;
		String cmdName = result.getStringParameter("any").toLowerCase().trim();

		if (cmdName.isEmpty()) {
			message.getChannel().sendMessage("That's not a command you silly goof.");
			return;
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
			return;
		}

		EmbedBuilder embed = new EmbedBuilder().setTitle(askingAbout.getName() + ":").setColor(Color.BLUE)
				.setDescription("")
				.addField("Description", askingAbout.getDescription(), false)
				.addField("Usage", askingAbout.getUsage(), false)
				.addField("Example", askingAbout.getExample(), false);

		if (askingAbout instanceof ICommandModule) {
			if (((ICommandModule) askingAbout).getAliases().length > 0)
				embed.addField("Aliases", Arrays.toString(((ICommandModule) askingAbout).getAliases()), false);
		}
		message.getChannel().sendMessage("", embed);

	}
}
