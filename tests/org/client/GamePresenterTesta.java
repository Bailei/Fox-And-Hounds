package org.client;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.client.GamePresenter.View;
import org.client.GameApi.Container;
import org.client.GameApi.Delete;
import org.client.GameApi.Operation;
import org.client.GameApi.Set;
import org.client.GameApi.SetTurn;
import org.client.GameApi.UpdateUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Tests for {@link GamePresenter}.
 * Test plan:
 * There are several interesting states:
 * 1) empty state
 * 2) fox turn to want move state
 * 3) fox turn to want eat state
 * 4) fox turn to do move state
 * 5) fox turn to do eat state
 * 6) sheep turn to do move state
 * 7) game-over
 * There are several interesting yourPlayerId:
 * 1) fox player
 * 2) sheep player
 * 3) viewer
 * For each one of these states and for each yourPlayerId,
 * I will test what methods the presenters calls on the view and container.
 * In addition I will also test the interactions between the presenter and view, i.e.,
 * the view can call one of these methods:
 * 1) chooseNextPositionForFoxToMove
 * 2) chooseNextPositionForFoxToEat
 * 3) chooseNextPositionForSheepToMove
 * 4) MakeDoFoxNormalMove
 * 5) MakeDoFoxEatMove
 */
@RunWith(JUnit4.class)
public class GamePresenterTest {
	  /** The class under test. */
	  private GamePresenter gamePresenter;
	  private final GameLogic gameLogic = new GameLogic();
	  private View mockView;
	  private Container mockContainer;
	  
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
	  private static final String Fox_Move = "Fox_Move";
	  private static final String Fox_Eat = "Fox_Eat";
	  private static final String From = "From";
	  private static final String To = "To";
	  private static final String BOARD = "Board";
	  private static final String F = "F"; // FOX hand
	  private static final String S = "S"; // SHEEP hand 
	  private static final String EATEN = "EATEN";
	  private static final String ARRIVAL = "ARRIVAL";
	  private final int viewerId = GameApi.VIEWER_ID;
	  private final ImmutableList<Integer> playerIds = ImmutableList.of(fId, sId);
	  private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYER_ID, fId);
	  private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(PLAYER_ID, sId);
	  private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);

	  
	  /* The interesting states that I'll test. */
	  
	  //emptyState
	  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	  
	  //initialState
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
	  ImmutableMap<String, Object> initialState = ImmutableMap.<String, Object>builder()
			  .put(BOARD, boardForInitialState())
			  .put(F, ImmutableList.of(1, 2))
			  .put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			  .put(EATEN, ImmutableList.of())
			  .put(ARRIVAL, ImmutableList.of())
			  .build();  

	  //fox turn to want move state (13, 02)
	  private ArrayList<ArrayList<Integer>> boardForFoxWantMoveState() {
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
		  return board;
	  }
	  ImmutableMap<String, Object> FoxWantMoveState = ImmutableMap.<String, Object>builder()
			  .put(BOARD, boardForFoxWantMoveState())
			  .put(F, ImmutableList.of(1, 2))
			  .put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			  .put(EATEN, ImmutableList.of())
			  .put(ARRIVAL, ImmutableList.of(5))
			  .build();
	  
	  //fox turn to want eat state (24, 42)
	  private ArrayList<ArrayList<Integer>> boardForFoxWantEatState() {
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
		  return board;
	  }
	  ImmutableMap<String, Object> FoxWantEatState = ImmutableMap.<String, Object>builder()
			.put(BOARD, boardForFoxWantEatState())
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
	  
	  //fox turn to do move state (13, 02)
	  private ArrayList<ArrayList<Integer>> boardForFoxDoNormalMoveState() {
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
			return board;
		} 
		ImmutableMap<String, Object> FoxDoNormalMoveState = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Move, Yes)
				.put(BOARD, boardForFoxDoNormalMoveState())
				.put(From, "13")
				.put(To, "02")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
	  
	  
	  //fox turn to do eat state (24, 42)
	  private ArrayList<ArrayList<Integer>> boardForFoxDoEatMoveState() {
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
			return board;
		} 
	  ImmutableMap<String, Object> FoxDoEatMoveState = ImmutableMap.<String, Object>builder()
				.put(Is_Fox_Eat, Yes)
				.put(BOARD, boardForFoxDoEatMoveState())
				.put(From, "24")
				.put(To, "42")
				.put(F, ImmutableList.of(1, 2))
				.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
				.put(EATEN, ImmutableList.of())
				.put(ARRIVAL, ImmutableList.of(5))
				.build();
	  
	  //sheep turn to do move state	 (33, 23) 
	  private ArrayList<ArrayList<Integer>> boardForSheepDoNormalMoveState() {
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
		  return board;
	  } 
	  ImmutableMap<String, Object> SheepDoNormalMoveState = ImmutableMap.<String, Object>builder()
			.put(BOARD, boardForSheepDoNormalMoveState())
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of())
			.put(ARRIVAL, ImmutableList.of(5))
			.build();
	  
	  //fox win and game-over
	  private ArrayList<ArrayList<Integer>> boardForFoxWinState() {
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  0,  0,  2, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0, 11, 20,  0,  0,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 0,  0,  0,  1,  0, 15,  0);
			List<Integer> row5after = Arrays.<Integer>asList(10,  0,  0,  0,  0,  0, 16);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1,  0,  0, 19, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1,  0, 21, 22, -1, -1);		
			board.add(new ArrayList<Integer>(row1after));
			board.add(new ArrayList<Integer>(row2after));
			board.add(new ArrayList<Integer>(row3after));
			board.add(new ArrayList<Integer>(row4after));
			board.add(new ArrayList<Integer>(row5after));
			board.add(new ArrayList<Integer>(row6after));
			board.add(new ArrayList<Integer>(row7after));
		  return board;
	  } 
	  ImmutableMap<String, Object> FoxWinState = ImmutableMap.<String, Object>builder()
			.put(BOARD, boardForFoxWinState())
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(10, 11, 15, 16, 19, 20, 21, 22))
			.put(EATEN, ImmutableList.of(3, 4, 5, 6, 7, 8, 9, 12, 13, 14, 17, 18))
			.put(ARRIVAL, ImmutableList.of(20))
			.build();
	  
	  //sheep win and game-over
	  private ArrayList<ArrayList<Integer>> boardForSheepWinState() {
			ArrayList<ArrayList<Integer>> board = new ArrayList<ArrayList<Integer>>();
			List<Integer> row1after = Arrays.<Integer>asList(-1, -1,  3,  5, 12, -1, -1);
			List<Integer> row2after = Arrays.<Integer>asList(-1, -1,  4,  9,  7, -1, -1);
			List<Integer> row3after = Arrays.<Integer>asList( 0, 11, 20, 15, 14,  0,  0);
			List<Integer> row4after = Arrays.<Integer>asList( 0,  0,  1,  0,  2,  0, 17);
			List<Integer> row5after = Arrays.<Integer>asList(10,  0,  0,  0,  0,  0,  0);
			List<Integer> row6after = Arrays.<Integer>asList(-1, -1,  0,  0,  0, -1, -1);
			List<Integer> row7after = Arrays.<Integer>asList(-1, -1,  0,  0, 22, -1, -1);			
			board.add(new ArrayList<Integer>(row1after));
			board.add(new ArrayList<Integer>(row2after));
			board.add(new ArrayList<Integer>(row3after));
			board.add(new ArrayList<Integer>(row4after));
			board.add(new ArrayList<Integer>(row5after));
			board.add(new ArrayList<Integer>(row6after));
			board.add(new ArrayList<Integer>(row7after));
		  return board;
	  } 
	  ImmutableMap<String, Object> SheepWinState = ImmutableMap.<String, Object>builder()
			.put(BOARD, boardForSheepWinState())
			.put(F, ImmutableList.of(1, 2))
			.put(S, ImmutableList.of(3, 4, 5, 7, 9, 10, 12, 14, 15, 17, 20, 22))
			.put(EATEN, ImmutableList.of(6, 8, 11, 13, 16, 18, 19, 21))
			.put(ARRIVAL, ImmutableList.of(3, 5, 12, 4, 9, 7, 20, 15, 14))
			.build();
	  
	  @Before
	  public void runBefore() {
	    mockView = Mockito.mock(View.class);
	    mockContainer = Mockito.mock(Container.class);
	    gamePresenter = new GamePresenter(mockView, mockContainer);
	    verify(mockView).setPresenter(gamePresenter);
	  }

	  @After
	  public void runAfter() {
		  // This will ensure I didn't forget to declare any extra interaction the mocks have.
		  verifyNoMoreInteractions(mockContainer);
		  verifyNoMoreInteractions(mockView);
	  }
	  
	  @Test
	  public void testEmptyStateForW() {
		  gamePresenter.updateUI(createUpdateUI(fId, 0, emptyState));
		  verify(mockContainer).sendMakeMove(gameLogic.getMoveInitial(playerIds));
	  }
	  
	  @Test
	  public void testEmptyStateForB() {
		  gamePresenter.updateUI(createUpdateUI(sId, 0, emptyState));
	  }
	  
	  @Test
	  public void testEmptyStateForViewer() {
		  gamePresenter.updateUI(createUpdateUI(viewerId, 0, emptyState));
	  }

	 // fox want move
	  @Test
	  public void testFoxWantMoveStateForFoxTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, fId, FoxWantMoveState));		 
		  verify(mockView).setPlayerState(boardForFoxWantMoveState());
		  verify(mockView).chooseNextPositionForFox(ImmutableList.<String>of(), boardForFoxWantMoveState());
	  }
	  
	  @Test
	  public void testFoxWantMoveStateForSheepTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(sId, fId, FoxWantMoveState));
		  verify(mockView).setPlayerState(boardForFoxWantMoveState());
	  }
	  
	  @Test
	  public void testFoxWantMoveStateForViewerTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, fId, FoxWantMoveState));
		  verify(mockView).setViewerState(boardForFoxWantMoveState());
	  }
	  
	  // fox want eat
	  @Test
	  public void testFoxWantEatStateForFoxTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, fId, FoxWantEatState));		 
		  verify(mockView).setPlayerState(boardForFoxWantEatState());
		  verify(mockView).chooseNextPositionForFox(ImmutableList.<String>of(), boardForFoxWantEatState());
	  }
	  
	  @Test
	  public void testFoxWantEatStateForSheepTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(sId, fId, FoxWantEatState));
		  verify(mockView).setPlayerState(boardForFoxWantEatState());
	  }
	  
	  @Test
	  public void testFoxWantEatStateForViewerTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, fId, FoxWantEatState));
		  verify(mockView).setViewerState(boardForFoxWantEatState());
	  }
	  
	  // fox do move
	  @Test
	  public void testFoxDoNormalMoveStateForFoxTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, fId, FoxDoNormalMoveState));
		  verify(mockView).setPlayerState(boardForFoxDoNormalMoveState());
		  verify(mockContainer).sendMakeMove(gameLogic.doFoxNormalMove(gameLogic.gameApiStateToState(FoxDoNormalMoveState, Color.F , playerIds)));
	  }
	  
	  @Test
	  public void testFoxDoNormalMoveStateForSheepTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(sId, fId, FoxDoNormalMoveState));
		  verify(mockView).setPlayerState(boardForFoxDoNormalMoveState());
	  }
	  
	  @Test
	  public void testFoxDoNormalMoveStateForViewerTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, fId, FoxDoNormalMoveState));
		  verify(mockView).setViewerState(boardForFoxDoNormalMoveState());
	  }
	  
	  // fox do eat
	  @Test
	  public void testFoxDoEatMoveStateForFoxTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, fId, FoxDoEatMoveState));
		  verify(mockView).setPlayerState(boardForFoxDoEatMoveState());
		  State state = gameLogic.gameApiStateToState(FoxDoEatMoveState, Color.F , playerIds);
		  List<Operation> lastMove = gameLogic.doFoxEatMove(state);
		  verify(mockContainer).sendMakeMove(lastMove);
	  }
	  
	  @Test
	  public void testFoxDoEatMoveStateForSheepTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(sId, fId, FoxDoNormalMoveState));
		  verify(mockView).setPlayerState(boardForFoxDoEatMoveState());
	  }
	  
	  @Test
	  public void testFoxDoEatMoveStateForViewerTurnOfFox(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, fId, FoxDoNormalMoveState));
		  verify(mockView).setViewerState(boardForFoxDoEatMoveState());
	  }
	  
	  // sheep do move
	  @Test
	  public void testSheepDoNormalMoveStateForFoxTurnOfSheep(){
		  gamePresenter.updateUI(createUpdateUI(fId, sId, SheepDoNormalMoveState));		 
		  verify(mockView).setPlayerState(boardForSheepDoNormalMoveState());
	  }
	  
	  @Test
	  public void testSheepDoNormalMoveStateForSheepTurnOfSheep(){
		  gamePresenter.updateUI(createUpdateUI(sId, sId, SheepDoNormalMoveState));	
		  verify(mockView).setPlayerState(boardForSheepDoNormalMoveState());
		  verify(mockView).chooseNextPositionForSheep(ImmutableList.<String>of(), boardForSheepDoNormalMoveState());
	  }
	  
	  @Test
	  public void testSheepDoNormalMoveStateForViewerTurnOfSheep(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, sId, SheepDoNormalMoveState));		 
		  verify(mockView).setViewerState(boardForSheepDoNormalMoveState());
	  }
	  
	  //fox win and game-over
	  @Test
	  public void testFoxWinStateForFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, fId, FoxWinState));	
		  verify(mockView).setPlayerState(boardForFoxWinState());
	  }
	  
	  @Test
	  public void testFoxWinStateForSheep(){
		  gamePresenter.updateUI(createUpdateUI(sId, fId, FoxWinState));	
		  verify(mockView).setPlayerState(boardForFoxWinState());		  
	  }
	  
	  @Test
	  public void testFoxWinStateForViewer(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, fId, FoxWinState));	
		  verify(mockView).setViewerState(boardForFoxWinState());		  
	  }
	  
	  //sheep win and game-over
	  @Test
	  public void testSheepWinStateForFox(){
		  gamePresenter.updateUI(createUpdateUI(fId, sId, SheepWinState));	
		  verify(mockView).setPlayerState(boardForSheepWinState());		  
	  }
	  
	  @Test
	  public void testSheepWinStateForSheep(){
		  gamePresenter.updateUI(createUpdateUI(sId, sId, SheepWinState));	
		  verify(mockView).setPlayerState(boardForSheepWinState());		  
	  }
	  
	  @Test
	  public void testSheepWinStateForViewer(){
		  gamePresenter.updateUI(createUpdateUI(viewerId, sId, SheepWinState));	
		  verify(mockView).setViewerState(boardForSheepWinState());		  
	  }
	  
	  private UpdateUI createUpdateUI(
			  int yourPlayerId, int turnOfPlayerId, Map<String, Object> state) {
		    	// Our UI only looks at the current state
		  		// (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
		    	return new UpdateUI(yourPlayerId, playersInfo, state,
		        emptyState, // we ignore lastState
		        ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
		        0,
		        ImmutableMap.<Integer, Integer>of());
	  }
}
