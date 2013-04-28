package org.kanwang.hw6.client;

import java.util.LinkedList;

import org.kanwang.hw6.Match;
import org.kanwang.hw6.Player;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GameServiceAsync {
	public void login(String requestUri, AsyncCallback<LoginInfo> async);
	public void submitMove(String move, Match m, String user, AsyncCallback<Match> async);
	public void getMatches(String user, AsyncCallback<LinkedList<Match>> async);
	public void getMatchWithID(String matchID, AsyncCallback<Match> async);
	public void createMatch(String w, String b, AsyncCallback<Match> async);
	public void deleteMatch(long matchID, String user, AsyncCallback<Match> async);
	public void autoCreate(String user, AsyncCallback<Match> async);
	public void updateRating(String matchID, AsyncCallback<Player> async);
	public void getPlayer(String userID, AsyncCallback<Player> async);
}