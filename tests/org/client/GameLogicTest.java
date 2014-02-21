package org.client;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


@RunWith(JUnit4.class)
public class GameLogicTest {
	/** The object under test. */
	GameLogic gameLogic = new GameLogic();
	
	private void assertMoveOk(VerifyMove verifyMove) {
		gameLogic.checkMoveIsLegal(verifyMove);
	}

	private void assertHacker(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = gameLogic.verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	/* The entries used in the game are:
	*   Is_Fox_Move:Yes, Is_Fox_Eat:Yes, From, To, Board, F, S, EATEN, ARRIVAL
	* When we send operations on these keys, it will always be in the above order.
	*/		
	private static final String PLAYER_ID = "playerId";
	private final int fId = 0;
	private final int sId = 1;
	private final String TURN = "TURN";
	private static final String Is_Fox_Move = "Is_Fox_Move";
	private static final String Is_Fox_Eat = "Is_Fox_Eat";
	private static final String Yes = "Yes";
	private static final String From = "From";
	private static final String To = "To";
	private static final String BOARD = "Board";
	private static final String F = "F"; // FOX hand
	private static final String S = "S"; // SHEEP hand 
	private static final String EATEN = "EATEN";
	private static final String ARRIVAL = "ARRIVAL";
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYER_ID, fId);
	private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(PLAYER_ID, sId);
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
	
	private VerifyMove move(
			int lastMovePlayerId, 
			Map<String, Object> lastState, 
			List<Operation> lastMove){
		return new VerifyMove(playersInfo, emptyState, lastState, lastMove, lastMovePlayerId, ImmutableMap.<Integer, Integer>of());
	}
	
	private ArrayList<ArrayList<Integer>> boardForInitialState() {
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
		return board;
	}
	
	private final ImmutableList<Operation> initialMoveByF = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(BOARD, boardForInitialState()));
	
	@Test
	public void testInitialMove() {
		assertMoveOk(move(fId, emptyState, initialMoveByF));
	}

	@Test
	public void testInitialMoveByWrongPlayer(){
		assertHacker(move(sId, emptyState, initialMoveByF));
	}
	
	@Test
	public void testInitialMoveFromNonEmptyState(){
		assertHacker(move(fId, nonEmptyState, initialMoveByF));
	}
	
	ImmutableList<Operation> fNormalForwardMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(Is_Fox_Move, Yes),
			new Set(From, "13"),
			new Set(To, "03"));	
	@Test
	public void testInitialMoveWithExtroOperation(){
		assertHacker(move(fId, emptyState, fNormalForwardMove));
	}
	
	@Test
	public void testFox1WantNormalMoveInWrongState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(BOARD, board)
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(fId),
				new Set(Is_Fox_Move, Yes),
				new Set(From, "13"),
				new Set(To, "03"));		
		assertHacker(move(fId, state, operations));
		assertHacker(move(sId, state, operations));			
}
	
	@Test
	public void testFox2WantNormalMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(BOARD, board)
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(fId),
				new Set(Is_Fox_Move, Yes),
				new Set(From, "24"),
				new Set(To, "23"));
						
		assertMoveOk(move(fId, state, operations));
		assertHacker(move(sId, state, operations));	
}
	
	@Test
	public void testFox2DoNormalForwardMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  1,  2,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(From, "13")
				.put(To, "23")
				.put(BOARD, board)
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter));
									
		assertMoveOk(move(fId, state, operations));
		assertHacker(move(sId, state, operations));	
}
	
	@Test
	public void testFoxDoNormalBackwardMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(BOARD, board)
				.put(From, "13")
				.put(To, "03")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter));
									
		assertMoveOk(move(fId, state, operations));
		assertHacker(move(sId, state, operations));	
}
	@Test
	public void testFox2DoNormalDiagonalMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  1,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  12, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(BOARD, board)
				.put(From, "13")
				.put(To, "02")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter));
									
		assertMoveOk(move(fId, state, operations));
		assertHacker(move(sId, state, operations));	
}
	
	@Test
	public void testFox1WantEatMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(BOARD, board)
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(fId),
				new Set(Is_Fox_Eat, Yes),
				new Set(From, "24"),
				new Set(To, "42"));		
		
		assertMoveOk(move(fId, state, operations));		
}	
	@Test
	public void testFox2DoEatMoveInRightStateButCanNotContinullyEat(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 3,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 0,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 3,  0,  5,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 0,  4, 12,  0,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  2, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Eat, Yes)
				.put(BOARD, board)
				.put(From, "24")
				.put(To, "42")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Eat),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of(6)),
				new Set(ARRIVAL, ImmutableList.of(5)));
		
		assertMoveOk(move(fId, state, operations));
}
	@Test
	public void testFox2DoEatMoveInRightStateAndCanContinullyEat(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  0,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  2, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Eat, Yes)
				.put(BOARD, board)
				.put(From, "24")
				.put(To, "42")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(fId),
				new Delete(Is_Fox_Eat),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of(6)),
				new Set(ARRIVAL, ImmutableList.of(5)));
		
		assertMoveOk(move(fId, state, operations));
}
	
	@Test
	public void testFox2DoEatMoveInRightStateButWrongContinuelyEat(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  0,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  2, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Eat, Yes)
				.put(BOARD, board)
				.put(From, "24")
				.put(To, "42")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Eat),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of(6)),
				new Set(ARRIVAL, ImmutableList.of(5)));
		
		assertHacker(move(fId, state, operations));
}
	
	@Test
	public void testFox2DoWrongEatMoveInRightState(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  6,  0,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  0, 13,  2, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(BOARD, board)
				.put(From, "24")
				.put(To, "44")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of(7)));
		
		assertHacker(move(fId, state, operations));
}
	@Test
	public void testFox2DoWrongEatMoveInRightStateButSheepNotBeEatten(){
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
			List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
			List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1));
			board.add(new ArrayList<Integer>(row2));
			board.add(new ArrayList<Integer>(row3));
			board.add(new ArrayList<Integer>(row4));
			board.add(new ArrayList<Integer>(row5));
			board.add(new ArrayList<Integer>(row6));
			board.add(new ArrayList<Integer>(row7));
			
			ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
			List<Integer> row5after = Arrays.<Integer>asList(10, 11,  2, 13, 14, 15, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
			boardAfter.add(new ArrayList<Integer>(row1after));
			boardAfter.add(new ArrayList<Integer>(row2after));
			boardAfter.add(new ArrayList<Integer>(row3after));
			boardAfter.add(new ArrayList<Integer>(row4after));
			boardAfter.add(new ArrayList<Integer>(row5after));
			boardAfter.add(new ArrayList<Integer>(row6after));
			boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(BOARD, board)
				.put(From, "24")
				.put(To, "42")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of()));
		ImmutableList<Operation> operations1 = ImmutableList.<Operation>of(
				new SetTurn(sId),
				new Delete(Is_Fox_Move),
				new Set(BOARD, boardAfter),
				new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22)),
				new Set(EATEN, ImmutableList.of(6)));
		
		assertHacker(move(fId, state, operations));
		assertHacker(move(fId, state, operations1));
}

	@Test
	public void testNormalForwardMoveBySheep(){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  2, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
		List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));
		
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  2, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  6,  0,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  0,  7,  8,  9);
		List<Integer> row5after = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
		List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
		
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(From, "33"),
			new Set(To, "23"),
			new Set(BOARD, boardAfter),
			new Set(ARRIVAL, ImmutableList.of(5, 6)));
		
		assertMoveOk(move(sId, state, operations));
	}
	
	@Test
	public void testNormalSidesWayMoveBySheep(){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  2, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
		List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));
		
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  2, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0,  5,  0,  0,  0,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
		List<Integer> row5after = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
		List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
		
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(From, "22"),
			new Set(To, "21"),
			new Set(BOARD, boardAfter),
			new Set(ARRIVAL, ImmutableList.of()));	
		
		assertMoveOk(move(sId, state, operations));
	}
	
	@Test
	public void testWrongBackwardsMoveBySheep(){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  2, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  0,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4, 12,  6,  7,  8,  9);
		List<Integer> row5 = Arrays.<Integer>asList(10, 11,  0, 13, 14, 15, 16);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));
		
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
		List<Integer> row5after = Arrays.<Integer>asList(10, 11, 12, 13, 14, 15, 16);
		List<Integer> row6after = Arrays.<Integer>asList(-1, -1, 17, 18, 19, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
		
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(BOARD, boardAfter));	
		
		assertHacker(move(sId, state, operations));	
	}
	
	@Test
	public void testFoxWantMoveButOutofBoard(){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
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
	
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
	
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(Is_Fox_Move, Yes),
			new Set(From, "24"),
			new Set(To, "15"));		
	
		assertHacker(move(fId, state, operations));		
	}
	
	@Test
	public void testSheepMoveOutofBoard(){
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
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
	
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  1,  0, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0,  0,  5,  0,  2,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 3,  4,  0,  6,  7,  8,  9);
		List<Integer> row5after = Arrays.<Integer>asList(10, 11, 12, 13, 14, 15, 16);
		List<Integer> row6after = Arrays.<Integer>asList(-1, 17,  0, 18, 19, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1, 20, 21, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
		
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
		
		//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(BOARD, boardAfter));	
		
		assertHacker(move(sId, state, operations));	
	}
	
	@Test
	public void testFoxWonAndEndGame(){	
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  0,  0,  2, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0, 11, 20,  0,  0,  0,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 0,  0,  0,  0,  0, 15,  0);
		List<Integer> row5 = Arrays.<Integer>asList(10,  0,  0, 18,  0,  0, 16);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1,  0,  1, 19, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1,  0, 21, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));
		
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  0,  2, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0, 11, 20,  0,  0,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 0,  0,  0,  1,  0, 15,  0);
		List<Integer> row5after = Arrays.<Integer>asList(10,  0,  0,  0,  0,  0, 16);
		List<Integer> row6after = Arrays.<Integer>asList(-1, -1,  0,  0, 19, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1,  0, 21, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
	
	ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(Is_Fox_Eat, Yes)
			.put(BOARD, board)
			.put(From, "53")
			.put(To, "33")
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(10, 11, 15, 16, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 12, 13, 14, 17))
			.put(ARRIVAL, ImmutableList.of(20))
			.build();
	//The order of operations: turn, Is_Fox_Move, Is_Fox_Eat, From, To, Board, F, S, EATEN, ARRIVAL
	ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(sId),
			new Delete(Is_Fox_Eat),
			new Set(BOARD, boardAfter),
			new Set(S, ImmutableList.of(10, 11, 15, 16, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 12, 13, 14, 17, 18)),
			new Set(ARRIVAL, ImmutableList.of(20)),
			new EndGame(fId));
			assertMoveOk(move(fId, state, operations));	
	}
	
	@Test
	public void testSheepWonAndEndGame(){	
		ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1 = Arrays.<Integer>asList(-1, -1,  3,  5, 12, -1, -1);
		List<Integer> row2 = Arrays.<Integer>asList(-1, -1,  4,  9,  7, -1, -1);
		List<Integer> row3 = Arrays.<Integer>asList( 0, 11, 20, 15,  0, 14,  0);
		List<Integer> row4 = Arrays.<Integer>asList( 0,  0,  1,  0,  2,  0, 17);
		List<Integer> row5 = Arrays.<Integer>asList(10,  0,  0,  0,  0,  0,  0);
		List<Integer> row6 = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row7 = Arrays.<Integer>asList(-1, -1,  0,  0, 22, -1, -1);		
		board.add(new ArrayList<Integer>(row1));
		board.add(new ArrayList<Integer>(row2));
		board.add(new ArrayList<Integer>(row3));
		board.add(new ArrayList<Integer>(row4));
		board.add(new ArrayList<Integer>(row5));
		board.add(new ArrayList<Integer>(row6));
		board.add(new ArrayList<Integer>(row7));
		
		ArrayList<ArrayList<Integer>> boardAfter = new ArrayList<ArrayList<Integer>>();
		List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  3,  5, 12, -1, -1);
		List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  4,  9,  7, -1, -1);
		List<Integer> row3after = Arrays.<Integer>asList( 0, 11, 20, 15, 14,  0,  0);
		List<Integer> row4after = Arrays.<Integer>asList( 0,  0,  1,  0,  2,  0, 17);
		List<Integer> row5after = Arrays.<Integer>asList(10,  0,  0,  0,  0,  0,  0);
		List<Integer> row6after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
		List<Integer> row7after = Arrays.<Integer>asList(-1, -1,  0,  0, 22, -1, -1);		
		boardAfter.add(new ArrayList<Integer>(row1after));
		boardAfter.add(new ArrayList<Integer>(row2after));
		boardAfter.add(new ArrayList<Integer>(row3after));
		boardAfter.add(new ArrayList<Integer>(row4after));
		boardAfter.add(new ArrayList<Integer>(row5after));
		boardAfter.add(new ArrayList<Integer>(row6after));
		boardAfter.add(new ArrayList<Integer>(row7after));
	
		ImmutableMap<String, Object> state = ImmutableMap.<String, Object>builder()
			.put(BOARD, board)
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 7, 9, 10, 12, 14, 15, 17, 20, 22))
			.put(EATEN, ImmutableList.of(6, 8, 11, 13, 16, 18, 19, 21))
			.put(ARRIVAL, ImmutableList.of(3, 5, 12, 4, 9, 7, 20, 15))
			.build();
		
		ImmutableList<Operation> operations = ImmutableList.<Operation>of(
			new SetTurn(fId),
			new Set(From, "25"),
			new Set(To, "24"),
			new Set(BOARD, boardAfter),
			new Set(ARRIVAL, ImmutableList.of(3, 5, 12, 4, 9, 7, 20, 15, 14)),
			new EndGame(sId));
			
		assertMoveOk(move(sId, state, operations));			
	}
}
