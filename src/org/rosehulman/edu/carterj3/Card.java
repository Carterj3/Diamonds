package org.rosehulman.edu.carterj3;

import org.rosehulman.edu.carterj3.CONSTANTS.Suit;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;

public class Card implements Comparable<Card> {
	public Suit suit;
	public Value value;

	public Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
	}

	public int getPoints() {
		int value = 0;
		if (this.suit.equals(Suit.Diamond)) {
			value++;
		}
		switch (this.value) {
		case Ace:
			value++;
		case King:
			value++;
		case Queen:
			value++;
		case Jack:
			value++;
		default:
			break;
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == Card.class) {
			Card other = (Card) obj;
			return other.suit == this.suit && other.value == this.value;
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(Card o) {
		if ((o.suit == Suit.Diamond) && (this.suit != Suit.Diamond)) {
			return -1;
		} else if ((o.suit != Suit.Diamond) && (this.suit == Suit.Diamond)) {
			return 1;
		} else if (o.suit == this.suit) {
			return this.value.compareTo(o.value);
		}
		return 0;
	}

}