package com.teamwizardry.wizardrybot.api;

import ai.api.model.Result;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public abstract class Module {

	public int getPriority() {
		return 0;
	}

	public boolean overrideResponseCheck() {
		return false;
	}

	public boolean isListed() {
		return true;
	}

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getUsage();

	public abstract String getExample();

	public void onMessage(DiscordApi api, Message message, Result result, Command command) {

	}
}
