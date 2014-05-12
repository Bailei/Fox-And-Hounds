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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GameEntryPoint implements EntryPoint{
	GamePresenter gamePresenter;
	ContainerConnector container;
	//IteratingPlayerContainer container;
	
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
		//container = new IteratingPlayerContainer(game, 2);
		GameGraphics gameGraphics = new GameGraphics();
		gamePresenter = new GamePresenter(gameGraphics, container);

		
//		final ListBox playerSelect = new ListBox();
//		playerSelect.addItem("FoxPlayer");
//		playerSelect.addItem("SheepPlayer");
//		playerSelect.addItem("Viewer");
//		playerSelect.addChangeHandler(new ChangeHandler(){
//			@Override
//			public void onChange(ChangeEvent event){
//				int selectedIndex = playerSelect.getSelectedIndex();
//				String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
//						: container.getPlayerIds().get(selectedIndex);
//				container.updateUi(playerId);
//			}
//		});
		
		
		FlowPanel flowPanel = new FlowPanel();
//		flowPanel.add(playerSelect);
		flowPanel.add(gameGraphics);
		RootPanel.get("mainDiv").add(flowPanel);
		
//		RootPanel.get("mainDiv").add(gameGraphics);
		
		container.sendGameReady();
//		container.updateUi(container.getPlayerIds().get(0));	
		scaleGame();
	}
	
	 private void scaleGame() {
		    double scaleX = (double) Window.getClientWidth() / (double) gameSetting.width;
		    double scaleY = (double) Window.getClientHeight() / (double) gameSetting.height;
		    double scale = Math.max(scaleX, scaleY);
		    scale = (scale > 1.0) ? 1.0 : scale;
		    gameSetting.scale = scale;
	 }
}
