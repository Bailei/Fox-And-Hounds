package org.client;


public class Piece {
	private Color color;
	private PieceKind kind;
	
	public Piece(Color color, PieceKind kind){
		this.color = color;
		this.kind = kind;
	}
	
	public Color getColor(){
		return color;
	}
	
	public PieceKind getKind(){
		return kind;
	}
}

