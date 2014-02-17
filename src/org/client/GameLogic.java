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
import org.client.GameApi.SetTurn;
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
	private static final String Yes = "Yes";
	private static final String Board = "Board";
	private static final String EATEN = "EATEN";
	private static final String ARRIVAL = "ARRIVAL";
	private static final String From = "From";
	private static final String To = "To";

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
  	
	private List<Operation> foxNormalMove(State lastState, List<Operation> lastMove) {
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		operations.add(new Set(Is_Fox_Move, Yes));
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		Integer from = (Integer) setFrom.getValue();
		Integer to = (Integer) setTo.getValue();
		
		if(!checkAPositionIsFox(from) || !checkFoxCanMove(from)){
			throw new RuntimeException();
		}
		operations.add(new Set(From, from));
		operations.add(new Set(To, to));
		return operations;
	}

	private List<Operation> doFoxNormalMove(State lastState, List<Operation> lastMove) {
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		Integer from = (Integer) setFrom.getValue();
		Integer to = (Integer) setTo.getValue();  
		
		//找出另外一只狐狸的位置
		Integer anotherFox = 0;
		
		if(!checkAPositionIsFox(from) || checkFoxCanEat(from) || checkFoxCanEat(anotherFox)){
			throw new RuntimeException();
		}else if(checkAPositionIsEmpty(to)){
//			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor().ordinal())));
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));
			
			//更新board，from位置的狐狸删去，to位置添加狐狸
			operations.add(new Set(Board));
			
			operations.add(new Delete(Is_Fox_Move));
		}
		return operations;
	}

	private List<Operation> foxEatMove(State lastState, List<Operation> lastMove) {
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		operations.add(new Set(Is_Fox_Eat, Yes));
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		Integer from = (Integer) setFrom.getValue();
		Integer to = (Integer) setTo.getValue();
		
		if(!checkAPositionIsFox(from) || !checkFoxCanEat(from)){
			throw new RuntimeException();
		}
		operations.add(new Set(From, from));
		operations.add(new Set(To, to));
		return operations;
	}

	private List<Operation> doFoxEatMove(State lastState, List<Operation> lastMove) {
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		Integer from = (Integer) setFrom.getValue();
		Integer to = (Integer) setTo.getValue();  
		
		//找出另外一只狐狸的位置
		//Integer anotherFox = 0;
		if(checkAPositionIsFox(from) && checkFoxCanEat(from)){
			if(checkFoxCanEat(to)){
				operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			}else{
				//operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor().ordinal())));
				operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));
			}
			
			//更新board，from位置的狐狸删去，to位置添加狐狸，from和to之间的sheep删去
			operations.add(new Set(Board, ()));
			
			//更新Sheep的list和EATEN的list，以及ARRIVAL的list
			operations.add(new Set(S, ()));
			operations.add(new Set(EATEN, ()));
			
			//check does fox win
			if(EATEN中羊数量 >= 12){
				ENDGAME;
			}
			
			operations.add(new Set(ARRIVAL, ()));
			
			operations.add(new Delete(Is_Fox_Eat));
		}else{
			throw new RuntimeException();
		}
		return operations;
	}
	
	private List<Operation> doSheepMove(State lastState, List<Operation> lastMove) {
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		Integer from = (Integer) setFrom.getValue();
		Integer to = (Integer) setTo.getValue();
		
		if(checkSheepCanMove(from, to)){
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));
			
			//更新board，from位置的狐狸删去，to位置添加狐狸，from和to之间的sheep删去
			operations.add(new Set(Board, ()));
			
			//更新Sheep的list和EATEN的list，以及ARRIVAL的list
			operations.add(new Set(S, ()));
			operations.add(new Set(EATEN, ()));
			operations.add(new Set(ARRIVAL, ()));
			if(ARRIVAL中羊的数量 == 9){
				ENDGAME;
			}
		}else{
			throw new RuntimeException();
		}
		return operations;
	}
		
	private boolean checkFoxCanEat(Integer integer) {
		// TODO Auto-generated method stub	
		return false;
	}

	private boolean checkFoxCanMove(Integer integer) {
		// TODO Auto-generated method stub		
		return false;
	}
	
	private boolean checkAPositionIsFox(Integer integer){
		return false;
	}
	
	private boolean checkAPositionIsSheep(Integer integer){
		return false;
	}
	
	private boolean checkAPositionIsEmpty(Integer integer){
		return false;
	}
	
	private boolean checkSheepCanMove(Integer integerFrom, Integer integerTo){
		return false;
	}

	private int getHowManySheepHaveBeenEaten(){
		// TODO Auto-generated method stub
		return 0;
	}
	
	private int getHowManySheepHaveBeenArrived(){
		// TODO Auto-generated method stub
		return 0;
	}	
	
	private List<Operation> getExceptedOperations(
			Map<String, Object> lastState, List<Operation> lastMove,
			List<Integer> playerIds) {
		// TODO Auto-generated method stub
		return null;
	}

	List<Operation> getInitialMove(int foxPlayerId, int sheepPlayerId){
		List<Operation> operations = Lists.newArrayList();
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, F, S, EATEN, ARRIVAL
		operations.add(new SetTurn(foxPlayerId));
		operations.add(new Set(Board, ImmutableList.of(
				-1, -1,  1,  0,  2, -1, -1,
				-1, -1,  0,  0,  0, -1, -1,
				 0,  0,  0,  0,  0,  0,  0,
				 3,  4,  5,  6,  7,  8,  9,
				10, 11, 12, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1)));
		operations.add(new Set(F, ImmutableList.of(1, 2)));
		operations.add(new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)));
		operations.add(new Set(EATEN, ImmutableList.of()));
		operations.add(new Set(ARRIVAL, ImmutableList.of()));
		
		return operations;
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
			return foxNormalMove(lastState, lastMove);
		}else if(lastMove.contains(new Delete(Is_Fox_Move))){
			return doFoxNormalMove(lastState, lastMove);
		}else if(lastMove.contains(new Set(Is_Fox_Eat, Yes))){
			return foxEatMove(lastState, lastMove);
		}else if(lastMove.contains(new Delete(Is_Fox_Eat))){
			return doFoxEatMove(lastState, lastMove);
		}else{
			return doSheepMove(lastState, lastMove);
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
