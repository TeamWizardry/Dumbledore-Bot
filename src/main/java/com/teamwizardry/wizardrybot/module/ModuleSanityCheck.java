package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Statistics;
import com.teamwizardry.wizardrybot.api.StringConstants;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.util.Arrays;

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
		return "Say one of the following: " + Arrays.toString(StringConstants.hi) + " - followed by: " + Arrays.toString(StringConstants.albus);
	}

	@Override
	public String getExample() {
		return "'hey alby!' or 'sup albus!' or 'bonjour albus' or 'bonjour dumbledore!'";
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command) {
		if (command.isPotentiallyACommand()) return;
		if (!command.hasSaidHey()) return;

		String reply = result.getFulfillment().getSpeech();
		message.getChannel().sendMessage(reply);
		Statistics.INSTANCE.addToStat("sanity_checks");
	}
}
