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

/*
	private void assertMoveOk(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
			assertEquals(new VerifyMoveDone(), verifyDone);
	}

	private void assertHacker(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
			assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker found"), verifyDone);
	}
*/
	
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

/*
	private List<Operation> getInitialOperation(){
		return gameLogic.getInitialMove(fId, sId);
	}
*/
	
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
	
	private final ImmutableMap<String, Object> turnOfFoxInOneState = ImmutableMap.<String, Object>of(
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

	private final ImmutableMap<String, Object> turnOfSheepInOneState = ImmutableMap.<String, Object>of(
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
		new Set(TURN, S),
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

	//fox2 eat sheep6 but sheep6 still alive (lastState: turnofF)
	private final ImmutableList<Operation> foxMoveToEatSheepButSheepAlive = ImmutableList.<Operation>of(
		new Set(TURN, S),
		new Set(BOARD, ImmutableList.of(
				-1, -1,  0,  0,  0, -1, -1,
				-1, -1,  0,  1,  0, -1, -1,
				 0,  0,  5,  0,  0,  0,  0,
				 3,  4, 12,  6,  7,  8,  9,
				10, 11,  2, 13,  14, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1)),		
		new Set(F, ImmutableList.of(1, 2)),
		new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
		new Set(EATEN, ImmutableList.of()));
	
	//fox2 eat sheep7 by wrong move (lastState: turnofF)
	private final ImmutableList<Operation> foxMoveToEatSheepByWrongMove = ImmutableList.<Operation>of(
		new Set(TURN, S),
		new Set(BOARD, ImmutableList.of(
				-1, -1,  0,  0,  0, -1, -1,
				-1, -1,  0,  1,  0, -1, -1,
				 0,  0,  5,  0,  0,  0,  0,
				 3,  4, 12,  6,  0,  8,  9,
				10, 11,  0, 13,  2, 15, 16,
				-1, -1, 17, 18, 19, -1, -1,
				-1, -1, 20, 21, 22, -1, -1)),		
		new Set(F, ImmutableList.of(1, 2)),
		new Set(S, ImmutableList.of(3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
		new Set(EATEN, ImmutableList.of(7)));

	//fox1 move forward  (lastState: turnofF)
	private final ImmutableList<Operation> fNormalForwardMove = ImmutableList.<Operation>of(
			new Set(TURN, S),
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
	private final ImmutableList<Operation> fNormalBackwardMove = ImmutableList.<Operation>of(
			new Set(TURN, S),
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
	private final ImmutableList<Operation> fNormalDiagonalMove = ImmutableList.<Operation>of(
			new Set(TURN, S),
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
			new Set(TURN, F),
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
			new Set(TURN, F),
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

	//sheep6 move forward two squares(lastState: turnofS)
	private final ImmutableList<Operation> sWrongTwoSquaresMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  5,  0,  0, -1, -1,
					-1, -1,  0,  1,  0, -1, -1,
					 0,  0,  0,  0,  2,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	//sheep6 move backwards(lastState: turnofS)
	private final ImmutableList<Operation> sWrongBackwardsMove = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  1,  0, -1, -1,
					 0,  0,  5,  0,  2,  0,  0,
					 3,  4,  0,  6,  7,  8,  9,
					10, 11,  12, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),		
			new Set(F, ImmutableList.of(1, 2)),
			new Set(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of()));
	
	//fox2 move out of the board
	private final ImmutableList<Operation> fMoveOutOfBoard = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  1,  0,  2, -1,
					 0,  0,  5,  0,  0,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, -1, 17, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),
			new Set(F, ImmutableList.of(1, 2)),
			new Set(F, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of())
			);	
	
	//sheep17 move out of the board
	private final ImmutableList<Operation> sMoveOutOfBoard = ImmutableList.<Operation>of(
			new Set(TURN, F),
			new Set(BOARD, ImmutableList.of(
					-1, -1,  0,  0,  0, -1, -1,
					-1, -1,  0,  1,  0, -1, -1,
					 0,  0,  5,  0,  0,  0,  0,
					 3,  4, 12,  6,  7,  8,  9,
					10, 11,  0, 13, 14, 15, 16,
					-1, 17,  0, 18, 19, -1, -1,
					-1, -1, 20, 21, 22, -1, -1)),
			new Set(F, ImmutableList.of(1, 2)),
			new Set(F, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)),
			new Set(EATEN, ImmutableList.of())
			);
	
	@Test
	public void testInitialMove() {
		VerifyMove verifyMove = move(fId, emptyState, initialMoveByF);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());
	}

	@Test
	public void testInitialMoveByWrongPlayer(){
		VerifyMove verifyMove = move(sId, emptyState, initialMoveByF);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
//		assertHacker(move(sId, emptyState, getInitialOperation()));
	}
	
	@Test
	public void testInitialMoveFromNonEmptyState(){
		VerifyMove verifyMove = move(fId, nonEmptyState, initialMoveByF);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
//		assertHacker(move(fId, nonEmptyState, getInitialOperation()));
	}
	
	@Test
	public void testInitialMoveWithExtroOperation(){
		VerifyMove verifyMove = move(fId, emptyState, fNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}

/*
	@Test
	public void testPieceToString(){
		
	}
*/
	
	@Test
	public void testNormalForwardMoveByFox(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, fNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testNormalBackwardMoveByFox(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, fNormalBackwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testNormalDiagonalMoveByFox(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, fNormalDiagonalMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testFoxMoveToEatSheep(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, foxMoveToEatSheep);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testFoxEatSheepButSheepNotBeEatten(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, foxMoveToEatSheepButSheepAlive);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testWrongMovebyFoxToEatSheep(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, foxMoveToEatSheepByWrongMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}

	@Test
	public void testNormalForwardMoveBySheep(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, sNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testNormalSidesWayMoveBySheep(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, sNormalSidesWayMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(0, verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testWrongTwoSquaresForwardMove(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, sWrongTwoSquaresMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());			
	}
	
	@Test
	public void testWrongBackwardsMoveBySheep(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, sWrongBackwardsMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testMoveWithWrongPiece1(){
		VerifyMove verifyMove = move(fId, turnOfFoxInOneState, fNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testMoveWithWrongPiece2(){
		VerifyMove verifyMove = move(fId, turnOfFoxInOneState, sNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testMoveWithWrongPiece3(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, fNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testMoveWithWrongPiece4(){
		VerifyMove verifyMove = move(sId, turnOfSheepInOneState, sNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testMoveWithWrongPiece5(){
		VerifyMove verifyMove = move(sId, turnOfSheepInOneState, fNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testMoveWithWrongPiece6(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, sNormalForwardMove);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}
	
	@Test
	public void testFoxMoveOutofBoard(){
		VerifyMove verifyMove = move(sId, turnOfFoxInOneState, fMoveOutOfBoard);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testSheepMoveOutofBoard(){
		VerifyMove verifyMove = move(fId, turnOfSheepInOneState, sMoveOutOfBoard);
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);	
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
	
	@Test
	public void testFoxHasToMoveIfCanEat(){
		
	}
	
	@Test
	public void testTwoFoxMoveAtOneTime(){
		
	}
	
	@Test
	public void testTwoSheepMoveAtOneTime(){
		
	}
	
	@Test
	public void test(){
		
	}
	
	@Test
	public void testEndGame(){	
		
	}
}
