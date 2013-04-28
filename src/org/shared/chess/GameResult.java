package org.shared.chess;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class GameResult {
  private Color winner; // in case of draw then winner is null 
  private GameResultReason gameResultReason;
  
  public GameResult(Color winner, GameResultReason gameResultReason) {
    this.winner = winner;
    this.gameResultReason = checkNotNull(gameResultReason);
  }

  public Color getWinner() {
    return winner;
  }
  
  public boolean isDraw() {
    return winner == null;
  }
  
  public GameResultReason getGameResultReason() {
    return gameResultReason;
  }

  @Override
  public String toString() {
    return "GameResult [winner=" + winner + ", gameResultReason=" + gameResultReason + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(gameResultReason, winner);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof GameResult)) return false;
    GameResult other = (GameResult) obj;
    return Objects.equal(gameResultReason, other.gameResultReason)
      && Objects.equal(winner, other.winner);
  }
}
