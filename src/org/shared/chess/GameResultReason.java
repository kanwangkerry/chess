package org.shared.chess;

/**
 * Reasons why the chess game ended in draw or victory.
 * See http://en.wikipedia.org/wiki/Chess
 */
public enum GameResultReason {
  /** 
   * A win is declared if
   * a player's king is threatened with capture (i.e. is in check) 
   * and there is no way to counter the threat.
   * See http://en.wikipedia.org/wiki/Checkmate
   */
  CHECKMATE,
  /**
   * A draw is declared if
   * no capture has been made and no pawn has been moved in the last fifty consecutive moves.
   * See http://en.wikipedia.org/wiki/Fifty-move_rule
   */
  FIFTY_MOVE_RULE,
  /**
   * A draw is declared if
   * the same position occurs three times.
   * See http://en.wikipedia.org/wiki/Threefold_repetition
   */
  THREEFOLD_REPETITION_RULE,
  /**
   * A draw is declared if
   * the player whose turn it is to move is not in check but has no legal moves.
   * See http://en.wikipedia.org/wiki/Stalemate
   */
  STALEMATE,
}
