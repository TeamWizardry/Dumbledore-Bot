package com.teamwizardry.wizardrybot.module.reminder;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.teamwizardry.wizardrybot.api.ThreadManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;

public class ModuleDebugReminders extends Module implements ICommandModule {

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
		return "Debug Reminders";
	}

	@Override
	public String getDescription() {
		return "Debug reminders.";
	}

	@Override
	public String getUsage() {
		return "'hey albus,debug-reminders <command>";
	}

	@Override
	public String getExample() {
		return "'hey albus, debug-reminders [<is-active> <millis>], [<restart>], //TODO: [<list-all>], [<list-user> <user>], [<clear-all>], [<clear-user> <user>]";
	}

	@Override
	public String[] getAliases() {
		return new String[]{"debug-reminders"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		boolean isAdmin = false;
		for (Role role : message.getServer().get().getRolesOf(message.getAuthor().asUser().get())) {
			if (role.getPermissions().getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
				isAdmin = true;
				break;
			}
		}
		if (!isAdmin) return;

		if (command.getCommandArguments().startsWith("restart")) {
			ThreadManager.INSTANCE.addThread(new Thread(() -> {
				RemindMeRunnable.run = false;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				RemindMeRunnable.run = true;
				ModuleRemindMe.thread = new Thread(new RemindMeRunnable());
				ModuleRemindMe.thread.start();
				message.getChannel().sendMessage("Reminder system restarted successfully.");
			}));


		} else if (command.getCommandArguments().startsWith("is-active")) {
			int t;
			try {
				t = Integer.parseInt(command.getCommandArguments().split(" ")[1]);
			} catch (NumberFormatException e) {
				message.getChannel().sendMessage(e.getMessage());
				return;
			}
			final int time = t;
			ThreadManager.INSTANCE.addThread(new Thread(() -> {
				RemindMeRunnable.isRunning = false;
				long tick = System.currentTimeMillis();

				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (RemindMeRunnable.isRunning) {
					message.getChannel().sendMessage("Reminders are definitely running properly. Took " + (Math.abs(tick - RemindMeRunnable.loopTicks)) + " millis");
				} else {
					message.getChannel().sendMessage("Reminders aren't ticking! Took " + (Math.abs(tick - RemindMeRunnable.loopTicks)) + " millis");
				}
			}));
		}
	}
}
