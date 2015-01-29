package org.rosehulman.edu.carterj3;

import java.util.ArrayList;

import org.rosehulman.edu.carterj3.Card;

import com.diamonds.SocketServerReplyThread;

public class Player {
	public String name;
	public ArrayList<Card> hand;
	public ArrayList<Card> tricks;
	public Integer bid;
	public Integer points;
	public int score;
	
	public SocketServerReplyThread socket;
	public int socketID;
	public int position;

	public Player(String name, int position) {
		this.name = name;
		this.hand = new ArrayList<Card>(13);
		this.points = 0;
		this.score = 0;
		this.position = position;
	}
	
	@Override
	public boolean equals(Object obj) {
		// in reality should do some ID magic
		if(obj.getClass() == Player.class){
			return socketID == (((Player)obj).socketID);
		}
		return super.equals(obj);
	}
}
