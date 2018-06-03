package com.teamwizardry.wizardrybot.module;

import ai.api.model.Result;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.Map;

public class ModuleTime extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"sched", "schedule", "time"};
	}

	@Override
	public String getName() {
		return "Time";
	}

	@Override
	public String getDescription() {
		return "Sync time with multiple people in different timezones";
	}

	@Override
	public String getUsage() {
		return "hey albus, time <register/add <person> <timezone>>/<view [person]>/<remove <person>>";
	}

	@Override
	public String getExample() {
		return "hey albus, register demoniaque GMT+2";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		String[] args = command.getCommandArguments().replaceAll(" +", " ").split(" ");

		if (args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase("add")) {
			if (args.length < 3) {
				message.getChannel().sendMessage("Incorrect command usage.");
				return;
			}

			String person = args[1].trim();
			String timezone = args[2].trim().toLowerCase();

			if (person.isEmpty()) {
				message.getChannel().sendMessage("Name is empty.");
				return;
			}

			boolean foundZone = false;
			for (String zone : DateTimeZone.getAvailableIDs()) {
				if (zone.toLowerCase().contains(timezone)) {
					timezone = zone;
					foundZone = true;
					break;
				}
			}

			if (!foundZone) {
				message.getChannel().sendMessage("Couldn't find timezone `" + timezone + "`. Try another.");
				return;
			} else {
				message.getChannel().sendMessage("Timezone validated. Using `" + timezone + "`");
			}

			File file = new File("timezones-" + message.getServer().get().getIdAsString());

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				JsonElement object = new JsonParser().parse(new FileReader(file));

				if (object == null || object.isJsonNull()) {
					object = new JsonObject();
				}

				object.getAsJsonObject().addProperty(person, timezone);

				FileWriter writer = new FileWriter(file);
				writer.write(new Gson().toJson(object));
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[0].equalsIgnoreCase("remove")) {

			if (args.length < 2) {
				return;
			}
			String person = args[1];

			File file = new File("timezones-" + message.getServer().get().getIdAsString());

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				JsonElement object = new JsonParser().parse(new FileReader(file));

				if (object == null || object.isJsonNull()) {
					return;
				}

				if (object.getAsJsonObject().has(person)) {
					object.getAsJsonObject().remove(person);

					FileWriter writer = new FileWriter(file);
					writer.write(new Gson().toJson(object));
					writer.flush();
					message.getChannel().sendMessage("Removed `" + person + "` from list.");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[0].equalsIgnoreCase("view")) {

			String person = null;
			if (args.length >= 2) {
				person = args[1];
			}

			File file = new File("timezones-" + message.getServer().get().getIdAsString());

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				JsonElement object = new JsonParser().parse(new FileReader(file));

				if (object == null || object.isJsonNull()) {
					object = new JsonObject();
				}

				if (person == null) {
					if (object.getAsJsonObject().entrySet().isEmpty()) {
						message.getChannel().sendMessage("No person registered.");
						return;
					}

					int longestName = 0;
					for (Map.Entry<String, JsonElement> element : object.getAsJsonObject().entrySet()) {
						if (element.getKey().length() > longestName) longestName = element.getKey().length();
					}

					StringBuilder builder = new StringBuilder();

					builder.append("```");
					for (Map.Entry<String, JsonElement> element : object.getAsJsonObject().entrySet()) {
						DateTimeZone timezone = DateTimeZone.forID(element.getValue().getAsJsonPrimitive().getAsString());
						DateTime dateTime = new DateTime(timezone);

						DateTimeFormatter twelveHour = DateTimeFormat.forPattern("hh:mm a");
						DateTimeFormatter twentyFourHour = DateTimeFormat.forPattern("HH:mm a");

						int subtract = longestName - element.getKey().length();

						builder.append(element.getKey())
								.append(StringUtils.repeat(" ", subtract))
								.append(" -> ")
								.append(dateTime.toString(twelveHour))
								.append(" | ")
								.append(dateTime.toString(twentyFourHour))
								.append(" -- ")
								.append(timezone.getID())
								.append("\n");
					}
					builder.append("```");

					message.getChannel().sendMessage(builder.toString());
				} else {

					if (!object.getAsJsonObject().has(person)) {
						message.getChannel().sendMessage("`" + person + "` is not registered.");
						return;
					}

					String zone = object.getAsJsonObject().getAsJsonPrimitive(person).getAsString();

					StringBuilder builder = new StringBuilder();

					builder.append("```");
					DateTimeZone timezone = DateTimeZone.forID(zone);
					DateTime dateTime = new DateTime(timezone);

					DateTimeFormatter twelveHour = DateTimeFormat.forPattern("hh:mm a");
					DateTimeFormatter twentyFourHour = DateTimeFormat.forPattern("HH:mm a");

					builder.append(person)
							.append("-> ")
							.append(dateTime.toString(twelveHour))
							.append(" | ")
							.append(dateTime.toString(twentyFourHour))
							.append(" -- ")
							.append(timezone.getID())
							.append("\n");
					builder.append("```");

					message.getChannel().sendMessage(builder.toString());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			message.getChannel().sendMessage("Incorrect command usage.");
		}
	}
}
