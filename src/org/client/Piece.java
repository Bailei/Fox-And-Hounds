package org.client;


public class Piece {
	private Color color;
	private PieceKind kind;
	
	//判断棋子是否在进入paddock area
	//private boolean pieceArrive = false;
	
	public Piece(Color color, PieceKind kind){
		this.color = color;
		this.kind = kind;
	}
	
/*
	public Piece(Color color, PieceKind kind, boolean pieceArrive){
		this.color = color;
		this.kind = kind;
		this.pieceArrive = pieceArrive;
	}
*/
	
	public Color getColor(){
		return color;
	}
	
	public PieceKind getKind(){
		return kind;
	}

/*
	public boolean getPieceArrive(){
		return pieceArrive;
	}
*/
}

