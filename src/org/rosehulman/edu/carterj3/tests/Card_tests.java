package org.rosehulman.edu.carterj3.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rosehulman.edu.carterj3.CONSTANTS.Suit;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;
import org.rosehulman.edu.carterj3.Card;

public class Card_tests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test 
	public void test_twoClubsPointsValue(){
		Card twoClubs = new Card(Suit.Club,Value.Two);
		
		assertTrue(twoClubs.getPoints() == 0);
	}
	
	@Test 
	public void test_twoDiamondsPointsValue(){
		Card twoClubs = new Card(Suit.Diamond,Value.Two);
		
		assertTrue(twoClubs.getPoints() == 1);
	}
	
	@Test 
	public void test_aceDiamondsPointsValue(){
		Card twoClubs = new Card(Suit.Diamond,Value.Ace);
		
		assertTrue(twoClubs.getPoints() == 5);
	}
	
	@Test 
	public void test_kingDiamondsPointsValue(){
		Card twoClubs = new Card(Suit.Diamond,Value.King);
		
		assertTrue(twoClubs.getPoints() == 4);
	}
	
	@Test 
	public void test_queenDiamondsPointsValue(){
		Card twoClubs = new Card(Suit.Diamond,Value.Queen);
		
		assertTrue(twoClubs.getPoints() == 3);
	}
	
	@Test 
	public void test_jackDiamondsPointsValue(){
		Card twoClubs = new Card(Suit.Diamond,Value.Jack);
		
		assertTrue(twoClubs.getPoints() == 2);
	}

	@Test
	public void test_diamondsAreTrump() {
		Card twoDiamonds = new Card(Suit.Diamond,Value.Two);
		for(Suit s : new Suit[]{Suit.Heart,Suit.Club,Suit.Spade}){
			for(Value v : Value.values()){
				assertTrue(twoDiamonds.compareTo(new Card(s,v)) > 0);
			}
		}
	}
	
	@Test
	public void test_nonDiamondsAreNotTrump(){
		Card twoClubs = new Card(Suit.Club,Value.Two);
		for(Suit s : new Suit[]{Suit.Heart,Suit.Spade}){
			for(Value v : Value.values()){
				assertTrue(twoClubs.compareTo(new Card(s,v)) == 0);
			}
		}
	}
	
	@Test
	public void test_twoClubsLessThanThreeOfClubs(){
		Card twoClubs = new Card(Suit.Club,Value.Two);
		Card threeClubs = new Card(Suit.Club,Value.Three);
		assertTrue(twoClubs.compareTo(threeClubs) < 0);
		assertTrue(threeClubs.compareTo(twoClubs) > 0);
	}
	
	@Test
	public void test_twoClubsEqualsTwoClubs(){
		Card twoClubs_A = new Card(Suit.Club,Value.Two);
		Card twoClubs_B = new Card(Suit.Club,Value.Two);
		
		assertTrue(twoClubs_A.equals(twoClubs_B));
		assertTrue(twoClubs_B.equals(twoClubs_A));
		assertTrue(twoClubs_B.equals(twoClubs_B));
	}
	
	@Test
	public void test_twoClubsNotEqualTwoDiamonds(){
		Card twoClubs = new Card(Suit.Club,Value.Two);
		Card twoDiamonds = new Card(Suit.Diamond,Value.Two);
		
		assertFalse(twoClubs.equals(twoDiamonds));
		assertFalse(twoDiamonds.equals(twoClubs));
	}
	
	@Test
	public void test_twoClubsNotEqualThreeClubs(){
		Card twoClubs = new Card(Suit.Club,Value.Two);
		Card threeClubs = new Card(Suit.Club,Value.Three);
		
		assertFalse(twoClubs.equals(threeClubs));
		assertFalse(threeClubs.equals(twoClubs));
	}

}
