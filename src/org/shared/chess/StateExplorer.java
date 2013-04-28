package org.shared.chess;

import java.util.Set;

/**
 * Explores the state graph.
 */
public interface StateExplorer {
  /**
   * Returns all the possible moves from the given state.
   * For example, from the initial state we have 16 possible moves
   * for the 8 pawns (each pawn can move either 1 or 2 squares)
   * and 4 possible moves for the two knights.
   * So in total, 
   *   getPossibleMoves(new State()) 
   * should return a list with 20 moves.
   */
  Set<Move> getPossibleMoves(State state);

  /**
   * Returns the possible moves from the given state that begin at start.
   * For example, 
   *   getPossibleMovesFromPosition(new State(), new Position(1,0)) 
   * should return a list with 2 moves for the pawn at position 1x0.
   */
  Set<Move> getPossibleMovesFromPosition(State state, Position start);
  
  /**
   * Returns the list of start positions of all possible moves.
   * For example, 
   *   getPossibleStartPositions(new State()) 
   * should return a list with 10 possible start positions:
   *   8 positions for the pawns (1x0 till 1x7)
   *   2 positions for the knights (0x1 and 0x6).
   */
  Set<Position> getPossibleStartPositions(State state);
}
