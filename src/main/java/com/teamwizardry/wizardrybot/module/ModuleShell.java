package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ModuleShell extends Module implements ICommandModule {

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Shell";
	}

	@Override
	public String getDescription() {
		return "Run shell commands";
	}

	@Override
	public String getUsage() {
		return "hey albus, shell <shell stuff>";
	}

	@Override
	public String getExample() {
		return "'hey albus, shell ls";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"shell"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if ((String.valueOf(message.getAuthor().getId())).equals("136826665069314048")) {

			message.getChannel().sendMessage("Executing...");

			if (command.getCommandArguments().contains("start.sh")) {
				File file = new File("amRestarting.json");
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException ignored) {
					}
				}
				if (file.exists()) {
					try {
						JsonObject object = new JsonObject();
						object.addProperty("channel", message.getChannel().getId());
						FileWriter writer = new FileWriter(file);
						new Gson().toJson(object, writer);
						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

				CommandLine shellCommand = new CommandLine("sh").addArgument("-c");
				shellCommand.addArgument(command.getCommandArguments(), false);
				Executor exec = new DefaultExecutor();
				exec.setStreamHandler(streamHandler);
				exec.execute(shellCommand);

				message.getChannel().sendMessage("output: ```" + outputStream.toString() + "```");
			} catch (IOException e) {
				message.getChannel().sendMessage("I messed up -> " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
