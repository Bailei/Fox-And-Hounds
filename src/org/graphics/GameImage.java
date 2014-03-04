package org.graphics;

import java.util.Arrays;

import org.client.*;

/**
 * A representation of card image.
 */
public final class GameImage extends Equality{
	
	enum GameImageKind{
		BOARD,
		FOX,
		SHEEP,
		EMPTY,
	}
	
	public static class Factory{
		public static GameImage getFox(){
			return new GameImage(GameImageKind.FOX);
		}
		
		public static GameImage getSheep(){
			return new GameImage(GameImageKind.SHEEP);
		}
		
		public static GameImage getBoard(){
			return new GameImage(GameImageKind.BOARD);
		}
		
		public static GameImage getEmpty(){
			return new GameImage(GameImageKind.EMPTY);
		}
	}
	
	public final GameImageKind kind;
	
	private GameImage(GameImageKind kind){
		this.kind = kind;
	}
	
	
	@Override
	public Object getId() {
		return Arrays.asList(kind);
	}	
}
