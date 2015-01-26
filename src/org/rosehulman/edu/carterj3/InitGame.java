package org.rosehulman.edu.carterj3;

/**
 * InitGame is meant to handle connecting the 4 players together.
 * 
 * @author carterj3
 * 
 */
public class InitGame extends GameAction {

	Player player1;
	Player player2;
	Player player3;
	Player player4;

	public InitGame(Player p1, Player p2, Player p3, Player p4) {
		this.player1 = p1;
		this.player2 = p2;
		this.player3 = p3;
		this.player4 = p4;
	}
}
