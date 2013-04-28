package org.shared.chess;

/**
 * Ignore the threefold-repetition rule - it will be implemented outside the game state:<br>
 * http://en.wikipedia.org/wiki/Threefold_repetition<br>
 */
public interface StateChanger {
  /**
   * Make a chess move and change state to reflect the new game state.
   * If the move is illegal, the method throws IllegalMove.
   * 
   * This method ends the game in all occassions except THREEFOLD_REPETITION_RULE.
   * http://en.wikipedia.org/wiki/Chess#End_of_the_game
   */
  public void makeMove(State state, Move move) throws IllegalMove;
}
