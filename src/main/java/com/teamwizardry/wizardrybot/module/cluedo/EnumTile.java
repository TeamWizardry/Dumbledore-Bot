package com.teamwizardry.wizardrybot.module.cluedo;

public enum EnumTile {
	NULL("Null"),
	FLOOR("Floor"),
	FINAL_ACCUSATION("Final Accusation"),
	KITCHEN("Kitchen"),
	BALLROOM("Ballroom"),
	CONSERVATORY("Conservatory"),
	DINING_ROOM("Dining Room"),
	LOUNGE("Lounge"),
	HALL("Hall"),
	STUDY("Study"),
	BILLIARD_ROOM("Billiard Room"),
	LIBRARY("Library");


	private String name;

	EnumTile(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isRoom() {
		return this != FLOOR;
	}
}
