package com.ikiu.ikiuaur.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.ikiu.ikiuaur.ui.IconMarker;
import com.ikiu.ikiuaur.ui.Marker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Xml;

/*
 * This class parses data from assets folder xml file
 */

public class LocationXmlParser {

	private static final String ns = null;
	private InputStream in;
	private static final String XMLSOURCE = "locations.xml";

	public ArrayList<Marker> parse(Context ctx, int color, Bitmap icon,
			Typeface face) throws XmlPullParserException, IOException {

		try {
			Log.d("parser", "starting");
			in = ctx.getApplicationContext().getAssets().open(XMLSOURCE);
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			Log.d("parser", "readingLocation");
			return readLocation(parser, color, icon, face);
		} finally {
			in.close();
		}
	}

	private ArrayList<Marker> readLocation(XmlPullParser parser, int color,
			Bitmap icon, Typeface face) throws XmlPullParserException,
			IOException {

		ArrayList<Marker> markers = new ArrayList<Marker>();

		parser.require(XmlPullParser.START_TAG, ns, "locations");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("location")) {
				Log.d("parser", "readingMarker");
				markers.add(readMarker(parser, color, icon, face));
			} else {
				skip(parser);
			}
		}
		return markers;
	}

	private Marker readMarker(XmlPullParser parser, int color, Bitmap icon,
			Typeface face) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, ns, "location");
		String name = null;
		double lat = 0, lon = 0, alt = 0;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String temp = parser.getName();
			if (temp.equals("name")) {
				name = readName(parser);
			} else if (temp.equals("lat")) {
				lat = readLat(parser);
			} else if (temp.equals("long")) {
				lon = readLon(parser);
			} else if (temp.equals("alt")) {
				alt = readAlt(parser);
			} else {
				skip(parser);
			}
		}
		Log.d("read", name);
		return new IconMarker(name, lat, lon, alt, color, icon, face);
	}

	private String readName(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String name = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");
		return name;
	}

	private double readLat(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, "lat");
		double lat = Double.valueOf(readText(parser));
		parser.require(XmlPullParser.END_TAG, ns, "lat");
		return lat;
	}

	private double readLon(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, "long");
		double lon = Double.valueOf(readText(parser));
		parser.require(XmlPullParser.END_TAG, ns, "long");
		return lon;
	}

	private double readAlt(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, "alt");
		double alt = Double.valueOf(readText(parser));
		parser.require(XmlPullParser.END_TAG, ns, "alt");
		return alt;
	}

	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
