package org.rosehulman.edu.carterj3;

import org.rosehulman.edu.carterj3.Card;

public class PlayCardAction extends GameAction {

	Card card;
	Player player;

	public PlayCardAction(Card card, Player player){
		this.card = card;
		this.player = player;
	}
}
