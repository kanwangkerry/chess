package org.kanwang.hw2_5;

import java.util.HashSet;
import java.util.Set;

import org.kanwang.hw2.StateChangerImpl;
import org.shared.chess.Color;
import org.shared.chess.Move;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.Position;
import org.shared.chess.State;
import org.shared.chess.StateExplorer;

public class StateExplorerImpl implements StateExplorer {

	/**
	 * get the possible moves in this state.
	 */
	@Override
	public Set<Move> getPossibleMoves(State state) {
		HashSet<Move> result = new HashSet<Move>();
		Set<Position> positions = this.getPossibleStartPositions(state);
		for (Position start : positions) {
			result.addAll(this.getPossibleMovesFromPosition(state, start));
		}
		return result;
	}
	
	/**
	 * get the possible moves of color c.
	 * @param state
	 * @param c
	 * @return
	 */

	public Set<Move> getPossibleMovesOneSide(State state, Color c) {
		HashSet<Move> result = new HashSet<Move>();
		Set<Position> positions = this.getPossibleStartPositions(state);
		for (Position start : positions) {
			if(state.getPiece(start).getColor() == c)
			result.addAll(this.getPossibleMovesFromPosition(state, start));
		}
		return result;

	}
	
	/**
	 * get the possible moves from the start position
	 */

	@Override
	public Set<Move> getPossibleMovesFromPosition(State state, Position start) {
		HashSet<Move> result = new HashSet<Move>();
		Piece startPiece = state.getPiece(start);
		Position to;
		Piece toPiece;
		Move move;
		StateChangerImpl stateChanger = new StateChangerImpl();
		if(startPiece == null)
			return result;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				to = new Position(i, j);
				toPiece = state.getPiece(to);
				if (toPiece != null && startPiece.getColor() == toPiece.getColor())
					continue;
				if (start.equals(to))
					continue;
				if ((startPiece.getColor() == Color.WHITE
						&& startPiece.getKind() == PieceKind.PAWN && start
						.getRow() == 6)
						|| (startPiece.getColor() == Color.BLACK
								&& startPiece.getKind() == PieceKind.PAWN && start
								.getRow() == 1)) {
					move = new Move(start, to, PieceKind.BISHOP);
					if (!stateChanger.checkMoveRule(state, move))
						continue;
					if (stateChanger.underCheckAfterMove(state, move))
						continue;
					result.add(move);
					move = new Move(start, to, PieceKind.KNIGHT);
					result.add(move);
					move = new Move(start, to, PieceKind.ROOK);
					result.add(move);
					move = new Move(start, to, PieceKind.QUEEN);
					result.add(move);
				} else {
					move = new Move(start, to, null);
					if (stateChanger.underCheckAfterMove(state, move))
						continue;
					if (stateChanger.checkMoveRule(state, move))
						result.add(move);
				}
			}
		}
		return result;
	}

	/**
	 * get the possbile start position
	 */
	@Override
	public Set<Position> getPossibleStartPositions(State state) {
		HashSet<Position> result = new HashSet<Position>();
		if(state.getGameResult() != null)
			return result;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (state.getPiece(i, j) != null 
						&& state.getPiece(i, j).getColor() == state.getTurn() 
						&& !this.getPossibleMovesFromPosition(state, new Position(i, j)).isEmpty())
					result.add(new Position(i, j));
			}
		}
		return result;
	}

}
