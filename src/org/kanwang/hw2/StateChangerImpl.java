package org.kanwang.hw2;

import java.util.Set;

import org.kanwang.hw2_5.StateExplorerImpl;
import org.shared.chess.Color;
import org.shared.chess.GameResult;
import org.shared.chess.GameResultReason;
import org.shared.chess.IllegalMove;
import org.shared.chess.Move;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.Position;
import org.shared.chess.State;
import org.shared.chess.StateChanger;

public class StateChangerImpl implements StateChanger {

	/**
	 * check the current status. throw IllegalMove exception if this move is not
	 * legal.
	 * 
	 * @param state
	 * @param move
	 * @throws IllegalMove
	 */
	void checkStatus(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();

		if (from.getCol() < 0 || from.getCol() > 7 || from.getRow() < 0
				|| from.getRow() > 7 || to.getCol() < 0 || to.getCol() > 7
				|| to.getRow() < 0 || to.getRow() > 7) {
			throw new IllegalMove();
		}

		Piece fromPiece = state.getPiece(from);
		Piece toPiece = state.getPiece(to);
		// Game already ended!
		if (state.getGameResult() != null) {
			throw new IllegalMove();
		}

		// Nothing to move!
		if (fromPiece == null) {
			throw new IllegalMove();
		}

		// Wrong player moves!
		if (fromPiece.getColor() != state.getTurn()) {
			throw new IllegalMove();
		}

		// move to the same color
		if (toPiece != null && fromPiece.getColor() == toPiece.getColor()) {
			throw new IllegalMove();
		}

		// check same move
		if (from.equals(to))
			throw new IllegalMove();

		// check piecekind's move rule

		if (!checkMoveRule(state, move))
			throw new IllegalMove();

		// check illegal move: under check after move;
		if (underCheckAfterMove(state, move))
			throw new IllegalMove();

	}

	/**
	 * check if the move is legal according to the rule of this kind.
	 * @param state
	 * @param move
	 * @return
	 */
	public boolean checkMoveRule(State state, Move move) {
		Position from = move.getFrom();
		Position to = move.getTo();

		if (move.getPromoteToPiece() != null
				&& state.getPiece(move.getFrom()).getKind() != PieceKind.PAWN)
			return false;
		
		switch (state.getPiece(from).getKind()) {
		case PAWN:
			return checkPawnMove(from, to, state, move.getPromoteToPiece());
		case ROOK:
			return checkRookMove(from, to, state);
		case KNIGHT:
			return checkKnightMove(from, to);
		case BISHOP:
			return checkBishopMove(from, to, state);
		case KING:
			return checkKingMove(from, to, state);
		case QUEEN:
			return checkQueenMove(from, to, state);
		default:
			return false;
		}
	}

	/**
	 * check if endenger position is underattack. use in castling.
	 * @param state
	 * @param attackColor
	 * @param endanger
	 * @return
	 */
	boolean underAttack(State state, Color attackColor, Position endanger)
	{
		Move capture;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Piece attack = state.getPiece(i, j);
				if (attack == null)
					continue;
				if (attack.getColor() != attackColor)
					continue;
				if (attack.getKind() == PieceKind.PAWN
						&& i == (attackColor == Color.WHITE ? 6 : 1))
					capture = new Move(new Position(i, j), endanger,
							PieceKind.QUEEN);
				else
					capture = new Move(new Position(i, j), endanger, null);
				if (checkMoveRule(state, capture)) {
					return true;
				}
			}
		}
		return false;
		
	}
	
	/**
	 * chekc is underCheckColor's king is under check
	 * @param state
	 * @param underCheckColor
	 * @return
	 */
	boolean underCheck(State state, Color underCheckColor){
		Position king = null;
		Color attackColor = (underCheckColor == Color.WHITE ? Color.BLACK: Color.WHITE);
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (state.getPiece(i, j) != null
						&& state.getPiece(i, j).getColor() == underCheckColor
						&& state.getPiece(i, j).getKind() == PieceKind.KING)
					king = new Position(i, j);
			}
		}
		if (king == null)
			throw new IllegalMove();
		return underAttack(state, attackColor, king);
	}
	
	/**
	 * check is under attack after move. If so this is not a legal move.
	 * @param state
	 * @param move
	 * @return
	 */
	
	public boolean underCheckAfterMove(State state, Move move) {
		State start = state.copy();
		Color underCheckColor = start.getTurn();
		if(checkMoveRule(start, move) == false)
			return false;
		makeMoveInState(start, move);
		return underCheck(start, underCheckColor);
	}
	
	/**
	 * check if using enpassant.
	 * This can be judged from the move.
	 * @param from
	 * @param to
	 * @param enpassant
	 * @param color
	 * @return
	 */

	boolean checkUseEnpassant(Position from, Position to, Position enpassant,
			Color color) {
		if (color == Color.WHITE) {
			if (enpassant != null
					&& Math.abs(from.getCol() - enpassant.getCol()) == 1
					&& from.getRow() == enpassant.getRow()
					&& to.getCol() == enpassant.getCol()
					&& to.getRow() == enpassant.getRow() + 1)
				return true;
			else
				return false;
		} else {
			if (enpassant != null
					&& Math.abs(from.getCol() - enpassant.getCol()) == 1
					&& from.getRow() == enpassant.getRow()
					&& to.getCol() == enpassant.getCol()
					&& to.getRow() == enpassant.getRow() - 1)
				return true;
			else
				return false;
		}
	}

	boolean checkPawnMove(Position from, Position to, State state,
			PieceKind promotion) {
		if (state.getPiece(from).getColor() == Color.WHITE) {
			if (to.getRow() == 7
					&& (promotion == null || promotion == PieceKind.KING || promotion == PieceKind.PAWN))
				return false;
			if (promotion != null && to.getRow() != 7)
				return false;
			if (checkUseEnpassant(from, to, state.getEnpassantPosition(),
					state.getPiece(from).getColor())) {
				return true;
			} else if (from.getCol() != to.getCol()) {
				if (Math.abs(from.getCol() - to.getCol()) != 1
						|| to.getRow() != from.getRow() + 1
						|| state.getPiece(to) == null) {
					return false;
				} else
					return true;
			} else {
				if (state.getPiece(to) != null)
					return false;
				else {
					if (from.getRow() == 1) {
						if (to.getRow() == 2)
							return true;
						else if (to.getRow() == 3
								&& state.getPiece(2, to.getCol()) == null)
							return true;
						else
							return false;
					} else if (to.getRow() != from.getRow() + 1) {
						return false;
					} else
						return true;
				}
			}
		} else {
			if (to.getRow() == 0
					&& (promotion == null || promotion == PieceKind.KING || promotion == PieceKind.PAWN))
				return false;
			if (promotion != null && to.getRow() != 0)
				return false;
			if (checkUseEnpassant(from, to, state.getEnpassantPosition(),
					state.getPiece(from).getColor())) {
				return true;
			} else if (from.getCol() != to.getCol()) {
				if (Math.abs(from.getCol() - to.getCol()) != 1
						|| to.getRow() != from.getRow() - 1
						|| state.getPiece(to) == null) {
					return false;
				} else
					return true;
			} else {
				if (state.getPiece(to) != null)
					return false;
				else {
					if (from.getRow() == 6) {
						if (to.getRow() == 5)
							return true;
						else if (to.getRow() == 4
								&& state.getPiece(5, to.getCol()) == null)
							return true;
						else
							return false;
					} else if (to.getRow() != from.getRow() - 1) {
						return false;
					} else
						return true;
				}
			}
		}
	}

	boolean checkRookMove(Position from, Position to, State state) {
		if (from.getCol() != to.getCol() && from.getRow() != to.getRow())
			return false;
		if (from.getCol() != to.getCol()) {
			int x = (int) Math.signum(to.getCol() - from.getCol());
			for (int i = from.getCol() + x; i != to.getCol(); i += x) {
				if (state.getPiece(from.getRow(), i) != null)
					return false;
			}
		} else {
			int x = (int) Math.signum(to.getRow() - from.getRow());
			for (int i = from.getRow() + x; i != to.getRow(); i += x) {
				if (state.getPiece(i, from.getCol()) != null)
					return false;
			}
		}
		return true;
	}

	boolean checkKnightMove(Position from, Position to) {
		if ((Math.abs(from.getCol() - to.getCol()) == 2 && Math.abs(from
				.getRow() - to.getRow()) == 1)
				|| (Math.abs(from.getCol() - to.getCol()) == 1 && Math.abs(from
						.getRow() - to.getRow()) == 2))
			return true;
		else
			return false;
	}

	boolean checkBishopMove(Position from, Position to, State state) {
		if (Math.abs(from.getCol() - to.getCol()) != Math.abs(from.getRow()
				- to.getRow()))
			return false;
		int x = (int) Math.signum(to.getCol() - from.getCol());
		int y = (int) Math.signum(to.getRow() - from.getRow());
		for (int tx = from.getCol() + x, ty = from.getRow() + y; tx != to
				.getCol() && ty != to.getRow(); tx += x, ty += y) {
			if (state.getPiece(ty, tx) != null)
				return false;
		}
		return true;

	}

	boolean checkKingMove(Position from, Position to, State state) {
		if (state.isCanCastleKingSide(Color.WHITE)
				&& from.equals(new Position(0, 4))
				&& to.equals(new Position(0, 6))
				&& state.getPiece(0, 5) == null && state.getPiece(0, 6) == null
				&& !underAttack(state, Color.BLACK, new Position(0, 4))
				&& !underAttack(state, Color.BLACK, new Position(0, 5))
				&& !underAttack(state, Color.BLACK, new Position(0, 6))) {
			return true;
		}
		if (state.isCanCastleQueenSide(Color.WHITE)
				&& from.equals(new Position(0, 4))
				&& to.equals(new Position(0, 2))
				&& state.getPiece(0, 3) == null && state.getPiece(0, 2) == null
				&& state.getPiece(0, 1) == null
				&& !underAttack(state, Color.BLACK, new Position(0, 4))
				&& !underAttack(state, Color.BLACK, new Position(0, 3))
				&& !underAttack(state, Color.BLACK, new Position(0, 2))) {
			return true;
		}
		if (state.isCanCastleKingSide(Color.BLACK)
				&& from.equals(new Position(7, 4))
				&& to.equals(new Position(7, 6))
				&& state.getPiece(7, 5) == null && state.getPiece(7, 6) == null
				&& !underAttack(state, Color.WHITE, new Position(7, 4))
				&& !underAttack(state, Color.WHITE, new Position(7, 5))
				&& !underAttack(state, Color.WHITE, new Position(7, 6))) {
			return true;
		}
		if (state.isCanCastleQueenSide(Color.BLACK)
				&& from.equals(new Position(7, 4))
				&& to.equals(new Position(7, 2))
				&& state.getPiece(7, 3) == null && state.getPiece(7, 2) == null
				&& state.getPiece(7, 1) == null
				&& !underAttack(state, Color.WHITE, new Position(7, 4))
				&& !underAttack(state, Color.WHITE, new Position(7, 3))
				&& !underAttack(state, Color.WHITE, new Position(7, 2))) {
			return true;
		}

		if (Math.abs(from.getCol() - to.getCol()) > 1
				|| Math.abs(from.getRow() - to.getRow()) > 1)
			return false;
		else
			return true;
	}

	boolean checkQueenMove(Position from, Position to, State state) {
		if (Math.abs(from.getCol() - to.getCol()) != Math.abs(from.getRow()
				- to.getRow())
				&& from.getCol() != to.getCol() && from.getRow() != to.getRow())
			return false;

		int x = (int) Math.signum(to.getCol() - from.getCol());
		int y = (int) Math.signum(to.getRow() - from.getRow());
		for (int tx = from.getCol() + x, ty = from.getRow() + y; tx != to
				.getCol() || ty != to.getRow(); tx += x, ty += y) {
			if (state.getPiece(ty, tx) != null)
				return false;
		}
		return true;

	}
	
	/**
	 * make this move in the state, i.e. change the state to another state.
	 * @param state
	 * @param move
	 */

	void makeMoveInState(State state, Move move) {
		Piece toPiece = state.getPiece(move.getTo());
		Piece fromPiece = state.getPiece(move.getFrom());

		// set empty move
		if (toPiece != null || fromPiece.getKind() == PieceKind.PAWN)
			state.setNumberOfMovesWithoutCaptureNorPawnMoved(0);
		else
			state.setNumberOfMovesWithoutCaptureNorPawnMoved(state
					.getNumberOfMovesWithoutCaptureNorPawnMoved() + 1);
		
		// set can castle
		setCanCastling(state, move);

		// set enpassant
		if (fromPiece.getKind() == PieceKind.PAWN && Math.abs(move.getFrom().getCol() - move.getTo().getCol()) == 1 && state.getPiece(move.getTo()) == null)
			state.setPiece(state.getEnpassantPosition(), null);

		state.setEnpassantPosition(null);
		if (fromPiece.getKind() == PieceKind.PAWN) {
			if (state.getTurn() == Color.WHITE && move.getFrom().getRow() == 1
					&& move.getTo().getRow() == 3)
				state.setEnpassantPosition(move.getTo());
			if (state.getTurn() == Color.BLACK && move.getFrom().getRow() == 6
					&& move.getTo().getRow() == 4)
				state.setEnpassantPosition(move.getTo());
		}

		// set turn
		state.setTurn(fromPiece.getColor() == Color.WHITE ? Color.BLACK
				: Color.WHITE);

		// set piece
		state.setPiece(move.getFrom(), null);

		state.setPiece(move.getTo(), fromPiece);
		
		// set state for castling.
		if (fromPiece.getKind() == PieceKind.KING
				&& Math.abs(move.getFrom().getCol() - move.getTo().getCol()) == 2) {
			if (fromPiece.getColor() == Color.WHITE
					&& move.getTo().getCol() == 2) {
				state.setPiece(0, 3, new Piece(Color.WHITE, PieceKind.ROOK));
				state.setPiece(0, 0, null);
			}
			if (fromPiece.getColor() == Color.WHITE
					&& move.getTo().getCol() == 6) {
				state.setPiece(0, 5, new Piece(Color.WHITE, PieceKind.ROOK));
				state.setPiece(0, 7, null);
			}
			if (fromPiece.getColor() == Color.BLACK
					&& move.getTo().getCol() == 2) {
				state.setPiece(7, 3, new Piece(Color.BLACK, PieceKind.ROOK));
				state.setPiece(7, 0, null);
			}
			if (fromPiece.getColor() == Color.BLACK
					&& move.getTo().getCol() == 6) {
				state.setPiece(7, 5, new Piece(Color.BLACK, PieceKind.ROOK));
				state.setPiece(7, 7, null);
			}
		}
		if (move.getPromoteToPiece() != null)
			state.setPiece(move.getTo(),
					new Piece(fromPiece.getColor(), move.getPromoteToPiece()));
	}

	/**
	 * set the canCastling variables of the state
	 * @param state
	 * @param move
	 */
	void setCanCastling(State state, Move move) {
		if (move.getFrom().equals(new Position(0, 4))
				|| move.getTo().equals(new Position(0, 4))) {
			state.setCanCastleKingSide(Color.WHITE, false);
			state.setCanCastleQueenSide(Color.WHITE, false);
		}
		if (move.getFrom().equals(new Position(7, 4))
				|| move.getTo().equals(new Position(7, 4))) {
			state.setCanCastleKingSide(Color.BLACK, false);
			state.setCanCastleQueenSide(Color.BLACK, false);
		}
		if (move.getFrom().equals(new Position(0, 0))
				|| move.getTo().equals(new Position(0, 0))) {
			state.setCanCastleQueenSide(Color.WHITE, false);
		}
		if (move.getFrom().equals(new Position(0, 7))
				|| move.getTo().equals(new Position(0, 7))) {
			state.setCanCastleKingSide(Color.WHITE, false);
		}
		if (move.getFrom().equals(new Position(7, 0))
				|| move.getTo().equals(new Position(7, 0))) {
			state.setCanCastleQueenSide(Color.BLACK, false);
		}
		if (move.getFrom().equals(new Position(7, 7))
				|| move.getTo().equals(new Position(7, 7))) {
			state.setCanCastleKingSide(Color.BLACK, false);
		}
	}

	/**
	 * check the game result
	 * @param state
	 */
	void setGameResult(State state)
	{
		if (state.getNumberOfMovesWithoutCaptureNorPawnMoved() == 100){
			state.setGameResult(new GameResult(null,
					GameResultReason.FIFTY_MOVE_RULE));
			return;
		}

		StateExplorerImpl e = new StateExplorerImpl();
		Set<Move> possibleMoves = e.getPossibleMovesOneSide(state, state.getTurn());
		if(possibleMoves.isEmpty()){
			System.out.println(state);
			if(this.underCheck(state, state.getTurn()))
				state.setGameResult(new GameResult((state.getTurn()==Color.WHITE?Color.BLACK:Color.WHITE), GameResultReason.CHECKMATE));
			else
				state.setGameResult(new GameResult(null, GameResultReason.STALEMATE));
		}
	}
	
	/**
	 * check move and make state.
	 */	
	@Override
	public void makeMove(State state, Move move) throws IllegalMove {
		//check if the move is legal
		checkStatus(state, move);
		//make this move
		makeMoveInState(state, move);
		//check the game result status
		setGameResult(state);
	}

}
