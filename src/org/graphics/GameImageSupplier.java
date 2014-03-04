package org.graphics;

import com.google.gwt.resources.client.ImageResource;

/**
 * A mapping from Card to its ImageResource.
 * The images are all of size ??*?? (width x height).
 */
public class GameImageSupplier{
	private final GameImages gameImages;
	
	public GameImageSupplier(GameImages gameImages){
		this.gameImages = gameImages;
	}
	
	public ImageResource getResource(GameImage gameImage){
		switch(gameImage.kind){
		case FOX:
			return getFoxImage();
		case SHEEP:
			return getSheepImage();
		case BOARD:
			return getBoardImage();
		case EMPTY:
			return getEmptyImage();
		default:
			throw new RuntimeException("Forgot kind=" + gameImage.kind);
		}
	}

	private ImageResource getSheepImage() {
		return gameImages.sheep();
	}

	private ImageResource getFoxImage() {
		return gameImages.fox();
	}
	
	private ImageResource getBoardImage() {
		return gameImages.board();
	}
	
	private ImageResource getEmptyImage() {
		return gameImages.empty();
	}
}
