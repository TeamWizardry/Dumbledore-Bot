package com.teamwizardry.wizardrybot.module.reminder;

import com.google.gson.*;
import com.teamwizardry.wizardrybot.WizardryBot;
import com.teamwizardry.wizardrybot.api.Utils;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.joda.time.DateTime;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class RemindMeRunnable implements Runnable {

	public static volatile boolean run = true;
	public static volatile boolean isRunning = true;
	public static volatile long loopTicks = 0;

	@Override
	public void run() {
		while (run) {
			isRunning = true;
			loopTicks = System.currentTimeMillis();

			File file = new File("remind_me.json");
			if (!file.exists()) {
				try {
					if (!file.createNewFile()) {
						System.out.println("ERROR: Could not create remind_me.json.");
						run = false;
					}
				} catch (IOException e) {
					System.out.println("ERROR: Could not create remind_me.json. -> " + e.getMessage());
					System.out.println("-------------------------------------------------------");
					e.printStackTrace();
					run = false;
				}
			}
			if (file.exists()) {
				if (!file.canRead()) {
					System.out.println("ERROR: Cannot read remind_me.json");
					run = false;
				}

				if (!file.canWrite()) {
					System.out.println("ERROR: Cannot write to remind_me.json");
					run = false;
				}

				try {
					JsonElement jsonElement = new JsonParser().parse(new FileReader(file));
					if (!jsonElement.isJsonObject()) continue;
					JsonObject object = jsonElement.getAsJsonObject();

					if (object.has("list") && object.get("list").isJsonArray()) {
						JsonArray array = object.getAsJsonArray("list");

						for (JsonElement element : array) {
							if (!element.isJsonObject()) continue;
							JsonObject reminder = element.getAsJsonObject();

							if (reminder.has("time")
									&& reminder.get("time").isJsonPrimitive()
									&& reminder.has("reminder")
									&& reminder.get("reminder").isJsonPrimitive()
									&& reminder.has("user")
									&& reminder.get("user").isJsonPrimitive()
									&& reminder.has("origin_time")
									&& reminder.get("origin_time").isJsonPrimitive()) {
								String message = reminder.getAsJsonPrimitive("reminder").getAsString();
								DateTime time = new DateTime(reminder.getAsJsonPrimitive("time").getAsLong());
								long originTime = reminder.getAsJsonPrimitive("origin_time").getAsLong();
								long userID = reminder.getAsJsonPrimitive("user").getAsLong();

								Channel channel = null;
								Server server = null;
								if (reminder.has("channel")) {
									String channelString = reminder.getAsJsonPrimitive("channel").getAsString();
									String[] parts = channelString.split("@");
									channel = WizardryBot.API.getChannelById(parts[0]).get();
									server = WizardryBot.API.getServerById(parts[1]).get();
								}
								if (channel == null) return;

								try {
									if (time.getMillis() - System.currentTimeMillis() <= 0) {
										User user = Utils.getUser(userID, WizardryBot.API);
										if (user != null) {

											String or;
											long difference = System.currentTimeMillis() - originTime;
											if (TimeUnit.MILLISECONDS.toSeconds(difference) > 60) {
												if (TimeUnit.MILLISECONDS.toMinutes(difference) > 60) {
													if (TimeUnit.MILLISECONDS.toHours(difference) > 24) {
														if (TimeUnit.MILLISECONDS.toDays(difference) > 30) {
															or = (time.getMonthOfYear() - new DateTime().getMonthOfYear() + " months");
														} else or = TimeUnit.MILLISECONDS.toDays(difference) + " days";
													} else or = TimeUnit.MILLISECONDS.toHours(difference) + " hours";
												} else or = TimeUnit.MILLISECONDS.toMinutes(difference) + " minutes";
											} else or = TimeUnit.MILLISECONDS.toSeconds(difference) + " seconds";

											channel.asTextChannel().ifPresent(textChannel -> textChannel.sendMessage(
													user.getMentionTag()
															+ " Hey! You told me to remind you "
															+ "`" + or + "` ago"
															+ " about the following:\n"
															+ "```"
															+ message
															+ "```"));
										}

										array.remove(element);

										FileWriter writer = new FileWriter(file);
										new Gson().toJson(jsonElement, writer);
										writer.flush();
										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (FileNotFoundException e) {
					System.out.println("ERROR: Could not read remind_me.json. -> " + e.getMessage());
					System.out.println("-------------------------------------------------------");
					e.printStackTrace();
					run = false;
				}
			}
		}
	}
}
