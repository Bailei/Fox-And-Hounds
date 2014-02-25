package org.client;

import java.util.ArrayList;
import java.util.List;

import org.client.GameApi.Container;
import org.client.GameApi.Operation;
import org.client.GameApi.Set;
import org.client.GameApi.SetTurn;
import org.client.GameApi.UpdateUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The presenter that controls the game graphics.
 * We use the MVP pattern:
 * the model is {@link GameState},
 * the view will have the game graphics and it will implement {@link GamePresenter.View},
 * and the presenter is {@link GamePresenter}.
 */
public class GamePresenter {
	interface View{
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
		void chooseNextPositionForSheepToMove(List<String> SelectedPosition, ArrayList<ArrayList<Integer>> board);
		
		void chooseNextPositionForFoxToMove(List<String> SelectedPosition, ArrayList<ArrayList<Integer>> board);
		
		void chooseNextPositionForFoxToEat(List<String> SelectedPosition, ArrayList<ArrayList<Integer>> board);
	}
	
	private final GameLogic gameLogic = new GameLogic();
	private final View view;
	private final Container container;
	//A viewer doesn't have a color 
	private Optional<Color> myColor;
	private State gameState;
	private List<String> selectedPosition;
	
//	private static final String Is_Fox_Move = "Is_Fox_Move";
//	private static final String Is_Fox_Eat = "Is_Fox_Eat";
//	private static final String Yes = "Yes";
	private static final String From = "From";
	private static final String To = "To";
	
	public GamePresenter(View view, Container container){
		this.view = view;
		this.container = container;
		view.setPresenter(this);
	}
	
	//Updates the presenter and the view with the state in updateUI
	public void updateUI(UpdateUI updateUI){
		List<Integer> playerIds = updateUI.getPlayerIds();
		int yourPlayerId = updateUI.getYourPlayerId();
		int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
		myColor = yourPlayerIndex == 0 ? Optional.of(Color.F) : yourPlayerIndex == 1 ? Optional.of(Color.S) : Optional.<Color>absent();
		selectedPosition = Lists.newArrayList();
		
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
		
		if(updateUI.isAiPlayer()){
		      // TODO: implement AI in a later HW!
		      // container.sendMakeMove(..);
			return;
		}
		
		//Must be a player!
		Color myC = myColor.get();
		Color opponent = myC.getOppositeColor();
//		int numberOfEatenSheep = gameState.getEATEN().size();
//		int numberOfArrivedSheep = gameState.getARRIVAL().size();
		view.setPlayerState(gameState.getBoard());
		if(isMyTurn()){
			if(isFoxTurn()){
				if(!gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					if(gameState.Fox_Move()){
						chooseNextPositionForFoxToMove();
					}else if(gameState.Fox_Eat()){
						chooseNextPositionForFoxToEat();
					}	
				}else if(gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					MakeDoFoxNormalMove();
				}else if(gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					MakeDoFoxEatMove();
				}
			}else if(isMyTurn() && isSheepTurn()){
				chooseNextPositionForSheepToMove();
			}
		}			
	}
	
	private void chooseNextPositionForSheepToMove() {
		view.chooseNextPositionForSheepToMove(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}

	private void chooseNextPositionForFoxToMove() {
		view.chooseNextPositionForFoxToMove(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}
	
	private void chooseNextPositionForFoxToEat() {
		view.chooseNextPositionForFoxToEat(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}

	private boolean isMyTurn() {
		return myColor.isPresent() && myColor.get() == gameState.getTurn();
	}
	
	private boolean isFoxTurn(){
		return myColor.get() == Color.F;
	}
	
	private boolean isSheepTurn(){
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
	void positionSelectedForFoxToMove(String st){
		check(isMyTurn() && isFoxTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		if(gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) < 12){
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsFox(st, gameState)
						&& gameLogic.checkFoxCanMove(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForFoxToMove();
				}else{
					chooseNextPositionForFoxToMove();
				}	
			}else if(selectedPosition.size() == 1){
				if(selectedPosition.contains(st)){
					chooseNextPositionForFoxToMove();
				}else if(gameLogic.checkAPositionIsEmpty(st, gameState)){
					String from = selectedPosition.get(0);
					int xfrom = Integer.valueOf(from) / 10;
					int yfrom = Integer.valueOf(from) % 10;		
					int xto = Integer.valueOf(st) / 10;
					int yto = Integer.valueOf(st) % 10;
					if((Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 1) ||
							(Math.abs(xfrom-xto) == 0 && Math.abs(yfrom - yto) == 1) || 
							(Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 0))
						selectedPosition.add(st);
				}else{
					chooseNextPositionForFoxToMove();
				}
			}
		}
	}
	
	/**
	 * Choose from_position and to_position from the board for fox
	 * The view can only call this method if the presenter called {@link View#chooseNextPositionForFoxToEat}
	 */
	void positionSelectedForFoxToEat(String st){
		check(isMyTurn() && isFoxTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		if(gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) < 12){
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsFox(st, gameState)
						&& gameLogic.checkFoxCanEat(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForFoxToEat();
				}else{
					chooseNextPositionForFoxToEat();
				}	
			}else if(selectedPosition.size() == 1){
				if(selectedPosition.contains(st)){
					chooseNextPositionForFoxToEat();
				}else if(gameLogic.checkAPositionIsEmpty(st, gameState)){
					String from = selectedPosition.get(0);
					int xfrom = Integer.valueOf(from) / 10;
					int yfrom = Integer.valueOf(from) % 10;		
					int xto = Integer.valueOf(st) / 10;
					int yto = Integer.valueOf(st) % 10;
					if((Math.abs(xfrom-xto) == 2 && Math.abs(yfrom - yto) == 2) ||
							(Math.abs(xfrom-xto) == 2 && Math.abs(yfrom - yto) == 0) || 
							(Math.abs(xfrom-xto) == 0 && Math.abs(yfrom - yto) == 2))
						selectedPosition.add(st);
				}else{
					chooseNextPositionForFoxToEat();
				}
			}
		}
	}
		
	/**
	 * Choose from_position and to_position from the board for sheep
	 * The view can only call this method if the presenter called {@link View#chooseNextPositionForSheep}
	 */
	void positionSelectedForSheepToMove(String st){
		check(isMyTurn() && isSheepTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		if(gameLogic.getHowManySheepHaveBeenArrived(gameState.getARRIVAL()) < 9){
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsSheep(st, gameState)
						&& gameLogic.checkFoxCanMove(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForSheepToMove();
				}else{
					chooseNextPositionForSheepToMove();
				}	
			}else if(selectedPosition.size() == 1){
				if(selectedPosition.contains(st)){
					chooseNextPositionForFoxToMove();
				}else if(gameLogic.checkAPositionIsEmpty(st, gameState)){
					String from = selectedPosition.get(0);
					int xfrom = Integer.valueOf(from) / 10;
					int yfrom = Integer.valueOf(from) % 10;		
					int xto = Integer.valueOf(st) / 10;
					int yto = Integer.valueOf(st) % 10;
					if((Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 1) ||
							(Math.abs(xfrom-xto) == 0 && Math.abs(yfrom - yto) == 1) || 
							(Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 0))
						selectedPosition.add(st);
				}else{
					chooseNextPositionForSheepToMove();
				}
			}
		}
	}
	
	/**
	 * Sends a move.
	 * The view can only call this method if the presenter passed
	 */
	private void sendInitialMove(List<Integer> playerIds){
		container.sendMakeMove(gameLogic.getMoveInitial(playerIds));
	}
	
	private void MakeDoFoxNormalMove(){
		List<Operation> operations = Lists.newArrayList();		
//		operations.add(new SetTurn(gameState.getPlayerId(myColor.get())));
//		operations.add(new Set(Is_Fox_Move, Yes));
		operations.add(new Set(From, selectedPosition.get(0)));
		operations.add(new Set(To, selectedPosition.get(1)));
		
		container.sendMakeMove(gameLogic.doFoxNormalMove(gameState, operations));
	}
	
	private void MakeDoFoxEatMove(){
		List<Operation> operations = Lists.newArrayList();
//		operations.add(new SetTurn(gameState.getPlayerId(myColor.get())));
//		operations.add(new Set(Is_Fox_Eat, Yes));
		operations.add(new Set(From, selectedPosition.get(0)));
		operations.add(new Set(To, selectedPosition.get(1)));
		
		container.sendMakeMove(gameLogic.doFoxEatMove(gameState, operations));
	}	
}
