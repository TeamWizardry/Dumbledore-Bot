package com.teamwizardry.wizardrybot;


import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamwizardry.wizardrybot.api.*;
import com.teamwizardry.wizardrybot.module.ModuleAboutCommand;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WizardryBot {

	public static WizardryBot wizardryBot;
	public static DiscordApi API;

	public static ArrayList<Module> modules = new ArrayList<>();
	private static HashSet<String> commands = new HashSet<>();
	private static HashSet<Message> context = new HashSet<>();

	public static float THINKTHRESHHOLD = 0.85f;

	@Nullable
	public static File ffmpegExe = null;
	@Nullable
	public static File ffProbe = null;

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

		// INSTALLERS
		{
			System.out.println("<<------------------------------------------------------------------------>>");
			File binDir = new File("bin/");
			if (!binDir.exists()) binDir.mkdirs();

			{
				try {
					File domainsFile = new File(binDir, "domains.txt");

					if (!domainsFile.exists()) {
						System.out.println("domains whitelist does not exist! Downloading...");

						URL urlObject = new URL("https://paste.ee/r/XGYeP");
						URLConnection urlConnection = urlObject.openConnection();
						urlConnection.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");

						try (InputStream in = urlConnection.getInputStream()) {
							Files.copy(in, domainsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

							System.out.println("Successfully downloaded domains whitelist!");
						}

					}

					if (domainsFile.exists()) {
						Domains.INSTANCE.init(domainsFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			{
				File ffmpegDir = new File(binDir, "ffmpeg/");
				if (!ffmpegDir.exists()) ffmpegDir.mkdirs();

				File ffmpegZip = new File(ffmpegDir, "ffmpegZip.zip");
				File ffmpegExtractDir = new File(ffmpegDir, "ffmpeg-4.0.2-win64-static");

				if (!ffmpegZip.exists()) {
					try {
						System.out.println("ffmpeg does not exist! Downloading...");
						URL urlObject = new URL("https://ffmpeg.zeranoe.com/builds/win64/static/ffmpeg-4.0.2-win64-static.zip");
						URLConnection urlConnection = urlObject.openConnection();
						urlConnection.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");

						try (InputStream in = urlConnection.getInputStream()) {
							Files.copy(in, ffmpegZip.toPath(), StandardCopyOption.REPLACE_EXISTING);

							ZipFile zipFile = new ZipFile(ffmpegZip.getPath());
							zipFile.extractAll(ffmpegDir.getAbsolutePath());
							System.out.println("Successfully downloaded ffmpeg!");
						} catch (ZipException e) {
							e.printStackTrace();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (!ffmpegExtractDir.exists()) {
					System.out.println("Could not download ffmpeg!!");
				}

				File exe = new File(ffmpegExtractDir, "bin/ffmpeg.exe");
				File probe = new File(ffmpegExtractDir, "bin/ffprobe.exe");

				if (exe.exists()) ffmpegExe = exe;
				if (probe.exists()) ffProbe = probe;
			}

			{
				File youtubeDL = new File(binDir, "youtube-dl.exe");

				if (!youtubeDL.exists()) {

					try {
						System.out.println("Youtube-dl does not exist! Downloading...");
						URL urlObject = new URL("https://yt-dl.org/latest/youtube-dl.exe");
						URLConnection urlConnection = urlObject.openConnection();
						urlConnection.setRequestProperty("User-Agent", "Google Chrome Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36.");

						try (InputStream in = urlConnection.getInputStream()) {
							Files.copy(in, youtubeDL.toPath(), StandardCopyOption.REPLACE_EXISTING);

							System.out.println("Successfully downloaded youtube-dl.exe!");
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Installation Complete");
			System.out.println("<<------------------------------------------------------------------------>>");
		}

		Thread apiUpdateThread = new Thread(() -> {
			URL url = wizardryBot.getClass().getClassLoader().getResource("profiles/dumbledore_" + (new Random().nextInt(7) + 1) + ".jpg");
			if (url != null) {
				try {
					BufferedImage img = ImageIO.read(url);
					api.updateAvatar(img);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


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
				commands.add(module.getName().toLowerCase());
				if (module instanceof ICommandModule) {
					ICommandModule cmd = (ICommandModule) module;
					Collections.addAll(commands, cmd.getAliases());
				}
			}
		}

		api.addMessageCreateListener(messageCreateEvent -> {

			//	if (messageCreateEvent.getChannel().getId() == 407963020631736323L)

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

		Command command = new Command(message, commands);

		if (command.getResult() == null) return;
		Result result = command.getResult();
		Statistics.INSTANCE.addToStat("messages_analyzed");

		boolean shouldRespond = shouldRespond(command, message);

		HashSet<Module> priorityList = new HashSet<>();
		for (Module module : modules) {
			if (shouldRespond || module.overrideResponseCheck()) {
				module.onMessage(api, message, result, command);

				if (module instanceof ICommandModule) {

					ICommandModule moduleCmd = (ICommandModule) module;

					if (command.getCommand() != null
							&& Arrays.asList(moduleCmd.getAliases()).contains(command.getCommand())) {

						priorityList.add(module);

					} else if (moduleCmd.getActionID() != null) {
						if (doesPassResult(result, moduleCmd.getActionID())) {
							priorityList.add(module);
						}
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

				final ICommandModule cmdModule = ((ICommandModule) highestPriority);
				Module finalHighestPriority = highestPriority;

				Thread thread = new Thread(() -> {

					if (!cmdModule.onCommand(api, message, command, result)) {
						message.getChannel().sendMessage("That's not how you use that command.");
						ModuleAboutCommand.sendCommandMessage(message, finalHighestPriority);
					}
					Statistics.INSTANCE.addToStat("commands_triggered");
				});
				thread.start();
			}
		} else {

			if (command.getCommand() == null || command.getCommand().isEmpty()) return;

			for (String cmd : commands) {
				if (command.getCommand().toLowerCase().equals(cmd.toLowerCase())) {

					Module reverse = null;
					for (Module module : modules) {
						if (module.getName().toLowerCase().contains(cmd) || module.getName().toLowerCase().replace(" ", "").contains(cmd)
								|| cmd.contains(module.getName().toLowerCase()) || cmd.contains(module.getName().toLowerCase().replace(" ", ""))) {
							reverse = module;
							break;
						}
						if (module instanceof ICommandModule) {
							ICommandModule reverseCMD = (ICommandModule) module;
							for (String alias : reverseCMD.getAliases()) {
								if (alias.contains(cmd)) {
									reverse = module;
									break;
								}
							}
						}
					}

					if (reverse != null) {
						if (!reverse.overrideIncorrectUsage()) {
							message.getChannel().sendMessage("That's not how you use that command.");
							ModuleAboutCommand.sendCommandMessage(message, reverse);
						} else {
							if (reverse instanceof ICommandModule) {

							}
						}
					}
				}
			}
		}
	}

	private static boolean shouldRespond(Command command, Message message) {
		for (Iterator<Message> iterator = context.iterator(); iterator.hasNext(); ) {
			Message context = iterator.next();
			if (context.getAuthor().getId() == message.getAuthor().getId()
					&& context.getChannel().getId() == message.getChannel().getId()) {
				iterator.remove();
				return true;
			}
		}

		if (command.hasSaidHey()) context.add(message);

		return command.hasSaidHey();
	}

	public static boolean doesPassResult(@Nullable Result result, @NotNull String actionID) {
		return result != null
				&& result.getAction() != null
				&& result.getAction().equals(actionID)
				&& result.getScore() >= THINKTHRESHHOLD;
	}
}
