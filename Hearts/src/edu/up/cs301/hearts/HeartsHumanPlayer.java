package edu.up.cs301.hearts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.up.cs301.animation.AnimationSurface;
import edu.up.cs301.animation.Animator;
import edu.up.cs301.card.Card;
import edu.up.cs301.card.Rank;
import edu.up.cs301.card.Suit;
import edu.up.cs301.game.GameHumanPlayer;
import edu.up.cs301.game.GameMainActivity;
import edu.up.cs301.game.R;
import edu.up.cs301.game.infoMsg.GameInfo;
import edu.up.cs301.game.infoMsg.IllegalMoveInfo;
import edu.up.cs301.game.infoMsg.NotYourTurnInfo;

/**
 * HeartsHumanPlayer -
 * 
 * Object to represent a human player using the game. Contains information
 * regarding creation of the gui along with general player information
 * 
 * @author Steven Lind, Kyle Michel, David Rodden
 * @version 12/5/2014
 *
 */
public class HeartsHumanPlayer extends GameHumanPlayer implements Animator {

	// our game state
	protected HeartsState state;

	// our activity
	private Activity myActivity;

	// the animation surface
	private AnimationSurface surface;

	// the background color
	private int backgroundColor;

	private Paint paint;
	private boolean hasChecked;
	private int design = 0;
	private Path wallPath;
	private ArrayList<PointF> scorePoint = new ArrayList<PointF>(3);
	private static float width, height;
	private ArrayList<Card> hand;
	private float currentspacing;
	private Card[] selectedCards;

	/**
	 * HeartsHumanPlayer:
	 * 
	 * Constructor for the HeartsHumanPlayer object
	 * @param name the name of the player to be created
	 */
	public HeartsHumanPlayer(String name) {
		super(name);
		backgroundColor = 0xff006400;
		paint = new Paint();
		selectedCards = new Card[3];

		// Set up dummy card arraylist
		hand = new ArrayList<Card>();
	}

	/**
	 * setAsGui:
	 * 
	 * Sets the given activity as the GUI for the human player
	 * @param GameMainActivity
	 */
	public void setAsGui(GameMainActivity activity) {
		myActivity = activity;
		// Load the layout resource for the new configuration
		activity.setContentView(R.layout.hearts_human_player);

		// link the animator (this object) to the animation surface
		surface = (AnimationSurface) myActivity.findViewById(R.id.animation_surface);
		surface.setAnimator(this);

		// read in the card images
		Card.initImages(activity);

		// if the state is not null, simulate having just received the state so
		// that
		// any state-related processing is done
		if (state != null) {
			receiveInfo(state);
		}
	}

	@Override
	public View getTopView() {
		return myActivity.findViewById(R.id.top_gui_layout);
	}

	@Override
	public void receiveInfo(GameInfo info) {
		Log.i("HeartsHumanPlayer", "receiving updated state (" + info.getClass() + ")");
		if (info instanceof IllegalMoveInfo || info instanceof NotYourTurnInfo) {
			// if we had an out-of-turn or illegal move, flash the screen
			surface.flash(Color.RED, 50);
		} else if (!(info instanceof HeartsState)) {
			// otherwise, if it's not a game-state message, ignore
			return;
		} else {
			// it's a game-state object: update the state. Since we have an
			// animation
			// going, there is no need to explicitly display anything. That will
			// happen
			// at the next animation-tick, which should occur within 1/20 of a
			// second
			this.state = (HeartsState) info;
			hand = state.getPlayerHand(playerNum);
			Log.i("human player", "receiving");
		}
	}

	@Override
	public int interval() {
		return 20;
	}

	@Override
	public int backgroundColor() {
		return backgroundColor;
	}

	@Override
	public boolean doPause() {
		return false;
	}

	@Override
	public boolean doQuit() {
		return false;
	}

	@Override
	public void tick(Canvas g) {	
			width = g.getWidth();
			height = g.getHeight();
			if (!hasChecked) {
				pointUpdate();
			}
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
			int j = 0;
			for (int i = 0; i < scorePoint.size(); i++) {
				if (state == null) {
					return;
				}
				if (state.getTurnIdx() == i) {

					paint.setColor(Color.CYAN);
					if (i > 0) {
						g.drawRect(new Rect((int) (scorePoint.get(i - 1).x - 10), (int) (scorePoint
										.get(i - 1).y - 60), (int) (scorePoint.get(i - 1).x + 110),
										(int) (scorePoint.get(i - 1).y + 10)), paint);
					} else if (i == 0) {
						g.drawRect(new Rect((int) (scorePoint.get(3).x - 10),
								(int) (scorePoint.get(3).y - 60), (int) (scorePoint.get(3).x + 110),
								(int) (scorePoint.get(3).y + 10)), paint);
					}
				}
				if (i == playerNum) {
					paint.setColor(Color.YELLOW);
					paint.setTextSize(30);
					g.drawText(name, scorePoint.get(3).x, scorePoint.get(3).y - 30, paint);
					paint.setColor(Color.WHITE);
					paint.setTextSize(20);
					g.drawText("Score: " + state.getOverallScore(i) + " (" + state.getHandScore(i)
							+ ")", scorePoint.get(3).x, scorePoint.get(3).y, paint);
				} else {
					paint.setColor(Color.WHITE);
					paint.setTextSize(20);
					g.drawText("Score: " + state.getOverallScore(i) + " (" + state.getHandScore(i)
							+ ")", scorePoint.get(j).x, scorePoint.get(j).y, paint);
					if (allPlayerNames == null) {
						return;
					}
					g.drawText(allPlayerNames[i], scorePoint.get(j).x, scorePoint.get(j).y - 30, paint);
					j++;
				}
			}
			Rect r = new Rect((int) width / 5, (int) ((height / 4) - (height / 8)),
					(int) (width - width / 5), (int) ((height - height / 4) - (height / 8)));
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff640000);
			g.drawRect(r, paint);
			paint.setColor(Color.WHITE);
			paint.setTextSize(20);
			g.drawText("Current Trick", (float) (width / 4.6), (float) (height / 5.3), paint);
			paint.setColor(Color.YELLOW);
			paint.setTextSize(15);
			g.drawText("01", (float) (width / 3.2), (float) (height / 5.9), paint);
			paint.setStyle(Paint.Style.STROKE);
			g.drawRect(r, paint);
			r = new Rect((int) width / 5, (int) ((height / 4) - (height / 8)), (int) (width / 3),
					(int) ((height / 3) - (height / 8)));
			g.drawRect(r, paint);
			for (design = 0; design < 3; design++) {
				pathHelper();
				g.drawPath(wallPath, paint);
			}
			if (state.getSubState() == HeartsState.PLAYING) {
				drawTrick(g);
			}
			else {
				drawButton(g);
			}
			drawCards(g);
	}

	@Override
	public void onTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(hand.isEmpty()){
				return;
			}
			float tx = event.getX();
			float ty = event.getY();
			Rect trickBoundingBox = new Rect((int) width / 5, (int) ((height / 4) - (height / 8)),
					(int) (width - width / 5), (int) ((height - height / 4) - (height / 8)));
			RectF buttonBoundingBox = new RectF((float) (width / 2.17), (float) ((height / 2.15) - (height / 8)),
					(float) (width - width / 2.17), (float) ((height - height / 2.15) - (height / 8)));
			if (state.getSubState() == HeartsState.PLAYING && selectedCards[0] != null && trickBoundingBox.contains((int) tx, (int) ty)) {
				// playing card
				game.sendAction(new HeartsPlayAction(this, selectedCards[0]));
				selectedCards[0] = null;
			} 
			else if (state.getSubState() == HeartsState.PASSING && buttonBoundingBox.contains(tx,ty) && threeCardsSelected()) {
				// passing cards
				game.sendAction(new HeartsPassAction(this,selectedCards));
				selectedCards = new Card[3];
			}
			//Card selection for passing
			else if (state.getSubState() == HeartsState.PASSING) {
				int place = 0;
				int handIdx = (int) ((hand.size()) * tx / width);
				for (place = 0; place < 3; place++) {
					if ((selectedCards[place] == null && selectedCards[(place+1)%3] != hand.get(handIdx) && selectedCards[(place+2)%3] != hand.get(handIdx)) || place == 2 || (hand.get(handIdx) == selectedCards[place])) {
						break;
					}
				}
				if (ty > height - (height / 3) && !(hand.get(handIdx) == selectedCards[place])) {// in
																							// bounds
																							// vertically
					selectedCards[place] = hand.get(handIdx);
				} else {
					selectedCards[place] = null;
				}
			}
			// Selection / deselection of cards for playing
			else {
				int handIdx = (int) ((hand.size()) * tx / width);
				if (ty > height - (height / 3) && !(hand.get(handIdx) == selectedCards[0])) {// in
																							// bounds
																							// vertically
					selectedCards[0] = hand.get(handIdx);
				} else {
					selectedCards[0] = null;
				}
			}
		}
	}
	/**
	 * pointUpdate:
	 * 
	 * Updates points, utilized in drawing the GUI
	 */
	public void pointUpdate() {
		PointF p;
		p = new PointF(width / 10, height / 10);
		scorePoint.add(p);
		p = new PointF(width / 2, height / 10);
		scorePoint.add(p);
		p = new PointF(width - width / 10, height / 10);
		scorePoint.add(p);
		p = new PointF(width / 10, height - (float) (height / 2.5));
		scorePoint.add(p);
		hasChecked = true;
	}
	/**
	 * PathHelper:
	 * 
	 * Helper method used in drawing the GUI
	 */
	public void pathHelper() {
		int triangleDepth = 20;
		wallPath = new Path();
		switch (design) {
		case 0:
			wallPath.moveTo(width / 5, height - height / 4 - (height / 8));
			wallPath.lineTo(width / 5, height - height / 4 - triangleDepth - (height / 8));
			wallPath.lineTo(width / 5 + triangleDepth, height - height / 4 - (height / 8));
			wallPath.lineTo(width / 5, height - height / 4 - (height / 8));
			break;
		case 1:
			wallPath.moveTo(width - width / 5, height - height / 4 - (height / 8));
			wallPath.lineTo(width - width / 5, height - height / 4 - triangleDepth - (height / 8));
			wallPath.lineTo(width - width / 5 - triangleDepth, height - height / 4 - (height / 8));
			wallPath.lineTo(width - width / 5, height - height / 4 - (height / 8));
			break;
		case 2:
			wallPath.moveTo(width - width / 5, height / 4 - (height / 8));
			wallPath.lineTo(width - width / 5, height / 4 + triangleDepth - (height / 8));
			wallPath.lineTo(width - width / 5 - triangleDepth, height / 4 - (height / 8));
			wallPath.lineTo(width - width / 5, height / 4 - (height / 8));
			break;
		}
	}

	/**
	 * drawCards:
	 * 
	 * Draws the cards currently in the player's hand
	 * @param g The canvas we're drawing on
	 */
	private void drawCards(Canvas g) {
		currentspacing = hand.size();
		for (int i = 0; i < hand.size(); i++) {
			boolean isselected = false;
			for (int j = 0; j < selectedCards.length; j++) {
				if (selectedCards[j] != null && hand.get(i).equals(selectedCards[j])) {
					isselected = true;
				}
			}
			if (isselected) {
				drawSelectedCard(g, hand.get(i), i);
			} else {
				//bounding box for an unselected card
				RectF r = new RectF((width / currentspacing) * i, (height - (height / 3)),
						(width / currentspacing) * i + 150, height);
				hand.get(i).drawOn(g, r);
			}
		}
	}

	/**
	 * drawSelectedCard:
	 * 
	 * Called when a card is selected, draws the selected card
	 * @param g The canvas we're drawing on
	 * @param c The selected card
	 * @param loc Location of the tap
	 */
	private void drawSelectedCard(Canvas g, Card c, int loc) {
		if (c == null) {
			return;
		}
		paint.setColor(Color.YELLOW);
		//bounding box for a selected card
		RectF highlight = new RectF((width / currentspacing) * loc - 1,
				(height - (height / 3)) - 21, (width / currentspacing) * loc + 151, height - 19);
		g.drawRect(highlight, paint);
		RectF r = new RectF((width / currentspacing) * loc, (height - (height / 3)) - 20,
				(width / currentspacing) * loc + 150, height - 20);
		c.drawOn(g, r);
	}

	/**
	 * drawTrick:
	 * 
	 * Draws the cards in the current trick
	 * 
	 * @param g The canvas we're drawing on
	 */
	private void drawTrick(Canvas g) {
		if (state == null) {
			return;
		}
		Card[] trick = state.getCurrentTrick();
		for (int i = 0; i < trick.length; i++) {
			if (trick[i] != null) {
				//bounding box each card in the trick
				RectF r = new RectF((width / 5) + i * (width - (2 * width / 5)) / 4 + 20,
						height / 4, (width / 5) + i * (width - (2 * width / 5)) / 4 + 170, height
								- (3 * height / 8) - 20);
				trick[i].drawOn(g, r);
			}
		}
	}
	
	/**
	 * Forces the animation surface to update with a new game state
	 * 
	 * @param nstate
	 * 		The new game state
	 */
	public void forceRedraw(HeartsState nstate) {
		state = nstate;
		hand = state.getPlayerHand(playerNum);
		Canvas g = surface.getHolder().lockCanvas(null);
		tick(g);
		surface.getHolder().unlockCanvasAndPost(g);
	}
	
	/**
	 * drawButton
	 * 
	 * Draws a button-like rectangle that for all intents and purposes acts as a button,
	 * just drawn onto the animation surface.
	 * 
	 * @param g
	 * 		The canvas we're drawing on
	 */
	private void drawButton(Canvas g) {
		RectF buttonArea = new RectF((float) (width / 2.17), (float) ((height / 2.15) - (height / 8)),
				(float) (width - width / 2.17), (float) ((height - height / 2.15) - (height / 8)));
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		g.drawRect(buttonArea, paint);
		paint.setColor(Color.YELLOW);
		if (state.getSubState() == HeartsState.PASSING) {
			g.drawText("Pass Cards", (float) (width/2.165), (float) (height/2) - (height / 8), paint);
		} else {// receiving
			g.drawText("Receive Cards", (float) (width/2.165), (float) (height/2) - (height / 8), paint);
		}
		paint.setStyle(Paint.Style.STROKE);
	}
	
	/**
	 * threeCardsSelected
	 * 
	 * Tests to see if three cards are selected and we can therefore pass them
	 * 
	 * @return
	 * 		whether or not three cards are selected
	 */
	private boolean threeCardsSelected() {
		for (int i = 0; i < selectedCards.length; i++) {
			if (selectedCards[i] == null) {
				return false;
			}
		}
		return true;
	}
}
