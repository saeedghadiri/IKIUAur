package com.ikiu.ikiuaur.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.example.ikiuaur.R;
import com.ikiu.ikiuaur.ui.IconMarker;
import com.ikiu.ikiuaur.ui.Marker;

/**
 * This class is for adding data from local data sources
 */
public class LocalDataSource extends DataSource {

	private List<Marker> cachedMarkers = new ArrayList<Marker>();
	private List<Marker> xmlMarkers;
	private static Bitmap icon = null;
	Context ctx;
	private Typeface face;
	private static int color = Color.DKGRAY;
	private LocationXmlParser lxp;

	public LocalDataSource(Resources res, Context ctx) {
		if (res == null)
			throw new NullPointerException();

		createIcon(res);

		face = Typeface.createFromAsset(ctx.getAssets(), "Font/hemmat.ttf");
		lxp = new LocationXmlParser();
		this.ctx = ctx;

	}

	protected void createIcon(Resources res) {
		if (res == null)
			throw new NullPointerException();

		icon = BitmapFactory.decodeResource(res, R.drawable.icon);
	}

	public List<Marker> getMarkers() {
		/*
		Marker atl = new IconMarker(ctx.getResources().getString(
				R.string.bazarcheh), 35.655237, 51.387857, 0, color, icon, face);
		cachedMarkers.add(atl);
	*/
		try {
			xmlMarkers = lxp.parse(ctx, color, icon, face);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			Log.e("parserError", e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("parserError", e.toString());
		}

		if (xmlMarkers != null) {
			cachedMarkers.addAll(xmlMarkers);
		}


		return cachedMarkers;
	}

}
