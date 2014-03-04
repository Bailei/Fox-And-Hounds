package org.graphics;

import org.client.GameLogic;
import org.client.GamePresenter;
import org.client.GameApi;
import org.client.GameApi.Game;
import org.client.GameApi.IteratingPlayerContainer;
import org.client.GameApi.UpdateUI;
import org.client.GameApi.VerifyMove;

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
	IteratingPlayerContainer container;
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
		container = new IteratingPlayerContainer(game, 2);
		GameGraphics gameGraphics = new GameGraphics();
		gamePresenter = new GamePresenter(gameGraphics, container);
		
		final ListBox playerSelect = new ListBox();
		playerSelect.addItem("FoxPlayer");
		playerSelect.addItem("SheepPlayer");
		playerSelect.addItem("Viewer");
		playerSelect.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event){
				int selectedIndex = playerSelect.getSelectedIndex();
				int playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
						: container.getPlayerIds().get(selectedIndex);
				container.updateUi(playerId);
			}
		});
		
		FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(gameGraphics);
		flowPanel.add(playerSelect);
		RootPanel.get("mainDiv").add(flowPanel);
		
		container.sendGameReady();
		container.updateUi(container.getPlayerIds().get(0));
	}	
}
