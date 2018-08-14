package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import org.apache.commons.io.FileUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

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
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result) {
		try {
			String path = command.getArguments().trim().replace(" ", "%20");
			FileUtils.copyURLToFile(
					new URL("http://suggestqueries.google.com/complete/search?client=chrome&q=" + path),
					new File("autocomplete.json"), 10000, 10000);

			File file = new File("autocomplete.json");
			if (!file.exists()) return true;
			JsonElement element = new JsonParser().parse(new FileReader(file));


			if (element.isJsonArray()) {
				JsonArray array = element.getAsJsonArray();
				for (JsonElement element1 : array) {
					if (element1.isJsonArray()) {
						JsonArray secondArray = element1.getAsJsonArray();
						{
							carosal.putIfAbsent(command.getArguments(), new ArrayList<>());
							if (carosal.get(command.getArguments()).isEmpty())
								for (JsonElement element2 : secondArray) {
									if (element2.isJsonPrimitive()) {
										carosal.get(command.getArguments()).add(element2.getAsString());
										Statistics.INSTANCE.addToStat("auto_completes");
									}
								}
						}
						{
							if (!carosal.get(command.getArguments()).isEmpty()) {
								String s = Utils.processMentions(carosal.get(command.getArguments()).remove(new Random().nextInt(carosal.get(command.getArguments()).size() - 1)));
								message.getChannel().sendMessage(s);
								ModuleObjectiveQuestion.runLookup(message, s);
								Statistics.INSTANCE.addToStat("auto_complete_lookups");
							}
						}
						file.delete();
						return true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
}
