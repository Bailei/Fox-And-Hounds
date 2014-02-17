package org.client;


public class Piece {
	private PieceKind pieceKind;
	
	public Piece(PieceKind pieceKind){
		this.pieceKind = pieceKind;
	}

	public PieceKind getPieceKind(){
		return pieceKind;
	}
}

