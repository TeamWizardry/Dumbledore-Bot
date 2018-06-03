package com.teamwizardry.wizardrybot.api;

import ai.api.model.Result;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

public interface ICommandModule {

	String getActionID();

	String[] getAliases();

	void onCommand(DiscordApi api, Message message, Command command, Result result);
}
