package org.shared.chess;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import com.google.common.base.Objects;

/**
 * http://en.wikipedia.org/wiki/Board_representation_(chess)
 */
public class State {
  public static final int ROWS = 8;
  public static final int COLS = 8;
  
  /** turn is the color of the player that should play next */
  private Color turn = Color.WHITE;
  /** board contains all the pieces on the board */
  private Piece[][] board = new Piece[ROWS][COLS];
  /**
   * canCastleKingSide[0] is true iff 
   *   the white king has never moved and the white king-side rook has never moved.
   * (canCastleKingSide[1] is for the black king and rook)
   * canCastleQueenSide[0] is true iff 
   *   the white king has never moved and the white queen-side rook has never moved.
   * (canCastleQueenSide[1] is for the black king and rook)
   * 
   * Castling is a special move in the game of chess involving the king and either of the original 
   * rooks of the same color. It is the only move in chess (except promotion) in which a player 
   * moves two pieces at the same time. Castling consists of moving the king two squares towards 
   * a rook on the player's first rank, 
   * then moving the rook onto the square over which the king crossed.
   * Castling can only be done if the king has never moved, the rook involved has never moved, 
   * the squares between the king and the rook involved are not occupied, the king is not in check, 
   * and the king does not cross over or end on a square in which it would be in check.
   * http://en.wikipedia.org/wiki/Castling
   */
  private boolean[] canCastleKingSide = new boolean[]{true, true};
  private boolean[] canCastleQueenSide = new boolean[]{true, true};
  /**
   * enpassantPosition is either null or the position of a pawn that just moved two squares forward.
   * Note that we must have that:
   *  enpassantPosition == null || getPiece(enpassantPosition).getKind()==PieceKind.PAWN
   * 
   * En passant is a special pawn capture which can occur immediately after a player moves a pawn 
   * two squares forward from its starting position, and an enemy pawn could have captured it had 
   * the same pawn moved only one square forward. 
   * http://en.wikipedia.org/wiki/En_passant
   */
  private Position enpassantPosition;
  /**
   * How many moves were made without capture or moving a pawn. After fifty
   * moves by each side, the game draws. I.e, when this number reaches a 100,
   * the game is over in a draw. http://en.wikipedia.org/wiki/Fifty-move_rule
   */
  private int numberOfMovesWithoutCaptureNorPawnMoved = 0;
  /**
   * gameResult is null when the game is in progress, and non-null when the game is over.
   */
  private GameResult gameResult;
  
  public State() {
    for (Color color : Color.values()) {
      for (int c = 0; c < COLS; c++) {
        setPiece(color.isWhite() ? 1 : ROWS - 2, c, new Piece(color, PieceKind.PAWN));
      }
      int secondRow = color.isWhite() ? 0 : ROWS - 1;
      setPiece(secondRow, 0, new Piece(color, PieceKind.ROOK));
      setPiece(secondRow, 7, new Piece(color, PieceKind.ROOK));
      setPiece(secondRow, 1, new Piece(color, PieceKind.KNIGHT));
      setPiece(secondRow, 6, new Piece(color, PieceKind.KNIGHT));
      setPiece(secondRow, 2, new Piece(color, PieceKind.BISHOP));
      setPiece(secondRow, 5, new Piece(color, PieceKind.BISHOP));
      setPiece(secondRow, 3, new Piece(color, PieceKind.QUEEN));
      setPiece(secondRow, 4, new Piece(color, PieceKind.KING));
    }
  }
  
  public State(Color turn, Piece[][] board, 
      boolean[] canCastleKingSide, boolean[] canCastleQueenSide, Position enpassantPosition,
      int numberOfMovesWithoutCaptureNorPawnMoved, GameResult gameResult) {
    this.turn = turn;
    System.arraycopy(canCastleKingSide, 0, this.canCastleKingSide, 0, 2);
    System.arraycopy(canCastleQueenSide, 0, this.canCastleQueenSide, 0, 2);
    this.enpassantPosition = enpassantPosition;
    this.numberOfMovesWithoutCaptureNorPawnMoved = numberOfMovesWithoutCaptureNorPawnMoved;
    this.gameResult = gameResult;
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.board[r][c] = board[r][c];
      }
    }
  }
  
  public Color getTurn() {
    return turn;
  }

  public void setTurn(Color turn) {
    this.turn = checkNotNull(turn);
  }

  public void setPiece(Position position, Piece piece) {
    setPiece(position.getRow(), position.getCol(), piece);
  }
  
  public void setPiece(int row, int col, Piece piece) {
    board[row][col] = piece;
  }
  
  public Piece getPiece(Position position) {
    return getPiece(position.getRow(), position.getCol());
  }

  public Piece getPiece(int row, int col) {
    return board[row][col];
  }
  
  public boolean isCanCastleKingSide(Color color) {
    return canCastleKingSide[color.ordinal()];
  }

  public void setCanCastleKingSide(Color color, boolean canCastleKingSide) {
    this.canCastleKingSide[color.ordinal()] = canCastleKingSide;
  }

  public boolean isCanCastleQueenSide(Color color) {
    return canCastleQueenSide[color.ordinal()];
  }

  public void setCanCastleQueenSide(Color color, boolean canCastleQueenSide) {
    this.canCastleQueenSide[color.ordinal()] = canCastleQueenSide;
  }

  public Position getEnpassantPosition() {
    return enpassantPosition;
  }

  public void setEnpassantPosition(Position enpassantPosition) {
    this.enpassantPosition = enpassantPosition;
  }

  public int getNumberOfMovesWithoutCaptureNorPawnMoved() {
    return numberOfMovesWithoutCaptureNorPawnMoved;
  }

  public void setNumberOfMovesWithoutCaptureNorPawnMoved(int numberOfMovesWithoutCaptureNorPawnMoved) {
    this.numberOfMovesWithoutCaptureNorPawnMoved = numberOfMovesWithoutCaptureNorPawnMoved;
  }

  public GameResult getGameResult() {
    return gameResult;
  }

  public void setGameResult(GameResult gameResult) {
    this.gameResult = gameResult;
  }

  public State copy() {
    return new State(turn, board, canCastleKingSide, canCastleQueenSide, enpassantPosition,
        numberOfMovesWithoutCaptureNorPawnMoved, gameResult);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        turn,
        Arrays.deepHashCode(board), 
        Arrays.hashCode(canCastleKingSide), Arrays.hashCode(canCastleQueenSide), 
        enpassantPosition, numberOfMovesWithoutCaptureNorPawnMoved,
        gameResult);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof State)) return false;
    State other = (State) obj;
    return Arrays.deepEquals(board, other.board)
      && Objects.equal(turn, other.turn)
      && Arrays.equals(canCastleKingSide, other.canCastleKingSide)
      && Arrays.equals(canCastleQueenSide, other.canCastleQueenSide)
      && Objects.equal(enpassantPosition, other.enpassantPosition)
      && Objects.equal(numberOfMovesWithoutCaptureNorPawnMoved, other.numberOfMovesWithoutCaptureNorPawnMoved)
      && Objects.equal(gameResult, other.gameResult);
  }

  @Override
  public String toString() {
    return "State [" 
        + "turn=" + turn + ", " 
        + "board=" + Arrays.deepToString(board)
        + ", canCastleKingSide=" + Arrays.toString(canCastleKingSide) 
        + ", canCastleQueenSide=" + Arrays.toString(canCastleQueenSide)
        + ", " + (enpassantPosition != null ? "enpassantPosition=" + enpassantPosition + ", " : "")
        + (gameResult != null ? "gameResult=" + gameResult + ", " : "")
        + "numberOfMovesWithoutCaptureNorPawnMoved=" + numberOfMovesWithoutCaptureNorPawnMoved
        + "]";
  }
}
