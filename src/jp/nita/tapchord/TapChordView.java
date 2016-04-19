package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class TapChordView extends View {
	static boolean debugMode = false;

	int width, height, originalX, originalY, originalScroll;
	int situation, destination, step, scroll, upper, destScale;
	int playing, playingX, playingY, tappedX, destinationScroll;
	int playingID;
	boolean darken, vibration, keyboardIndicatorsTapped;
	int keyState[][] = new int[15][3];
	int lastKeyState[][] = new int[15][3];
	int statusbarKeycodes[] = {KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4};
	int keycodes[][] = { { 0, 0, 0 },
			{ 0, 0, 0 },
			{ KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_Z },
			{ KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_X },
			{ KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_C },
			{ KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_V },
			{ KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_B },
			{ KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_N },
			{ KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_M },
			{ KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_COMMA },
			{ KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_L, KeyEvent.KEYCODE_PERIOD },
			{ KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_SEMICOLON, KeyEvent.KEYCODE_SLASH },
			{ KeyEvent.KEYCODE_GRAVE, KeyEvent.KEYCODE_APOSTROPHE, KeyEvent.KEYCODE_BACKSLASH },
			{ 0, 0, 0 },
			{ 0, 0, 0 } };
	int specialKeycodes[] = {KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_0,
			KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER};

	int scale = 0;
	int soundRange = 0;
	int pulling = 0;

	int statusbarFlags[] = { 0, 0, 0, 0 };
	int lastTapped = -1;
	long lastTappedTime = -1;
	int toolbarPressed = -1;
	int scalePressed = Statics.FARAWAY;
	float stepMax = 1.0f;
	float barsShowingRate = 1.0f;

	private Vibrator vib;

	Handler handler = new Handler();

	Integer notesOfChord[] = new Integer[0];
	Sound sound = null;

	SparseIntArray taps = new SparseIntArray();
	List<Shape> shapes = new ArrayList<Shape>();
	
	Object keyWatcher = new Object();
	Timer stopTimer = null;
	Timer cancelSwitchingStatusBarTimer = null;
	Timer cancelSpecialKeyTimer = null;
	long lastKeyWatchedTime;
	boolean shiftKeyPressed = false;

	public TapChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		situation = Statics.SITUATION_NORMAL;
		destination = Statics.SITUATION_NORMAL;
		step = 0;
		playing = 0;
		scroll = 0;
		upper = 0;
		darken = false;
		vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void init(Context context) {
		getPreferenceValues();
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		int x, y, c, d, delta;
		float w;
		Paint paint = new Paint();
		Paint textPaint = new Paint();
		RectF rect;
		String str = "";
		FontMetrics fontMetrics = textPaint.getFontMetrics();

		width = canvas.getWidth();
		height = canvas.getHeight();

		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		int rad = Statics.getRadiusOfButton(height);

		textPaint.setAntiAlias(true);
		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));
		textPaint.setTextSize(rad / 2);

		rect = new RectF(0, 0, width, height);
		paint.setColor(Statics.getColor(Statics.COLOR_WHITE, 0, darken));
		canvas.drawRect(rect, paint);

		if (situation == Statics.SITUATION_TRANSPOSE || situation == Statics.SITUATION_TRANSPOSING
				|| destination == Statics.SITUATION_TRANSPOSE || destination == Statics.SITUATION_TRANSPOSING) {
			paint.setStyle(Style.FILL);
			paint.setColor(Statics.getColor(Statics.COLOR_PASTELGRAY, 0, darken));
			canvas.drawRect(Statics.getRectOfToolbar(width, height, 1.0f), paint);

			d = (toolbarPressed == 0) ? 1 : 0;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE, d, darken));
			rect = Statics.getRectOfToolbarButton(0, 0, width, height, 1.0f);
			canvas.drawOval(rect, paint);
			str = getContext().getString(R.string.ok);
			w = textPaint.measureText(str);
			canvas.drawText(str, rect.centerX() - w / 2,
					rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);

			d = (toolbarPressed == 1) ? 1 : 0;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE, d, darken));
			rect = Statics.getRectOfToolbarTransposingButton(0, 0, width, height, 1.0f);
			canvas.drawOval(rect, paint);
			str = Statics.SCALES[7];
			w = textPaint.measureText(str);
			canvas.drawText(str, rect.centerX() - w / 2,
					rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);
		}

		delta = scroll / (height / 5);
		for (x = -7 - delta; x <= 7 - delta; x++) {
			d = (x == scalePressed) ? 1 : 0;
			paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY, d, darken));

			rect = Statics.getRectOfButton(x, -2, width, height, scroll);
			canvas.drawOval(rect, paint);

			if (x + scale < -7) {
				textPaint.setColor(Statics.getColor(Statics.COLOR_GRAY, 0, darken));
				str = Statics.getStringOfScale(x + scale + 12);
			} else if (x + scale > 7) {
				textPaint.setColor(Statics.getColor(Statics.COLOR_GRAY, 0, darken));
				str = Statics.getStringOfScale(x + scale - 12);
			} else {
				textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));
				str = Statics.getStringOfScale(x + scale);
			}
			w = textPaint.measureText(str);
			canvas.drawText(str, rect.centerX() - w / 2,
					rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);
		}

		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));

		delta = scroll / (height / 5);
		for (x = -7 - delta; x <= 7 - delta; x++) {
			int maj = x + 15 + scale;
			if (maj < 0)
				maj += 12;
			if (maj >= 36)
				maj -= 12;
			int min = x + 18 + scale;
			if (min < 0)
				min += 12;
			if (min >= 36)
				min -= 12;
			int xx = (x + 360) % 12;
			for (y = -1; y <= 1; y++) {
				c = 0;
				d = 0;
				if (playing > 0 && playingX == x && playingY == y)
					d = 1;
				switch (xx) {
				case 11:
				case 0:
				case 1:
					c = Statics.COLOR_RED;
					break;
				case 2:
				case 3:
				case 4:
					c = Statics.COLOR_YELLOW;
					break;
				case 5:
				case 6:
				case 7:
					c = Statics.COLOR_GREEN;
					break;
				case 8:
				case 9:
				case 10:
					c = Statics.COLOR_BLUE;
					break;
				}
				if (situation == Statics.SITUATION_TRANSPOSE || destination == Statics.SITUATION_TRANSPOSE) {
					c = Statics.COLOR_LIGHTGRAY;
				}
				paint.setColor(Statics.getColor(c, d, darken));

				rect = Statics.getRectOfButton(x, y, width, height, scroll);
				canvas.drawOval(rect, paint);

				switch (y) {
				case -1:
					str = Statics.NOTES_5TH[maj] + Statics.SUS4;
					break;
				case 0:
					str = Statics.NOTES_5TH[maj];
					break;
				case 1:
					str = Statics.NOTES_5TH[min] + Statics.MINOR;
					break;
				}
				w = textPaint.measureText(str);
				canvas.drawText(str, rect.centerX() - w / 2,
						rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);
			}
		}

		if (situation == Statics.SITUATION_TRANSPOSE || destination == Statics.SITUATION_TRANSPOSE) {
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height / 160.0f);
			for (x = -7; x <= 7; x++) {
				int xx = (x + 360) % 12;
				for (y = -1; y <= 1; y++) {
					c = 0;
					switch (xx) {
					case 11:
					case 0:
					case 1:
						c = Statics.COLOR_RED;
						break;
					case 2:
					case 3:
					case 4:
						c = Statics.COLOR_YELLOW;
						break;
					case 5:
					case 6:
					case 7:
						c = Statics.COLOR_GREEN;
						break;
					case 8:
					case 9:
					case 10:
						c = Statics.COLOR_BLUE;
						break;
					}
					paint.setColor(Statics.getColor(c, 0, darken));

					int sc = scroll;
					if (situation == Statics.SITUATION_TRANSPOSING)
						sc = 0;
					rect = Statics.getRectOfButton(x, y, width, height, sc);
					canvas.drawOval(rect, paint);
				}
			}

			paint.setColor(Statics.getColor(Statics.COLOR_RED, 0, darken));

			int sc = scroll;
			if (situation == Statics.SITUATION_TRANSPOSING)
				sc = 0;
			rect = Statics.getRectOfButton(0, -2, width, height, sc);
			canvas.drawOval(rect, paint);
		}

		paint.setStyle(Style.FILL);

		paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY, 0, darken));
		canvas.drawRect(Statics.getRectOfStatusBar(width, height, barsShowingRate), paint);

		paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY, 0, darken));
		canvas.drawRect(Statics.getRectOfToolbar(width, height, barsShowingRate), paint);

		for (x = 0; x < 4; x++) {
			d = (statusbarFlags[x] > 0) ? 1 : 0;
			paint.setColor(Statics.getColor(Statics.COLOR_ORANGE, d, darken));
			rect = Statics.getRectOfStatusBarButton(x, 0, width, height, barsShowingRate);
			canvas.drawOval(rect, paint);

			if (statusbarFlags[x] >= 2)
				textPaint.setColor(Statics.getColor(Statics.COLOR_ORANGE, 0, darken));
			else
				textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));
			str = Statics.TENSIONS[x];
			if (x == 2 && statusbarFlags[3] > 0)
				str = "6";
			if (x == 3 && statusbarFlags[2] > 0)
				str = "6";
			w = textPaint.measureText(str);
			canvas.drawText(str, rect.centerX() - w / 2,
					rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);
		}

		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));

		for (x = 0; x < 3; x++) {
			d = (toolbarPressed == x) ? 1 : 0;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE, d, darken));
			rect = Statics.getRectOfToolbarButton(x, 0, width, height, barsShowingRate);
			canvas.drawOval(rect, paint);

			switch (x) {
			case 0:
				str = getContext().getString(R.string.action_settings);
				break;
			case 1:
				str = getContext().getString(R.string.darken);
				break;
			case 2:
				str = Statics.getStringOfScale(scale);
				break;
			default:
				str = "";
			}
			w = textPaint.measureText(str);
			canvas.drawText(str, rect.centerX() - w / 2,
					rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2, textPaint);
		}

		if (Statics.getRectOfStatusBarButton(3, 0, width, height, barsShowingRate).right < Statics
				.getRectOfKeyboardIndicator(0, 0, width, height, barsShowingRate).left) {
			for (x = 0; x < 12; x++) {
				paint.setColor(Statics.getColor(Statics.COLOR_GRAY, 0, darken));
				if (keyboardIndicatorsTapped)
					paint.setColor(Statics.getColor(Statics.COLOR_DARKGRAY, 0, darken));
				rect = Statics.getRectOfKeyboardIndicator(x, 0, width, height, barsShowingRate);
				canvas.drawOval(rect, paint);
			}

			for (int i = 0; i < notesOfChord.length; i++) {
				paint.setColor(Statics.getColor(Statics.COLOR_ABSOLUTE_LIGHT, 0, darken));
				rect = Statics.getRectOfKeyboardIndicator(notesOfChord[i], 2, width, height, barsShowingRate);
				canvas.drawOval(rect, paint);
			}
		}

		paint.setColor(Statics.getColor(Statics.COLOR_GRAY, 0, darken));
		rect = Statics.getRectOfScrollBar(width, height, barsShowingRate);
		canvas.drawRect(rect, paint);

		paint.setColor(Statics.getColor(Statics.COLOR_DARKGRAY, 0, darken));
		rect = Statics.getRectOfScrollNob(scroll, upper, width, height, barsShowingRate);
		canvas.drawRect(rect, paint);

		if (darken) {
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height / 25);
			paint.setColor(Statics.getColor(Statics.COLOR_ABSOLUTE_CYAN, 1, darken));
			for (int i = 0; i < shapes.size(); i++) {
				Shape shape = shapes.get(i);
				paint.setAlpha(255 * shape.lifetime / Shape.getMaxLifetime());

				if (shape.style == Shape.STYLE_LINE) {
					float r = shape.radStart;
					float ax = shape.center.x - (float) (Math.cos(r / 360.0 * Math.PI * 2) * width);
					float ay = shape.center.y - (float) (Math.sin(r / 360.0 * Math.PI * 2) * width);
					float bx = shape.center.x + (float) (Math.cos(r / 360.0 * Math.PI * 2) * width);
					float by = shape.center.y + (float) (Math.sin(r / 360.0 * Math.PI * 2) * width);
					canvas.drawLine(ax, ay, bx, by, paint);
				}
				if (shape.style == Shape.STYLE_CIRCLE) {
					float cx = shape.center.x;
					float cy = shape.center.y;
					canvas.drawCircle(cx, cy,
							(float) (height * (0.2f
									+ (float) (Shape.getMaxLifetime() - shape.lifetime) / Shape.getMaxLifetime())
									* 0.8f),
							paint);
				}
				if (shape.style == Shape.STYLE_TRIANGLE) {
					float l = (float) (height
							* (0.3f + (float) (Shape.getMaxLifetime() - shape.lifetime) / Shape.getMaxLifetime())
							* 0.7f);
					float r = ((shape.radStart * shape.lifetime)
							+ (shape.radEnd * (Shape.getMaxLifetime() - shape.lifetime))) / Shape.getMaxLifetime();
					float ax = shape.center.x + (float) (Math.cos((r) / 360.0 * Math.PI * 2) * l);
					float ay = shape.center.y + (float) (Math.sin((r) / 360.0 * Math.PI * 2) * l);
					float bx = shape.center.x + (float) (Math.cos((r + 120) / 360.0 * Math.PI * 2) * l);
					float by = shape.center.y + (float) (Math.sin((r + 120) / 360.0 * Math.PI * 2) * l);
					float cx = shape.center.x + (float) (Math.cos((r + 240) / 360.0 * Math.PI * 2) * l);
					float cy = shape.center.y + (float) (Math.sin((r + 240) / 360.0 * Math.PI * 2) * l);
					canvas.drawLine(ax, ay, bx, by, paint);
					canvas.drawLine(bx, by, cx, cy, paint);
					canvas.drawLine(cx, cy, ax, ay, paint);
				}
				if (shape.style == Shape.STYLE_SQUARE) {
					float l = (float) (height
							* (0.3f + (float) (Shape.getMaxLifetime() - shape.lifetime) / Shape.getMaxLifetime())
							* 0.7f);
					float r = ((shape.radStart * shape.lifetime)
							+ (shape.radEnd * (Shape.getMaxLifetime() - shape.lifetime))) / Shape.getMaxLifetime();
					float ax = shape.center.x + (float) (Math.cos((r) / 360.0 * Math.PI * 2) * l);
					float ay = shape.center.y + (float) (Math.sin((r) / 360.0 * Math.PI * 2) * l);
					float bx = shape.center.x + (float) (Math.cos((r + 90) / 360.0 * Math.PI * 2) * l);
					float by = shape.center.y + (float) (Math.sin((r + 90) / 360.0 * Math.PI * 2) * l);
					float cx = shape.center.x + (float) (Math.cos((r + 180) / 360.0 * Math.PI * 2) * l);
					float cy = shape.center.y + (float) (Math.sin((r + 180) / 360.0 * Math.PI * 2) * l);
					float dx = shape.center.x + (float) (Math.cos((r + 270) / 360.0 * Math.PI * 2) * l);
					float dy = shape.center.y + (float) (Math.sin((r + 270) / 360.0 * Math.PI * 2) * l);
					canvas.drawLine(ax, ay, bx, by, paint);
					canvas.drawLine(bx, by, cx, cy, paint);
					canvas.drawLine(cx, cy, dx, dy, paint);
					canvas.drawLine(dx, dy, ax, ay, paint);
				}
			}
		}

		// デバッグ用
		if (debugMode) {
			paint.setColor(Statics.getColor(Statics.COLOR_BLACK, 0, darken));
			canvas.drawText("" + Sound.requiredTime, 4, 20, textPaint);
		}
	}

	public boolean actionDown(int x, int y, int id) {
		int i;
		RectF rect;

		if (situation == Statics.SITUATION_TRANSPOSE || situation == Statics.SITUATION_TRANSPOSING) {
			rect = Statics.getRectOfToolbarButton(0, 0, width, height, 1.0f);
			if (rect.contains(x, y)) {
				toolbarPressed = 0;
				taps.put(id, Statics.TOOLBAR_BUTTON);
				vibrate();
				invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
				return false;
			}
			rect = Statics.getRectOfToolbarTransposingButton(0, 0, width, height, 1.0f);
			if (rect.contains(x, y)) {
				toolbarPressed = 1;
				taps.put(id, Statics.TOOLBAR_BUTTON);
				vibrate();
				invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
				return false;
			}
		} else {
			for (i = 0; i < 3; i++) {
				rect = Statics.getRectOfToolbarButton(i, 0, width, height, barsShowingRate);
				if (rect.contains(x, y)) {
					toolbarPressed = i;
					taps.put(id, Statics.TOOLBAR_BUTTON);
					vibrate();
					invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
					return false;
				}
			}
		}

		if (situation == Statics.SITUATION_TRANSPOSE) {
			for (i = -7; i <= 7; i++) {
				rect = Statics.getRectOfButton(i, -2, width, height, scroll);
				if (rect.contains(x, y)) {
					scalePressed = i;
					taps.put(id, Statics.TRANSPOSE_SCALE_BUTTON);
					vibrate();
					invalidate();
					return false;
				}
			}
		} else if (situation == Statics.SITUATION_NORMAL) {
			for (i = 0; i < 4; i++) {
				rect = Statics.getRectOfStatusBarButton(i, 0, width, height, barsShowingRate);
				if (rect.contains(x, y)) {
					if (lastTapped == i && System.currentTimeMillis() - lastTappedTime < 400)
						statusbarFlags[i] = 2;
					else
						statusbarFlags[i] = 1;
					taps.put(id, Statics.STATUSBAR_BUTTON);
					vibrate();
					lastTapped = i;
					lastTappedTime = System.currentTimeMillis();
					invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
					return false;
				}
			}
			if (Statics.getRectOfScrollNob(scroll, upper, width, height, barsShowingRate).contains(x, y)) {
				originalX = x;
				originalY = y;
				originalScroll = scroll;
				taps.put(id, Statics.SCROLL_NOB);
				vibrate();
				invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
				return false;
			} else if (Statics.getRectOfStatusBarButton(3, 0, width, height, barsShowingRate).right < Statics
					.getRectOfKeyboardIndicator(0, 0, width, height, barsShowingRate).left
					&& Statics.getRectOfKeyboardIndicators(0, width, height, 1.0f).contains(x, y)) {
				if (!keyboardIndicatorsTapped) {
					keyboardIndicatorsTapped = true;
					taps.put(id, Statics.KEYBOARD_INDICATORS);
					vibrate();
					invalidate(Statics.RectFToRect(Statics.getRectOfKeyboardIndicators(2, width, height, 1.0f)));
				}
				return false;
			} else if (Statics.getRectOfToolbar(width, height, 1.0f).contains(x, y)) {
				if (scroll == 0) {
					for (i = 0; i < 4; i++) {
						if (statusbarFlags[i] >= 2)
							statusbarFlags[i] = 0;
					}
				} else {
					scroll = 0;
				}
				taps.put(id, Statics.SCROLL_BAR);
				vibrate();
				invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
				return false;
			} else if (Statics.getRectOfStatusBar(width, height, 1.0f).contains(x, y)) {
				boolean statusbarFlag = false;
				for (i = 0; i < 4; i++) {
					if (statusbarFlags[i] >= 2)
						statusbarFlag = true;
				}
				if (statusbarFlag) {
					for (i = 0; i < 4; i++) {
						if (statusbarFlags[i] >= 2)
							statusbarFlags[i] = 0;
					}
				} else {
					scroll = 0;
				}
				taps.put(id, Statics.STATUS_BAR);
				vibrate();
				invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
				return false;
			}
		}

		if (playing <= 0) {
			if (Statics.getRectOfButtonArea(width, height).contains(x, y)) {
				Point buttonXY = Statics.getXYOfButton(x, y, width, height, scroll);
				if (buttonXY.y >= -1 && buttonXY.y <= 1) {
					play(buttonXY.x, buttonXY.y);
					originalScroll = scroll;
					tappedX = x;
					playingID = id;
					taps.put(playingID, Statics.CHORD_BUTTON);
					vibrate();
					if (darken) {
						shapes.add(new Shape(new PointF(x, y)));
					}
					invalidate();
					return true;
				}
			}
		} else {
			if (Statics.getRectOfButtonArea(width, height).contains(x, y)) {
				// destinationScroll=originalScroll+(x-tappedX);
				pulling = 1;
				startPullingAnimation();
				invalidate(Statics.RectFToRect(Statics.getRectOfButtonArea(width, height)));
				return true;
			}
		}
		return false;
	}

	public boolean actionMove(int x, int y, int id) {
		boolean chordPressed = false;
		RectF rect;
		int i, kind;
		if (id >= 0)
			kind = taps.get(id);
		else
			kind = 0;
		switch (kind) {
		case Statics.SCROLL_NOB:
			if (-y + originalY > height / 5) {
				if (upper == 0) {
					vibrate();
					scroll = 0;
					upper = height / 35 / 5 * 2;
				}
			} else {
				scroll = (int) (-x + originalX) * 5 + originalScroll;
				if (scroll < -Statics.getScrollMax(width, height))
					scroll = -Statics.getScrollMax(width, height);
				if (scroll > Statics.getScrollMax(width, height))
					scroll = Statics.getScrollMax(width, height);
				upper = 0;
			}
			invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
			break;
		case Statics.STATUSBAR_BUTTON:
			for (i = 0; i < 4; i++) {
				rect = Statics.getRectOfStatusBarButton(i, 0, width, height, barsShowingRate);
				if (rect.contains(x, y)) {
					if (statusbarFlags[i] >= 2 && lastTapped == i)
						continue;
					if (statusbarFlags[i] == 0)
						vibrate();
					statusbarFlags[i] = 1;
				}
			}
			if (y > height * 7 / 35) {
				for (i = 0; i < 4; i++) {
					if (statusbarFlags[i] == 1) {
						statusbarFlags[i] = 2;
						vibrate();
					}
				}
			}
			invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
			break;
		case Statics.TOOLBAR_BUTTON:
			toolbarPressed = -1;
			if (situation == Statics.SITUATION_TRANSPOSE || situation == Statics.SITUATION_TRANSPOSING) {
				rect = Statics.getRectOfToolbarButton(0, 0, width, height, 1.0f);
				if (rect.contains(x, y)) {
					toolbarPressed = 0;
					taps.put(id, Statics.TOOLBAR_BUTTON);
				}
				rect = Statics.getRectOfToolbarTransposingButton(0, 0, width, height, 1.0f);
				if (rect.contains(x, y)) {
					toolbarPressed = 1;
					taps.put(id, Statics.TOOLBAR_BUTTON);
				}
			} else {
				for (i = 0; i < 3; i++) {
					rect = Statics.getRectOfToolbarButton(i, 0, width, height, barsShowingRate);
					if (rect.contains(x, y)) {
						toolbarPressed = i;
						taps.put(id, Statics.TOOLBAR_BUTTON);
					}
				}
			}
			invalidate(Statics.RectFToRect(Statics.getRectOfToolbar(width, height, 1.0f)));
			break;
		case Statics.CHORD_BUTTON:
			if (id == playingID) {
				if (situation == Statics.SITUATION_NORMAL) {
					if (pulling == 2) {
						scroll = originalScroll + (x - tappedX);
						if (y > height * 4 / 5) {
							int vert = height / 5;
							scroll = (scroll / vert) * vert;
						}
						if (scroll < -Statics.getScrollMax(width, height))
							scroll = -Statics.getScrollMax(width, height);
						if (scroll > Statics.getScrollMax(width, height))
							scroll = Statics.getScrollMax(width, height);
					} else if (pulling == 1) {
						destinationScroll = originalScroll + (x - tappedX);
					} else if (Math.abs(x - tappedX) > height / 5) {
						originalScroll = scroll;
						// destinationScroll=originalScroll+(x-tappedX);
						pulling = 1;
						step = 100 / MainActivity.heartBeatInterval;
						startPullingAnimation();
					}
				}
				chordPressed = true;
			} else {
				chordPressed = actionDown(x, y, id);
			}
			invalidate(Statics.RectFToRect(Statics.getRectOfButtonArea(width, height)));
			invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
			break;
		case Statics.SCROLL_BAR:
		case Statics.TRANSPOSE_SCALE_BUTTON:
		case Statics.STATUS_BAR:
		case Statics.KEYBOARD_INDICATORS:
			break;
		default:
			chordPressed = actionDown(x, y, id);
			break;
		}
		return chordPressed;
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		int x, y, id;
		boolean chordPressed = false;
		Sound.tappedTime = System.currentTimeMillis();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Log.i("TapChordView","DOWN Count:"+event.getPointerCount());
			x = (int) event.getX(event.getActionIndex());
			y = (int) event.getY(event.getActionIndex());
			id = event.getPointerId(event.getActionIndex());
			actionDown(x, y, id);
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i("TapChordView","MOVE Count:"+event.getPointerCount());
			for (int index = 0; index < event.getPointerCount(); index++) {
				x = (int) event.getX(index);
				y = (int) event.getY(index);
				id = event.getPointerId(index);
				chordPressed |= actionMove(x, y, id);
			}
			if (chordPressed == false) {
				playingID = -1;
				pulling = 0;
				stop();
			}
			break;
		case MotionEvent.ACTION_UP:
			// Log.i("TapChordView","UP Count:"+event.getPointerCount());
			stop();
			for (int l = 0; l < 4; l++) {
				if (statusbarFlags[l] == 1)
					statusbarFlags[l] = 0;
			}
			if (toolbarPressed >= 0)
				toolbarReleased(toolbarPressed);
			if (scalePressed != Statics.FARAWAY)
				scaleReleased(scalePressed);
			if (keyboardIndicatorsTapped)
				keyboardIndicatorsReleased();
			toolbarPressed = -1;
			scalePressed = Statics.FARAWAY;
			keyboardIndicatorsTapped = false;
			playingID = -1;
			pulling = 0;
			upper = 0;
			taps = new SparseIntArray();
			invalidate();
			break;
		default:
			break;
		}
		return true;
	}
	
	public boolean keyPressed(int keyCode, KeyEvent event) {
		Log.i("TapChordView", "pressed " + keyCode);
		if (event.getRepeatCount() > 0 || event.isLongPress()) {
			return true;
		}

		synchronized (keyWatcher) {
			for (int x = 0; x < 15; x++) {
				for (int y = 0; y < 3; y++) {
					if (keycodes[x][y] == 0) continue;
					if (keycodes[x][y] == keyCode) {
						if (shiftKeyPressed) {
							int newX = x + 6;
							if (newX > 13) {
								newX -= 12;
							}
							playWithKey(newX,y);
						} else {
							playWithKey(x,y);
						}
						return true;
					}
				}
			}
			for (int i = 0; i < 4; i++) {
				if (statusbarKeycodes[i] == keyCode) {
					switchStatusBarWithKey(i);
					return true;
				}
			}
			for (int i = 0; i < specialKeycodes.length; i++) {
				if (specialKeycodes[i] == keyCode) {
					performSpecialKey(i);
					return true;
				}
			}

			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				scroll -= height * 7 / 35f;
				if (scroll < -Statics.getScrollMax(width, height))
					scroll = -Statics.getScrollMax(width, height);
				if (scroll > Statics.getScrollMax(width, height))
					scroll = Statics.getScrollMax(width, height);
				invalidate();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				scroll += height * 7 / 35f;
				if (scroll < -Statics.getScrollMax(width, height))
					scroll = -Statics.getScrollMax(width, height);
				if (scroll > Statics.getScrollMax(width, height))
					scroll = Statics.getScrollMax(width, height);
				invalidate();
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
				scroll = 0;
				invalidate();
				break;
			default:
				break;
			}
		}
		return false;
	}

	public boolean keyLongPressed(int keyCode, KeyEvent event) {
		Log.i("TapChordView", "longPressed " + keyCode);
		return false;
	}

	public boolean keyReleased(int keyCode, KeyEvent event) {
		Log.i("TapChordView", "released " + keyCode);
		if (event.getRepeatCount() > 0 || event.isLongPress()) {
			return true;
		}

		synchronized (keyWatcher) {
			for (int x = 0; x < 15; x++) {
				for (int y = 0; y < 3; y++) {
					if (keycodes[x][y] == 0) continue;
					if (keycodes[x][y] == keyCode) {
						stopWithKey(x,y);
						return true;
					}
				}
			}
			for (int i = 0; i < 4; i++) {
				if (statusbarKeycodes[i] == keyCode) {
					cancelSwitchingStatusBar(i);
					return true;
				}
			}
			for (int i = 0; i < specialKeycodes.length; i++) {
				if (specialKeycodes[i] == keyCode) {
					cancelSpecialKey(i);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean playWithKey(final int x,final int y) {
		if (stopTimer != null) {
			stopTimer.cancel();
			stopTimer = null;
			return false;
		}
		
		play(x - 7, y - 1);
		
		return true;
	}
	
	public boolean stopWithKey(final int x,final int y) {
		stopTimer = new Timer();
		stopTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						stop();
						stopTimer = null;
					}
				});
			}
		}, 100);
		
		return true;
	}

	public boolean switchStatusBarWithKey(final int index) {
		if (cancelSwitchingStatusBarTimer != null) {
			cancelSwitchingStatusBarTimer.cancel();
			cancelSwitchingStatusBarTimer = null;
			return false;
		}

		statusbarFlags[index] = 2 - statusbarFlags[index];
		invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));

		return true;
	}

	public boolean cancelSwitchingStatusBar(final int index) {
		cancelSwitchingStatusBarTimer = new Timer();
		cancelSwitchingStatusBarTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						cancelSwitchingStatusBarTimer = null;
					}
				});
			}
		}, 100);
		
		return true;
	}
	
	public boolean performSpecialKey(final int index) {
		if (cancelSpecialKeyTimer != null) {
			cancelSpecialKeyTimer.cancel();
			cancelSpecialKeyTimer = null;
			return false;
		}
		
		switch (specialKeycodes[index]) {
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_DEL:
			boolean statusbarFlag = false;
			for (int i = 0; i < 4; i++) {
				if (statusbarFlags[i] >= 2)
					statusbarFlag = true;
			}
			if (statusbarFlag) {
				for (int i = 0; i < 4; i++) {
					if (statusbarFlags[i] >= 2)
						statusbarFlags[i] = 0;
				}
			} else {
				scroll = 0;
			}
			invalidate();
			break;
		case KeyEvent.KEYCODE_SPACE:
			if (scroll == 0) {
				for (int i = 0; i < 4; i++) {
					if (statusbarFlags[i] >= 2)
						statusbarFlags[i] = 0;
				}
			} else {
				scroll = 0;
			}
			invalidate();
			break;
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftKeyPressed = true;
			break;
		case KeyEvent.KEYCODE_ENTER:
			Intent intent = new Intent((Activity) this.getContext(), SettingsActivity.class);
			this.getContext().startActivity(intent);
			break;
		default:
			break;
		}
		
		return true;
	}
	
	public boolean cancelSpecialKey(final int index) {
		cancelSpecialKeyTimer = new Timer();
		cancelSpecialKeyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						switch (specialKeycodes[index]) {
						case KeyEvent.KEYCODE_SHIFT_LEFT:
						case KeyEvent.KEYCODE_SHIFT_RIGHT:
							shiftKeyPressed = false;
							break;
						default:
							break;
						}
						cancelSpecialKeyTimer = null;
					}
				});
			}
		}, 100);
		
		return true;
	}

	public void toolbarReleased(int which) {
		if (situation == Statics.SITUATION_TRANSPOSE) {
			switch (which) {
			case 0:
				originalScroll = scroll;
				startAnimation(1 - situation);
				break;
			case 1:
				startTransposingAnimation(0);
				break;
			}
		} else {
			switch (which) {
			case 0:
				Intent intent = new Intent((Activity) this.getContext(), SettingsActivity.class);
				this.getContext().startActivity(intent);
				break;
			case 1:
				setDarken(!darken);
				break;
			case 2:
				originalScroll = scroll;
				startAnimation(1 - situation);
				break;
			default:
				break;
			}
		}
		toolbarPressed = -1;
		invalidate();
	}

	public void scaleReleased(int which) {
		int ds;
		ds = which + scale;
		if (ds < -7)
			ds += 12;
		if (ds > 7)
			ds -= 12;
		startTransposingAnimation(ds);
		invalidate();
	}

	public void keyboardIndicatorsReleased() {
		final TextView rangeView = new TextView(this.getContext());
		rangeView.setText("" + Statics.getStringOfSoundRange(soundRange));
		rangeView.setTextAppearance(this.getContext(), android.R.style.TextAppearance_Inverse);
		final SeekBar seekBar = new SeekBar(this.getContext());
		seekBar.setProgress(soundRange + 24);
		seekBar.setMax(48);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				rangeView.setText("" + Statics.getStringOfSoundRange(seekBar.getProgress() - 24));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		final LinearLayout layout = new LinearLayout(this.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(rangeView);
		layout.addView(seekBar);
		layout.setPadding(8, 8, 8, 8);
		new AlertDialog.Builder(this.getContext()).setTitle(this.getContext().getString(R.string.settings_sound_range))
				.setView(layout)
				.setPositiveButton(this.getContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						soundRange = seekBar.getProgress() - 24;
						Statics.setPreferenceValue(TapChordView.this.getContext(), Statics.PREF_SOUND_RANGE,
								soundRange);
						getPreferenceValues();
					}
				})
				.setNegativeButton(this.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();

		invalidate();
	}

	public void play(int x, int y) {
		release();
		notesOfChord = Statics.getNotesOfChord(x + scale, y, statusbarFlags);
		Integer f[] = (Statics.convertNotesToFrequencies(notesOfChord, soundRange));
		sound = new Sound(f, this.getContext());
		playing = 1;
		playingX = x;
		playingY = y;
		sound.play();
		invalidate(Statics.RectFToRect(Statics.getRectOfButton(x, y, width, height, scroll)));
		invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
	}

	public void stop() {
		if (sound != null) {
			sound.stop();
		}
		playing = 0;
		notesOfChord = new Integer[0];
		invalidate(Statics.RectFToRect(Statics.getRectOfButtonArea(width, height)));
		invalidate(Statics.RectFToRect(Statics.getRectOfStatusBar(width, height, 1.0f)));
	}

	public void release() {
		if (sound != null) {
			sound.release();
			sound = null;
		}
		playing = 0;
		notesOfChord = new Integer[0];
		invalidate();
	}

	public void activityPaused() {
		release();
	}

	public void activityResumed() {
		getPreferenceValues();
		invalidate();
	}

	public void heartbeat(int interval) {
		if (step > 0) {
			step--;
			if (situation == Statics.SITUATION_NORMAL) {
				switch (destination) {
				case Statics.SITUATION_TRANSPOSE:
					scroll = (int) (originalScroll * step / stepMax);
					barsShowingRate = (float) step / stepMax;
					break;
				}
			} else if (situation == Statics.SITUATION_TRANSPOSE) {
				switch (destination) {
				case Statics.SITUATION_NORMAL:
					barsShowingRate = (float) (stepMax - step) / stepMax;
					break;
				}
			} else if (situation == Statics.SITUATION_TRANSPOSING) {
				switch (destination) {
				case Statics.SITUATION_TRANSPOSE:
					scroll = (int) ((scale - destScale) * (height / 5) * (stepMax - step) / stepMax);
					break;
				}
			}
			if (step == 0 && situation != destination) {
				if (situation == Statics.SITUATION_TRANSPOSING) {
					setScale(destScale);
					scroll = 0;
				}
				situation = destination;
			}
			handler.post(new Repainter());
		}
		if (pulling == 1) {
			int max = 100 / MainActivity.heartBeatInterval;
			scroll = (destinationScroll * (max - step) + scroll * step) / max;
			if (scroll < -Statics.getScrollMax(width, height))
				scroll = -Statics.getScrollMax(width, height);
			if (scroll > Statics.getScrollMax(width, height))
				scroll = Statics.getScrollMax(width, height);
			handler.post(new Repainter());
		}
		if (shapes.size() > 0) {
			for (int i = shapes.size() - 1; i >= 0; i--) {
				shapes.get(i).lifetime--;
				if (shapes.get(i).lifetime <= 0)
					shapes.remove(i);
			}
			handler.post(new Repainter());
		}
	}

	private class Repainter implements Runnable {
		@Override
		public void run() {
			invalidate();
		}
	}

	public void startAnimation(int dest) {
		destination = dest;
		step = (int) stepMax;
	}

	public void startTransposingAnimation(int ds) {
		situation = Statics.SITUATION_TRANSPOSING;
		step = (int) stepMax;
		destScale = ds;
	}

	public void startPullingAnimation() {
		pulling = 1;
		step = (int) stepMax;
	}

	public void setScale(int s) {
		scale = s;
		Statics.setPreferenceValue(this.getContext(), Statics.PREF_SCALE, scale);
	}

	public void setDarken(boolean d) {
		darken = d;
		Statics.setPreferenceValue(this.getContext(), Statics.PREF_DARKEN, darken ? 1 : 0);
	}

	public void getPreferenceValues() {
		scale = Statics.getPreferenceValue(this.getContext(), Statics.PREF_SCALE, 0);
		darken = Statics.getPreferenceValue(this.getContext(), Statics.PREF_DARKEN, 0) > 0;
		soundRange = Statics.getPreferenceValue(this.getContext(), Statics.PREF_SOUND_RANGE, 0);
		vibration = Statics.getPreferenceValue(this.getContext(), Statics.PREF_VIBRATION, 0) > 0;
		stepMax = 100.0f / MainActivity.heartBeatInterval;
	}

	public void vibrate() {
		if (vibration)
			vib.vibrate(Statics.VIBRATION_LENGTH);
	}

}
