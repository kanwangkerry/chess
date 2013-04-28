package org.kanwang.hw5;

import org.kanwang.hw3.Presenter.View;

import com.google.gwt.animation.client.Animation;

public class AnimationMove extends Animation{
	
	int frow, trow, fcol, tcol;
	View view;
	
	public AnimationMove(int fromRow, int fromCol, int toRow, int toCol, View view){
		frow = fromRow;
		trow = toRow;
		fcol = fromCol;
		tcol = toCol;
		this.view = view;
	}
	
	@Override
	protected void onStart()
	{
		AdvancedHandlers.handleAnimationBegin(frow, fcol, view);
	}
	
	@Override
	protected void onComplete()
	{
		AdvancedHandlers.handleAnimationEnd(trow, tcol, view);
	}

	@Override
	protected void onUpdate(double progress) {
		AdvancedHandlers.handleAnimationUpdate(frow, fcol, trow, tcol, progress, view);
	}

}
