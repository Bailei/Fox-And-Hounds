package org.client;

public class FromTo{
	private Position from;
	private Position to;
	
	public FromTo(Position from, Position to){
		this.from = from;
		this.to = to;
	}
	
	public Position getFrom(){
		return from;
	}
	
	public Position getTo(){
		return to;
	}
}
