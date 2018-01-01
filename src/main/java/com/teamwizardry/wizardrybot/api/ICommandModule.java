package com.teamwizardry.wizardrybot.api;

import ai.api.model.Result;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.message.Message;

public interface ICommandModule {

	String getActionID();

	String[] getAliases();

	void onCommand(DiscordApi api, Message message, Command command, Result result);
}
