package org.shared.chess;

/**
 * Player color in a game of Chess: either white or black.
 */
public enum Color {
  WHITE,
  BLACK,
  ;

  public Color getOpposite() {
    return this == WHITE ? BLACK : WHITE;
  }

  public boolean isWhite() {
    return this == WHITE;
  }

  public boolean isBlack() {
    return this == BLACK;
  }

  @Override
  public String toString() {
    return isWhite() ? "W" : "B";
  }
}
