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

public class ModuleCommands extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "input.abilities";
	}

	@Override
	public String getName() {
		return "Commands";
	}

	@Override
	public String getDescription() {
		return "Get a list of all commands";
	}

	@Override
	public String getUsage() {
		return "hey albus, <question>";
	}

	@Override
	public String getExample() {
		return "'hey albus, what can you do?' or 'what are some of your abilities?";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		StringBuilder commands = new StringBuilder();
		for (Module module : WizardryBot.modules) {
			if (module.isListed())
				commands.append("- ").append(module.getName()).append("\n");
		}

		EmbedBuilder embed = new EmbedBuilder().setTitle("List of stuff I can do").setColor(Color.BLUE)
				.setDescription(commands.toString());
		message.getChannel().sendMessage("Type 'hey albus, ' followed by the command in question, for example 'hey albus, what does the math command do?", embed);
	}
}
