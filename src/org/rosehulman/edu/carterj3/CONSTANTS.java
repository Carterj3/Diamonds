package org.rosehulman.edu.carterj3;

import java.util.Random;

import org.rosehulman.edu.carterj3.Card;

public class CONSTANTS {
	
	public static final int PLAYER_COUNT = 4;
	
	public static final int POINTS_TO_WIN = 100;
	public static final int POINTS_TO_LOSE = -POINTS_TO_WIN;
	
	public static final Card TwoOfClubs = new Card(Suit.Club,Value.Two);

	public static final int POINTS_PER_ROUND = 15;
	
	public static enum Suit{
		Diamond,
		Heart,
		Spade,
		Club
	}
	
	public static enum Value{
		Two,
		Three,
		Four,
		Five,
		Six,
		Seven,
		Eight,
		Nine,
		Ten,
		Jack,
		Queen,
		King,
		Ace
	}
	
	public static Random getSeed(){
		return new Random(System.nanoTime());
	}

}
