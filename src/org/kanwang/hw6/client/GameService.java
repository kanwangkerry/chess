package org.kanwang.hw6.client;

import java.util.LinkedList;
import org.kanwang.hw6.Match;
import org.kanwang.hw6.Player;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface GameService extends RemoteService {
	public LoginInfo login(String requestUri);
	public Match submitMove(String move, Match m, String user);
	public LinkedList<Match> getMatches(String user);
	public Match getMatchWithID(String matchID);
	public Match createMatch(String w, String b);
	public Match deleteMatch(long matchID, String user);
	public Match autoCreate(String user);
	public Player updateRating(String matchID);
	public Player getPlayer(String userID);
}
