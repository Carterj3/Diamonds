package org.rosehulman.edu.carterj3;

public class BidAction extends GameAction {

	public Player player;
	public Integer bid;
	
	public BidAction(Player player, int bid){
		this.player = player;
		this.bid = bid;
	}
}
