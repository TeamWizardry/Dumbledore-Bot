package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public class ModuleWizardry extends Module {

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getName() {
		return "Wizardry";
	}

	@Override
	public String getDescription() {
		return "Ask questions about wizardry";
	}

	@Override
	public String getUsage() {
		return null;
	}

	@Override
	public String getExample() {
		return null;
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command) {
		//Result result1 = WizardryAI.INSTANCE.think(message.getContent());
		//if (result1 == null) return;
		//String reply = result1.getFulfillment().getSpeech();
		//message.getChannel().sendMessage(reply);
//
		//message.getChannel().sendMessage(result1.getAction());
	}
}
