package org.rosehulman.edu.carterj3;

import org.rosehulman.edu.carterj3.CONSTANTS.Suit;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class Card implements Comparable<Card> {
	public Suit suit;
	public Value value;

	public Card(Suit suit, Value value) {
		this.suit = suit;
		this.value = value;
	}

	public Card(String s) {
		String suit = s.split(";")[0];
		String value = s.split(";")[1];

		this.suit = Suit.valueOf(suit);
		this.value = Value.valueOf(value);
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

	@Override
	public String toString() {
		return this.suit.toString() + ";" + this.value.toString();
	}

	private String getValueForResource() {
		switch (this.value) {
		case Two:
			return "2";
		case Three:
			return "3";
		case Four:
			return "4";
		case Five:
			return "5";
		case Six:
			return "6";
		case Seven:
			return "7";
		case Eight:
			return "8";
		case Nine:
			return "9";
		case Ten:
			return "10";
		case Jack:
			return "jack";
		case Queen:
			return "queen";
		case King:
			return "king";
		case Ace:
			return "ace";
		default:
			Log.d( com.diamonds.app.CONSTANTS.TAG, "Err, invalid switch :: " + this.value);
			return "";
		}
	}

	private String getSuitForResource() {
		switch (this.suit) {
		case Diamond:
			return "diamonds";
		case Heart:
			return "hearts";
		case Spade:
			return "spades";
		case Club:
			return "clubs";
		default:
			Log.d(com.diamonds.app.CONSTANTS.TAG, "Err, invalid switch :: " + this.suit);
			return "";
		}
	}

	public String getResourceName() {
		String name = "card_" + getValueForResource() + "_of_"
				+ getSuitForResource();
		
		return name;
		

	}
}