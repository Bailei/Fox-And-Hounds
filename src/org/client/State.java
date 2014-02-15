package org.client;

public class State {
	public static final int NUM_ROWS = 7;
	public static final int NUM_COLS = 7;
		
	private Color turn = Color.F;

	private Piece[][] board = new Piece[NUM_ROWS][NUM_COLS];
	
	public State(){
		setPiece(0, 2, new Piece(Color.F, PieceKind.FOX));
		setPiece(0, 4, new Piece(Color.F, PieceKind.FOX));
		
		setPiece(3, 0, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 1, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 2, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 3, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 4, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 5, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(3, 6, new Piece(Color.S, PieceKind.SHEEP));
		
		setPiece(4, 0, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 1, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 2, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 3, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 4, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 5, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(4, 6, new Piece(Color.S, PieceKind.SHEEP));
		
		setPiece(5, 2, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(5, 3, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(5, 4, new Piece(Color.S, PieceKind.SHEEP));
		
		setPiece(6, 2, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(6, 3, new Piece(Color.S, PieceKind.SHEEP));
		setPiece(6, 4, new Piece(Color.S, PieceKind.SHEEP));		
	}

	public State(Color turn, Piece[][] board) {
		this.turn = turn;
		for (int r = 0; r < NUM_ROWS; r++) {
			for (int c = 0; c < NUM_COLS; c++) {
				this.board[r][c] = board[r][c];
			}
		}
	}
	
	public Color getTurn() {
		return turn;
	}

	public void setTurn(Color turn) {
		this.turn = turn;
	}

	public void setPiece(Position position, Piece piece) {
		setPiece(position.getRow(), position.getCol(), piece);
	}
	  
	public void setPiece(int row, int col, Piece piece) {
		board[row][col] = piece;
	}
	  
	public Piece getPiece(Position position) {
		return getPiece(position.getRow(), position.getCol());
	}

	public Piece getPiece(int row, int col) {
		return board[row][col];
	}	
}
	

