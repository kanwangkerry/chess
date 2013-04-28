package org.kanwang.hw3;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GameImages extends ClientBundle {
	@Source("black_tile.png")
	ImageResource blackTile();

	@Source("white_tile.png")
	ImageResource whiteTile();
	
	@Source("b_pawn.png")
	ImageResource blackPawn();
	
	@Source("b_bishop.png")
	ImageResource blackBishop();
	
	@Source("b_knight.png")
	ImageResource blackKnight();
	
	@Source("b_king.png")
	ImageResource blackKing();
	
	@Source("b_queen.png")
	ImageResource blackQueen();
	
	@Source("b_rook.png")
	ImageResource blackRook();
	
	@Source("w_pawn.png")
	ImageResource whitePawn();
	
	@Source("w_bishop.png")
	ImageResource whiteBishop();
	
	@Source("w_knight.png")
	ImageResource whiteKnight();
	
	@Source("w_king.png")
	ImageResource whiteKing();
	
	@Source("w_queen.png")
	ImageResource whiteQueen();
	
	@Source("w_rook.png")
	ImageResource whiteRook();
}
