package org.graphics;

import com.google.gwt.i18n.client.Constants;

public interface I18nMessages extends Constants{
	@DefaultStringValue("Fox and Sheep")
	String gameName();
	
	@DefaultStringValue("You won!")
	String won();
	
	@DefaultStringValue("You lost!")
	String lost();
}
