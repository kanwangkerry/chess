package org.shared.chess;

/**
 * Possible reasons for a illegal move are:
 * The player with the wrong color did a move,
 * The move starts from an empty square,
 * Game already over,
 * illegal castling, enpassant, capture, promition,
 * illegal response to check (A check response must result in a position where the king is 
 * no longer under direct attack).
 */
public class IllegalMove extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public IllegalMove() {
  }
}
