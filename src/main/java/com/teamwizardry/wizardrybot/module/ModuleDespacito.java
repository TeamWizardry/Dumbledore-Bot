package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.AI;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.Module;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.io.File;
import java.util.Locale;

public class ModuleDespacito extends Module {
	@Override
	public String getName() {
		return "Despacito";
	}

	@Override
	public String getDescription() {
		return "Play Despacito";
	}

	@Override
	public String getUsage() {
		return "<albus or alexa> play despacito 2 <text>";
	}

	@Override
	public String getExample() {
		return "this is so sad.. alexa play despacito 2";
	}

	@Override
	public boolean overrideResponseCheck() {
		return true;
	}

	@Override
	public void onMessage(DiscordApi api, Message message, Result result, Command command) {
		if (command.hasSaidHey() || message.getContent().toLowerCase(Locale.getDefault()).contains("alexa")) {

			Result resultito = AI.INSTANCE.think(message.getContent());

			if (resultito != null && resultito.getAction().equals("input.despacito") && resultito.getScore() >= 0.8) {

				boolean despa2 = message.getContent().contains("cito 2");

				File file;
				if (despa2) {
					file = new File("despacito/despacito_2.mp3");
				} else {
					file = new File("despacito/despacito.mp3");
				}
				message.getChannel().sendMessage(file);
			}
		}
	}
}
