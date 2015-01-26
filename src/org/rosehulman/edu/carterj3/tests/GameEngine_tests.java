package org.rosehulman.edu.carterj3.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rosehulman.edu.carterj3.BidAction;
import org.rosehulman.edu.carterj3.CONSTANTS;
import org.rosehulman.edu.carterj3.Card;
import org.rosehulman.edu.carterj3.DealCardsAction;
import org.rosehulman.edu.carterj3.GameEngine;
import org.rosehulman.edu.carterj3.InitGame;
import org.rosehulman.edu.carterj3.PlayCardAction;
import org.rosehulman.edu.carterj3.Player;
import org.rosehulman.edu.carterj3.PlayerNotFoundException;
import org.rosehulman.edu.carterj3.StartGame;
import org.rosehulman.edu.carterj3.CONSTANTS.Suit;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;
import org.rosehulman.edu.carterj3.GameEngine.GameState;

public class GameEngine_tests {

	private GameEngine engine;

	private Player player1 = new Player("1");
	private Player player2 = new Player("2");
	private Player player3 = new Player("3");
	private Player player4 = new Player("4");

	@Before
	public void setUp() throws Exception {
		engine = new GameEngine();
	}

	@After
	public void tearDown() throws Exception {
		engine = null;
	}

	@Test
	public void test_GameEngineStartsUnitialized() {
		assertTrue(engine.getState() == GameState.UNITIALIZED);
	}

	@Test
	public void test_GameEngineAfterInitialization() {
		InitGame initGame = new InitGame(player1, player2, player3, player4);
		engine.HandleAction(initGame);

		assertTrue(engine.player1.equals(player1));
		assertTrue(engine.player2.equals(player2));
		assertTrue(engine.player3.equals(player3));
		assertTrue(engine.player4.equals(player4));

		assertTrue(engine.getState() == GameState.INITIALIZED);
	}

	@Test
	public void test_GameEngineAfterStarting() {
		test_GameEngineAfterInitialization();
		StartGame startGame = new StartGame();
		engine.HandleAction(startGame);

		assertTrue(engine.deck != null);
		assertTrue(engine.deck.size() == 52);

		assertTrue(engine.getState() == GameState.WAITING_FOR_HANDS);
	}

	@Test
	public void test_GameEnginerAfterCardsDealt() {
		test_GameEngineAfterStarting();
		DealCardsAction dealCards = new DealCardsAction();
		engine.HandleAction(dealCards);

		assertTrue(engine.deck.size() == 0);

		for (Player p : engine.order) {
			assertTrue(p.hand.size() == 13);
			// Make sure cards are unique
			int count = 0;
			for (Card c : p.hand) {
				for (Player p2 : engine.order) {
					for (Card c2 : p2.hand) {
						if (c2.equals(c)) {
							count++;
						}
					}
				}
				assertTrue(count == 1);
				count = 0;
			}
			// - Make sure cards are unique
		}

		assertTrue(engine.getState() == GameState.WAITING_FOR_BIDS);
	}

	@Test
	public void test_GameEngineAfterBids() {
		test_GameEnginerAfterCardsDealt();

		BidAction bid1 = new BidAction(player1, 1);
		BidAction bid2 = new BidAction(player2, 2);
		BidAction bid3 = new BidAction(player3, 3);
		BidAction bid4 = new BidAction(player4, 4);

		engine.HandleAction(bid1);
		assertTrue(engine.player1.bid == bid1.bid);
		assertTrue(engine.getState() == GameState.WAITING_FOR_BIDS);

		engine.HandleAction(bid2);
		assertTrue(engine.player2.bid == bid2.bid);
		assertTrue(engine.getState() == GameState.WAITING_FOR_BIDS);

		engine.HandleAction(bid3);
		assertTrue(engine.player3.bid == bid3.bid);
		assertTrue(engine.getState() == GameState.WAITING_FOR_BIDS);

		engine.HandleAction(bid4);
		assertTrue(engine.player4.bid == bid4.bid);
		assertTrue(engine.getState() == GameState.ROUND_START);
	}

	@Test
	public void test_GameEngineAfterRoundStart() throws PlayerNotFoundException {
		test_GameEngineAfterBids();

		Player playerWithTwoClubs = null;
		Player playerWithoutTwoClubs = null;
		Card notTwoOfClubs = null;

		for (Player p : engine.order) {
			if (p.hand.contains(new Card(Suit.Club, Value.Two))) {
				playerWithTwoClubs = p;
				for (Card c : p.hand) {
					if (!c.equals(CONSTANTS.TwoOfClubs)) {
						notTwoOfClubs = c;
					}
				}
			} else {
				playerWithoutTwoClubs = p;
			}
		}

		PlayCardAction playCardThatPlayerDoesntHave = new PlayCardAction(
				CONSTANTS.TwoOfClubs, playerWithoutTwoClubs);
		engine.HandleAction(playCardThatPlayerDoesntHave);

		assertTrue(!engine.pot.keySet().contains(CONSTANTS.TwoOfClubs));
		assertTrue(engine.getPlayer(playerWithoutTwoClubs).hand.size() == 13);

		PlayCardAction playCardThatIsntTwoOfClubs = new PlayCardAction(
				notTwoOfClubs, playerWithTwoClubs);
		engine.HandleAction(playCardThatIsntTwoOfClubs);

		assertTrue(!engine.pot.keySet().contains(CONSTANTS.TwoOfClubs));
		assertTrue(!engine.pot.keySet().contains(notTwoOfClubs));
		assertTrue(engine.getPlayer(playerWithTwoClubs).hand.size() == 13);

		PlayCardAction playCardThatIsTwoOfClubs = new PlayCardAction(
				CONSTANTS.TwoOfClubs, playerWithTwoClubs);
		engine.HandleAction(playCardThatIsTwoOfClubs);

		assertTrue(engine.order.get(3).equals(playerWithTwoClubs));
		assertTrue(engine.pot.keySet().contains(CONSTANTS.TwoOfClubs));
		assertTrue(!engine.pot.keySet().contains(notTwoOfClubs));
		assertTrue(engine.getPlayer(playerWithTwoClubs).hand.size() == 12);
		assertTrue(!engine.getPlayer(playerWithTwoClubs).hand
				.contains(CONSTANTS.TwoOfClubs));
		assertTrue(engine.getState() == GameState.TRICK_OCCURING);
	}

	private Card getPlayableCard(Player p) throws PlayerNotFoundException {
		Player player = engine.getPlayer(p);
		for (Card c : player.hand) {
			if (c.suit.equals(engine.lead)) {
				return c;
			}
		}

		return player.hand.get(0);

	}

	@Test
	public void test_GameEngineAfterTrickPlayed()
			throws PlayerNotFoundException {
		test_GameEngineAfterRoundStart();
	}

}
