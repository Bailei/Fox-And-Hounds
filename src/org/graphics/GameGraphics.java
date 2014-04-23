package org.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.client.GamePresenter;
import org.client.GameLogic;
import org.client.State;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import org.sounds.GameSounds;

/**
 * Graphics for the game.
 */
public class GameGraphics extends Composite implements GamePresenter.View {
	public interface GameGraphicsUiBinder extends UiBinder<Widget, GameGraphics>{	
	}
	
	@UiField
	GameCss css;
	
	@UiField
	Grid gameGrid;

//	@UiField
//	FlexTable animationArea;
	
//	@UiField
//	Button animationImplement;
	
	private boolean enableClicks = false;	
	private final GameImageSupplier gameImageSupplier;
	private GamePresenter presenter;
	private State gameState;
	private Audio pieceDown;
	private boolean clicked = false;
	
	public GameGraphics(){
		final AnimationDemo ai = GWT.create(AnimationDemo.class);
		GameImages gameImages = GWT.create(GameImages.class);
	    GameSounds gameSounds = GWT.create(GameSounds.class);
		this.gameImageSupplier = new GameImageSupplier(gameImages);
		GameGraphicsUiBinder uiBinder = GWT.create(GameGraphicsUiBinder.class);
		initWidget(uiBinder.createAndBindUi(this));
		
//		animationImp(ai);
		
		if (Audio.isSupported()) {
		      pieceDown = Audio.createIfSupported();
		      pieceDown.addSource(gameSounds.pieceDownMp3().getSafeUri()
		                      .asString(), AudioElement.TYPE_MP3);
		      pieceDown.addSource(gameSounds.pieceDownWav().getSafeUri()
		                      .asString(), AudioElement.TYPE_WAV);
		}
		
		gameGrid.resize(7, 7);
		gameGrid.setCellPadding(0);
		gameGrid.setCellSpacing(0);
		gameGrid.setBorderWidth(0);
		gameGrid.setWidth("350px");
		gameGrid.setHeight("350px");

		/*
	    for(int i = 0; i< 7; i++){
	        for(int j = 0; j< 7; j++){
	          gameGrid.getCellFormatter().setHeight(i, j, "48px");
	          gameGrid.getCellFormatter().setWidth(i, j, "50px");  
	        }
	    }
	    
	    
		for(int row = 0; row < 7; row++){
			for(int col = 0; col < 7; col++){
				final Image image = new Image();
				board[row][col] = image;
				image.setWidth("100%");	
				image.setResource(gameImages.empty());
				gameGrid.setWidget(row, col, image);
			}
		}
		*/

	}
	
	public List<List<Image>> createGridImage(ArrayList<ArrayList<Integer>> board) {
		List<List<GameImage>> images = Lists.newArrayList();
	    for (int i = 0; i < 7; i++) {
	    	images.add(Lists.<GameImage>newArrayList());
	    	for(int j = 0; j < 7;j++) {
	    		if(board.get(i).get(j) == 1 || board.get(i).get(j) == 2) {
	    			images.get(i).add(GameImage.Factory.getFox());
	    		}
	    		if(board.get(i).get(j) == 0) {	
	    			images.get(i).add(GameImage.Factory.getEmpty()); 
	    		}
	    		if(board.get(i).get(j) >= 3 && board.get(i).get(j) <= 22) {
	    			images.get(i).add(GameImage.Factory.getSheep());
	    		}
	    		if(board.get(i).get(j) == -1) {
	    			images.get(i).add(GameImage.Factory.getEmpty());
	    		}
	    	}
	    }  
	    return createImages(images, true, board);
	}
	
	private List<List<Image>> createImages(List<List<GameImage>> images, boolean withClick, ArrayList<ArrayList<Integer>> board){
		List<List<Image>> res = Lists.newArrayList();
		int i = 0;
		int j = 0;
		for(List<GameImage> imgList : images){
			res.add(Lists.<Image>newArrayList());
			j = 0;
			for(GameImage img : imgList) {		
				Image image = new Image(gameImageSupplier.getResource(img));
				
				final int row = i;
				final int col = j;
				final ArrayList<ArrayList<Integer>> b = board;
//				if(withClick){
					image.addClickHandler(new ClickHandler(){
						@Override
						public void onClick(ClickEvent event){
							
							String st = row * 10 + col + "";
							int tmp = b.get(row).get(col);

							if(enableClicks){
								if(presenter.isFoxTurn() && presenter.isMyTurn()){
									if(tmp == 1 || tmp == 2 || tmp == 0){
										presenter.positionSelectedForFox(st);
									}
								}else if(presenter.isSheepTurn() && presenter.isMyTurn()){
									if((tmp >= 3 && tmp <= 22) || tmp == 0){
										presenter.positionSelectedForSheep(st);					
									}
								}
							}
						}
					});
//				}
		
				res.get(i).add(image);
				j++;
		
			}
			i++;
		}
	
		return res;
	}

	private void placeImages(Grid grid, List<List<Image>> images) {
		grid.clear();
	    for(int i = 0;i < 7; i++){
	    	for(int j = 0; j < 7; j++) {
	    		FlowPanel imageContainer = new FlowPanel();
	    		Image tmp = images.get(i).get(j);
	    			imageContainer.add(tmp);
	    			imageContainer.setWidth("100%");
	    			gameGrid.setWidget(i, j, imageContainer);
	    	}
	    }
	}
	  
	private void disableClicks(){
		enableClicks = false;
	}
	
	private void enableClicks(){
		//TODO
	}

	@Override
	public void setPresenter(GamePresenter gamePresenter) {
		this.presenter = gamePresenter;
	}

	@Override
	public void setViewerState(ArrayList<ArrayList<Integer>> board) {
		placeImages(gameGrid, createGridImage(board));
		disableClicks();
	}

	@Override
	public void setPlayerState(ArrayList<ArrayList<Integer>> board) {
        List<List<Image>> images = createGridImage(board);
		placeImages(gameGrid, images);
		disableClicks();
	}	
	
	@Override
	public void chooseNextPositionForSheep(List<String> SelectedPosition,
			ArrayList<ArrayList<Integer>> board) {
		enableClicks = true;
	}

	@Override
	public void chooseNextPositionForFox(List<String> SelectedPosition,
			ArrayList<ArrayList<Integer>> board) {
		enableClicks = true;
		if(SelectedPosition.size() == 2){
			playPieceDownSound();
			presenter.finishFoxMoveSelectPosition();
		}
	}		
	
	@Override
	public void playPieceDownSound() {
		if (pieceDown != null) {
			pieceDown.play();
		}
	}
	
	//Just for test
	private native void test(String message) /*-{
		$wnd.alert(message);
	}-*/;
	
/*	
	private void animationImp(final AnimationDemo ai){
		animationArea.clear();
		final Image wolf = new Image(ai.wolf());
		wolf.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event){
				clicked = true;
			}
		});
		final int width = wolf.getWidth();
		final int height = wolf.getHeight();
		
		animationArea.setBorderWidth(2);
		CellFormatter cf = animationArea.getCellFormatter();
		
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				cf.setWidth(i, j, "45px");
				cf.setHeight(i, j, "45px");
				final SimplePanel simplePanel = new SimplePanel();
				simplePanel.setPixelSize(width, height);
				animationArea.setWidget(i, j, simplePanel);
				
				final Image empty = new Image(ai.empty());
				empty.setPixelSize(width, height);
				simplePanel.add(empty);
				
				empty.addClickHandler(new ClickHandler(){
					@Override
					public void onClick(ClickEvent event){
						if(clicked){
							Animation animation = new PieceMovingAnimation(wolf, empty, ai.wolf(), ai.empty(), ai.empty(), pieceDown);
							animation.run(1200);
						}
						clicked = false;
					}
				});
				
				if(i == 1 && j == 1){
					simplePanel.setWidget(wolf);
				}
			}
		}
	}
*/
}
