package org.kanwang.hw8;

import com.google.gwt.i18n.client.Constants;

public interface ChessConstants extends Constants {
	@DefaultStringValue("Please sign in to your Google Account to access the Chess application.")
	String loginInfo();
	
	@DefaultStringValue("Sign In")
	String loginAnchor();
	
	@DefaultStringValue("Log Out")
	String logoutAnchor();
	
	@DefaultStringValue("Game not Begin")
	String gameNotBegin();
	
	@DefaultStringValue("New Game")
	String newgame();
	
	@DefaultStringValue("Create Match")
	String buttonNewMatch();
	
	@DefaultStringValue("Auto Match")
	String buttonAutoMatch();
	
	@DefaultStringValue("Delete Match")
	String buttonDeleteMatch();
	
	@DefaultStringValue("Play With AI")
	String buttonAI();
	
	@DefaultStringValue("match info wrong!")
	String alertWrongMatchInfo();
	
	@DefaultStringValue("Wait for another waiting user!")
	String alertAutoMatchWait();
	
	@DefaultStringValue("Channel Closed!")
	String alertChannelClose();
	
	@DefaultStringValue("In Game: White's Turn")
	String labelWhiteTurn();
	
	@DefaultStringValue("In Game: Black's Turn")
	String labelBlackTurn();
	
	@DefaultStringValue("Game Over: Checkmate, White's win!")
	String labelGameOverWhite();
	
	@DefaultStringValue("Game Over: CheckMate, Black's win!")
	String labelGameOverBlack();
	

	@DefaultStringValue("Game Over: Fifty Moves Rule, Draw!")
	String labelGameOverfifty();
	
	@DefaultStringValue("Game Over: Stalemate, Draw!")
	String labelGameOverstale();
	
	@DefaultStringValue("English")
	String langEnglish();
	
	@DefaultStringValue("Chinese")
	String langChinese();

}
