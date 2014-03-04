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
	
		 public void testButton1();
		 
		 public void testButton2();
		 
		 public void testButton3();
		 
		 public void testButton4();
		 
		 public void testButton5();
	}
	
	private final GameLogic gameLogic = new GameLogic();
	private final View view;
	private final Container container;
	//A viewer doesn't have a color 
	private Optional<Color> myColor;
	private State gameState;
	private List<String> selectedPosition;
	
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
				
//				view.testButton2();
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
		
		
		view.setPlayerState(gameState.getBoard());
//		view.testButton2();
		if(isMyTurn()){
			if(isFoxTurn() && !endGame(gameState)){
				if(!gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					chooseNextPositionForFox();
				}else if(gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat()){
					MakeDoFoxNormalMove();
				}else if(!gameState.Is_Fox_Move() && gameState.Is_Fox_Eat()){
					MakeDoFoxEatMove();
				}
			}else if(isSheepTurn() && !endGame(gameState)){
				chooseNextPositionForSheep();
			}
		}			
	}
	private boolean endGame(State gameState){
		if(gameLogic.getHowManySheepHaveBeenArrived(gameState.getARRIVAL()) == 9
				||gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) == 12)
			return true;
		return false;
	}
	
	private void chooseNextPositionForSheep() {
		view.chooseNextPositionForSheep(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
	}

	private void chooseNextPositionForFox() {
		view.chooseNextPositionForFox(ImmutableList.copyOf(selectedPosition), gameState.getBoard());
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
	public void positionSelectedForFox(String st){
		check(isMyTurn() && isFoxTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		
		if(gameLogic.getHowManySheepHaveBeenEaten(gameState.getEATEN()) < 12){
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsFox(st, gameState)
						&& gameLogic.checkFoxCanMove(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForFox();
				}else{
					chooseNextPositionForFox();
				}	
			}else if(selectedPosition.size() == 1){
				if(selectedPosition.contains(st)){
					chooseNextPositionForFox();
				}else if(gameLogic.checkAPositionIsEmpty(st, gameState)){
						selectedPosition.add(st);
						List<Operation> operations = Lists.newArrayList();
						operations.add(new SetTurn(-1));
						operations.add(new SetTurn(3));
						operations.add(new Set("From", selectedPosition.get(0)));
						operations.add(new Set("To", selectedPosition.get(1)));
						container.sendMakeMove(gameLogic.foxEatMove(gameState, operations));
				}else{
					chooseNextPositionForFox();
				}
			}
		}
	}
		
	/**
	 * Choose from_position and to_position from the board for sheep
	 * The view can only call this method if the presenter called {@link View#chooseNextPositionForSheep}
	 */
	public void positionSelectedForSheep(String st){
		check(isMyTurn() && isSheepTurn() && !gameState.Is_Fox_Move() && !gameState.Is_Fox_Eat());
		if(gameLogic.getHowManySheepHaveBeenArrived(gameState.getARRIVAL()) < 9){
			if(selectedPosition.size() == 0){
				if(gameLogic.checkAPositionIsSheep(st, gameState)
						&& gameLogic.checkFoxCanMove(st, gameState)){
					selectedPosition.add(st);
					chooseNextPositionForSheep();
				}else{
					chooseNextPositionForSheep();
				}	
			}else if(selectedPosition.size() == 1){
				if(selectedPosition.contains(st)){
					chooseNextPositionForSheep();
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
					List<Operation> operations = Lists.newArrayList();
					operations.add(new SetTurn(-1));
					operations.add(new Set("From", selectedPosition.get(0)));
					operations.add(new Set("To", selectedPosition.get(1)));
					container.sendMakeMove(gameLogic.foxNormalMove(gameState, operations));
				}else{
					chooseNextPositionForSheep();
				}
			}
		}
	}
	
	/*
	public void finishFoxSelectPosition() {
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn(-1));
		operations.add(new SetTurn(3));
		operations.add(new Set("From", selectedPosition.get(0)));
		operations.add(new Set("To", selectedPosition.get(1)));
		container.sendMakeMove(gameLogic.foxEatMove(gameState, operations));
	}

	public void finishFoxMoveSelectPosition() {
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn(-1));
		operations.add(new SetTurn(3));
		operations.add(new Set("From", selectedPosition.get(0)));
		operations.add(new Set("To", selectedPosition.get(1)));
		container.sendMakeMove(gameLogic.foxNormalMove(gameState, operations));
	}
	*/
		
	/**
	 * Sends a move.
	 * The view can only call this method if the presenter passed
	 */
	private void sendInitialMove(List<Integer> playerIds){
		
		List<Operation> move = gameLogic.getMoveInitial(playerIds);
		
		container.sendMakeMove(move);
	}
	
	private void MakeDoFoxNormalMove(){		
		container.sendMakeMove(gameLogic.doFoxNormalMove(gameState));
	}
	
	private void MakeDoFoxEatMove(){
		container.sendMakeMove(gameLogic.doFoxEatMove(gameState));
	}	
}
