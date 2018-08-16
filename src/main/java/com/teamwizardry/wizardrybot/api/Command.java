package com.teamwizardry.wizardrybot.api;

import ai.api.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class Command {

	private Result result = null;
	private String command = null;
	private boolean isPotentiallyACommand;
	private boolean hasSaidHey = false;
	@Nullable
	private String afterHey = null;

	public Command(@NotNull Message message, HashSet<String> commands) {
		String content = Utils.processMentions(message);

		String safeContent = content.toLowerCase().trim();
		if (safeContent.startsWith("hey albus")
				|| safeContent.startsWith("hey abluis")
				|| safeContent.startsWith("hey ablus")
				|| safeContent.startsWith("hey alby")
				|| safeContent.startsWith("hey dumbledore")) {
			hasSaidHey = true;
			Statistics.INSTANCE.addToStat("hey_albuses");
		}

		if (content.contains(",")) {
			afterHey = StringUtils.substringAfter(content, ",").trim();
		} else if (StringUtils.countMatches(content, " ") > 1) {
			// Two spaces over
			afterHey = StringUtils.substringAfter(StringUtils.substringAfter(content, " ").trim(), " ").trim();
		}

		isPotentiallyACommand = afterHey != null && !afterHey.isEmpty();

		if (isPotentiallyACommand) {
			result = AI.INSTANCE.think(afterHey);
		} else {
			String toThink = content.contains(",") ? content.split(",")[1].trim() : content;
			if (!toThink.isEmpty())
				result = AI.INSTANCE.think(toThink);
		}

		// Now process the command used
		if (hasSaidHey && isPotentiallyACommand) {
			for (String string : commands) {

				if (afterHey.toLowerCase().startsWith(string)) {
					command = string;
					afterHey = afterHey.substring(string.length()).trim();
					break;
				}
				if (afterHey.toLowerCase().startsWith(string.replace(" ", ""))) {
					command = string;
					afterHey = afterHey.substring(string.replace(" ", "").length()).trim();
					break;
				}
			}
		}
	}

	public String getCommand() {
		return command;
	}

	@Nonnull
	public String getArguments() {
		String args = "";
		if (afterHey != null && !afterHey.isEmpty()) {
			args = afterHey;
		}

		return args;
	}

	public boolean hasSaidHey() {
		return hasSaidHey;
	}

	public boolean isPotentiallyACommand() {
		return isPotentiallyACommand;
	}

	public Result getResult() {
		return result;
	}
}
