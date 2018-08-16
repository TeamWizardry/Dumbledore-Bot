package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class ModuleSanityCheck extends Module {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public String getName() {
		return "Sanity Check";
	}

	@Override
	public String getDescription() {
		return "Ping the bot.";
	}

	@Override
	public String getUsage() {
		return "Say hey albus";
	}

	@Override
	public String getExample() {
		return "'hey alby' or 'hey albus' or 'hey dumbledore'";
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command, boolean whatsapp) {
		if (!command.hasSaidHey()) return;
		if (command.isPotentiallyACommand()) return;

		String reply = result.getFulfillment().getSpeech();
		message.getChannel().sendMessage(reply);
		Statistics.INSTANCE.addToStat("sanity_checks");
	}
}
