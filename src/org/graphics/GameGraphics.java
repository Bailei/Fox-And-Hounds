package org.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.client.GamePresenter;
import org.client.GameLogic;

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
	HorizontalPanel playArea;
	@UiField
	Grid gameGrid;

	//private boolean enableClicksForFox = false;
	//private boolean enableClicksForSheep = false;
	//private boolean enableClicksFoxEmpty = false;
	private boolean enableClicks = false;
	
	private final GameImageSupplier gameImageSupplier;
	private GamePresenter presenter;
	private Image[][] board = new Image[7][7];
	
	public GameGraphics(){
		GameImages gameImages = GWT.create(GameImages.class);
		this.gameImageSupplier = new GameImageSupplier(gameImages);
		GameGraphicsUiBinder uiBinder = GWT.create(GameGraphicsUiBinder.class);
		initWidget(uiBinder.createAndBindUi(this));
		
		gameGrid.resize(7, 7);
		gameGrid.setCellPadding(0);
		gameGrid.setCellSpacing(0);
		gameGrid.setBorderWidth(0);
		gameGrid.setWidth("50px");
		
		/*
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
	    	for(int j=0; j<7;j++) {
	    		if(board.get(i).get(j) == 1 || board.get(i).get(j) == 2)
	    		images.get(i).add(GameImage.Factory.getFox());
	    		if(board.get(i).get(j) == 0)
	    		images.get(i).add(GameImage.Factory.getEmpty());
	    		if(board.get(i).get(j) >= 3 && board.get(i).get(j) <= 22)
	    		images.get(i).add(GameImage.Factory.getFox());
	    	}
	    }
	    return createImages(images, false, board);
	}
	
	private List<List<Image>> createImages(List<List<GameImage>> images, boolean withClick, ArrayList<ArrayList<Integer>> board){
		List<List<Image>> res = Lists.newArrayList();
		int i = 0;
		int j = 0;
		for(List<GameImage> imgList : images){
			res.add(Lists.<Image>newArrayList());
			for(GameImage img : imgList) {
				if(img == null) continue;
				final GameImage imgFinal = img;
				Image image = new Image(gameImageSupplier.getResource(img));
			
				final int row = i;
				final int col = j;
				final ArrayList<ArrayList<Integer>> b = board;
				if(withClick){
					image.addClickHandler(new ClickHandler(){
						@Override
						public void onClick(ClickEvent event){
							String st = Integer.toString(row * 10 + col);
							if(enableClicks){
								if(b.get(row).get(col) == 1 || b.get(row).get(col) == 2){
									presenter.positionSelectedForFox(st);
								}else if(b.get(row).get(col) >= 3 || b.get(row).get(col) <= 22){
									presenter.positionSelectedForSheep(st);
								}
							}
						}
					});
				}			
				res.get(i).add(image);
				j++;
			}
			i++;
		}
		return res;
	}
	
	private void placeImages(Grid grid, List<List<Image>> images) {
	    grid.clear();
	    for(int i=0;i<7;i++){
	    	for(int j=0;j<7;j++) {
	    		FlowPanel imageContainer = new FlowPanel();
	    		Image tmp = images.get(i).get(j);
	    		if(tmp != null){
	    			imageContainer.add(tmp);
	    			imageContainer.setWidth("100%");
	    			grid.setWidget(i, j, imageContainer);
	    		}
	    	}
	    }
	}
	  
	private Image createBoard() {
		GameImage images = GameImage.Factory.getBoard();
		return createImages(images);
	}
	
	private Image createImages(GameImage images){
		Image image = new Image(gameImageSupplier.getResource(images));
		return image;
	}
	
	private void placeBoardImage(HorizontalPanel panel, Image images){
		panel.clear();
		FlowPanel imageContainer = new FlowPanel();
		imageContainer.add(images);
		panel.add(imageContainer);
	}
	
	private void disableClicks(){
		enableClicks = false;
	}

	@Override
	public void setPresenter(GamePresenter gamePresenter) {
		this.presenter = gamePresenter;
	}

	@Override
	public void setViewerState(ArrayList<ArrayList<Integer>> board) {
		placeBoardImage(playArea, createBoard());
		placeImages(gameGrid, createGridImage(board));
		disableClicks();
	}

	@Override
	public void setPlayerState(ArrayList<ArrayList<Integer>> board) {
		placeBoardImage(playArea, createBoard());
		placeImages(gameGrid, createGridImage(board));
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
	}		
}
