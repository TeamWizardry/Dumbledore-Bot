package com.teamwizardry.wizardrybot.api;

import java.util.ArrayList;

public class ThreadManager {

	public static ThreadManager INSTANCE = new ThreadManager();

	public ArrayList<Thread> threads = new ArrayList<>();

	private ThreadManager() {

	}

	public Thread addThread(Thread thread) {
		threads.add(thread);
		thread.start();
		return thread;
	}

	public void stopAll() {
		ArrayList<Thread> temp = new ArrayList<>(threads);
		for (Thread thread : temp) {
			thread.stop();
			threads.remove(thread);
		}
	}

	public void tick() {
		ArrayList<Thread> temp = new ArrayList<>(threads);
		for (Thread thread : temp) {
			if (!thread.isAlive())
				threads.remove(thread);
		}
	}
}
