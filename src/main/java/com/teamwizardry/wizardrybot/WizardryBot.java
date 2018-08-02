package com.teamwizardry.wizardrybot;


import ai.api.model.Result;
import com.cloudinary.Cloudinary;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WizardryBot {

	public static WizardryBot wizardryBot;
	public static DiscordApi API;
	public static BufferedImage currentAvatar;
	public static String currentUsername;
	public static Cloudinary cloudinary;

	public static ArrayList<Module> modules = new ArrayList<>();
	private static HashSet<String> commands = new HashSet<>();
	private static HashSet<Channel> heyAlbused = new HashSet<>();

	public static float THINKTHRESHHOLD = 0.85f;

	public static URL domains;

	public static boolean DEV_MODE = false;

	public static void main(String[] args) {
		wizardryBot = new WizardryBot();

		if (args.length <= 0 || args[0].isEmpty()) {
			System.out.println("No key provided.");
			return;
		}

		String KEY = args[0];

		System.out.println("Checking bot authorization...");
		if (!Keys.authorize(KEY)) return;
		System.out.println("Authorized!");

		new DiscordApiBuilder().setToken(KEY).login().thenAccept(api -> {
			System.out.println(api.createBotInvite());
			init(api, wizardryBot);
			System.out.println("YOU SHALL NOT PASS!");
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});
	}

	private static void init(DiscordApi api, WizardryBot wizardryBot) {
		Thread domainThread = new Thread(() -> {
			try {
				domains = new URL("https://raw.githubusercontent.com/TeamWizardry/Dumbledore-Bot/master/domains.txt");

				Domains.INSTANCE.getClass();
			} catch (
					MalformedURLException e) {
				e.printStackTrace();
			}
		});

		domainThread.start();

		Thread apiUpdateThread = new Thread(() -> {
			//try {
			//if (api.getServerById("348507228380332032") != null) {
			//	api.updateUsername(currentUsername = "Harry Potter").get();
			//	Thread.sleep(1000);
			//	api.setGame("Quidditch");
			//	URL url = wizardryBot.getClass().getClassLoader().getResource("profiles/harry_potter.jpg");
			//	if (url != null) {
			//		BufferedImage img = currentAvatar = ImageIO.read(url);
			//		Thread.sleep(1000);
			//		api.updateAvatar(img);
			//	}
			//} else {
			//	api.updateUsername(currentUsername = "Albus Dumbledore").get();
			//	Thread.sleep(1000);
			//	api.setGame("Elder Scrolls VI");
			//	URL url = wizardryBot.getClass().getClassLoader().getResource("profiles/dumbledore_" + (new Random().nextInt(7) + 1) + ".jpg");
			//	if (url != null) {
			//		BufferedImage img = currentAvatar = ImageIO.read(url);
			//		Thread.sleep(1000);
			//		api.updateAvatar(img);
			//	}
			//}

			Map<String, String> config = new HashMap<>();
			config.put("cloud_name", Keys.CLOUDINARY_NAME);
			config.put("api_key", Keys.CLOUDINARY_KEY);
			config.put("api_secret", Keys.CLOUDINARY_SECRET);
			cloudinary = new Cloudinary(config);
			//} catch (InterruptedException | ExecutionException | IOException e) {
			//	e.printStackTrace();
			//}
		});
		apiUpdateThread.setDaemon(true);
		apiUpdateThread.start();

		// Modules and Commands init
		{
			Reflections reflections = new Reflections("com.teamwizardry.wizardrybot.module");
			Set<Class<? extends Module>> classes = reflections.getSubTypesOf(Module.class);
			for (Class<? extends Module> clazz : classes) {
				try {
					modules.add(clazz.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			for (Module module : modules) {
				if (module instanceof ICommandModule) {
					ICommandModule cmd = (ICommandModule) module;
					Collections.addAll(commands, cmd.getAliases());
				}
			}
		}

		api.addMessageCreateListener(messageCreateEvent -> {
			processMessage(messageCreateEvent.getMessage(), messageCreateEvent.getApi());
		});

		File file = new File("amRestarting.json");
		if (file.exists()) {
			try {
				JsonElement element = new JsonParser().parse(new FileReader(file));
				if (element.isJsonObject()) {
					JsonObject object = element.getAsJsonObject();
					if (object.has("channel") && object.get("channel").isJsonPrimitive()) {
						String channelID = object.getAsJsonPrimitive("channel").getAsString();
						api.getTextChannelById(channelID).ifPresent(textChannel -> textChannel.sendMessage("Successfully restarted!"));
					}
				}
			} catch (IOException ignored) {
			}
			file.delete();
		}
	}

	private static void processMessage(Message message, DiscordApi api) {
		AtomicBoolean carryOn = new AtomicBoolean(true);
		message.getUserAuthor().ifPresent(user -> {
			if (user.isBot()) {
				carryOn.set(false);
				return;
			}
			if (user.isYourself()) {
				carryOn.set(false);
			}
		});

		if (!carryOn.get()) return;

		ThreadManager.INSTANCE.tick();

		Statistics.INSTANCE.addToStat("messages_analyzed");
		Command command = new Command(message, commands);
		String after = command.getCommandArguments();
		Result result = (command.hasSaidHey() && command.getResultWithoutHey() == null) ? (after.isEmpty() ? null : AI.INSTANCE.think(Utils.processMentions(after))) : command.getResultWithoutHey();

		if (!command.hasSaidHey()) heyAlbused.remove(message.getChannel());
		else heyAlbused.add(message.getChannel());

		boolean shouldRespond = shouldRespond(command, message);

		if (shouldRespond
				&& result != null
				&& result.getAction().contains("smalltalk")
				&& result.getScore() >= THINKTHRESHHOLD) {
			String response = result.getFulfillment().getSpeech();
			if (!response.isEmpty()) {
				message.getChannel().sendMessage(response);
				return;
			}
		}

		HashSet<Module> priorityList = new HashSet<>();
		for (Module module : modules) {
			if (shouldRespond || module.overrideResponseCheck()) {
				module.onMessage(api, message, result, command);
				
				if (module instanceof ICommandModule) {

					ICommandModule moduleCmd = (ICommandModule) module;

					if (command.getCommandUsed() != null
							&& Arrays.asList(moduleCmd.getAliases()).contains(command.getCommandUsed())) {

						if (moduleCmd.getActionID() != null) {
							if (doesPassResult(result, moduleCmd.getActionID())) {
								priorityList.add(module);
							}
						} else priorityList.add(module);

					} else if (moduleCmd.getAliases().length <= 0) {
						if (moduleCmd.getActionID() != null) {
							if (doesPassResult(result, moduleCmd.getActionID())) {
								priorityList.add(module);
							}
						} else priorityList.add(module);
					}
				}
			}
		}

		Module highestPriority = null;
		for (Module module : priorityList) {
			if (highestPriority == null) highestPriority = module;
			else if (module.getPriority() > highestPriority.getPriority()) highestPriority = module;
		}
		if (highestPriority != null) {
			if (highestPriority instanceof ICommandModule) {
				((ICommandModule) highestPriority).onCommand(api, message, command, result);
				Statistics.INSTANCE.addToStat("commands_triggered");
			}
			highestPriority.onMessage(api, message, result, command);
		}
	}

	private static boolean shouldRespond(Command command, Message message) {
		return (command.hasSaidHey() || heyAlbused.contains(message.getChannel()));
	}

	public static boolean doesPassResult(@Nullable Result result, @NotNull String actionID) {
		return result != null
				&& result.getAction() != null
				&& result.getAction().equals(actionID)
				&& result.getScore() >= THINKTHRESHHOLD;
	}
}
