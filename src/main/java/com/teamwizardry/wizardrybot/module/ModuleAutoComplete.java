package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.message.Message;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ModuleAutoComplete extends Module implements ICommandModule {

	private HashMap<String, ArrayList<String>> carosal = new HashMap<>();

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String getName() {
		return "Auto Complete";
	}

	@Override
	public String getDescription() {
		return "Albus will autocomplete your sentence (according to google's api)";
	}

	@Override
	public String getUsage() {
		return "hey albus, autocomplete <sentence to autocomplete>";
	}

	@Override
	public String getExample() {
		return "'hey albus, autocomplete potatoes are'";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"autocomplete", "autocomp", "predict"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		ThreadManager.INSTANCE.addThread(new Thread(() -> {
			try {
				String path = command.getCommandArguments().trim().replace(" ", "%20");
				FileUtils.copyURLToFile(
						new URL("http://suggestqueries.google.com/complete/search?client=chrome&q=" + path),
						new File("autocomplete.json"), 10000, 10000);

				File file = new File("autocomplete.json");
				if (!file.exists()) return;
				JsonElement element = new JsonParser().parse(new FileReader(file));


				if (element.isJsonArray()) {
					JsonArray array = element.getAsJsonArray();
					for (JsonElement element1 : array) {
						if (element1.isJsonArray()) {
							JsonArray secondArray = element1.getAsJsonArray();
							{
								carosal.putIfAbsent(command.getCommandArguments(), new ArrayList<>());
								if (carosal.get(command.getCommandArguments()).isEmpty())
									for (JsonElement element2 : secondArray) {
										if (element2.isJsonPrimitive())
											carosal.get(command.getCommandArguments()).add(element2.getAsString());
									}
							}
							{
								if (!carosal.get(command.getCommandArguments()).isEmpty())
									message.getChannel().sendMessage(Utils.processMentions(carosal.get(command.getCommandArguments()).remove(new Random().nextInt(carosal.get(command.getCommandArguments()).size() - 1))));
							}
							file.delete();
							return;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}
}
