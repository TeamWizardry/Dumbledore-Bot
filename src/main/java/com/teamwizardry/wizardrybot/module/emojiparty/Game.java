package com.teamwizardry.wizardrybot.module.emojiparty;

import java.util.ArrayList;

public class Game {

	public final EmojiGames emojiGames;
	public final String name;
	public String[] emojis;
	public String readableEmojiString;

	public Game(EmojiGames emojiGames, String name, String... emojis) {
		this.emojiGames = emojiGames;
		this.name = name;
		ArrayList<String> emojiList = new ArrayList<>();
		for (String s : emojis) {
			emojiList.add(":" + s + ":");
		}
		this.emojis = emojiList.toArray(new String[emojiList.size()]);

		StringBuilder builder = new StringBuilder();
		for (String s : emojiList) {
			builder.append(s).append(" ");
		}
		readableEmojiString = builder.toString();
	}
}
