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
		return new String[]{"commands", "cmds"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {
		StringBuilder commands = new StringBuilder();
		for (Module module : WizardryBot.modules) {
			if (module.isListed())
				commands.append("- ").append(module.getName()).append("\n");
		}

		EmbedBuilder embed = new EmbedBuilder().setTitle("List of things I can do").setColor(Color.YELLOW)
				.setDescription(commands.toString());
		message.getChannel().sendMessage("Type `hey albus` followed by a question for a command.\nFor example `hey albus, what does the math command do?`\nPlease note, you must say `command` after the command's name, otherwise i'll think it's a lookup question", embed);
		return true;
	}

	@Override
	public boolean overrideIncorrectUsage() {
		return true;
	}
}
