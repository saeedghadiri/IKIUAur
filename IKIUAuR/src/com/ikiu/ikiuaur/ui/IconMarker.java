package com.ikiu.ikiuaur.ui;

import com.ikiu.ikiuaur.uiobjects.PaintableIcon;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;

/**
 * This class extends Marker and draws an icon;
 * 
 */
public class IconMarker extends Marker {

	private Bitmap bitmap = null;

	public IconMarker(String name, double latitude, double longitude,
			double altitude, int color, Bitmap bitmap, Typeface face) {
		super(name, latitude, longitude, altitude, color,face);
		this.bitmap = bitmap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawIcon(Canvas canvas) {
		if (canvas == null || bitmap == null)
			throw new NullPointerException();

		// gpsSymbol is defined in Marker
		if (gpsSymbol == null)
			gpsSymbol = new PaintableIcon(bitmap, 96, 96);
		super.drawIcon(canvas);
	}
}
