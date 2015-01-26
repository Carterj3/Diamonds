package org.rosehulman.edu.carterj3;

import java.util.ArrayList;

import org.rosehulman.edu.carterj3.Card;

public class Player {
	public String name;
	public ArrayList<Card> hand;
	public ArrayList<Card> tricks;
	public Integer bid;
	public Integer points;
	public int score;

	public Player(String name) {
		this.name = name;
		this.hand = new ArrayList<Card>(13);
		this.points = 0;
		this.score = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		// in reality should do some ID magic
		if(obj.getClass() == Player.class){
			return name.equals(((Player)obj).name);
		}
		return super.equals(obj);
	}
}
