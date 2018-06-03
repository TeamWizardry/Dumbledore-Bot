package com.teamwizardry.wizardrybot.module.emojiparty;

import ai.api.model.Result;
import com.google.common.collect.HashMultimap;
import com.teamwizardry.wizardrybot.api.Command;
import com.teamwizardry.wizardrybot.api.ICommandModule;
import com.teamwizardry.wizardrybot.api.Module;
import com.vdurmont.emoji.EmojiManager;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.HashMap;
import java.util.Random;

public class ModuleEmojiParty extends Module implements ICommandModule {

	public static HashMap<Channel, Game> games = new HashMap<>();

	private HashMultimap<Server, Game> carosal = HashMultimap.create();

	@Override
	public String getName() {
		return "Emoji Party";
	}

	@Override
	public String getDescription() {
		return "Albus will give you a set of emojis that correlate to a famous movie you need to guess.";
	}

	@Override
	public String getUsage() {
		return "hey albus, emojiparty <movie/show/mod>";
	}

	@Override
	public String getExample() {
		return "'hey albus, emojiparty movie'";
	}

	@Override
	public String getActionID() {
		return null;
	}

	@Override
	public String[] getAliases() {
		return new String[]{"emojiparty", "ep"};
	}

	@Override
	public void onCommand(DiscordApi api, Message message, Command command, Result result) {
		EmojiGames emojiGames;
		if (command.getCommandArguments().contains("movie")) emojiGames = EmojiGames.MOVIE;
		else if (command.getCommandArguments().contains("show")) emojiGames = EmojiGames.SHOW;
		else if (command.getCommandArguments().contains("mod")) emojiGames = EmojiGames.MOD;
		else {
			if (!games.containsKey(message.getChannel())) {
				message.getChannel().sendMessage("Ok but what type of emoji game do you want to play? Your options are: movie, show, mod");
			} else {
				Game game = games.get(message.getChannel());
				LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
				int check = distance.apply(game.name.toLowerCase(), command.getCommandArguments());
				if (check < 4) {
					message.getChannel().sendMessage(EmojiManager.getForAlias("tada").getUnicode() + "Correct!" + EmojiManager.getForAlias("tada").getUnicode());
					games.remove(message.getChannel());
				} else message.getChannel().sendMessage("Incorrect! Try again!");
			}
			return;
		}

		if (emojiGames.getList() == null) return;

		message.getServer().ifPresent(server -> {
			if (carosal.get(server).size() == emojiGames.getList().size())
				carosal.clear();
		});


		Random rand = new Random();
		int random = rand.nextInt(emojiGames.getList().size() - 1);

		while (carosal.get(message.getServer().get()).contains(emojiGames.getList().get(random))) {
			random = rand.nextInt(emojiGames.getList().size() - 1);
		}
		Game game = emojiGames.getList().get(random);
		if (game == null) {
			message.getChannel().sendMessage("Something went wrong...");
			return;
		}
		carosal.put(message.getServer().get(), game);
		games.put(message.getChannel(), game);

		message.getChannel().sendMessage("Alright! Guess the " + emojiGames.name().toLowerCase() + ":\n" + game.readableEmojiString);
	}
}
