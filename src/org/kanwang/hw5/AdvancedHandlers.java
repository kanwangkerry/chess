package org.kanwang.hw5;

import org.kanwang.hw3.Presenter.View;

public class AdvancedHandlers {
	public static void handleAnimationUpdate(int frow, int fcol, int trow, int tcol, double progress, View view)
	{
		frow = 7-frow;
		trow = 7-trow;
		view.setAnimaitonUpdate(frow, fcol, trow, tcol, progress);
	}
	
	public static void handleAnimationBegin(int row, int col, View view){
		row = 7 - row;
		view.setAnimationBegin(row, col);
	}
	public static void handleAnimationEnd(int row, int col, View view){
		row = 7-row;
		view.setAnimationEnd(row, col);
	}
	
}
