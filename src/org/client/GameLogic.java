package org.client;

import java.util.List;

import org.client.GameApi.Operation;
import org.client.GameApi.VerifyMove;
import org.client.GameApi.VerifyMoveDone;

public class GameLogic {
  public VerifyMoveDone verify(VerifyMove verifyMove) {
    try{
    	checkMoveIsLegal(verifyMove);
    	return new VerifyMoveDone();
    }catch(Exception e){
    	return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
    }
  }
  
  void checkMoveIsLegal(VerifyMove verifyMove){
	  //HW2
  }
  
  List<Operation> getInitialMove(int whitePlayerId, int blackPlayerId){
		// TODO Auto-generated method stub
		return null;
  }
}
