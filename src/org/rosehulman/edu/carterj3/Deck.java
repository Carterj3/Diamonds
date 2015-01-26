package org.rosehulman.edu.carterj3;

import java.util.ArrayList;
import java.util.Collections;

import org.rosehulman.edu.carterj3.CONSTANTS.Suit;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;

public class Deck {

	ArrayList<Card> deck;
	
	public Deck() {
		deck = new ArrayList<Card>(52);
		for (Suit s : Suit.values()) {
			for (Value v : Value.values()) {
				this.deck.add(new Card(s, v));
			}
		}
	}

	public void shuffle() {
		Collections.shuffle(deck, CONSTANTS.getSeed());
	}

	public Card getCard() {
		return deck.remove(0);
	}

	public boolean hasNext() {
		return deck.size() > 0;
	}

	public int size() {
		return deck.size();
	}

}
