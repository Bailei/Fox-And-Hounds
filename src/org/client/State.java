package org.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.LinkedList;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class State {
	private final Color turn;
	private final ArrayList<ArrayList<Integer>> Board;
	private final ImmutableList<Integer> playerIds;
	private boolean Is_Fox_Move;
	private boolean Is_Fox_Eat;	
	private final ImmutableList<Integer> Fox;
	private final ImmutableList<Integer> Sheep;
	private final ImmutableList<Integer> EATEN;
	private final ImmutableList<Integer> ARRIVAL;	

	public State(Color turn, ArrayList<ArrayList<Integer>> Board, ImmutableList<Integer> playerIds, 
			boolean Is_Fox_Move, boolean Is_Fox_Eat,
			ImmutableList<Integer> Fox, ImmutableList<Integer> Sheep,
			ImmutableList<Integer> EATEN, ImmutableList<Integer> ARRIVAL) {
		super();
		this.turn = checkNotNull(turn);
		this.Board = checkNotNull(Board);
		this.playerIds = checkNotNull(playerIds);

		this.Fox = checkNotNull(Fox);
		this.Sheep = checkNotNull(Sheep);
		this.EATEN = checkNotNull(EATEN);
		this.ARRIVAL = checkNotNull(ARRIVAL);
		
		this.Is_Fox_Move = Is_Fox_Move;
		this.Is_Fox_Eat = Is_Fox_Eat;
	}
	
	public Color getTurn() {
		return turn;
	}

	public ImmutableList<Integer> getPlayerIds(){
		return playerIds;
	}
	
	public int getPlayerId(Color color){
		return playerIds.get(color.ordinal());
	}
	
	public ArrayList<ArrayList<Integer>> getBoard(){
		return Board;
	}
	
	public ImmutableList<Integer> getFox(){
		return Fox;
	}
	
	public ImmutableList<Integer> getSheep(){
		return Sheep;
	}
	
	public ImmutableList<Integer> getEATEN(){
		return EATEN;
	}
	
	public ImmutableList<Integer> getARRIVAL(){
		return ARRIVAL;
	}
	
	public boolean Is_Fox_Move(){
		return Is_Fox_Move;
	}
	
	public boolean Is_Fox_Eat(){
		return Is_Fox_Eat;
	}	
	
}
	

