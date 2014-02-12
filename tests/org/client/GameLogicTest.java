package org.client;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;



@RunWith(JUnit4.class)
public class GameLogicTest {
	/** The object under test. */
	GameLogic gameLogic = new GameLogic();

	private void assertMoveOk(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
			assertEquals(new VerifyMoveDone(), verifyDone);
	}

	private void assertHacker(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
			assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker found"), verifyDone);
	}
	
/*
	private void assertMoveOk(VerifyMove verifyMove){
		gameLogic.checkMoveIsLegal(verifyMove);
	}
	
	private void assertHacker(VerifyMove verifyMove){
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
*/
	private static final String PLAYER_ID = "playerId";
	
	private final int fId = 1;
	private final int sId = 2;
	private final String TURN = "TURN";
	private static final String F = "F"; // FOX hand
	private static final String S = "S"; // SHEEP hand
	private static final String BOARD = "board"; 
	private static final String EATEN = "EATEN";
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYER_ID, fId);
	private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(PLAYER_ID, sId);
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
	
	private VerifyMove move(
			int lastMovePlayerId, 
			Map<String, Object> lastState, 
			List<Operation> lastMove){
		return new VerifyMove(fId, playersInfo, emptyState, lastState, lastMove, lastMovePlayerId);
	}
	
	private List<Operation> getInitialOperation(){
		return gameLogic.getInitialMove(fId, sId);
	}
	
	private final ImmutableMap<String, Object> initialState = ImmutableMap.<String, Object>of(
		TURN, F,
		BOARD, ImmutableList.of(
				-1, -1,  1,  0,  2, -1, -1,
				-1, -1,  0,  0,  0, -1, -1,
				 0,  0,  0,  0,  0,  0,  0,
				 3,  4,  5,  6,  7,  8,  9,
				10, 11, 12, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1),
		F, ImmutableList.of(1, 2),
		S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22),
		EATEN, ImmutableList.of());
	
	private final ImmutableMap<String, Object> turnOfF = ImmutableMap.<String, Object>of(
		TURN, F,
		BOARD, ImmutableList.of(
				-1, -1,  0,  0,  0, -1, -1,
				-1, -1,  0,  1,  0, -1, -1,
				 0,  0,  5,  0,  2,  0,  0,
				 3,  4, 12,  6,  7,  8,  9,
				10, 11,  0, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1),
		F, ImmutableList.of(1, 2),
		S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22),
		EATEN, ImmutableList.of());

	private final ImmutableMap<String, Object> turnOfS = ImmutableMap.<String, Object>of(
		TURN, S,
		BOARD, ImmutableList.of(
				-1, -1,  0,  0,  0, -1, -1,
				-1, -1,  0,  1,  0, -1, -1,
				 0,  0,  5,  0,  2,  0,  0,
				 3,  4, 12,  6,  7,  8,  9,
				10, 11,  0, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1),		
		F, ImmutableList.of(1, 2),
		S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22),
		EATEN, ImmutableList.of());
	
	private final ImmutableList<Operation> initialMoveByF = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
				-1, -1,  1,  0,  2, -1, -1,
				-1, -1,  0,  0,  0, -1, -1,
				 0,  0,  0,  0,  0,  0,  0,
				 3,  4,  5,  6,  7,  8,  9,
				10, 11, 12, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1)),
			new Set(F, ImmutableList.of(1, 2)),
			new Set(F, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of())
			);
	
	//fox2 eat sheep6 (lastState: turnofF)
	private final ImmutableList<Operation> foxMoveToEatSheep = ImmutableList.<Operation>of(
		new Set(TURN, F),
		new Set(BOARD, ImmutableList.of(
				-1, -1,  0,  0,  0, -1, -1,
				-1, -1,  0,  1,  0, -1, -1,
				 0,  0,  5,  0,  0,  0,  0,
				 3,  4, 12,  0,  7,  8,  9,
				10, 11,  2, 13, 14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1)),		
		new Set(F, ImmutableList.of(1, 2)),
		new Set(S, ImmutableList.of(3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
		new Set(EATEN, ImmutableList.of(6)));

	//fox1 move forward  (lastState: turnofF)
	private final ImmutableList<Operation> fNormalForwardMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  0,  0, -1, -1,
					 0,  0,  5,  1,  2,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));

	//fox1 move backward  (lastState: turnofF)
	private final ImmutableList<Operation> fNormaBackwardMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  1,  0, -1, -1,
					-1, -1,  0,  0,  0, -1, -1,
					 0,  0,  5,  0,  2,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	//fox1 move diagonal (lastState: turnofF)
	private final ImmutableList<Operation> fNormaBackwardMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  1,  0,  0, -1, -1,
					-1, -1,  0,  0,  0, -1, -1,
					 0,  0,  5,  0,  2,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	//sheep6 move forward (lastState: turnofS)
	private final ImmutableList<Operation> sNormalForwardMove = ImmutableList.<Operation>of(
			new Set(TURN, S),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  1,  0, -1, -1,
					 0,  0,  5,  6,  2,  0,  0,
					 3,  4, 12,  0,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	//sheep6 move sideways (lastState: turnofS)
	private final ImmutableList<Operation> sNormalSidesWayMove = ImmutableList.<Operation>of(
			new Set(TURN, S),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  1,  0, -1, -1,
					 0,  5,  0,  0,  2,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	@Test
	public void testInitialMove() {
		VerifyMove verifyMove = move(fId, emptyState, initialMoveByF);
		
	}

	@Test
	public void testInitialMoveByWrongPlayer(){
		assertHacker(move(sId, emptyState, getInitialOperation()));
	}
	
	@Test
	public void testInitialMoveFromNonEmptyState(){
		assertHacker(move(fId, nonEmptyState, getInitialOperation()));
	}
	
	@Test
	public void testNormalMoveByWhite(){
//		assertHacker(move(fId, turnOfF, MoveOfF));
	}
	
	@Test
	public void testNormalMoveByBlack(){
//		assertHacker(move(fId, turnOfS, MoveOfS));
	}
	
	@Test
	public void testInitialMoveWithExtroOperation(){
		List<Operation> initialOperations = getInitialOperation();

	}
	
	@Test
	public void testPieceToString(){
		
	}
	
	@Test
	public void testNormalMoveByFox(){
		
	}
	
	@Test
	public void testWrongMovebyFox(){
		
	}

	@Test
	public void testNormalMoveBySheep(){
		
	}
	
	@Test
	public void testWrongBackwardsMoveBySheep(){
		
	}
	
	@Test
	public void testWrongForwardsMoveBySheep(){
		
	}
	
	@Test
	public void testNormalEatByFox(){
		
	}
	
	@Test
	public void testWrongEatByFox(){
		
	}
	
	@Test
	public void testWrongEatBySheep(){
		
	}
	
	@Test
	public void testMoveWithWrongPiece(){
		
	}
	
	@Test
	public void testMoveOutofBoard(){
		
	}
	
	@Test
	public void testEndGame(){
		
	}
	

}
