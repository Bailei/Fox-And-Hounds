package org.graphics;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Constants;

@DefaultLocale("en")
public interface I18nMessages extends Constants{	
	@DefaultStringValue("You won!")
	String foxWon();
	
	@DefaultStringValue("You lost!")
	String sheepWon();
}
