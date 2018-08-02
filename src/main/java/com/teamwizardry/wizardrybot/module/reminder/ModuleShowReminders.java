package com.teamwizardry.wizardrybot.module.reminder;

import ai.api.model.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Utils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.joda.time.DateTime;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ModuleShowReminders extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "show_reminders";
	}

	@Override
	public String getName() {
		return "Show Reminders";
	}

	@Override
	public String getDescription() {
		return "Albus will tell you what reminders you told him to remember.";
	}

	@Override
	public String getUsage() {
		return "'hey albus, <ask about reminders>'";
	}

	@Override
	public String getExample() {
		return "'hey albus, what are my reminders?`";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		File file = new File("remind_me.json");
		if (!file.exists()) {
			message.getChannel().sendMessage("You don't have any reminders you silly goof.");
			return;
		}

		if (!file.canRead()) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry.");
			return;
		}

		if (!file.canWrite()) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry.");
			return;
		}

		try {
			JsonElement jsonElement = new JsonParser().parse(new FileReader(file));
			if (!jsonElement.isJsonArray()) {
				jsonElement = new JsonArray();
			}
			JsonArray array = jsonElement.getAsJsonArray();

			Set<JsonObject> objects = new HashSet<>();
			for (JsonElement element : array) {
				if (!element.isJsonObject()) continue;
				JsonObject object1 = element.getAsJsonObject();
				if (!object1.has("user") || !object1.get("user").isJsonObject()) continue;
				User user = Utils.lookupUserFromHash(object1.getAsJsonObject("user"), api);
				if (user == null) continue;
				objects.add(object1);
			}

			int count = 0;
			StringBuilder reminders = new StringBuilder();
			if (objects.isEmpty()) {
				message.getChannel().sendMessage("You don't have any reminders you silly goof.");
			} else {
				for (JsonObject object1 : objects) {
					count++;
					if (object1.has("reminder") && object1.get("reminder").isJsonPrimitive()
							&& object1.has("time") && object1.get("time").isJsonPrimitive()
							&& object1.has("origin_time") && object1.get("origin_time").isJsonPrimitive()) {
						DateTime time = new DateTime(object1.getAsJsonPrimitive("time").getAsLong());
						long originTime = object1.getAsJsonPrimitive("origin_time").getAsLong();

						String or;
						long difference = time.getMillis() - System.currentTimeMillis();
						if (TimeUnit.MILLISECONDS.toSeconds(difference) > 59) {
							if (TimeUnit.MILLISECONDS.toMinutes(difference) > 59) {
								if (TimeUnit.MILLISECONDS.toHours(difference) > 23) {
									if (TimeUnit.MILLISECONDS.toDays(difference) > 29) {
										or = (time.getMonthOfYear() - new DateTime().getMonthOfYear() + " months");
									} else or = TimeUnit.MILLISECONDS.toDays(difference) + " days";
								} else or = TimeUnit.MILLISECONDS.toHours(difference) + " hours";
							} else or = TimeUnit.MILLISECONDS.toMinutes(difference) + " minutes";
						} else or = TimeUnit.MILLISECONDS.toSeconds(difference) + " seconds";

						reminders.append("- ")
								.append(Utils.decrypt(object1.getAsJsonPrimitive("reminder").getAsString()))
								.append(" - ").append(or).append("\n");
					}
					array.remove(object1);
				}
			}
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("")
					.setColor(Color.GREEN)
					.addField("Number of Reminders", count + "", false)
					.addField("Reminders", reminders.toString(), false);
			message.getChannel().sendMessage("", embed);

		} catch (IOException e) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry. [Error]: " + e.getMessage());
			System.out.println("ERROR: Could not read remind_me.json. -> " + e.getMessage());
			System.out.println("-------------------------------------------------------");
			e.printStackTrace();
		}
	}
}
