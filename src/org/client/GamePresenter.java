package org.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.graphics.GameGraphics;
import org.graphics.I18nMessages;
//import org.graphics.MessageBox;


import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
//import com.googlecode.mgwt.ui.client.dialog.Dialogs.AlertCallback;

//import org.sounds.GameSounds;

/**
 * The presenter that controls the game graphics.
 * We use the MVP pattern:
 * the model is {@link GameState},
 * the view will have the game graphics and it will implement {@link GamePresenter.View},
 * and the presenter is {@link GamePresenter}.
 */
public class GamePresenter {
	public interface View{
	    /**
	     * Sets the presenter. The viewer will call certain methods on the presenter, e.g.,
	     * when a position is selected({@link #positionSelected})
	     * 
	     * The process of Fox making an eating move looks as follows to the viewer:
	     * 1) The viewer calls {@link #positionSelected} two times to select from_position and to_position
	     * 
	     * The process of Fox making an eating move looks as follows to the presenter:
	     * 1) The presenter calls {@link #chooseNextPositionForFoxToEat} and passes the current selection.
	     */
		void setPresenter(GamePresenter gamePresenter);
		
		//Set the state for a viewer, i.e., not one of the players.
		void setViewerState(ArrayList<ArrayList<Integer>> board);
		
		/**
		 * Set the state for a player (whether the player has the turn or not).
		 */
		void setPlayerState(ArrayList<ArrayList<Integer>> board);
		
		/**
		 * Ask the player to choose the next position or finish his selection.
		 * We pass what position are selected.
		 * The user have to choose two position: from_position, to_position.
		 * (by calling {@link #finnishedSelectingPosition}; only allowed if selectedPosition.size == 2).
		 */
		void chooseNextPositionForSheep(List<String> SelectedPosition, ArrayList<ArrayList<Integer>> board);
		
		void chooseNextPositionForFox(List<String> SelectedPosition, ArrayList<ArrayList<Integer>> board);
		
		void playPieceDownSound();

		void displayResult(String winner);
	}
	
	private final GameLogic gameLogic = new GameLogic();
	private final View view;
	private final Container container;
	//A viewer doesn't have a color 
	private Optional<Color> myColor;
	private State gameState;
	private List<String> selectedPosition;
	private GameGraphics gameGraphics = new GameGraphics();
//	private final I18nMessages i18n;
	
	public GamePresenter(View view, Container container){
		this.view = view;
		this.container = container;
		view.setPresenter(this);
//		this.i18n = (I18nMessages) GWT.create(I18nMessages.class);
	}
	
	//Updates the presenter and the view with the state in updateUI
	public void updateUI(UpdateUI updateUI){
		
		
		List<String> playerIds = updateUI.getPlayerIds();
		String yourPlayerId = updateUI.getYourPlayerId();
		int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
		myColor = yourPlayerIndex == 0 ? Optional.of(Color.F) : yourPlayerIndex == 1 ? Optional.of(Color.S) : Optional.<Color>absent();
		selectedPosition = Lists.newArrayList();
		boolean auto = false;
		
		if(updateUI.getState().isEmpty()){
		
			//The F player sends the initial setup move
			if(myColor.isPresent() && myColor.get().isFox()){
				sendInitialMove(playerIds);
			}
			return;
		}
		
		Color turnOfColor = null;
		for(Operation operation : updateUI.getLastMove()){
			if(operation instanceof SetTurn){
				turnOfColor = Color.values()[playerIds.indexOf(((SetTurn) operation).getPlayerId())];
			}
		}
		
		gameState = gameLogic.gameApiStateToState(updateUI.getState(), turnOfColor, playerIds);
	
		if(updateUI.isViewer()){
			view.setViewerState(gameState.getBoard());
			return;
		}
		
		//Must be a player!
		Color myC = myColor.get();
		Color opponent = myC.getOppositeColor();
		
		if(updateUI.isAiPlayer()){
		  auto = true;
		}
		
		view.setPlayerState(gameState.getBoard());
		checkGameOver();
		
		if(isMyTurn()){
		  if(!auto) {
			if(isFoxTurn()){
				if(!gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					chooseNextPositionForFox();
				}else if(gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					MakeDoFoxNormalMove();
				}else if(!gameState.Is_Fox_Move() && gameState.Is_Fox_Eat()){
					MakeDoFoxEatMove();
				}
			}else if(isSheepTurn()){
				chooseNextPositionForSheep();
			}
		  }
		  else {
			chooseNextPositionForSheepAuto();
		  }
		}
	}
	private boolean endGame(State gameState){
		if(gameLogic.getHowManySheepHaveBeenArrived(gameState.getARRIVAL()) == 9
				||gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) == 12)
			return true;
		return false;
	}
	
	/**
	   * Shows a message and plays appropriate audio if the game has ended.
	   */
	public void checkGameOver() {
	    if (gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) == 12) { 
	    	String winner = "Fox Won!";
	    	view.displayResult(winner);
	    }
	    else if (gameLogic.getHowManySheepHaveBeenArrived(gameState.getARRIVAL()) == 9) { 
	    	String winner = "Sheep Won!";
	    	view.displayResult(winner);
	    }
	}
	
	private void chooseNextPositionForSheep() {
		view.chooseNextPositionForSheep(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}

	private void chooseNextPositionForFox() {
		view.chooseNextPositionForFox(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}
	
	private void chooseNextPositionForSheepAuto(){
		boolean flag = false;
		Random ran = new Random();
		while(!flag){
			int x = ran.nextInt(100) % 7;
			int y = ran.nextInt(100) % 7;
				String st = Integer.toString(x * 10 + y);
				String st_up = Integer.toString((x - 1) * 10 + y);
				String st_left = Integer.toString(x * 10 + (y - 1));
				String st_right = Integer.toString(x * 10 + (y + 1));
				
				if(gameLogic.checkAPositionIsSheep(st, gameState)
						&& gameLogic.checkSheepCanMove(st, gameState)){
					if(gameLogic.checkSheepCanMoveFrom2To(st, st_up, gameState)){
						selectedPosition.add(st);
						selectedPosition.add(st_up);
						flag = true;
						break;
					}else if(gameLogic.checkSheepCanMoveFrom2To(st, st_left, gameState)){
						selectedPosition.add(st);
						selectedPosition.add(st_left);
						flag = true;
						break;
					}else if(gameLogic.checkSheepCanMoveFrom2To(st, st_right, gameState)){
						selectedPosition.add(st);
						selectedPosition.add(st_right);
						flag = true;
						break;
					}else{
						continue;
					}
				}else{
					continue;
				}	
		}
		if(selectedPosition.size() == 2){
			List<Operation> operations = Lists.newArrayList();
			operations.add(new SetTurn("-1"));
			operations.add(new Set("From", selectedPosition.get(0)));
			operations.add(new Set("To", selectedPosition.get(1)));
			gameGraphics.playPieceDownSound();
			container.sendMakeMove(gameLogic.doSheepMove(gameState, operations));
		}
	}

	public boolean isMyTurn() {
		return myColor.isPresent() && myColor.get() == gameState.getTurn();
	}
	
	public boolean isFoxTurn(){
		return myColor.get() == Color.F;
	}
	
	public boolean isSheepTurn(){
		return myColor.get() == Color.S;
	}
			
	private void check(boolean val){
		if(!val){
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Choose from_position and to_position from the board for fox
	 * The view can only call this method if the presenter called {@link View#chooseNextPositionForFoxToMove}
	 */
	public void positionSelectedForFox(String st){
		check(isMyTurn() && isFoxTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsFox(st, gameState) 
						&& (gameLogic.checkFoxCanMove(st, gameState) || gameLogic.checkFoxCanEat(st, gameState)) 
						&& !gameLogic.checkFoxCanEat(gameLogic.findAnotherFox(st, gameState), gameState))
				{	
					selectedPosition.add(st);
					chooseNextPositionForFox();}
				else if(gameLogic.checkAPositionIsFox(st, gameState) 
							&& gameLogic.checkFoxCanEat(st, gameState) 
							&& gameLogic.checkFoxCanEat(gameLogic.findAnotherFox(st, gameState), gameState))
					{	
						selectedPosition.add(st);
						chooseNextPositionForFox();	
				}else if(gameLogic.checkAPositionIsFox(st, gameState) 
						&& !gameLogic.checkFoxCanEat(st, gameState) 
						&& gameLogic.checkFoxCanEat(gameLogic.findAnotherFox(st, gameState), gameState)){
					return;
				}	
			}else if(selectedPosition.size() == 1){
				String from = selectedPosition.get(0);
				String to = st;
				if(selectedPosition.contains(st)){
					return;
				}else if(gameLogic.checkFoxCanEat(from, gameState) 
						&& gameLogic.checkFoxCanEatFrom2To(from, to, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForFox();
				}else if(gameLogic.checkFoxCanMove(from, gameState) 
						&& !gameLogic.checkFoxCanEat(from, gameState)
						&& gameLogic.checkFoxCanMoveFrom2To(from, to, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForFox();
				}else{
					return;
				}
			}
	}
		
	/**
	 * Choose from_position and to_position from the board for sheep
	 * The view can only call this method if the presenter called {@link View#chooseNextPositionForSheep}
	 */
	public void positionSelectedForSheep(String st){
		check(isMyTurn() && isSheepTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsSheep(st, gameState)
						&& gameLogic.checkSheepCanMove(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForSheep();
				}else{
					chooseNextPositionForSheep();
				}	
			}else if(selectedPosition.size() == 1){
				String from = selectedPosition.get(0);
				if(selectedPosition.contains(st)){
					chooseNextPositionForSheep();
				}else if(gameLogic.checkSheepCanMoveFrom2To(from, st, gameState)){
					selectedPosition.add(st);

					List<Operation> operations = Lists.newArrayList();
					operations.add(new SetTurn("-1"));
					operations.add(new Set("From", selectedPosition.get(0)));
					operations.add(new Set("To", selectedPosition.get(1)));
					gameGraphics.playPieceDownSound();
					container.sendMakeMove(gameLogic.doSheepMove(gameState, operations));
				}else{
					chooseNextPositionForSheep();
				}				
			}
	}
	
	public void finishFoxMoveSelectPosition() {
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn("-1"));
		operations.add(new Set("AAA", 1));		
		operations.add(new Set("From", selectedPosition.get(0)));
		operations.add(new Set("To", selectedPosition.get(1)));
		String from = selectedPosition.get(0);
		String to = selectedPosition.get(1);
		
		if(!gameLogic.checkFoxCanEatFrom2To(from, to, gameState)){
			container.sendMakeMove(gameLogic.foxNormalMove(gameState, operations));
		}else{
			container.sendMakeMove(gameLogic.foxEatMove(gameState, operations));
		}	
	}
		
	/**
	 * Sends a move.
	 * The view can only call this method if the presenter passed
	 */
	private void sendInitialMove(List<String> playerIds){
		List<Operation> move = gameLogic.getMoveInitial(playerIds);
		container.sendMakeMove(move);
	}
	
	private void MakeDoFoxNormalMove(){		
		container.sendMakeMove(gameLogic.doFoxNormalMove(gameState));
	}
	
	private void MakeDoFoxEatMove(){
		container.sendMakeMove(gameLogic.doFoxEatMove(gameState));
	}	
	
	
	//Just for test
	private native void test(String message) /*-{
		$wnd.alert(message);
	}-*/;
}
