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
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game.
 */
public class GameGraphics extends Composite implements GamePresenter.View {
	public interface GameGraphicsUiBinder extends UiBinder<Widget, GameGraphics>{	
	}

	@UiField
	HorizontalPanel test1;
	
	@UiField
	HorizontalPanel test2;
	
	@UiField
	HorizontalPanel test3;
	
	@UiField
	HorizontalPanel test4;
	
	@UiField
	HorizontalPanel test5;
	
	@UiField
	GameCss css;
	
	@UiField
	Grid gameGrid;
	
	public void testButton1() {
		test1.add(new Button("test1"));
	}
	
	public void testButton2() {
		test2.add(new Button("test2"));
	}
	
	public void testButton3() {
		test3.add(new Button("test3"));
	}
	
	public void testButton4(String str) {
		test4.add(new Button(str));
	}
	
	public void testButton5() {
		test5.add(new Button("test5"));
	}

//	private boolean fox = true;
//	private boolean sheep = false;
//	private boolean enableClicksForFox = false;
//	private boolean enableClicksForSheep = false;
	//private boolean enableClicksFoxEmpty = false;
	private boolean enableClicks = false;	
	private final GameImageSupplier gameImageSupplier;
	private GamePresenter presenter;
	private State gameState;
	
	private PieceMovingAnimation animation;
	
	public GameGraphics(){
		GameImages gameImages = GWT.create(GameImages.class);
		this.gameImageSupplier = new GameImageSupplier(gameImages);
		GameGraphicsUiBinder uiBinder = GWT.create(GameGraphicsUiBinder.class);
		initWidget(uiBinder.createAndBindUi(this));
		
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
//							testButton4("***" + st);
							int tmp = b.get(row).get(col);
/*							
							if(enableClicksForFox){
									testButton4("fox turn choose");
								if(tmp == 1 || tmp == 2 || tmp == 0){
									presenter.positionSelectedForFox(st);
								}
							}else if(enableClicksForSheep){
								if((tmp >= 3 && tmp <= 22) || tmp == 0){
									testButton4("sheep turn choose");
									presenter.positionSelectedForSheep(st);					
								}
							}
*/
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
//		disableClicks();
	}

	@Override
	public void setPlayerState(ArrayList<ArrayList<Integer>> board) {
        List<List<Image>> images = createGridImage(board);
		placeImages(gameGrid, images);
//		testButton4("Sheep EATEN: " + state.getEATEN().size());
//		testButton4("Sheep ARRIVAL: " + state.getARRIVAL().size());
		disableClicks();
	}	
	
	@Override
	public void chooseNextPositionForSheep(List<String> SelectedPosition,
			ArrayList<ArrayList<Integer>> board) {
//        List<List<Image>> images = createGridImage(board);
//		enableClicksForFox = true;
//		enableClicksForSheep = false;
		enableClicks = true;
//		testButton4("chooseNextPosition");
//		if(SelectedPosition.size() == 2){
//			presenter.finishSheepMoveSelectPosition();
//		}
	}

	@Override
	public void chooseNextPositionForFox(List<String> SelectedPosition,
			ArrayList<ArrayList<Integer>> board) {
//        List<List<Image>> images = createGridImage(board);
//		enableClicksForSheep = true;
//		enableClicksForFox = false;
		enableClicks = true;
		if(SelectedPosition.size() == 2){			
			presenter.finishFoxMoveSelectPosition();
		}
	}		
}
