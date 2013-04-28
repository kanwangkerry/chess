package org.shared.chess;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Piece {
  private Color color;
  private PieceKind kind;

  public Piece(Color color, PieceKind kind) {
    this.color = checkNotNull(color);
    this.kind = checkNotNull(kind);
  }

  public Color getColor() {
    return color;
  }
  
  public PieceKind getKind() {
    return kind;
  }
  
  @Override
  public String toString() {
    return "(" + color + " " + kind + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(color, kind);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Piece)) return false;
    Piece other = (Piece) obj;
    return Objects.equal(color, other.color)
      && Objects.equal(kind, other.kind);
  }
}
