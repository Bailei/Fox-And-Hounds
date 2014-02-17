package org.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class State {
	private final Color turn;
	private final ImmutableList<Integer> playerIds;
	private final ImmutableList<Piece> Fox;
	private final ImmutableList<Piece> Sheep;
	private final ImmutableList<Piece> EATEN;
	private final ImmutableList<Piece> ARRIVAL;
	private boolean Is_Fox_Move;
	private boolean Is_Fox_Eat;		
	private final ImmutableList<Piece> Board;

	public State(Color turn, ImmutableList<Piece> Board, ImmutableList<Integer> playerIds, 
			boolean Is_Fox_Move, boolean Is_Fox_Eat,
			ImmutableList<Piece> EATEN, ImmutableList<Piece> ARRIVAL,
			ImmutableList<Piece> Fox, ImmutableList<Piece> Sheep) {
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
	
	public ImmutableList<Piece> getBoard(){
		return Board;
	}
	
	public ImmutableList<Piece> getFox(){
		return Fox;
	}
	
	public ImmutableList<Piece> getSheep(){
		return Sheep;
	}
	
	public ImmutableList<Piece> getEATEN(){
		return EATEN;
	}
	
	public ImmutableList<Piece> getARRIVAL(){
		return ARRIVAL;
	}
	
	public boolean Is_Fox_Move(){
		return Is_Fox_Move;
	}
	
	public boolean Is_Fox_Eat(){
		return Is_Fox_Eat;
	}	

/*
	public Piece setPiece(Position position, Piece piece){
		setPiece(position.getIndex(), piece);
	}
	
	public Piece setPiece(int index, Piece piece){
		setPiece(index, piece);
	}
*/
	public Piece getPiece(Position position){
		return getPiece(position.getIndex());
	}
	
	public Piece getPiece(int index){
		return Board.get(index);
	}
}
	

