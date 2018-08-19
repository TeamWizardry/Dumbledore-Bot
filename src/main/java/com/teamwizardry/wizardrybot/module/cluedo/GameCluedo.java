package com.teamwizardry.wizardrybot.module.cluedo;

import com.teamwizardry.wizardrybot.api.math.RandUtil;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GameCluedo {

	private boolean hasStarted = false;
	private boolean hasEnded = false;

	private HashMap<User, String> players = new HashMap<>();
	private HashMap<User, Set<String>> playerHandCards = new HashMap<>();

	private List<String> characterSelectionCarosal;
	private List<String> deckCarosalCharacters;
	private List<String> deckCarosalRooms;
	private List<String> deckCarosalWeapons;

	private String WEAPON;
	private String CHARACTER;
	private String ROOM;

	public Board board;

	public GameCluedo() {
		characterSelectionCarosal = new ArrayList<>(CluedoCards.getCharacters());
		deckCarosalCharacters = new ArrayList<>(CluedoCards.getCharacters());
		deckCarosalWeapons = new ArrayList<>(CluedoCards.getWeapons());
		deckCarosalRooms = new ArrayList<>(CluedoCards.getRooms());
		board = new Board();
	}

	public void start() {
		this.hasStarted = true;

		WEAPON = deckCarosalWeapons.remove(RandUtil.nextInt(deckCarosalWeapons.size() - 1));
		ROOM = deckCarosalRooms.remove(RandUtil.nextInt(deckCarosalRooms.size() - 1));
		CHARACTER = deckCarosalCharacters.remove(RandUtil.nextInt(deckCarosalCharacters.size() - 1));

		List<String> deckCarosalAll = new ArrayList<>();

		deckCarosalAll.addAll(deckCarosalCharacters);
		deckCarosalAll.addAll(deckCarosalWeapons);
		deckCarosalAll.addAll(deckCarosalRooms);

		Collections.shuffle(deckCarosalAll);

		int nbOfPlayers = players.size() - 1;
		int nbOfCards = deckCarosalAll.size() - 1;
		int nbOfCardsPerPlayer = nbOfCards / nbOfPlayers;

		for (User user : players.keySet()) {
			Set<String> hand = new HashSet<>();
			for (int i = 0; i < nbOfCardsPerPlayer; i++) {
				if (deckCarosalAll.isEmpty()) return;
				hand.add(deckCarosalAll.remove(0));
			}
			playerHandCards.put(user, hand);
		}

		if (!deckCarosalAll.isEmpty()) {
			for (User user : players.keySet()) {
				if (!deckCarosalAll.isEmpty()) {
					Set<String> hand = playerHandCards.get(user);
					hand.add(deckCarosalAll.remove(0));
				}
			}
		}
	}

	public Set<String> getPlayerHand(User user) {
		if (!players.containsKey(user)) return new HashSet<>();

		return playerHandCards.get(user);
	}

	public boolean addPlayer(User user) {
		if (players.containsKey(user)) return false;
		players.put(user, characterSelectionCarosal.remove(RandUtil.nextInt(characterSelectionCarosal.size() - 1)));
		return true;
	}

	@Nullable
	public String getUserCharacter(User user) {
		if (players.containsKey(user))
			return players.get(user);
		return null;
	}

	public HashMap<User, String> getPlayers() {
		return players;
	}

	public boolean hasEnded() {
		return hasEnded;
	}

	public void end() {
		this.hasEnded = true;
	}

	public boolean hasStarted() {
		return hasStarted;
	}
}
