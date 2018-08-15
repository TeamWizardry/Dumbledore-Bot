package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class ModuleThanks extends Module implements ICommandModule {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public String getActionID() {
		return "input.thanks";
	}

	@Override
	public String getName() {
		return "Thanks";
	}

	@Override
	public String getDescription() {
		return "Thank albus for something.";
	}

	@Override
	public String getUsage() {
		return "say thank you with albus's name to indicate that you are thanking albus";
	}

	@Override
	public String getExample() {
		return "'thanks albus!'";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (!result.getParameters().containsKey("albus")) return true;
		if (!result.getParameters().containsKey("thanks")) return false;

		String reply = result.getFulfillment().getSpeech();
		message.getChannel().sendMessage(reply);
		Statistics.INSTANCE.addToStat("compliments_given");

		return true;
	}
}
