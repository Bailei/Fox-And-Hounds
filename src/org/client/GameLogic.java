package org.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.client.GameApi.Delete;
import org.client.GameApi.EndGame;
import org.client.GameApi.Operation;
import org.client.GameApi.Set;
import org.client.GameApi.SetVisibility;
import org.client.GameApi.Shuffle;
import org.client.GameApi.VerifyMove;
import org.client.GameApi.VerifyMoveDone;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class GameLogic {
	private static final String TURN = "TURN";
	private static final String F = "F";
	private static final String S = "S";
	private static final String Is_Fox_Move = "Is_Fox_Move";
	private static final String Is_Fox_Eat = "Is_Fox_Eat";
//	private static final String Is_Sheep_Move = "Is_Sheep_Move";
	private static final String Yes = "Yes";

	public VerifyMoveDone verify(VerifyMove verifyMove) {
		try{
			checkMoveIsLegal(verifyMove);
			return new VerifyMoveDone();
		}catch(Exception e){
			return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
		}
	}
  
	void checkMoveIsLegal(VerifyMove verifyMove){
		List<Operation> lastMove = verifyMove.getLastMove(); 
		Map<String, Object> lastState = verifyMove.getLastState();
		//checking the operations are as expected
		List<Operation> exceptedOperations = getExceptedOperations(
				lastState, lastMove, verifyMove.getPlayerIds());
		check(exceptedOperations.equals(lastMove),exceptedOperations, lastMove);
		
		//checking the right player did the move
		//Color gotMoveFromColor = Color.values()[verifyMove.getPlayerIndex(verifyMove.getLastMovePlayerId())];
		//check(gotMoveFromColor == getExpectedMoveFromColor(lastState), gotMoveFromColor);
		
		// We use SetTurn, so we don't need to check that the correct player did the move.
	    // However, we do need to check the first move is done by the white player (and then in the
	    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
	    if (lastState.isEmpty()) {
	      check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
	    }
	}
  	
	
	private List<Operation> doSheepMove(State lastState) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Operation> doFoxEatMove(State lastState) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Operation> foxEatMove(State lastState) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Operation> doFoxNormalMove(State lastState) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Operation> foxNormalMove(State lastState) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean checkWhetherFoxCanEatSheep() {
		// TODO Auto-generated method stub	
		return false;
	}

	private boolean checkWhetherFoxCanMove() {
		// TODO Auto-generated method stub		
		return false;
	}

	private int checkHowManySheepHaveBeenEaten(){
		// TODO Auto-generated method stub
		return 0;
	}
	
	private int checkHowManySheepHaveBeenArrived(){
		// TODO Auto-generated method stub
		return 0;
	}	
	
	private List<Operation> getExceptedOperations(
			Map<String, Object> lastState, List<Operation> lastMove,
			List<Integer> playerIds) {
		// TODO Auto-generated method stub
		return null;
	}

	List<Operation> getInitialMove(int whitePlayerId, int blackPlayerId){
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	List<Operation> getExpectedOperations(Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds,
			int lastMovePlayerId){
		if(lastApiState.isEmpty()){
			return getInitialMove(playerIds.get(0), playerIds.get(1));
		}
		State lastState = gameApiStateToState(lastApiState, Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
		//There are 5 types of moves:
		// 1) foxNormalMove
		// 2) doFoxNormalMove
		// 3) foxEatMove
		// 4) doFoxEatMove
		// 5) doSheepMove
		if(lastMove.contains(new Set(Is_Fox_Move, Yes))){
			return foxNormalMove(lastState);
		}else if(lastMove.contains(new Delete(Is_Fox_Move))){
			return doFoxNormalMove(lastState);
		}else if(lastMove.contains(new Set(Is_Fox_Eat, Yes))){
			return foxEatMove(lastState);
		}else if(lastMove.contains(new Delete(Is_Fox_Eat))){
			return doFoxEatMove(lastState);
		}else{
			return doSheepMove(lastState);
		}
	}

	@SuppressWarnings("unchecked")
	private State gameApiStateToState(Map<String, Object> gameApiState,
			Color turnOfColor, List<Integer> playerIds){
		// TODO Auto-generated method stub
		return null;
	}
	
	private void check(boolean val, Object... debugArguments){
		if (!val){
			throw new RuntimeException("We have a hacker! debugArguments=" 
					+ Arrays.toString(debugArguments));
		}
	}
}
