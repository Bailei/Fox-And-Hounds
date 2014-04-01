package org.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Delete;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.Shuffle;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.graphics.GameGraphics;

public class GameLogic {
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
//		view.testButton5();
	    List<Operation> lastMove = verifyMove.getLastMove();
	    check(expectedOperations.equals(lastMove));		
		// We use SetTurn, so we don't need to check that the correct player did the move.
	    // However, we do need to check the first move is done by the white player (and then in the
	    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
	    if (verifyMove.getLastState().isEmpty()) {
	    	check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
	    }
	}
  	
	List<Operation> foxNormalMove(State lastState, List<Operation> lastMove) {
		check(!lastState.Is_Fox_Move());
		check(!lastState.Is_Fox_Eat());
//		view.testButton4("normal move");
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, From, To, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		
		//suppose that fox want to make a normal move
		// 0) new SetTurn(playerId of F);
		// 1) new Set(Is_Fox_Move, Yes);
		// 2) new Set(From, ...);
		// 3) new Set(To, ...);
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
		boolean b1 = checkAPositionIsFox(from, lastState);		
		boolean b2 = checkFoxCanMoveFrom2To(from, to, lastState);	
		boolean b3 = checkFoxCanEat(from, lastState);
		String anotherFox = findAnotherFox(from, lastState);
		boolean b4 = checkFoxCanEat(anotherFox, lastState);
		if(b1 && b2 && !b3 && !b4){			
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			operations.add(new Set(Is_Fox_Move, Yes));
			operations.add(new Set(From, from));
			operations.add(new Set(To, to));			
		}else{			
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		}
		return operations;
	}

//this move no bugs
	List<Operation> doFoxNormalMove(State lastState) {
		check(lastState.Is_Fox_Move());
		check(!lastState.Is_Fox_Eat());
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		//suppose that fox make a normal move
		// 0) new SetTurn(playerId of S);
		// 1) new Delete(Is_Fox_Move);
		// 2) new Set(From, ...);
		// 3) new Set(To, ...);
		// 4) new Set(Board, ...);
		// 5) ...		
		String from = lastState.getFrom();
		String to = lastState.getTo();
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		ArrayList<ArrayList<Integer>> newB = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<7;i++) {
			newB.add(new ArrayList<Integer>());
			for(int j=0;j<7;j++) {
				newB.get(i).add(lastB.get(i).get(j));
			}
		}
		String anotherFox = findAnotherFox(from, lastState);
		
		if(checkAPositionIsFox(from, lastState) && checkAPositionIsEmpty(to, lastState)
				&& !checkFoxCanEat(from, lastState) && !checkFoxCanEat(anotherFox, lastState)){
			int xfrom = Integer.valueOf(from) / 10;
			int yfrom = Integer.valueOf(from) % 10;		
			int xto = Integer.valueOf(to) / 10;
			int yto = Integer.valueOf(to) % 10;	
			newB.get(xto).set(yto, lastB.get(xfrom).get(yfrom));
			newB.get(xfrom).set(yfrom, 0);
			operations.add(new SetTurn(lastState.getPlayerIds().get(turnOfColor.getOppositeColor().ordinal())));					
			operations.add(new Delete(Is_Fox_Move));
			operations.add(new Set(Board, newB));
			
		}else{
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		}
		return operations;
	}

	List<Operation> foxEatMove(State lastState, List<Operation> lastMove) {
		check(!lastState.Is_Fox_Move());
		check(!lastState.Is_Fox_Eat());
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		//suppose that fox want to make a eat move
		// 0) new SetTurn(playerId of F);
		// 1) new Set(Is_Fox_Eat, Yes);
		// 2) new Set(From, ...);
		// 3) new Set(To, ...);		
		Set setFrom = (Set) lastMove.get(2);
		Set setTo = (Set) lastMove.get(3);		
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
//		view.testButton4("come on!");
		if(checkAPositionIsFox(from, lastState) && checkFoxCanEatFrom2To(from, to, lastState)){
//			view.testButton4("^^&*(^%$");
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
			operations.add(new Set(Is_Fox_Eat, Yes));
			operations.add(new Set(From, from));
			operations.add(new Set(To, to));
		}else{
//			view.testButton4("fuck");
			operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
		}
		return operations;
	}

	List<Operation> doFoxEatMove(State lastState) {
		check(!lastState.Is_Fox_Move());
		check(lastState.Is_Fox_Eat());
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		//suppose that fox make a eat move
		// 0) new SetTurn(playerId of S);
		// 1) new Delete(Is_Fox_Eat);
		// 2) new Set(Board, ...);
		// 3) new Set(S, ...);
		// 4) new Set(EATEN, ...);
		// 5) new Set(ARRIVAL, ...);
	    // AND: if fox have eaten 12 sheep, then the game ends!
	    // Let's determine just by looking at the lastState.

		String from = lastState.getFrom();
		String to = lastState.getTo();		
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		ArrayList<ArrayList<Integer>> newB = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<7;i++) {
			newB.add(new ArrayList<Integer>());
			for(int j=0;j<7;j++) {
				newB.get(i).add(lastB.get(i).get(j));
			}
		}
		
		List<Integer> lastS = lastState.getSheep();		
		List<Integer> lastEaten = lastState.getEATEN();
		List<Integer> lastArrival = lastState.getARRIVAL();
		
		int xfrom = Integer.valueOf(from) / 10;
		int yfrom = Integer.valueOf(from) % 10;		
		int xto = Integer.valueOf(to) / 10;
		int yto = Integer.valueOf(to) % 10;
		
		List<Integer> diedS = ImmutableList.<Integer>of(lastB.get((xfrom + xto) / 2).get((yfrom + yto) / 2));		
		List<Integer> newS = subtract(lastS, diedS);
		List<Integer> newEaten = concat(lastEaten, diedS);
		List<Integer> newArrival = lastArrival;
		if(lastArrival.contains(lastB.get((xfrom + xto) / 2).get((yfrom + yto) / 2))){
			newArrival = subtract(lastArrival, diedS);
		}
		if(checkAPositionIsFox(from, lastState) && checkFoxCanEat(from, lastState)){
			newB.get(xto).set(yto, lastB.get(xfrom).get(yfrom));
			newB.get(xfrom).set(yfrom, 0);
			newB.get((xfrom + xto) / 2).set((yfrom + yto) / 2, 0);
			if(checkFoxCanEat(to, newB)){
				operations.add(new SetTurn(lastState.getPlayerId(turnOfColor)));
				operations.add(new Delete(Is_Fox_Eat));
			}else{
				operations.add(new SetTurn(lastState.getPlayerIds().get(turnOfColor.getOppositeColor().ordinal())));
				operations.add(new Delete(Is_Fox_Eat));
			}
			operations.add(new Set(Board, newB));
			operations.add(new Set(S, newS));
			operations.add(new Set(EATEN, newEaten));
			operations.add(new Set(ARRIVAL, newArrival));
			
			if(getHowManySheepHaveBeenEaten(newEaten) >= 12){
				operations.add(new EndGame(lastState.getPlayerId(turnOfColor)));
			}
		}
		return operations;
	}

//this move no bugs	
	public List<Operation> doSheepMove(State lastState, List<Operation> lastMove) {
		check(!lastState.Is_Fox_Move());
		check(!lastState.Is_Fox_Eat());
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		Color turnOfColor = lastState.getTurn();
		List<Operation> operations = Lists.newArrayList();
		//suppose that sheep want to make a move
		// 0) new SetTurn(playerId of F);
		// 2) new Set(From, ...);
		// 3) new Set(To, ...);	
		// 4) new Set(ARRIVAL, ...);
	    // AND: if 9 sheep arrives at the paddock, then the game ends!
	    // Let's determine just by looking at the lastState.
		Set setFrom = (Set) lastMove.get(1);
		Set setTo = (Set) lastMove.get(2);
		String from = (String) setFrom.getValue();
		String to = (String) setTo.getValue();
		
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		ArrayList<ArrayList<Integer>> newB = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<7;i++) {
			newB.add(new ArrayList<Integer>());
			for(int j=0;j<7;j++) {
				newB.get(i).add(lastB.get(i).get(j));
			}
		}
		
		List<Integer> lastArrival = lastState.getARRIVAL();
		
		int xfrom = Integer.valueOf(from) / 10;
		int yfrom = Integer.valueOf(from) % 10;	
		int xto = Integer.valueOf(to) / 10;
		int yto = Integer.valueOf(to) % 10;
		check((Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 1) ||
				(Math.abs(xfrom-xto) == 0 && Math.abs(yfrom - yto) == 1) || 
				(Math.abs(xfrom-xto) == 1 && Math.abs(yfrom - yto) == 0));

		List<Integer> newArrival = lastArrival;
		List<Integer> leaveSheep = ImmutableList.<Integer>of(lastB.get(xfrom).get(yfrom));
		if(((xfrom >= 0 && xfrom <= 2 && yfrom >= 0 && yfrom <= 6) 
				|| (xfrom >= 5 && xfrom <= 6 && yfrom >= 0 && yfrom <= 6)
				|| (xfrom >= 3 && xfrom <= 6 && yfrom >= 2 && yfrom <= 4)) && (xto >= 0 && xto <= 2 && yto >= 2 && yto <= 4)){
			newArrival = concat(newArrival, leaveSheep);
		}
		if((xfrom >= 0 && xfrom <= 2 && yfrom >= 2 && yfrom <= 4) && ((xto >= 0 && xto <= 2 && yto >= 0 && yto <= 6) 
				|| (xto >= 5 && xto <= 6 && yto >= 0 && yto <= 6)
				|| (xto >= 3 && xto <= 6 && yto >= 2 && yto <= 4))){
			newArrival = subtract(lastArrival, leaveSheep);
		}
		
		boolean b = checkSheepCanMoveFrom2To(from, to, lastState);
//		view.testButton4("!!!");
		if(b){
//		view.testButton4("&&&&");
			newB.get(xto).set(yto, lastB.get(xfrom).get(yfrom));
			newB.get(xfrom).set(yfrom, 0);
			operations.add(new SetTurn(lastState.getPlayerIds().get(turnOfColor.getOppositeColor().ordinal())));
			operations.add(new Set(From, from));
			operations.add(new Set(To, to));
			operations.add(new Set(Board, newB));
			operations.add(new Set(ARRIVAL, newArrival));

			if(getHowManySheepHaveBeenArrived(newArrival) == 9){
				operations.add(new EndGame(lastState.getPlayerId(turnOfColor)));
			}
		}
		return operations;
	}
		
	boolean checkFoxCanEat(String st, State lastState) {
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		
		//x and y are both even or odd
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
			//x is even and y is odd, or x is odd and y is even
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
	
	boolean checkFoxCanEatFrom2To(String from, String to, State lastState) {
		int x = Integer.valueOf(from) / 10;
		int y = Integer.valueOf(from) % 10;
		int xto = Integer.valueOf(to) / 10;
		int yto = Integer.valueOf(to) % 10;
		
		//x and y are both even or odd
		if((x % 2 == 0 && y % 2 == 0) || ((x % 2) == 1 && (y % 2) == 1)){
			if(checkAPositionIsSheep(x - 1, y, lastState) && checkAPositionIsEmpty(x - 2, y, lastState) 
					&& (xto == x - 2) && (yto == y)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, lastState) && checkAPositionIsEmpty(x + 2, y, lastState) 
					&& (xto == x + 2) && (yto == y)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, lastState) && checkAPositionIsEmpty(x, y - 2, lastState) 
					&& (xto == x) && (yto == y - 2)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, lastState) && checkAPositionIsEmpty(x, y + 2, lastState) 
					&& (xto == x) && (yto == y + 2)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y + 1, lastState) && checkAPositionIsEmpty(x - 2, y + 2, lastState) 
					&& (xto == x - 2) && (yto == y + 2)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y  + 1, lastState) && checkAPositionIsEmpty(x + 2, y + 2, lastState) 
					&& (xto == x + 2) && (yto == y + 2)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y - 1, lastState) && checkAPositionIsEmpty(x - 2, y - 2, lastState) 
					&& (xto == x - 2) && (yto == y - 2)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y - 1, lastState) && checkAPositionIsEmpty(x + 2, y - 2, lastState) 
					&& (xto == x + 2) && (yto == y - 2)){
				return true;
			}else{
				return false;
			}
		}else{
			//x is even and y is odd, or x is odd and y is even
			if(checkAPositionIsSheep(x - 1, y, lastState) && checkAPositionIsEmpty(x - 2, y, lastState) 
					&& (xto == x - 2) && (yto == y)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, lastState) && checkAPositionIsEmpty(x + 2, y, lastState) 
					&& (xto == x + 2) && (yto == y)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, lastState) && checkAPositionIsEmpty(x, y - 2, lastState) 
					&& (xto == x) && (yto == y -2)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, lastState) && checkAPositionIsEmpty(x, y + 2, lastState) 
					&& (xto == x) && (yto == y + 2)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	boolean checkFoxCanEat(String st, ArrayList<ArrayList<Integer>> newBoard) {
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		
		//x and y are both even or odd
		if((x % 2 == 0 && y % 2 == 0) || ((x % 2) == 1 && (y % 2) == 1)){
			if(checkAPositionIsSheep(x - 1, y, newBoard) && checkAPositionIsEmpty(x - 2, y, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, newBoard) && checkAPositionIsEmpty(x + 2, y, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, newBoard) && checkAPositionIsEmpty(x, y - 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, newBoard) && checkAPositionIsEmpty(x, y + 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y + 1, newBoard) && checkAPositionIsEmpty(x - 2, y + 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y  + 1, newBoard) && checkAPositionIsEmpty(x + 2, y + 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x - 1, y - 1, newBoard) && checkAPositionIsEmpty(x - 2, y - 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y - 1, newBoard) && checkAPositionIsEmpty(x + 2, y - 2, newBoard)){
				return true;
			}else{
				return false;
			}
		}else{
			//x is even and y is odd, or x is odd and y is even
			if(checkAPositionIsSheep(x - 1, y, newBoard) && checkAPositionIsEmpty(x - 2, y, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x + 1, y, newBoard) && checkAPositionIsEmpty(x + 2, y, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x, y - 1, newBoard) && checkAPositionIsEmpty(x, y - 2, newBoard)){
				return true;
			}else if(checkAPositionIsSheep(x, y + 1, newBoard) && checkAPositionIsEmpty(x, y + 2, newBoard)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	boolean checkFoxCanMove(String st, State lastState) {
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		
		//x and y are both even or odd
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
			//x is even and y is odd, or x is odd and y is even
			if(checkAPositionIsEmpty(x - 1, y, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x + 1, y, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x, y - 1, lastState)){
				return true;
			}else if(checkAPositionIsEmpty(x, y + 1, lastState)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	boolean checkFoxCanMoveFrom2To(String from, String to, State lastState) {
		int xfrom = Integer.valueOf(from) / 10;
		int yfrom = Integer.valueOf(from) % 10;
		int xto = Integer.valueOf(to) / 10;
		int yto = Integer.valueOf(to) % 10;
		//x and y are both even or odd
//		view.testButton4(xfrom + "#" + yfrom + "#" + xto + "#" + yto);
		if((xfrom % 2 == 0 && yfrom % 2 == 0) || ((xfrom % 2) == 1 && (yfrom % 2) == 1)){
//			view.testButton4("))))))");
			if(checkAPositionIsEmpty(xfrom - 1, yfrom, lastState) && (xto == xfrom - 1) && (yto == yfrom)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom + 1, yfrom, lastState) && (xto == xfrom + 1) && (yto == yfrom)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom, yfrom - 1, lastState) && (xto == xfrom) && (yto == yfrom - 1)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom, yfrom + 1, lastState) && (xto == xfrom) && (yto == yfrom + 1)){
//				view.testButton4("(((((");
				return true;
			}else if(checkAPositionIsEmpty(xfrom - 1, yfrom + 1, lastState) && (xto == xfrom - 1) && (yto == yfrom + 1)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom + 1, yfrom + 1, lastState) && (xto == xfrom + 1) && (yto == yfrom + 1)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom - 1, yfrom - 1, lastState) && (xto == xfrom - 1) && (yto == yfrom - 1)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom + 1, yfrom - 1, lastState) && (xto == xfrom + 1) && (yto == yfrom - 1)){
				return true;
			}else{
//			view.testButton4("!!!!!!!");	
				return false;
			}
		}else{
			//x is even and y is odd, or x is odd and y is even
//			view.testButton4("odd & even");
			if(checkAPositionIsEmpty(xfrom - 1, yfrom, lastState) && (xto == xfrom - 1) && (yto == yfrom)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom + 1, yfrom, lastState) && (xto == xfrom + 1) && (yto == yfrom)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom, yfrom - 1, lastState) && (xto == xfrom) && (yto == yfrom - 1)){
				return true;
			}else if(checkAPositionIsEmpty(xfrom, yfrom + 1, lastState) && (xto == xfrom) && (yto == yfrom + 1)){
				return true;
			}else{
//			view.testButton4("odd & even" + "false");
				return false;
			}
		}
	}
	
	boolean checkSheepCanMove(String st, State lastState){
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		
		if(checkAPositionIsEmpty(x, y + 1, lastState)){
			return true;
		}else if(checkAPositionIsEmpty(x, y - 1, lastState)){
			return true;
		}else if(checkAPositionIsEmpty(x - 1, y, lastState)){
			return true;
		}else{
			return false;
		}
	}
	
	boolean checkSheepCanMoveFrom2To(String from, String to, State lastState){
		int x = Integer.valueOf(from) / 10;
		int y = Integer.valueOf(from) % 10;
		int xto = Integer.valueOf(to) / 10;
		int yto = Integer.valueOf(to) % 10;
		
		if(checkAPositionIsEmpty(x, y + 1, lastState) && (xto == x) && (yto == y + 1)){
			return true;
		}else if(checkAPositionIsEmpty(x, y - 1, lastState) && (xto == x) && (yto == y - 1)){
			return true;
		}else if(checkAPositionIsEmpty(x - 1, y, lastState) && (xto == x - 1) && (yto == y)){
			return true;
		}else{
			return false;
		}
	}
	
	boolean checkAPositionIsFox(String st, State lastState){
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();	
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;	
		if(lastB.get(x).get(y) == 1 || lastB.get(x).get(y) == 2)
			return true;
		return false;
	}

	boolean checkAPositionIsSheep(String st, State lastState){
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();	
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB.get(x).get(y) >= 3 && lastB.get(x).get(y) <= 22)
			return true;
		return false;
	}
	
	boolean checkAPositionIsSheep(int x, int y, State lastState){
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB.get(x).get(y) >= 3 && lastB.get(x).get(y) <= 22)
			return true;
		return false;
	}
	
	boolean checkAPositionIsSheep(int x, int y, ArrayList<ArrayList<Integer>> newBoard){
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && newBoard.get(x).get(y) >= 3 && newBoard.get(x).get(y) <= 22)
			return true;
		return false;
	}
	
	boolean checkAPositionIsEmpty(int x, int y, ArrayList<ArrayList<Integer>> newBoard){
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && newBoard.get(x).get(y) == 0){
			return true;
		}
		return false;
	}
	
	boolean checkAPositionIsEmpty(String st, State lastState){
//		view.testButton4(st + "^^");
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB.get(x).get(y) == 0){
//			view.testButton4("true");
			return true;
		}
//		view.testButton4("false");
		return false;
	}
	
	boolean checkAPositionIsEmpty(int x, int y, State lastState){
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		if(x >= 0 && x <= 6 && y >= 0 && y <= 6 && lastB.get(x).get(y) == 0)
			return true;
		return false;
	}

	String findAnotherFox(String st, State lastState){
		ArrayList<ArrayList<Integer>> lastB = lastState.getBoard();
		int x = Integer.valueOf(st) / 10;
		int y = Integer.valueOf(st) % 10;
		int anotherFox = 0;
		String anotherSt;
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 7; j++){
				if(lastB.get(x).get(y) == 1 && lastB.get(i).get(j) == 2)
					anotherFox = i * 10 + j;
				else if(lastB.get(x).get(y) == 2 && lastB.get(i).get(j) == 1){
					anotherFox = i * 10 + j;
				}
			}
		}		
		anotherSt = Integer.toString(anotherFox);
		return anotherSt;
	}
	
	int getHowManySheepHaveBeenEaten(List<Integer> newEaten){
		return newEaten.size();
	}
	
	int getHowManySheepHaveBeenArrived(List<Integer> newArrival){
		return newArrival.size();
	}	

	List<Operation> getMoveInitial(List<String> playerIds){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  1,  0,  2, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  0,  0,  0,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  5,  6,  7,  8,  9);
		List<Integer> row5 = Arrays.<Integer>asList(10, 11, 12, 13, 14, 15, 16);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));

	    String fPlayerId = playerIds.get(0);
	    String sPlayerId = playerIds.get(1);
		List<Operation> operations = Lists.newArrayList();
		//The order of operations: turn, Board, Is_Fox_Move, Is_Fox_Eat, F, S, EATEN, ARRIVAL
		operations.add(new SetTurn(fPlayerId));
		operations.add(new Set(Board, board));	
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
	    List<String> playerIds = verifyMove.getPlayerIds();
	
	    if (lastApiState.isEmpty()) {
	      return getMoveInitial(playerIds);
	    }
	   
	    String lastMovePlayerId = verifyMove.getLastMovePlayerId();
	    State lastState = gameApiStateToState(lastApiState,
	            Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);

		// 1) foxNormalMove
		// 2) doFoxNormalMove
		// 3) foxEatMove
		// 4) doFoxEatMove
		// 5) doSheepMove
//	    view.testButton4("" + lastMove.size());
		if(lastMove.contains(new Set(Is_Fox_Move, Yes))){
			//view.testButton1();
			return foxNormalMove(lastState, lastMove);
			//return new ArrayList<Operation>();
		}else if(lastMove.contains(new Delete(Is_Fox_Move))){
//			view.testButton2();
			return doFoxNormalMove(lastState);
		}else if(lastMove.contains(new Set(Is_Fox_Eat, Yes))){
//			view.testButton3();
			return foxEatMove(lastState, lastMove);
		}else if(lastMove.contains(new Delete(Is_Fox_Eat))){
//			view.testButton4("11111");
			return doFoxEatMove(lastState);
		}else{
//			view.testButton5();
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
	State gameApiStateToState(Map<String, Object> gameApiState,
			Color turnOfColor, List<String> playerIds){
		String fromValue = (String)gameApiState.get(From);
		String toValue = (String)gameApiState.get(To);
	    List<Integer> f = (List<Integer>) gameApiState.get(F);
	    List<Integer> s = (List<Integer>) gameApiState.get(S);
	    List<Integer> eaten = (List<Integer>) gameApiState.get(EATEN);
	    List<Integer> arrival = (List<Integer>) gameApiState.get(ARRIVAL);
	    ArrayList<ArrayList<Integer>> board = (ArrayList<ArrayList<Integer>>) gameApiState.get(Board);
	    ArrayList<ArrayList<Integer>> boardTemp = board;
	    
	    
	    return new State(
	        turnOfColor,
	        boardTemp,
	        ImmutableList.copyOf(playerIds),
	        gameApiState.containsKey(Is_Fox_Move),
	        gameApiState.containsKey(Is_Fox_Eat),
	        ImmutableList.copyOf(f),
	        ImmutableList.copyOf(s),
	        ImmutableList.copyOf(eaten),
	        ImmutableList.copyOf(arrival),
	    	fromValue,
	    	toValue);
	}
	
	private void check(boolean val, Object... debugArguments){
		if (!val){
			throw new RuntimeException("We have a hacker! debugArguments=" 
					+ Arrays.toString(debugArguments));
		}
	}
	
}