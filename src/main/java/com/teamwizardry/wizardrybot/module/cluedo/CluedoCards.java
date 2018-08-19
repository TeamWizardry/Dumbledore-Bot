package com.teamwizardry.wizardrybot.module.cluedo;

import com.teamwizardry.wizardrybot.api.math.Vec2d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class CluedoCards {

	private static final Set<String> CHARACTERS = new HashSet<>();
	private static final Set<String> WEAPONS = new HashSet<>();
	private static final Set<String> ROOMS = new HashSet<>();
	private static final Set<String> ALL = new HashSet<>();

	private static String[][] boardGrid = new String[22][22];

	private static Set<Vec2d> entrances = new HashSet<>();

	public static void init() {
		//BufferedImage boardImg = ImageIO.read(BOARD_SIMPLE);
		//Color[][] pixels = loadPixelsFromImage(boardImg);

		new Board();
	}

	private static Color[][] loadPixelsFromImage(BufferedImage image) {
		Color[][] colors = new Color[image.getWidth()][image.getHeight()];

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				colors[x][y] = new Color(image.getRGB(x, y));
			}
		}

		return colors;
	}

	static {
		CHARACTERS.add(Character.COLONEL_MUSTARD);
		CHARACTERS.add(Character.MISS_SCARLET);
		CHARACTERS.add(Character.MRS_PEACOCK);
		CHARACTERS.add(Character.MRS_WHITE);
		CHARACTERS.add(Character.PROFESSOR_PLUM);

		WEAPONS.add(Weapon.CANDLESTICK);
		WEAPONS.add(Weapon.DAGGER);
		WEAPONS.add(Weapon.ROPE);
		WEAPONS.add(Weapon.WRENCH);
		WEAPONS.add(Weapon.LEAD_PIPE);
		WEAPONS.add(Weapon.REVOLVER);

		ROOMS.add(Room.BALLROOM);
		ROOMS.add(Room.BILLIARD_ROOM);
		ROOMS.add(Room.CONSERVATORY);
		ROOMS.add(Room.DINING_ROOM);
		ROOMS.add(Room.HALL);
		ROOMS.add(Room.KITCHEN);
		ROOMS.add(Room.LIBRARY);
		ROOMS.add(Room.LOUNGE);
		ROOMS.add(Room.STUDY);

		ALL.addAll(ROOMS);
		ALL.addAll(CHARACTERS);
		ALL.addAll(WEAPONS);
	}

	public static Set<String> getCharacters() {
		return new HashSet<>(CHARACTERS);
	}

	public static Set<String> getRooms() {
		return new HashSet<>(ROOMS);
	}

	public static Set<String> getWeapons() {
		return new HashSet<>(WEAPONS);
	}

	public static Set<String> getAll() {
		return new HashSet<>(ALL);
	}

	public static final class Character {

		public static final String MR_GREEN = "mr green";
		public static final String MRS_PEACOCK = "mrs peacock";
		public static final String MISS_SCARLET = "miss scarlet";
		public static final String PROFESSOR_PLUM = "professor plum";
		public static final String COLONEL_MUSTARD = "colonel mustard";
		public static final String MRS_WHITE = "mrs white";

	}

	public static final class Weapon {

		public static final String REVOLVER = "revolver";
		public static final String DAGGER = "dagger";
		public static final String CANDLESTICK = "candlestick";
		public static final String ROPE = "rope";
		public static final String LEAD_PIPE = "lead_pipe";
		public static final String WRENCH = "wrench";

	}

	public static final class Room {

		public static final String KITCHEN = "kitchen";
		public static final String BALLROOM = "ballroom";
		public static final String CONSERVATORY = "conservatory";
		public static final String DINING_ROOM = "dining_room";
		public static final String LOUNGE = "lounge";
		public static final String HALL = "hall";
		public static final String STUDY = "study";
		public static final String BILLIARD_ROOM = "billiard_room";
		public static final String LIBRARY = "library";
	}
}
