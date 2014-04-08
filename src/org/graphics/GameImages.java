package org.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
	@Source("images/fox.png")
	ImageResource fox();
	
	@Source("images/sheep.png")
	ImageResource sheep();
	
	@Source("images/empty.png")
	ImageResource empty();
}
