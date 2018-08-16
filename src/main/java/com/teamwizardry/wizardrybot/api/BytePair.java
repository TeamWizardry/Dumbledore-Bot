package com.teamwizardry.wizardrybot.api;

public class BytePair {

	private final byte[] s1;
	private final byte[] s2;

	public BytePair(byte[] s1, byte[] s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	public byte[] getKey() {
		return s1;
	}

	public byte[] getValue() {
		return s2;
	}
}
