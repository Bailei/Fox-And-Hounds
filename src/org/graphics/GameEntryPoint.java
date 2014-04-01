package org.graphics;

import org.client.GameLogic;
import org.client.GamePresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.ContainerConnector;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GameEntryPoint implements EntryPoint{
	ContainerConnector container;
	GamePresenter gamePresenter;
	
	@Override
	public void onModuleLoad(){
		Game game = new Game(){
			@Override
			public void sendVerifyMove(VerifyMove verifyMove){
				container.sendVerifyMoveDone(new GameLogic().verify(verifyMove));
			}
			
			@Override
			public void sendUpdateUI(UpdateUI updateUI){
				gamePresenter.updateUI(updateUI);
			}
		};
		container = new ContainerConnector(game);
		GameGraphics gameGraphics = new GameGraphics();
		gamePresenter = new GamePresenter(gameGraphics, container);

		FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(gameGraphics);
		RootPanel.get("mainDiv").add(flowPanel);
		
		container.sendGameReady();
	}	
}
