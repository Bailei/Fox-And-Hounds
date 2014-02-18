package org.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
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
		List<Operation> expectedOperations = getExpectedOperations(verifyMove);
	    List<Operation> lastMove = verifyMove.getLastMove();
	    check(expectedOperations.equals(lastMove), expectedOperations, lastMove);		
		// We use SetTurn, so we don't need to check that the correct player did the move.
	    // However, we do need to check the first move is done by the white player (and then in the
	    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
	    if (verifyMove.getLastState().isEmpty()) {
	    	check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
	    }
	}
  	
	private List<Operation> foxNormalMove(State lastState, List<Operation> lastMove) {
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
		
		if(checkAPositionIsFox(from, lastState) && checkFoxCanMove(from, lastState)){
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			operations.add(new Set(Is_Fox_Move, Yes));
			operations.add(new Set(From, from));
			operations.add(new Set(To, to));
		}else{
			throw new RuntimeException();
		}
		return operations;
	}

	private List<Operation> doFoxNormalMove(State lastState, List<Operation> lastMove) {
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		Integer[][] lastB = lastState.getBoard();
		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();  
		String anotherFox = findAnotherFox(from, lastState);

		int xfrom = Integer.valueOf(from) % 10;
		int yfrom = Integer.valueOf(from) / 10;
		
		int xto = Integer.valueOf(from) % 10;
		int yto = Integer.valueOf(from) / 10;
		
		lastB[xto][yto] = lastB[xfrom][xfrom];
		lastB[xfrom][yfrom] = 0;
		
		if(checkAPositionIsFox(from, lastState) && checkAPositionIsEmpty(to, lastState)
				&& !checkFoxCanEat(from, lastState) && !checkFoxCanEat(anotherFox, lastState)){
			//operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor().ordinal())));
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));			
			//更新board，from位置的狐狸删去，to位置添加狐狸
			operations.add(new Set(Board, lastB));			
			operations.add(new Delete(Is_Fox_Move));
		}else{
			throw new RuntimeException();
		}
		return operations;
	}

	private List<Operation> foxEatMove(State lastState, List<Operation> lastMove) {
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
		
		if(checkAPositionIsFox(from, lastState) && checkFoxCanEat(from, lastState)){
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			operations.add(new Set(Is_Fox_Eat, Yes));
			operations.add(new Set(From, from));
			operations.add(new Set(To, to));
		}else{
			throw new RuntimeException();
		}
		return operations;
	}

	private List<Operation> doFoxEatMove(State lastState, List<Operation> lastMove) {
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();  
		
		Integer[][] lastB = lastState.getBoard();
		Integer[][] newB = lastB;
		
		List<Integer> lastS = lastState.getSheep();
		List<Integer> diedS = ImmutableList.<Integer>of();
		
		List<Integer> lastEaten = lastState.getEATEN();
		
		List<Integer> lastArrival = lastState.getARRIVAL();
		
		int xfrom = Integer.valueOf(from) % 10;
		int yfrom = Integer.valueOf(from) / 10;
		
		int xto = Integer.valueOf(from) % 10;
		int yto = Integer.valueOf(from) / 10;
		
		diedS.add(lastB[(xfrom + xto) / 2][(yfrom + yto) / 2]);
		
		newB[xto][yto] = lastB[xfrom][xfrom];
		newB[xfrom][yfrom] = 0;
		newB[(xfrom + xto) / 2][(yfrom + yto) / 2] = 0;
		
		List<Integer> newS = subtract(lastS, diedS);
		List<Integer> newEaten = concat(lastEaten, diedS);
		List<Integer> newArrival = lastArrival;
		if(lastArrival.contains(lastB[(xfrom + xto) / 2][(yfrom + yto) / 2])){
			newArrival = subtract(lastArrival, diedS);
		}
		
		if(checkAPositionIsFox(from, lastState) && checkFoxCanEat(from, lastState)){
			if(checkFoxCanEat(to, lastState)){
				operations.add(new Delete(Is_Fox_Eat));
				operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			}else{
				operations.add(new Delete(Is_Fox_Eat));
				//operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor().ordinal())));
				operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));
			}
			operations.add(new Set(Board, newB));
			operations.add(new Set(S, newS));
			operations.add(new Set(EATEN, newEaten));
			if(getHowManySheepHaveBeenArrived(lastState) >= 12){
				operations.add(new EndGame(lastState.getPlayerId(turnOfColor)));
			}
			operations.add(new Set(ARRIVAL, newArrival));
		}else{
			throw new RuntimeException();
		}
		return operations;
	}
	
	private List<Operation> doSheepMove(State lastState, List<Operation> lastMove) {
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
		
		Integer[][] lastB = lastState.getBoard();
		Integer[][] newB = lastB;
		
		List<Integer> lastArrival = lastState.getARRIVAL();
		
		int xfrom = Integer.valueOf(from) % 10;
		int yfrom = Integer.valueOf(from) / 10;
		
		int xto = Integer.valueOf(from) % 10;
		int yto = Integer.valueOf(from) / 10;
		
		newB[xto][yto] = lastB[xfrom][xfrom];
		newB[xfrom][yfrom] = 0;
		
		List<Integer> newArrival = lastArrival;
		if(xto >= 0 && xto <= 2 && yto >= 2 && yto <= 4){
			newArrival.add(newB[xto][yto]);
		}
		
		if(checkSheepCanMove(from, lastState)){
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor.getOppositeColor())));
			operations.add(new Set(Board, newB));
			operations.add(new Set(ARRIVAL, newArrival));
			
			if(getHowManySheepHaveBeenArrived(lastState) == 9){
				operations.add(new EndGame(lastState.getPlayerId(turnOfColor)));
			}
		}else{
			throw new RuntimeException();
		}
		return operations;
	}
		
	private boolean checkFoxCanEat(String st, State lastState) {
//		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		
		//同偶或同奇
		if((x % 2 == 0 && y % 2 == 0) || ((x % 2) == 1 && (y % 2) == 1)){
			if(checkAPositionIsSheep(x - 1, y, lastState) && checkAPositionIsEmpty(x - 2, y, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, lastState) && checkAPositionIsEmpty(x + 2, y, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, lastState) && checkAPositionIsEmpty(x, y - 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, lastState) && checkAPositionIsEmpty(x, y + 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y + 1, lastState) && checkAPositionIsEmpty(x - 2, y + 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y  + 1, lastState) && checkAPositionIsEmpty(x + 2, y + 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y - 1, lastState) && checkAPositionIsEmpty(x - 2, y - 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y - 1, lastState) && checkAPositionIsEmpty(x + 2, y - 2, lastState)){
				return true;
			}else{
				return false;
			}
		}else{
			//不同偶或不同奇
			if(checkAPositionIsSheep(x - 1, y, lastState) && checkAPositionIsEmpty(x - 2, y, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, lastState) && checkAPositionIsEmpty(x + 2, y, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, lastState) && checkAPositionIsEmpty(x, y - 2, lastState)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, lastState) && checkAPositionIsEmpty(x, y + 2, lastState)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	private boolean checkFoxCanMove(String st, State lastState) {
//		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		
		//同偶或同奇
		if((x % 2 == 0 && y % 2 == 0) || ((x % 2) == 1 && (y % 2) == 1)){
			if(checkAPositionIsEmpty(x - 1, y, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x, y - 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x, y + 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x - 1, y + 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y + 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x - 1, y - 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y - 1, lastState)){
				return true;
			}else{
				return false;
			}
		}else{
			//不同偶或不同奇
			if(checkAPositionIsEmpty(x - 1, y + 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y + 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x - 1, y - 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y - 1, lastState)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	private boolean checkSheepCanMove(String st, State lastState){
//		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		
		if(checkAPositionIsEmpty(x - 1, y + 1, lastState)){
			return true;
		}else if(checkAPositionIsEmpty(x + 1, y + 1, lastState)){
			return true;
		}else if(checkAPositionIsEmpty(x - 1, y - 1, lastState)){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean checkAPositionIsFox(String st, State lastState){
		Integer [][] lastB = lastState.getBoard();	
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;	
		if(lastB[x][y] != 1 || lastB[x][y] != 2)
			return false;
		return true;
	}
	
	private boolean checkAPositionIsFox(int x, int y, State lastState){
		Integer [][] lastB = lastState.getBoard();		
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB[x][y] != 1 || lastB[x][y] != 2)
			return false;
		return true;
	}
	
	private boolean checkAPositionIsSheep(String st, State lastState){
		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		if(lastB[x][y] < 3 || lastB[x][y] > 22)
			return false;
		return true;
	}
	
	private boolean checkAPositionIsSheep(int x, int y, State lastState){
		Integer [][] lastB = lastState.getBoard();
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB[x][y] >= 3 && lastB[x][y] <= 22)
			return false;
		return true;
	}
	
	private boolean checkAPositionIsEmpty(String st, State lastState){
		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		if(lastB[x][y] != 0)
			return false;
		return true;
	}
	
	private boolean checkAPositionIsEmpty(int x, int y, State lastState){
		Integer [][] lastB = lastState.getBoard();
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB[x][y] == 0)
			return false;
		return true;
	}

	private String findAnotherFox(String st, State lastState){
		Integer [][] lastB = lastState.getBoard();
		int x = Integer.valueOf(st) % 10;
		int y = Integer.valueOf(st) / 10;
		int anotherFox = 0;
		String anotherSt;
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 7; j++){
				if(lastB[x][y] == 1 && lastB[i][j] == 2)
					anotherFox = i * 10 + j;
				else if(lastB[x][y] == 2 && lastB[i][j] == 1){
					anotherFox = i * 10 + j;
				}
			}
		}		
		anotherSt = Integer.toString(anotherFox);
		return anotherSt;
	}
	
	private int getHowManySheepHaveBeenEaten(State lastState){
		ImmutableList<Integer> lastEaten = lastState.getEATEN();
		return lastEaten.size();
	}
	
	private int getHowManySheepHaveBeenArrived(State lastState){
		ImmutableList<Integer> lastArrival = lastState.getEATEN();
		return lastArrival.size();
	}	

	List<Operation> getMoveInitial(List<Integer> playerIds){
	    int fPlayerId = playerIds.get(0);
	    int sPlayerId = playerIds.get(1);
		List<Operation> operations = Lists.newArrayList();
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, F, S, EATEN, ARRIVAL
		operations.add(new SetTurn(fPlayerId));
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
	List<Operation> getExpectedOperations(VerifyMove verifyMove){
		List<Operation> lastMove = verifyMove.getLastMove();
	    Map<String, Object> lastApiState = verifyMove.getLastState();
	    List<Integer> playerIds = verifyMove.getPlayerIds();
	    if (lastApiState.isEmpty()) {
	      return getMoveInitial(playerIds);
	    }
	    int lastMovePlayerId = verifyMove.getLastMovePlayerId();
	    State lastState = gameApiStateToState(lastApiState,
	        Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
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
	
	<T> List<T> concat(List<T> a, List<T> b) {
		return Lists.newArrayList(Iterables.concat(a, b));
	}	

	<T> List<T> subtract(List<T> removeFrom, List<T> elementsToRemove) {
		check(removeFrom.containsAll(elementsToRemove), removeFrom, elementsToRemove);
	    List<T> result = Lists.newArrayList(removeFrom);
	    result.removeAll(elementsToRemove);
	    check(removeFrom.size() == result.size() + elementsToRemove.size());
	    return result;
	}
	
	@SuppressWarnings("unchecked")
	private State gameApiStateToState(Map<String, Object> gameApiState,
			Color turnOfColor, List<Integer> playerIds){
	    List<Integer> f = (List<Integer>) gameApiState.get(F);
	    List<Integer> s = (List<Integer>) gameApiState.get(S);
	    List<Integer> eaten = (List<Integer>) gameApiState.get(EATEN);
	    List<Integer> arrival = (List<Integer>) gameApiState.get(ARRIVAL);
	    Integer[][] board = (Integer[][]) gameApiState.get(Board);
	    Integer[][] boardTemp = board;
	    
	    return new State(
	        turnOfColor,
	        boardTemp,
	        ImmutableList.copyOf(playerIds),
	        gameApiState.containsKey(Is_Fox_Move),
	        gameApiState.containsKey(Is_Fox_Eat),
	        ImmutableList.copyOf(eaten),
	        ImmutableList.copyOf(arrival),
	        ImmutableList.copyOf(f),
	        ImmutableList.copyOf(s));
	}
	
	private void check(boolean val, Object... debugArguments){
		if (!val){
			throw new RuntimeException("We have a hacker! debugArguments=" 
					+ Arrays.toString(debugArguments));
		}
	}
}
