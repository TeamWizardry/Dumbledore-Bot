package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.ThreadManager;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.message.Message;

public class ModuleStop extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "albus-stop";
	}

	@Override
	public String getName() {
		return "Stop";
	}

	@Override
	public String getDescription() {
		return "Stop any thinking albus is doing right now.";
	}

	@Override
	public String getUsage() {
		return "hey albus, stop";
	}

	@Override
	public String getExample() {
		return "'hey albus, stop'";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"stop"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		message.getChannel().sendMessage("Alright, stopping everything...");
		ThreadManager.INSTANCE.stopAll();
	}
}
