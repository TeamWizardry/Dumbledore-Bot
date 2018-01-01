package com.teamwizardry.wizardrybot.module.emojiparty;

import java.util.ArrayList;

public enum EmojiGames {

	MOVIE, SHOW, MOD;

	public static ArrayList<Game> movies = new ArrayList<>();
	public static ArrayList<Game> shows = new ArrayList<>();
	private static ArrayList<Game> mods = new ArrayList<>();

	static {
		movies.add(new Game(MOVIE, "Harry Potter", "boy", "cloud_lightning", "sparkles"));
		movies.add(new Game(MOVIE, "Shrek", "ear", "candle", "green_heart", "couple_with_heart"));
		movies.add(new Game(MOVIE, "Lord of The Rings", "ring", "volcano"));
		movies.add(new Game(MOVIE, "The Martian", "alien", "globe_with_meridians"));
		movies.add(new Game(MOVIE, "The Lego Game", "man", "homes", "man", "homes", "man", "homes", "man"));
		movies.add(new Game(MOVIE, "Lego Batman", "man", "homes", "man", "homes", "man", "homes", "bat"));
		movies.add(new Game(MOVIE, "Batman vs Superman", "bat", "skull", "airplane", "man", "cityscape"));
		movies.add(new Game(MOVIE, "Willy Wonka", "chocolate_bar", "candy", "chocolate_bar", "candy", "chocolate_bar", "candy", "chocolate_bar", "candy"));
		movies.add(new Game(MOVIE, "Suicide Squad", "skull", "skull", "black_jocker", "fireworks", "fire", "gun", "dagger"));

		shows.add(new Game(SHOW, "Doctor Who", "timer", "clock", "timer", "man", "clock", "woman", "timer", "clock", "timer"));
		shows.add(new Game(SHOW, "Big Bang Theory", "nerd", "nerd", "nerd", "nerd", "red_heart", "woman", "woman", "woman", "woman"));
		shows.add(new Game(SHOW, "House", "man", "syringe", "nauseated_face"));
		shows.add(new Game(SHOW, "Game Of Thrones", "man", "woman", "seat", "man", "woman"));
		shows.add(new Game(SHOW, "Supernatural", "man", "man", "ghost", "angel", "smiling_imp"));
		shows.add(new Game(SHOW, "Family Guy", "hamburger", "man", "woman", "girl", "boy", "baby", "dog"));
		shows.add(new Game(SHOW, "Sherlock", "man", "mag", "man"));

		mods.add(new Game(MOD, "Botania", "seedling", "sunflower", "wilted_rose"));
		mods.add(new Game(MOD, "Thaumcraft", "sparkles", "nauseated_face", "ghost"));
	}

	EmojiGames() {
	}

	public ArrayList<Game> getList() {
		switch (this) {
			case MOVIE: {
				return movies;
			}
			case SHOW: {
				return shows;
			}
			case MOD:
				return mods;
		}
		return null;
	}
}
