package org.rosehulman.edu.carterj3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.rosehulman.edu.carterj3.CONSTANTS.Suit;

import android.util.Log;

import com.diamonds.MainActivity;

public class GameEngine {

	public enum GameState {
		UNITIALIZED, INITIALIZED, WAITING_FOR_HANDS, WAITING_FOR_BIDS, ROUND_START, TRICK_START, TRICK_OCCURING, ROUND_END, GAME_OVER
	}

	private GameState state;

	public Player player1;
	public Player player2;
	public Player player3;
	public Player player4;

	public Deck deck;

	public Player lastDealer;
	public ArrayList<Player> order;

	public Boolean diamondsPlayed = false;
	public Suit lead;
	public ArrayList<PlayerCardTuple> pot = new ArrayList<PlayerCardTuple>(); 

	public GameEngine() {
		this.setState(GameState.UNITIALIZED);
	}

	public Boolean HandleAction(GameAction action) {
		switch (this.getState()) {
		case UNITIALIZED:
			return unitializedHandler(action);
		case INITIALIZED:
			return initializedHandler(action);
		case WAITING_FOR_HANDS:
			return waitingForHandsHandler(action);
		case WAITING_FOR_BIDS:
			return waitingForBidHandler(action);
		case ROUND_START:
			return roundStartHandler(action);
		case TRICK_START:
			return roundOccuringHandler(action);
		case TRICK_OCCURING:
			return roundOccuringHandler(action);
		case ROUND_END:
			return roundEndHandler(action);
		case GAME_OVER:
			return false;
		default:
			return false;
		}
	}

	private boolean roundEndHandler(GameAction action) {

		// Score all the players
		for (Player p : order) {
			int diff = p.bid - p.points;
			p.score += CONSTANTS.POINTS_PER_ROUND - diff;

			p.bid = null;
			p.points = 0;
		}

		// Check if somebody won
		for (Player p : order) {
			if (p.score >= CONSTANTS.POINTS_TO_WIN
					|| p.score <= CONSTANTS.POINTS_TO_LOSE) {
				this.setState(GameState.GAME_OVER);
				return true;
			}
		}
		// have next person be the dealer
		rotateOrder(lastDealer);
		this.setState(GameState.WAITING_FOR_HANDS);
		return true;

	}

	private boolean roundOccuringHandler(GameAction action) {
		if (action.getClass() == PlayCardAction.class) {
			PlayCardAction playCardAction = (PlayCardAction) action;

			try {
				Player player = getPlayer(playCardAction.player);
				if (!(player.equals(order.get(0)))) {
					// not their turn
					return false;
				}

				if (!(player.hand.contains(playCardAction.card))) {
					// Player tried to play a card they didn't have
					return false;
				}
				// Is card out of suit and they have the suit?
				if (pot.size() > 0 && (playCardAction.card.suit != lead)
						&& playerHasSuit(lead, player)) {
					// player must follow suit
					return false;
				}
				// Can they lead diamonds?
				if (pot.size() == 0
						&& playCardAction.card.suit == Suit.Diamond
						&& !diamondsPlayed
						&& (playerHasSuit(Suit.Club, player)
								|| playerHasSuit(Suit.Heart, player) || playerHasSuit(
									Suit.Spade, player))) {
					// can't lead diamonds yet
					return false;
				}
				// Did they trump diamonds?
				if (pot.size() > 0 && playCardAction.card.suit == Suit.Diamond) {
					diamondsPlayed = true;
				}
				
				if (getState() == GameState.TRICK_START) {
					setState(GameState.TRICK_OCCURING);
					this.lead = playCardAction.card.suit;
					if(pot.size() != 0 ){
						Log.d(MainActivity.tag, "Err, pot is not 0");
					}
				}

				player.hand.remove(playCardAction.card);
				pot.add(new PlayerCardTuple(player, playCardAction.card));
				rotateOrder(player);

				
				String s = "";
				for(PlayerCardTuple ct : pot){
					s +=  " "+ct.card.toString();
				}
				if(pot.size() > 4){
					Log.d(MainActivity.tag, "Err, pot is too large "+pot.size());
				}
				Log.d(MainActivity.tag,"GameEngine Player ["+player.name+"] played ["+playCardAction.card+"] pot ["+s+"]");
				

				// Check if trick is over
				if (pot.size() == 4) {
					// Determine winner
					ArrayList<Card> trick = new ArrayList<Card>();
					for(PlayerCardTuple ct : pot){
						trick.add(ct.card);
					}
					Player winner = getWinner();
					// Give them the cards

					winner.tricks.addAll(trick);
					for (Card c : trick) {
						winner.points += c.getPoints();
					}
					// Reset pot
					pot.clear();
					// Set them to start the next trick
					rotateOrder(winner);
					order.add(0, order.remove(3));

					// Check if round is over
					if (player.hand.size() == 0) {
						this.setState(GameState.ROUND_END);
					} else {
						this.setState(GameState.TRICK_START);
					}
					return true;
				}
				return true;
			} catch (PlayerNotFoundException illigealPlayer) {
				// Log this probably
				return false;
			}

		}
		return false;

	}

	private Player getWinner() {
		Card highestCard = pot.get(0).card;
		Player highestPlayer = pot.get(0).player;
		
		for (PlayerCardTuple ct : pot) {
			if (highestCard.suit != Suit.Diamond || highestCard.suit != lead) {
				highestCard = ct.card;
				highestPlayer = ct.player;
			} else if (highestCard.suit == Suit.Diamond) {
				if (highestCard.compareTo(ct.card) < 0) {
					highestCard = ct.card;
					highestPlayer = ct.player;
				}
			} else if (highestCard.suit == lead) {
				if (highestCard.compareTo(ct.card) < 0) {
					highestCard = ct.card;
					highestPlayer = ct.player;
				}
			}
		}

		return highestPlayer;

	}

	private boolean playerHasSuit(Suit suit, Player player) {
		for (Card c : player.hand) {
			if (c.suit == suit) {
				return true;
			}
		}
		return false;
	}

	private boolean roundStartHandler(GameAction action) {
		if (action.getClass() == PlayCardAction.class) {
			PlayCardAction playCardAction = (PlayCardAction) action;
			try {
				Player player = getPlayer(playCardAction.player);
				if (!(player.hand.contains(playCardAction.card))) {
					// Player tried to play a card they didn't have
					return false;
				}
				if (!playCardAction.card.equals(CONSTANTS.TwoOfClubs)) {
					// Player tried to play a card that doesn't start a round
					return false;
				}
				// Remove two of card from their hand
				player.hand.remove(playCardAction.card);
				// Add it to pot
				pot.add(new PlayerCardTuple(player, playCardAction.card));
				// reorder players so that we know who is next
				rotateOrder(player);
				this.lead = Suit.Club;
				this.setState(GameState.TRICK_OCCURING);
			} catch (PlayerNotFoundException illigealPlayer) {
				return false;
			}
			return true;
		}
		return false;

	}

	/**
	 * Sets the next player to play to be the one after the given player
	 * 
	 * @param player
	 *            the player who just took their turn
	 */
	private void rotateOrder(Player player) {
		int i = 0;
		for (Player p : order) {
			if (p.equals(player)) {
				break;
			}
			i++;
		}

		for (int j = 0; j <= i; j++) {
			Player temp = order.remove(0);
			order.add(temp);
		}
	}

	/**
	 * Returns the game's copy of the given player. Will probably be useful
	 * since it shouldn't be reliable for each player to pass in the information
	 * about themselves and so they can translate network player to state player
	 * 
	 * @param player
	 * @return
	 * @throws PlayerNotFoundException
	 */
	public Player getPlayer(Player player) throws PlayerNotFoundException {
		for (Player p : order) {
			if (p.equals(player)) {
				return p;
			}
		}
		throw new PlayerNotFoundException();
	}

	private boolean waitingForBidHandler(GameAction action) {
		if (action.getClass() == BidAction.class) {
			BidAction bidAction = (BidAction) action;
			Boolean allBid = true;
			for (Player p : order) {
				if (bidAction.player.equals(p)) {
					p.bid = bidAction.bid;
				}
				if (p.bid == null) {
					allBid = false;
				}
			}

			if (allBid) {

				Player playerWithTwoOfClubs = null;
				for (Player p : order) {
					if (p.hand.contains(CONSTANTS.TwoOfClubs)) {
						playerWithTwoOfClubs = p;
					}
				}
				rotateOrder(playerWithTwoOfClubs);
				order.add(0, order.remove(3));

				this.setState(GameState.ROUND_START);
			}
			return true;
		}
		return false;
	}

	private boolean waitingForHandsHandler(GameAction action) {
		if (action.getClass() == DealCardsAction.class) {
			this.deck = new Deck();
			this.deck.shuffle();
			
			int i = 0;
			// Deal the cards
			while (deck.hasNext()) {
				order.get(i % order.size()).hand.add(deck.getCard());
				i++;
			}

			this.setState(GameState.WAITING_FOR_BIDS);
			return true;
		}
		return false;

	}

	private boolean initializedHandler(GameAction action) {
		if (action.getClass() == StartGameAction.class) {
			// Generate a deck
			
			this.order = new ArrayList<Player>(4);
			order.add(player1);
			order.add(player2);
			order.add(player3);
			order.add(player4);

			// lets have a random start
			rotateOrder(order.get(CONSTANTS.getSeed().nextInt(4)));

			this.setState(GameState.WAITING_FOR_HANDS);
			return true;
		}
		return false;

	}

	private boolean unitializedHandler(GameAction action) {
		if (action.getClass() == InitGameAction.class) {
			InitGameAction initGame = ((InitGameAction) action);

			switch (initGame.position) {
			case 0:
				player1 = initGame.player;
				break;
			case 1:
				player2 = initGame.player;
				break;
			case 2:
				player3 = initGame.player;
				break;
			case 3:
				player4 = initGame.player;
				break;
			}
			if (player1 != null && player2 != null && player3 != null
					&& player4 != null) {
				this.setState(GameState.INITIALIZED);
			}
			return true;
		}
		return false;
	}

	public GameState getState() {
		Log.d(MainActivity.tag,"GameEngine State was "+state.toString());
		return state;
	}

	public GameState setState(GameState state) {
		this.state = state;
		return state;
	}

}
