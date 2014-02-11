package org.client;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

//import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.client.GameApi.Delete;
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
	GameLogic gameLogic = new GameLogic();

	private void assertMoveOk(VerifyMove verifyMove){
		gameLogic.checkMoveIsLegal(verifyMove);
	}
	
	private void assertHacker(VerifyMove verifyMove){
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());		
	}
	
	private static final String PLAYER_ID = "playerId";
	
	private final int wId = 1;
	private final int bId = 2;
	private final String turn = "turn";
	private static final String W = "W"; // White hand
	private static final String B = "B"; // Black hand
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(playerId, wId);
	private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(playerId, bId);
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");

	private VerifyMove move(
			int lastMovePlayerId, 
			Map<String, Object> lastState, 
			List<Operation> lastMove){
		return new VerifyMove(wId, playersInfo, emptyState, lastState, lastMove, lastMovePlayerId);
	}
	
	private List<Operation> getInitialOperation(){
		return gameLogic.getInitialMove(wId, bId);
	}
	
	@Test
	public void testInitialMove() {
		assertMoveOk(move(wId, emptyState, getInitialOperation()));
	}

	@Test
	public void testInitialMoveByWrongPlayer(){
		assertHacker(move(bId, emptyState, getInitialOperation()));
	}
	
	@Test
	public void testInitialMoveFromNonEmptyState(){
		assertHacker(move(wId, nonEmptyState, getInitialOperation()));
	}
	
	@Test
	public void testInitialMoveWithExtroOperation(){
		List<Operation> initialOperations = getInitialOperation();
		initialOperations.add(new Set("S8", "SHEEP"));
		assertHacker(move(wId, emptyState, getInitialOperation()));
	}
	
	@Test
}
