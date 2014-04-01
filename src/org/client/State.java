package org.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.LinkedList;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class State {
	private final Color turn;
	private final ArrayList<ArrayList<Integer>> Board;
	private final ImmutableList<String> playerIds;
	private boolean Is_Fox_Move;
	private boolean Is_Fox_Eat;	
	private final String From;
	private final String To;
	private final ImmutableList<Integer> Fox;
	private final ImmutableList<Integer> Sheep;
	private final ImmutableList<Integer> EATEN;
	private final ImmutableList<Integer> ARRIVAL;	

	public State(Color turn, ArrayList<ArrayList<Integer>> Board, ImmutableList<String> immutableList, 
			boolean Is_Fox_Move, boolean Is_Fox_Eat,
			ImmutableList<Integer> Fox, ImmutableList<Integer> Sheep,
			ImmutableList<Integer> EATEN, ImmutableList<Integer> ARRIVAL, String From, String To) {
		super();
		this.turn = checkNotNull(turn);
		this.Board = checkNotNull(Board);
		this.playerIds = checkNotNull(immutableList);
		this.From = From;
		this.To = To;

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

	public ImmutableList<String> getPlayerIds(){
		return playerIds;
	}
	
	public String getPlayerId(Color color){
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

	public String getFrom(){
		return From;
	}
	
	public String getTo(){
		return To;
	}
}
	

