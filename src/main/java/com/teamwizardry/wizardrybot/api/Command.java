package com.teamwizardry.wizardrybot.api;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.WizardryBot;
import org.javacord.api.entity.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Command {

	@NotNull
	private final Message message;
	@Nullable
	private String commandUsed = null;
	private boolean hasSaidHey;
	private boolean isPotentiallyACommand;
	@NotNull
	private final String content;
	@Nullable
	private Result resultWithoutHey = null;

	public Command(@NotNull Message message, HashSet<String> commands) {
		this.message = message;

		content = Utils.processMentions(message);

		Result result = null;
		String[] splits = content.split(",");

		if (splits[0].contains("hey albus") || splits[0].contains("hey alby")) {
			hasSaidHey = true;
		} else {
			result = AI.INSTANCE.think(content.contains(",") ? splits[0] : content);
			hasSaidHey = WizardryBot.doesPassResult(result, "input.hey");
		}
		if (hasSaidHey) Statistics.INSTANCE.addToStat("hey_albuses");

		if (!hasSaidHey && result != null) {
			resultWithoutHey = result;
		}

		String[] split = content.trim().split(",");
		isPotentiallyACommand = (split.length >= 2 && !split[1].trim().isEmpty());

		// Now process the command used

		if (hasSaidHey && isPotentiallyACommand) {
			String[] hey = content.split(",");
			String afterHey = hey[1].trim().toLowerCase();

			for (String string : commands)
				if (afterHey.startsWith(string.toLowerCase())) {
					commandUsed = string.toLowerCase();
					break;
				}
		}
	}

	@Nullable
	public String getCommandUsed() {
		return commandUsed;
	}

	@NotNull
	public String getCommandArguments() {
		String[] hey = content.split(",");
		if (hey.length < 2) {
			return content.trim();
		} else {
			if (commandUsed != null) {
				return hey[1].substring(hey[1].indexOf(commandUsed) + commandUsed.length()).trim();
			} else {
				return hey[1].trim();
			}
		}
	}

	public boolean hasSaidHey() {
		return hasSaidHey;
	}

	public boolean isPotentiallyACommand() {
		return isPotentiallyACommand;
	}

	@Nullable
	public Result getResultWithoutHey() {
		return resultWithoutHey;
	}
}
