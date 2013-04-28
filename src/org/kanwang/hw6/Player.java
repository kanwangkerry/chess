package org.kanwang.hw6;

import java.io.Serializable;
import java.util.LinkedList;

import org.kanwang.hw6.client.LoginInfo;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Player implements Serializable{
	@Id String email;
	boolean isWaiting;
	String token;
	int rating = 1500;
//	LinkedList<String> channelToken = new LinkedList<String>();
//	LinkedList<String> match = new LinkedList<String>();
	
	public Player(){
		
	}
	
	public Player(LoginInfo login){
		this.email = login.getEmailAddress();
	}
	
	public String getID(){
		return email;
	}
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getToken(){
		return token;
	}
	
	public boolean isWaiting(){
		return this.isWaiting;
	}
	
	public void setWaiting(boolean w){
		this.isWaiting = w;
	}
	
	public void setRating(int nRating){
		this.rating = nRating;
	}
	
	public int getRating(){
		return this.rating;
	}
}

