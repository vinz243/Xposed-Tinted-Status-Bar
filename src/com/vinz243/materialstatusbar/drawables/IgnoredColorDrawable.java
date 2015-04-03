package com.vinz243.materialstatusbar.drawables;

import android.graphics.drawable.ColorDrawable;

public class IgnoredColorDrawable extends ColorDrawable {
	/* 
	 * Class that's checked with instanceof to be ignored when we set the ActionBar
	 * color again
	 */

	public IgnoredColorDrawable(int color) {
		super(color);
	}
}
