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

	
	private void assertMoveOk(VerifyMove verifyMove){
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(new VerifyMoveDone(), verifyDone);
	}
	
	private void assertHacker(VerifyMove verifyMove){
		VerifyMoveDone verifyDone = new GameLogic().verify(verifyMove);
		assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker Found"), verifyDone);		
	}
	
	private final int wId = 41;
	private final int bId = 42;
	private final String playerId = "playerId";
	private final String turn = "turn";
	private static final String W = "W"; // White hand
	private static final String B = "B"; // Black hand
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(playerId, wId);
	private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(playerId, bId);
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");

	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
