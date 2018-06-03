package com.teamwizardry.wizardrybot.module.reminder;

import ai.api.model.Result;
import com.google.gson.*;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.Utils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ModuleClearReminders extends Module implements ICommandModule {

	@Override
	public String getActionID() {
		return "input.clear_reminders";
	}

	@Override
	public String getName() {
		return "Clear Reminders";
	}

	@Override
	public String getDescription() {
		return "Albus will forget all the reminders you assigned to him for yourself.";
	}

	@Override
	public String getUsage() {
		return "'hey albus, clear my reminders'";
	}

	@Override
	public String getExample() {
		return "'hey albus, clear my reminders pls' or 'hey albus, delete all my reminders. kthxbye'";
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
			if (jsonElement.isJsonNull()) {
				jsonElement = new JsonObject();
			}
			if (!jsonElement.isJsonObject()) return;
			JsonObject object = jsonElement.getAsJsonObject();

			JsonArray array;
			if (object.has("list") && object.get("list").isJsonArray()) {
				array = object.getAsJsonArray("list");
			} else {
				message.getChannel().sendMessage("You don't have any reminders you silly goof.");
				return;
			}

			Set<JsonObject> objects = new HashSet<>();
			for (JsonElement element : array) {
				if (!element.isJsonObject()) continue;
				JsonObject object1 = element.getAsJsonObject();
				if (!object1.has("user") || !object1.get("user").isJsonPrimitive()) continue;
				User user = Utils.lookupUserFromHash(object1.getAsJsonPrimitive("user").getAsString());
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
					if (object1.has("reminder") && object1.get("reminder").isJsonPrimitive()) {
						reminders.append("- ").append(Utils.decrypt(object1.getAsJsonPrimitive("reminder").getAsString())).append("\n");
					}
					array.remove(object1);
				}
			}

			FileWriter writer = new FileWriter(file);
			new Gson().toJson(jsonElement, writer);
			writer.flush();

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("")
					.setColor(Color.GREEN)
					.addField("Number of Reminders", count + "", false)
					.addField("Reminders", reminders.toString(), false);
			message.getChannel().sendMessage("Successfully removed your reminders", embed);

		} catch (IOException e) {
			message.getChannel().sendMessage("I'm having trouble remembering things right now. Sorry. [Error]: " + e.getMessage());
			System.out.println("ERROR: Could not read remind_me.json. -> " + e.getMessage());
			System.out.println("-------------------------------------------------------");
			e.printStackTrace();
		}
	}
}
