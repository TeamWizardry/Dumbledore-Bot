package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.util.Random;

public class ModuleCompliment extends Module implements ICommandModule {

	@Override
	public String getName() {
		return "Courtesy";
	}

	@Override
	public String getDescription() {
		return "Dumbledore will respond to compliments.";
	}

	@Override
	public String getUsage() {
		return "hey albus, <compliment>";
	}

	@Override
	public String getExample() {
		return "'hey albus, ur pretty cool' or 'hey albus, ur so sweet. <3'";
	}

	@Override
	public String getActionID() {
		return "input.courtesy";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (result.getStringParameter("courtesy") == null) return;

		String compliment = result.getStringParameter("courtesy").toLowerCase().trim();
		if (compliment.equals("")) return;

		Random rand = new Random();

		int random = rand.nextInt(3);
		switch (random) {
			case 0: {
				message.getChannel().sendMessage("Thank you. <3");
				break;
			}
			case 1: {
				message.getChannel().sendMessage("Aww shucks. Thanks! :D");
				break;
			}
			case 2: {
				message.getChannel().sendMessage("Aww thanks! <3");
				break;
			}
		}
	}
}
