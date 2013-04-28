package org.kanwang.hw6;

import java.io.Serializable;
import java.util.Date;

import org.shared.chess.Color;
import org.shared.chess.State;
import org.kanwang.hw3.Presenter;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Match implements Serializable{
	@Id long id;
	String w = null, b = null;
	String state;
	int isDeleted = 0;
	Date startDate;

	static int index = 1;
	static long generateID(){
		return index++;
	}
	
	public Match(){};
	public Match(State state){
		this.id = Match.generateID();
		this.state = Presenter.makeStringfromState(state);
		this.setTurn(Color.WHITE);
		this.startDate = new Date();
	}
	
	public void setW(String p){
		w = p;
	}
	
	public void setB(String p){
		b = p;
	}
	
	public String getW(){
		return w;
	}
	
	public String getB(){
		return b;
	}
	
	public long getID(){
		return this.id;
	}
	
	public String getAnotherUser(String user){
		if(user.equals(this.w))
			return this.b;
		else return this.w;
	} 
	
	public void setNextTurn(){
		if(this.getTurn() == Color.WHITE){
			this.setTurn(Color.BLACK);
		}
		else{
			this.setTurn(Color.WHITE);
		}
	}
	
	public boolean isUserTurn(String user)
	{
		
		if(user.equals(this.w) && this.getTurn() == Color.WHITE)
			return true;
		if(user.equals(this.b) && this.getTurn() == Color.BLACK)
			return true;
		return false;
		
	}
	
	public Color getTurn()
	{
		State temp;
		try {
			temp = Presenter.getStateFromString(state);
			return temp.getTurn();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setTurn(Color c)
	{
		State temp;
		try {
			temp = Presenter.getStateFromString(state);
			temp.setTurn(c);
			this.state = Presenter.makeStringfromState(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getState()
	{
		return this.state;
	}
	
	public void setState(String s){
		this.state = s;
	}

	public boolean shouldDelete(String user){
		if(user.equals(this.w))
			return this.isDeleted == 2;
		else
			return this.isDeleted == 1;
	}
	public boolean isDeleted(String user)
	{
		if(user.equals(this.w))
			return this.isDeleted == 1;
		else
			return this.isDeleted == 2;
	}
	
	public void setDeleted(String user){
		if(user.equals(this.w))
			this.isDeleted = 1;
		else
			this.isDeleted = 2;
	}
	
	public Date getStartDate(){
		return this.startDate;
	}
	
	public void setIDAI(){
		this.id = -1;
	}
}

