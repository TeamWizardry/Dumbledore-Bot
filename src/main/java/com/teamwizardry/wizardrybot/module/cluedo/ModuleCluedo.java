package com.teamwizardry.wizardrybot.module.cluedo;

import ai.api.model.Result;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class ModuleCluedo extends Module implements ICommandModule {

	private static String PREFIX = "**[CLUEDO]** > ";
	private GameCluedo GAME_INSTANCE = new GameCluedo();

	@Override
	public String getName() {
		return "Cluedo";
	}

	@Override
	public String getDescription() {
		return "Play a game of cluedo";
	}

	@Override
	public String getUsage() {
		return "hey albus, cluedo <create/start/join>";
	}

	@Override
	public String getExample() {
		return null;
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"cluedo"};
	}

	@Override
	public boolean onCommand(DiscordApi api, Message message, Command command, Result result, boolean whatsapp) {
		String[] args = command.getArguments().toLowerCase().split(" ");

		if (args.length == 0 || args[0].isEmpty()) {
			return false;
		}

		boolean isAdmin = isAdmin(message.getServer().get(), message.getUserAuthor().get());

		if (args[0].equals("create")) {
			if (!isAdmin) {
				message.getChannel().sendMessage(PREFIX + "Only admins can use this command.");
				return true;
			}

			if (GAME_INSTANCE != null) {
				message.getChannel().sendMessage(PREFIX + "Game ended.");
			}
			GAME_INSTANCE = new GameCluedo();
			message.getChannel().sendMessage(PREFIX + "A new game of cluedo has been created! type `hey albus, cluedo join` to join it!'");
		}

		if (args[0].equals("start")) {
			if (!isAdmin) {
				message.getChannel().sendMessage(PREFIX + "Only admins can use this command.");
				return true;
			}

			if (GAME_INSTANCE.getPlayers().size() < 3) {
				message.getChannel().sendMessage(PREFIX + "Not enough players! " + (3 - GAME_INSTANCE.getPlayers().size()) + " more players required.");
				return true;
			}

			if (GAME_INSTANCE == null) {
				message.getChannel().sendMessage(PREFIX + "No game of cluedo is currently running.");
				message.getChannel().sendMessage(PREFIX + "Type /cluedo create to make a new game.");
				return true;
			}

			GAME_INSTANCE.start();
			message.getChannel().sendMessage(PREFIX + "Game Started!");

			StringBuilder tags = new StringBuilder();
			for (User user : GAME_INSTANCE.getPlayers().keySet()) {
				tags.append(user.getMentionTag()).append(" ");
			}

			for (User user : GAME_INSTANCE.getPlayers().keySet()) {
				StringBuilder cardList = new StringBuilder();
				for (String string : GAME_INSTANCE.getPlayerHand(user)) {
					cardList.append(":black_large_square: ").append(StringUtils.capitalize(string)).append("\n");
				}
				user.sendMessage("Your cards are:\n" + cardList.toString());
			}

			message.getChannel().sendMessage(PREFIX + tags + " Everyone check your private messages! I have sent you your cards!");
		}

		if (args[0].equals("join")) {
			if (GAME_INSTANCE == null) {
				message.getChannel().sendMessage(PREFIX + "No game of cluedo is currently running.");
				if (isAdmin)
					message.getChannel().sendMessage(PREFIX + "Type /cluedo create to make a new game.");
				return true;
			}

			if (GAME_INSTANCE.getPlayers().size() >= 6) {
				message.getChannel().sendMessage(PREFIX + "The game reached the maximum amount of players possible (6). You can't join the game.");
				return true;
			}

			User user = message.getUserAuthor().get();

			boolean joined = GAME_INSTANCE.addPlayer(user);

			if (joined) {
				String character = GAME_INSTANCE.getUserCharacter(user);
				if (character == null || character.isEmpty()) {
					message.getChannel().sendMessage(PREFIX + user.getMentionTag() + " Something went wrong. You can't join the game.");
					return true;
				}
				message.getChannel().sendMessage(PREFIX + user.getMentionTag() + " has joined the game!");
				message.getChannel().sendMessage(PREFIX + user.getMentionTag() + " Welcome, " + StringUtils.capitalize(character));
			} else {
				message.getChannel().sendMessage(PREFIX + user.getMentionTag() + " You are already in the game.");
			}
		}
		return true;
	}

	private boolean isAdmin(Server server, User user) {
		if (server.getOwner().equals(user)) {
			return true;
		}
		for (Role r : user.getRoles(server)) {
			if (r.getPermissions().getState(PermissionType.ADMINISTRATOR).equals(PermissionState.ALLOWED)) {
				return true;
			}
		}
		return false;
	}
}
