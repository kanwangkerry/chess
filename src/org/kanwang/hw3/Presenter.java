package org.kanwang.hw3;

import java.util.HashSet;
import java.util.Set;

import org.kanwang.hw2.StateChangerImpl;
import org.kanwang.hw2_5.StateExplorerImpl;
import org.kanwang.hw5.AnimationMove;
import org.kanwang.hw6.Match;
import org.kanwang.hw6.client.GameService;
import org.kanwang.hw6.client.GameServiceAsync;
import org.kanwang.hw6.client.LoginInfo;
import org.kanwang.hw9.AIPlayer;
import org.shared.chess.Color;
import org.shared.chess.GameResult;
import org.shared.chess.GameResultReason;
import org.shared.chess.Move;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.Position;
import org.shared.chess.State;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Presenter {
	public interface View {
		/**
		 * Renders the piece at this position. If piece is null then the
		 * position is empty.
		 */
		void setPiece(int row, int col, Piece piece);

		/**
		 * Turns the highlighting on or off at this cell. Cells that can be
		 * clicked should be highlighted.
		 */
		void setHighlighted(int row, int col, boolean highlighted);

		/**
		 * Indicate whose turn it is.
		 */
		void setWhoseTurn(Color color);

		/**
		 * Indicate whether the game is in progress or over.
		 */
		void setGameResult(GameResult gameResult);

		/**
		 * select a piece
		 */
		void setSelected(int row, int col, boolean selected);

		/**
		 * Clear the board
		 */
		void clearBoard();

		/**
		 * show possible end
		 */
		void showEndOnBoard(int row, int col, boolean isEnd);

		/**
		 * show promotion
		 */
		void showPromotionOnBoard(boolean isWaiting);

		/**
		 * animation begin event
		 */
		void setAnimationBegin(int row, int col);

		/**
		 * animation end event
		 */
		void setAnimationEnd(int row, int col);

		/**
		 * animation update event
		 */
		void setAnimaitonUpdate(int frow, int fcol, int trow, int tcol,
				double progress);

		/**
		 * if using animation
		 */
		boolean useAnimation();

		/**
		 * play move sound
		 */
		void playMoveSound();

		/**
		 * game over sound
		 */
		void playOverSound();

		/**
		 * set history
		 */
		// void setHistory();

		/**
		 * use memmory
		 */
		// void saveLocalMemory();
		//
		// State loadLocalMemory();

		/**
		 * Set the match info in the match info box
		 */
		void setMatchInfo();

		/**
		 * update rating
		 */
		void updateRating();

		void setRatingInfo(int rating);
	}

	State state = new State();

	private View view;

	// row & col on the state
	Position selectedPosition = null;

	private Set<Position> possibleEnd = new HashSet<Position>();
	private StateChangerImpl sChanger = new StateChangerImpl();
	private StateExplorerImpl sExplorer = new StateExplorerImpl();

	private PieceKind promotion = null;
	private Position endPositionWaitPromotion = null;

	public int steps = 0;

	private LoginInfo user;
	private Match match;

	private boolean vsAI = false;

	public void setView(View view) {
		this.view = view;
	}

	public void init_state() {
		this.selectedPosition = null;
		this.endPositionWaitPromotion = null;
		this.promotion = null;
		this.possibleEnd.clear();
		this.setState(state);
	}

	public void setState(State s) {
		this.state = s;
		view.setWhoseTurn(state.getTurn());
		view.setGameResult(state.getGameResult());
		view.clearBoard();
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setPiece(r, c, state.getPiece(r, c));
			}
		}
		// view.setHistory();
	}

	public void makeRomoteMove(Position from, Position to) {
		final int r = from.getRow();
		final int c = from.getCol();
		if (this.vsAI == false) {
			GameServiceAsync gameService = GWT.create(GameService.class);
			gameService.submitMove(
					Presenter.moveToString(new Move(from, to, promotion)),
					match, user.getEmailAddress(), new AsyncCallback<Match>() {
						public void onFailure(Throwable error) {
						}

						@Override
						public void onSuccess(Match result) {
							try {
								if (Presenter.getStateFromString(
										result.getState()).getGameResult() != null) {
									view.updateRating();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							selectedPosition = null;
							showPromotion(false);
							showPossibleEnd(false);
							view.setSelected(7 - r, c, false);
						}
					});
		} else {
			this.makeMoveStateAndView(from, to);
			this.match.setState(Presenter.makeStringfromState(this.state));
			Timer t = new Timer() {
				public void run() {
					AIPlayer ai = new AIPlayer();
					final Move temp = ai.getMove(state);
					promotion = temp.getPromoteToPiece();
					makeMoveStateAndView(temp.getFrom(), temp.getTo());
					match.setState(Presenter
							.makeStringfromState(state));
				}
			};
			t.schedule(500);
		}
	}

	void makeMoveStateAndView(Position from, Position to) {

		if (view.useAnimation()) {
			Animation moveAni = new AnimationMove(from.getRow(), from.getCol(),
					to.getRow(), to.getCol(), view);
			moveAni.run(500);

			sChanger.makeMove(state, new Move(from, to, promotion));
			view.setSelected(7 - from.getRow(), from.getCol(), false);

			selectedPosition = null;
			showPromotion(false);
			showPossibleEnd(false);
		} else {
			sChanger.makeMove(state, new Move(from, to, promotion));
			setState(state);
			view.setSelected(7 - from.getRow(), from.getCol(), false);

			selectedPosition = null;
			showPromotion(false);
			showPossibleEnd(false);
			view.playMoveSound();

		}
	}

	public void selectPiece(int row, int col) {
		if (!match.isUserTurn(this.user.getEmailAddress()))
			return;
		if (match.getB() == null || match.getW() == null)
			return;

		row = 7 - row;
		if (possibleEnd.contains(new Position(row, col))
				&& selectedPosition != null) {
			if (state.getPiece(selectedPosition).getKind() == PieceKind.PAWN
					&& ((state.getPiece(selectedPosition).getColor() == Color.WHITE && selectedPosition
							.getRow() == 6) || (state
							.getPiece(selectedPosition).getColor() == Color.BLACK && selectedPosition
							.getRow() == 1))) {
				this.endPositionWaitPromotion = new Position(row, col);
				this.showPromotion(true);
				return;
			}
			// move animation
			this.makeRomoteMove(selectedPosition, new Position(row, col));
			return;
		}

		if (state.getPiece(row, col) == null
				|| state.getPiece(row, col).getColor() != state.getTurn())
			return;

		if (selectedPosition == null) {
			selectedPosition = new Position(row, col);
			this.showPossibleEnd(true);
			view.setSelected(7 - row, col, true);
		} else {
			if (selectedPosition.getRow() != row
					|| selectedPosition.getCol() != col) {
				showPromotion(false);
				view.setSelected(7 - selectedPosition.getRow(),
						selectedPosition.getCol(), false);
				selectedPosition = new Position(row, col);
				showPossibleEnd(true);
				view.setSelected(7 - row, col, true);
			} else {
				selectedPosition = null;
				showPossibleEnd(false);
				showPromotion(false);
				view.setSelected(7 - row, col, false);
			}
		}
	}

	private void showPossibleEnd(boolean show) {

		if (!show) {
			for (Position e : possibleEnd) {
				view.showEndOnBoard(7 - e.getRow(), e.getCol(), show);
			}
			possibleEnd.clear();
		} else {
			Set<Move> possibleMove;
			for (Position e : possibleEnd) {
				view.showEndOnBoard(7 - e.getRow(), e.getCol(), false);
			}
			possibleEnd.clear();
			possibleMove = sExplorer.getPossibleMovesFromPosition(state,
					selectedPosition);
			for (Move e : possibleMove) {
				possibleEnd.add(e.getTo());
			}
			for (Position e : possibleEnd) {
				view.showEndOnBoard(7 - e.getRow(), e.getCol(), show);
			}
		}
	}

	private void showPromotion(boolean wait) {
		if (wait)
			view.showPromotionOnBoard(wait);
		else {
			view.showPromotionOnBoard(wait);
			this.promotion = null;
			this.endPositionWaitPromotion = null;
		}

	}

	public void overCell(int row, int col) {
		view.setHighlighted(row, col, true);
	}

	public void outCell(int row, int col) {
		view.setHighlighted(row, col, false);
	}

	public void handlePromotion(int row, int col) {
		if (this.endPositionWaitPromotion == null)
			return;
		switch (col) {
		case 0:
			this.promotion = PieceKind.BISHOP;
			break;
		case 1:
			this.promotion = PieceKind.QUEEN;
			break;
		case 2:
			this.promotion = PieceKind.KNIGHT;
			break;
		case 3:
			this.promotion = PieceKind.ROOK;
			break;
		default:
			break;
		}
		this.makeRomoteMove(selectedPosition, endPositionWaitPromotion);
		// this.makeMoveStateAndView(selectedPosition,
		// endPositionWaitPromotion);
	}

	public void setMatch(Match m) {
		this.match = m;
	}

	public Match getMatch() {
		return this.match;
	}

	public LoginInfo getUser() {
		return this.user;
	}

	public void setMovePromotion(PieceKind p) {
		this.promotion = p;
	}

	public void setMatchInfo() {
		view.setMatchInfo();
	}

	public void setRatingInfo(int rating) {
		view.setRatingInfo(rating);
	}
	
	public void setPlayAI(boolean vsAI){
		this.vsAI = vsAI;
	}
	
	public boolean getPlayAI(){
		return this.vsAI;
	}

	// public void saveState() {
	// view.saveLocalMemory();
	// }
	//
	// public State loadState() {
	// return view.loadLocalMemory();
	// }

	public static String makeStringfromState(State input) {
		StringBuffer s = new StringBuffer();
		// turn
		s.append(input.getTurn().toString() + "&");
		// board
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (input.getPiece(i, j) == null)
					s.append("null&");
				else
					s.append(input.getPiece(i, j).toString() + "&");
			}
		}
		// canCastling
		s.append(input.isCanCastleKingSide(Color.WHITE) + "&");
		s.append(input.isCanCastleKingSide(Color.BLACK) + "&");
		s.append(input.isCanCastleQueenSide(Color.WHITE) + "&");
		s.append(input.isCanCastleQueenSide(Color.BLACK) + "&");

		// enpassant
		if (input.getEnpassantPosition() == null) {
			s.append(8 + "&");
			s.append(8 + "&");
		} else {
			s.append(input.getEnpassantPosition().getRow() + "&");
			s.append(input.getEnpassantPosition().getCol() + "&");
		}

		// result
		if (input.getGameResult() == null) {
			s.append("N&W&");
		} else if (input.getGameResult().getGameResultReason() == GameResultReason.CHECKMATE) {
			s.append("C&" + input.getGameResult().getWinner() + "&");
		} else if (input.getGameResult().getGameResultReason() == GameResultReason.FIFTY_MOVE_RULE) {
			s.append("F&W&");
		} else if (input.getGameResult().getGameResultReason() == GameResultReason.STALEMATE) {
			s.append("S&W&");
		}

		// number of move

		s.append(input.getNumberOfMovesWithoutCaptureNorPawnMoved());

		return s.toString();
	}

	public void setUser(LoginInfo u) {
		this.user = u;
	}

	public static State getStateFromString(String s) throws Exception {
		State result = new State();
		String items[] = s.split("&");
		switch (items[0].charAt(0)) {
		case 'W':
			result.setTurn(Color.WHITE);
			break;
		case 'B':
			result.setTurn(Color.BLACK);
			break;
		}
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				result.setPiece(i, j, stringToPiece(items[1 + i * 8 + j]));
			}
		}
		if (items[65].endsWith("false"))
			result.setCanCastleKingSide(Color.WHITE, false);
		if (items[66].endsWith("false"))
			result.setCanCastleKingSide(Color.BLACK, false);
		if (items[67].endsWith("false"))
			result.setCanCastleQueenSide(Color.WHITE, false);
		if (items[68].endsWith("false"))
			result.setCanCastleQueenSide(Color.BLACK, false);
		int a, b;
		a = Integer.parseInt(items[69]);
		b = Integer.parseInt(items[70]);
		if (a != 8 && b != 8)
			result.setEnpassantPosition(new Position(a, b));

		switch (items[71].charAt(0)) {
		case 'N':
			result.setGameResult(null);
			break;
		case 'C':
			if (items[72].endsWith("W"))
				result.setGameResult(new GameResult(Color.WHITE,
						GameResultReason.CHECKMATE));
			else
				result.setGameResult(new GameResult(Color.BLACK,
						GameResultReason.CHECKMATE));
			break;
		case 'F':
			result.setGameResult(new GameResult(null,
					GameResultReason.FIFTY_MOVE_RULE));
			break;
		case 'S':
			result.setGameResult(new GameResult(null,
					GameResultReason.STALEMATE));
			break;
		default:
			result.setGameResult(null);
			break;
		}
		result.setNumberOfMovesWithoutCaptureNorPawnMoved(Integer
				.parseInt(items[73]));

		return result;
	}

	private static Piece stringToPiece(String s) {
		Piece a = null;
		if (s.charAt(1) == 'W') {
			switch (s.substring(3, s.length()).charAt(0)) {
			case 'P':
				a = new Piece(Color.WHITE, PieceKind.PAWN);
				break;
			case 'B':
				a = new Piece(Color.WHITE, PieceKind.BISHOP);
				break;
			case 'K':
				if (s.substring(3, s.length()).charAt(1) == 'I')
					a = new Piece(Color.WHITE, PieceKind.KING);
				else
					a = new Piece(Color.WHITE, PieceKind.KNIGHT);
				break;
			case 'Q':
				a = new Piece(Color.WHITE, PieceKind.QUEEN);
				break;
			case 'R':
				a = new Piece(Color.WHITE, PieceKind.ROOK);
				break;
			default:
				break;
			}
		} else {
			switch (s.substring(3, s.length()).charAt(0)) {
			case 'P':
				a = new Piece(Color.BLACK, PieceKind.PAWN);
				break;
			case 'B':
				a = new Piece(Color.BLACK, PieceKind.BISHOP);
				break;
			case 'K':
				if (s.substring(3, s.length()).charAt(1) == 'I')
					a = new Piece(Color.BLACK, PieceKind.KING);
				else
					a = new Piece(Color.BLACK, PieceKind.KNIGHT);
				break;
			case 'Q':
				a = new Piece(Color.BLACK, PieceKind.QUEEN);
				break;
			case 'R':
				a = new Piece(Color.BLACK, PieceKind.ROOK);
				break;
			default:
				break;
			}
		}
		return a;
	}

	public static String moveToString(Move move) {
		String result = move.getFrom().toString() + "&"
				+ move.getTo().toString() + "&";
		if (move.getPromoteToPiece() != null) {
			switch (move.getPromoteToPiece()) {
			case BISHOP:
				result += "B";
				break;
			case KING:
				result += "K";
				break;
			case KNIGHT:
				result += "N";
				break;
			case PAWN:
				result += "P";
				break;
			case QUEEN:
				result += "Q";
				break;
			case ROOK:
				result += "R";
				break;
			default:
				break;
			}
		}
		return result;
	}

	public static Move stringToMove(String m) {
		Move result;
		String temp[] = m.split("&");
		Position from = new Position(Integer.parseInt(temp[0].substring(1, 2)),
				Integer.parseInt(temp[0].substring(3, 4)));
		Position to = new Position(Integer.parseInt(temp[1].substring(1, 2)),
				Integer.parseInt(temp[1].substring(3, 4)));
		PieceKind promotion = null;
		if (temp.length == 3) {
			switch (temp[2].charAt(0)) {
			case 'B':
				promotion = PieceKind.BISHOP;
				break;
			case 'N':
				promotion = PieceKind.KNIGHT;
				break;
			case 'Q':
				promotion = PieceKind.QUEEN;
				break;
			case 'R':
				promotion = PieceKind.ROOK;
				break;
			default:
				promotion = null;
				break;
			}
		}
		result = new Move(from, to, promotion);
		return result;
	}

}
