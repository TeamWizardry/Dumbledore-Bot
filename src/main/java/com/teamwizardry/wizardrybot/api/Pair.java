package com.teamwizardry.wizardrybot.api;

public class Pair {

	private final String s1;
	private final String s2;

	public Pair(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	public String getKey() {
		return s1;
	}

	public String getValue() {
		return s2;
	}
}
