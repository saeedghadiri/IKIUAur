package com.ikiu.ikiuaur.uiobjects;

import java.text.BreakIterator;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;

public class PaintableBoxedText extends PaintableObject {

	private float width = 0, height = 0;
	private float areaWidth = 0, areaHeight = 0;
	private ArrayList<CharSequence> lineList = null;
	private float linesHeight = 0;
	private float maxLinesWidth = 0;
	private float pad = 0;

	private CharSequence txt = null;
	private float fontSize = 14;
	private int borderColor = Color.rgb(255, 255, 255);
	private int backgroundColor = Color.argb(160, 0, 0, 0);
	private int textColor = Color.rgb(255, 255, 255);

	private Typeface face = null;

	public PaintableBoxedText(CharSequence txtInit, float fontSizeInit,
			float maxWidth, Typeface face) {
		this(txtInit, fontSizeInit, maxWidth, Color.rgb(255, 255, 255), Color
				.argb(128, 0, 0, 0), Color.rgb(255, 255, 255), face);
	}

	public PaintableBoxedText(CharSequence txtInit, float fontSizeInit,
			float maxWidth, int borderColor, int bgColor, int textColor,
			Typeface face) {
		super(face);
		set(txtInit, fontSizeInit, maxWidth, borderColor, bgColor, textColor,
				face);
	}

	public void set(CharSequence txtInit, float fontSizeInit, float maxWidth,
			int borderColor, int bgColor, int textColor, Typeface face) {
		if (txtInit == null)
			throw new NullPointerException();

		this.borderColor = borderColor;
		this.backgroundColor = bgColor;
		this.textColor = textColor;
		this.pad = getTextAsc();
		this.face = face;

		set(txtInit, fontSizeInit, maxWidth);
	}

	public void set(CharSequence txtInit, float fontSizeInit, float maxWidth) {
		if (txtInit == null)
			throw new NullPointerException();

		try {
			calcBoxDimensions(txtInit, fontSizeInit, maxWidth);
		} catch (Exception ex) {
			ex.printStackTrace();
			calcBoxDimensions("TEXT PARSE ERROR", 12, 200);
		}
	}

	private void calcBoxDimensions(CharSequence txtInit, float fontSizeInit,
			float maxWidth) {
		if (txtInit == null)
			throw new NullPointerException();

		setFontSize(fontSizeInit, face);

		txt = txtInit;
		fontSize = fontSizeInit;
		areaWidth = maxWidth - pad;
		linesHeight = getTextAsc() + getTextDesc();

		if (lineList == null)
			lineList = new ArrayList<CharSequence>();
		else
			lineList.clear();

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(txt.toString());

		int start = boundary.first();
		int end = boundary.next();
		int prevEnd = start;
		while (end != BreakIterator.DONE) {
			CharSequence line = txt.subSequence(start, end);
			CharSequence prevLine = txt.subSequence(start, prevEnd);
			float lineWidth = getTextWidth(line, 0, line.length());

			if (lineWidth > areaWidth) {
				// If the first word is longer than lineWidth
				// prevLine is empty and should be ignored
				if (prevLine.length() > 0)
					lineList.add(prevLine);

				start = prevEnd;
			}

			prevEnd = end;
			end = boundary.next();
		}
		CharSequence line = txt.subSequence(start, prevEnd);
		lineList.add(line);

		maxLinesWidth = 0;
		for (CharSequence seq : lineList) {
			float lineWidth = getTextWidth(seq, 0, seq.length());
			if (maxLinesWidth < lineWidth)
				maxLinesWidth = lineWidth;
		}
		areaWidth = maxLinesWidth;
		areaHeight = linesHeight * lineList.size();

		width = areaWidth + pad * 2;
		height = areaHeight + pad * 2;
	}

	@Override
	public void paint(Canvas canvas) {
		if (canvas == null)
			throw new NullPointerException();

		canvas.save();
		canvas.translate(-width / 2, -height / 2);

		setFontSize(fontSize, face);

		setFill(true);
		setColor(backgroundColor);
		paintRoundedRect(canvas, x, y, width, height);

		setFill(false);
		setColor(borderColor);
		paintRoundedRect(canvas, x, y, width, height);

		float lineX = x + pad;
		float lineY = y + pad + getTextAsc();
		int i = 0;
		for (CharSequence line : lineList) {
			setFill(true);
			setStrokeWidth(0);
			setColor(textColor);
			paintText(canvas, lineX, lineY + (linesHeight * i), line, 0,
					line.length());
			i++;
		}

		canvas.restore();
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}
}
