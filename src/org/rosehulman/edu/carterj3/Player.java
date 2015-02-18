package org.rosehulman.edu.carterj3;

import java.util.ArrayList;

import org.rosehulman.edu.carterj3.Card;

import com.diamonds.app.SocketServerReplyThread;

public class Player implements Comparable<Player> {
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
		this.tricks =new ArrayList<Card>();
		this.points = 0;
		this.score = 0;
		this.position = position;
		this.socketID = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		// in reality should do some ID magic
		if(obj.getClass() == Player.class){
			return (compareTo((Player)obj)==0);
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(Player another) {
		return (name+socketID).compareTo((another.name+another.socketID));
	}
}
