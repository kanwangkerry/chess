package org.kanwang.hw9;

import org.kanwang.hw2_5.StateExplorerImpl;
import org.shared.chess.Move;
import org.shared.chess.State;

public class AIPlayer {
	public Move getMove(State s){
		StateExplorerImpl e = new StateExplorerImpl();
		return e.getPossibleMoves(s).iterator().next();
	}

}
