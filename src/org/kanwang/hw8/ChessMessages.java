package org.kanwang.hw8;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

public interface ChessMessages extends Messages {

	@DefaultMessage("{0}: in white, vs. {1}, ")
	String boxInGameStatusWhite(String matchID, String oppo);

	@DefaultMessage("{0}: in black, vs. {1}, ")
	String boxInGameStatusBlack(String matchID, String oppo);

	@DefaultMessage("{0}''s turn")
	String boxWhosTurn(String turn);

	@DefaultMessage("{0}: Game Over: Checkmate, {1}''s win!")
	String boxGameOverWin(String matchid, String winner);

	@DefaultMessage("{0}: Game Over: Fifty Moves Rule, Draw!")
	String boxGameOverfifty(String id);

	@DefaultMessage("{0}: Game Over: Stalemate, Draw!")
	String boxGameOverstale(String id);

	@DefaultMessage("Deleted Match: {0}")
	String alertDeleteMatch(String matchID);

	@DefaultMessage("Got Match: {0}")
	String alertCreateMatch(String matchID);

	@DefaultMessage("Match {0} has been updated!")
	String alertUpdate(String matchID);

	@DefaultMessage("Start Time: {0,date,medium}, {0,time,medium}")
	String labelStartDate(Date timestamp);
	
	@DefaultMessage("Rating: {0}")
	String labelRating(String rating);

}
