package org.rosehulman.edu.carterj3;

/**
 * InitGame is meant to handle connecting the 4 players together.
 * 
 * @author carterj3
 * 
 */
public class InitGameAction extends GameAction {

	Player player;
	Integer position;
	

	public InitGameAction(Player p, Integer position){
		this.player = p;
		this.position = position;
	}
}
