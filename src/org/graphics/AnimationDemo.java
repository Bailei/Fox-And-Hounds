package org.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.DataResource;

public interface AnimationDemo extends ClientBundle{

	@Source("animation/wolf.png")
	ImageResource wolf();

	@Source("animation/empty.png")
	ImageResource empty();
}
