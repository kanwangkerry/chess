package org.shared.chess;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Move {
  private Position from;
  private Position to;
  /**
   * When a pawn reaches the eighth rank, it can be promoted to another piece kind. 
   * promoteToPiece is null if no promotion is made.
   * See http://en.wikipedia.org/wiki/Promotion_(chess)
   */
  private PieceKind promoteToPiece;

  public Move(Position from, Position to, PieceKind promoteToPiece) {
    this.from = checkNotNull(from);
    this.to = checkNotNull(to);
    this.promoteToPiece = promoteToPiece;
  }
  
  public Position getFrom() {
    return from;
  }

  public Position getTo() {
    return to;
  }

  public PieceKind getPromoteToPiece() {
    return promoteToPiece;
  }

  @Override
  public String toString() {
    return from + "->" + to 
      + (promoteToPiece==null ? "" : " (promoting to " + promoteToPiece + ")");
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(from, to, promoteToPiece);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Move)) return false;
    Move other = (Move) obj;
    return Objects.equal(from, other.from)
      && Objects.equal(to, other.to)
      && Objects.equal(promoteToPiece, other.promoteToPiece);
  }
}
