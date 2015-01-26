package org.rosehulman.edu.carterj3.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rosehulman.edu.carterj3.Player;

public class Player_tests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_PlayerEqualsSelf() {
		Player player = new Player("Jeff");
		assertTrue(player.equals(player));
	}
	
	@Test
	public void test_PlayerEqualsSame() {
		Player player_A = new Player("Jeff");
		Player player_B = new Player("Jeff");
		
		assertTrue(player_A.equals(player_B));
		assertTrue(player_B.equals(player_A));
	}
	
	@Test
	public void test_PlayerJeffNotEqualDooley() {
		Player player_A = new Player("Jeff");
		Player player_B = new Player("Dooley");
		
		assertFalse(player_A.equals(player_B));
		assertFalse(player_B.equals(player_A));
	}

}
