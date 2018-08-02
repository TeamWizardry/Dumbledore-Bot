package com.teamwizardry.wizardrybot.module.reminder;

import ai.api.model.Result;
import com.google.gson.*;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Utils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ModuleRemindMe extends Module implements ICommandModule {

	public static Thread thread;

	public ModuleRemindMe() {
		thread = new Thread(new RemindMeRunnable());
		thread.start();
	}

	@Override
	public String getActionID() {
		return "input.remind_me";
	}

	@Override
	public String getName() {
		return "Remind Me";
	}

	@Override
	public String getDescription() {
		return "Albus will remind you of anything you want.";
	}

	@Override
	public String getUsage() {
		return "'hey albus, remind me to <thing> in <time>' or anything of similar syntax.";
	}

	@Override
	public String getExample() {
		return "'hey albus, remind me to kill opb in 3 days please.' or 'hey albus, remind me in 42 minutes to take my drugs.'";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		if (message.getContent().length() > 256) {
			message.getChannel().sendMessage("Your message is too long... Please condense it so the entire thing is less than 256 characters. Sorry... :(");
			return;
		}

		String reminder = result.getStringParameter("any");

		if (reminder.isEmpty()) {
			message.getChannel().sendMessage("What would you like me to remind you about? Please rephrase your statement.");
			return;
		}
		if (!result.getParameters().containsKey("date") && !result.getParameters().containsKey("time")) {
			message.getChannel().sendMessage("Ok but when would you like me to remind you of that? Please rephrase your sentence.");
			return;
		}
		Date date = result.getDateParameter("date");
		Date time = result.getTimeParameter("time");

		File file = new File("remind_me.json");
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) {
					message.getChannel().sendMessage("I'm having trouble preparing to memorize stuff right now. Sorry.");
					System.out.println("ERROR: Could not create remind_me.json.");
				}
			} catch (IOException e) {
				message.getChannel().sendMessage("I'm having trouble preparing to memorize stuff right now. Sorry.");
				System.out.println("ERROR: Could not create remind_me.json. -> " + e.getMessage());
				System.out.println("-------------------------------------------------------");
				e.printStackTrace();
			}
		}
		if (!file.exists()) {
			message.getChannel().sendMessage("I'm having trouble preparing to memorize things right now. Sorry.");
			System.out.println("ERROR: Cannot read remind_me.json");
			return;
		}

		if (!file.canRead()) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry.");
			System.out.println("ERROR: Cannot read remind_me.json");
			return;
		}

		if (!file.canWrite()) {
			message.getChannel().sendMessage("I'm having trouble memorizing things right now. Sorry.");
			System.out.println("ERROR: Cannot write to remind_me.json");
			return;
		}

		try {
			JsonElement jsonElement = new JsonParser().parse(new FileReader(file));
			if (jsonElement.isJsonNull()) {
				jsonElement = new JsonArray();
			}
			if (!jsonElement.isJsonArray()) return;
			JsonArray array = jsonElement.getAsJsonArray();

			int count = 0;
			for (JsonElement element : array) {
				if (element.isJsonObject()) {
					JsonObject object1 = element.getAsJsonObject();
					if (object1.has("user") && object1.get("user").isJsonPrimitive()) {
						if (Utils.checkHashMatch(message.getAuthor().getIdAsString(), object1.getAsJsonObject("user")))
							count++;
					}
				}
			}
			if (count > 50) {
				message.getChannel().sendMessage("No. You have too many reminders already.");
				return;
			}

			DateTime finalDate = new DateTime(date != null ? date.getTime() : time != null ? time.getTime() : 0);
			if (finalDate.compareTo(new DateTime()) <= 0) {
				message.getChannel().sendMessage("I may be a wizard, but I prefer to use my powers in the present time. Please rephrase your statement.");
				return;
			}

			JsonObject object1 = new JsonObject();
			object1.add("user", Utils.encryptString(message.getAuthor().getIdAsString()));
			if (message.getChannel() != null)
				object1.addProperty("channel", message.getChannel().getId() + "@" + message.getServer().get().getId());
			object1.addProperty("time", finalDate.getMillis());
			object1.addProperty("reminder", Utils.encrypt(reminder));
			object1.addProperty("origin_time", System.currentTimeMillis());

			array.add(object1);

			FileWriter writer = new FileWriter(file);
			writer.write(new Gson().toJson(jsonElement));
			writer.flush();

			String or;
			long difference = finalDate.getMillis() - System.currentTimeMillis();
			if (TimeUnit.MILLISECONDS.toSeconds(difference) > 60) {
				if (TimeUnit.MILLISECONDS.toMinutes(difference) > 60) {
					if (TimeUnit.MILLISECONDS.toHours(difference) > 24) {
						if (TimeUnit.MILLISECONDS.toDays(difference) > 30) {
							or = (finalDate.getMonthOfYear() - new DateTime().getMonthOfYear() + " months");
						} else or = TimeUnit.MILLISECONDS.toDays(difference) + " days";
					} else or = TimeUnit.MILLISECONDS.toHours(difference) + " hours";
				} else or = TimeUnit.MILLISECONDS.toMinutes(difference) + " minutes";
			} else or = TimeUnit.MILLISECONDS.toSeconds(difference) + " seconds";

			message.getChannel().sendMessage("Alright! I'll remind you `" + or + "` from now about the following: \n```" + reminder + "```");

		} catch (IOException e) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry. [Error]: " + e.getMessage());
			System.out.println("ERROR: Could not read remind_me.json. -> " + e.getMessage());
			System.out.println("-------------------------------------------------------");
			e.printStackTrace();
		}
	}
}
