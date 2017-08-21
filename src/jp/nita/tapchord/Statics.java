package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences.Editor;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class Statics {

	final public static int NIHIL = 0;
	final public static int FARAWAY = 256;

	final public static int SITUATION_NORMAL = 0;
	final public static int SITUATION_TRANSPOSE = 1;
	final public static int SITUATION_TRANSPOSE_MOVING = 2;
	// final public static int SITUATION_PULLING=3;

	final public static int CHORD_BUTTON = 1;
	final public static int STATUSBAR_BUTTON = 2;
	final public static int SCROLL_NOB = 3;
	final public static int TOOLBAR_BUTTON = 4;
	final public static int TOOL_BAR = 5;
	final public static int TRANSPOSE_SCALE_BUTTON = 6;
	final public static int STATUS_BAR = 7;
	final public static int KEYBOARD_INDICATORS = 8;

	final public static int COLOR_ABSOLUTE_CYAN = -128;
	final public static int COLOR_BLACK = -6;
	final public static int COLOR_DARKGRAY = -5;
	final public static int COLOR_GRAY = -4;
	final public static int COLOR_PASTELGRAY = -3;
	final public static int COLOR_LIGHTGRAY = -2;
	final public static int COLOR_WHITE = -1;
	final public static int COLOR_ABSOLUTE_LIGHT = 0;
	final public static int COLOR_RED = 1;
	final public static int COLOR_YELLOW = 2;
	final public static int COLOR_GREEN = 3;
	final public static int COLOR_BLUE = 4;
	final public static int COLOR_ORANGE = 5;
	final public static int COLOR_PURPLE = 6;

	final public static String SUS4 = "sus4";
	final public static String MINOR = "m";

	public static final String PREF_KEY = "tapchord";
	public static final String PREF_SCALE = "scale";
	public static final String PREF_DARKEN = "darken";
	public static final String PREF_VIBRATION = "vibration";
	public static final String PREF_VOLUME = "volume";
	public static final String PREF_SAMPLING_RATE = "sampling_rate";
	public static final String PREF_WAVEFORM = "waveform";
	public static final String PREF_SOUND_RANGE = "sound_range";
	public static final String PREF_ATTACK_TIME = "attack_time";
	public static final String PREF_DECAY_TIME = "decay_time";
	public static final String PREF_SUSTAIN_LEVEL = "sustain_level";
	public static final String PREF_RELEASE_TIME = "release_time";
	public static final String PREF_ENABLE_ENVELOPE = "enable_envelope";
	public static final String PREF_ANIMATION_QUALITY = "animation_quality";

	public static final int VIBRATION_LENGTH = 40;

	public static final String NOTES_C2B[] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

	public static String NOTES_5TH[] = { "Fbb", "Cbb", "Gbb", "Dbb", "Abb", "Ebb", "Bbb", "Fb", "Cb", "Gb", "Db", "Ab",
			"Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#", "E#", "B#", "Fx", "Cx", "Gx",
			"Dx", "Ax", "Ex", "Bx", "F#x" };

	public static String SCALES[] = { "b7", "b6", "b5", "b4", "b3", "b2", "b1", "#b0", "#1", "#2", "#3", "#4", "#5",
			"#6", "#7" };

	public static String TENSIONS[] = { "add9", "-5/aug", "7", "M7" };

	public static int color(int which, int pressed, boolean dark) {
		int r, g, b;
		if (dark == false) {
			switch (which) {
			case COLOR_BLACK:
				r = 0x00;
				g = 0x00;
				b = 0x00;
				break;
			case COLOR_DARKGRAY:
				r = 0x40;
				g = 0x40;
				b = 0x40;
				break;
			case COLOR_GRAY:
				r = 0x80;
				g = 0x80;
				b = 0x80;
				break;
			case COLOR_PASTELGRAY:
				r = 0xF8;
				g = 0xF8;
				b = 0xF8;
				break;
			case COLOR_LIGHTGRAY:
				r = 0xE0;
				g = 0xE0;
				b = 0xE0;
				break;
			case COLOR_ABSOLUTE_LIGHT:
				r = 0xFF;
				g = 0xFF;
				b = 0xFF;
				break;
			case COLOR_RED:
				r = 0xFF;
				g = 0xA0;
				b = 0xE0; // SUM=0x28
				break;
			case COLOR_YELLOW:
				r = 0xFF;
				g = 0xFF;
				b = 0x70;
				break;
			case COLOR_GREEN:
				r = 0xA0;
				g = 0xFF;
				b = 0xA0;
				break;
			case COLOR_BLUE:
				r = 0xA0;
				g = 0xE0;
				b = 0xFF;
				break;
			case COLOR_ORANGE:
				r = 0xFF;
				g = 0xC0;
				b = 0x80;
				break;
			case COLOR_PURPLE:
				r = 0xC0;
				g = 0xC0;
				b = 0xFF;
				break;
			default:
				r = 0xFF;
				g = 0xFF;
				b = 0xFF;
				break;
			}
			switch (pressed) {
			case 1:
				r /= 2;
				g /= 2;
				b /= 2;
				break;
			case -1:
				r = 256 - (256 - r) / 2;
				g = 256 - (256 - g) / 2;
				b = 256 - (256 - b) / 2;
				break;
			default:
				break;
			}
		} else {
			switch (which) {
			case COLOR_BLACK:
				r = 0;
				g = 80;
				b = 80;
				break;
			case COLOR_DARKGRAY:
				r = 0;
				g = 48;
				b = 48;
				break;
			case COLOR_GRAY:
				r = 0;
				g = 32;
				b = 32;
				break;
			case COLOR_PASTELGRAY:
				r = 0;
				g = 16;
				b = 16;
				break;
			case COLOR_LIGHTGRAY:
				r = 0;
				g = 8;
				b = 8;
				break;
			case COLOR_ABSOLUTE_LIGHT:
				r = 0;
				g = 64;
				b = 64;
				break;
			case COLOR_RED:
			case COLOR_YELLOW:
			case COLOR_GREEN:
			case COLOR_BLUE:
			case COLOR_ORANGE:
			case COLOR_PURPLE:
				r = 0;
				g = 32;
				b = 32;
				break;
			default: // WHITE
				r = 0;
				g = 0;
				b = 0;
				break;
			}
			switch (pressed) {
			case -1:
				r /= 2;
				g /= 2;
				b /= 2;
				break;
			case 1:
				r = 128 - (128 - r) / 2;
				g = 128 - (128 - g) / 2;
				b = 128 - (128 - b) / 2;
				break;
			default:
				break;
			}
		}
		if (r < 0)
			r = 0;
		if (r > 255)
			r = 255;
		if (g < 0)
			g = 0;
		if (g > 255)
			g = 255;
		if (b < 0)
			b = 0;
		if (b > 255)
			b = 255;

		if (which == COLOR_ABSOLUTE_CYAN) {
			r = 32;
			g = 196;
			b = 196;
		}

		return Color.argb(255, r, g, b);
	}

	public static RectF rectOfButtonArea(int width, int height) {
		float vert = height * 7 / 35f;
		return new RectF(0, vert, width, height - vert);
	}

	public static RectF rectOfButton(int x, int y, int width, int height, int scroll) {
		float vert = height * 7 / 35f;
		float pX = width / 2 + x * vert;
		float pY = height / 2 + y * vert;
		return new RectF(pX - vert / 2 + vert / 14 + scroll, pY - vert / 2 + vert / 14,
				pX + vert / 2 - vert / 14 + scroll, pY + vert / 2 - vert / 14);
	}

	public static Point pointOfButton(int x, int y, int width, int height, int scroll) {
		int vert = (int) (height * 7 / 35f);
		int resX = (int) Math.floor((float) (x - width / 2 - scroll) / vert + 0.5);
		int resY = (int) Math.floor((float) (y - height / 2) / vert + 0.5);
		return new Point(resX, resY);
	}

	public static Point topLeftOfButton(int x, int y, int width, int height, int scroll) {
		int vert = (int) (height * 7 / 35f);
		int resL = x / vert;
		int resT = y / vert;
		return new Point(resL, resT);
	}

	public static Point bottomRightButton(int x, int y, int width, int height, int scroll) {
		int vert = (int) (height * 7 / 35f);
		int resB = (width - x) / vert;
		int resR = (height - y) / vert;
		return new Point(resB, resR);
	}

	public static int scrollMax(int width, int height) {
		float vert = height / 35f;
		float max = (vert * 7) * 15;
		float nob = width;
		return (int) (max - nob) / 2;
	}

	public static RectF rectOfScrollBar(int width, int height, float showingRate) {
		float vert = height / 35f;
		float max = (vert * 7) * 15;
		float hidingDelta = vert * (1.0f - showingRate) * 7;
		return new RectF(vert * 2, vert * 30.5f + hidingDelta, vert * 2 + max / 5, vert * 32.5f + hidingDelta);
	}

	public static RectF rectOfScrollNob(int pos, int upper, int width, int height, float showingRate) {
		float vert = height / 35f;
		float max = (vert * 7) * 15;
		float nob = width / 5;
		float x = vert * 2 + max / 10 - pos / 5;
		float hidingDelta = vert * (1.0f - showingRate) * 7;
		return new RectF(x - nob / 2, vert * 30f - upper + hidingDelta, x + nob / 2, vert * 33f - upper + hidingDelta);
	}

	public static int radiusOfButton(int height) {
		return height * 7 / 70 - 8;
	}

	public static RectF rectOfStatusBar(int width, int height, float showingRate) {
		float vert = height * 7 / 35f;
		float hidingDelta = vert * (1.0f - showingRate);
		return new RectF(0, 0 - hidingDelta, width, height * 7 / 35 - hidingDelta);
	}

	public static RectF rectOfStatusBarButton(int x, int y, int width, int height, float showingRate) {
		float vert = height * 7 / 35f;
		float pX = x * vert + vert / 2;
		float hidingDelta = vert * (1.0f - showingRate);
		return new RectF(pX - vert / 2 + vert / 14, 0 + vert / 14 - hidingDelta, pX + vert / 2 - vert / 14,
				vert - vert / 14 - hidingDelta);
	}

	public static RectF rectOfToolbar(int width, int height, float showingRate) {
		float vert = height * 7 / 35f;
		float hidingDelta = vert * (1.0f - showingRate);
		return new RectF(0, height * 28 / 35 + hidingDelta, width, height + hidingDelta);
	}

	public static RectF rectOfToolbarButton(int x, int y, int width, int height, float showingRate) {
		float vert = height * 7 / 35f;
		float pX = x * vert + vert / 2;
		float hidingDelta = vert * (1.0f - showingRate);
		return new RectF(width - (pX + vert / 2) + vert / 14, height - vert + vert / 14 + hidingDelta,
				width - (pX - vert / 2) - vert / 14, height - vert / 14 + hidingDelta);
	}

	public static RectF rectOfToolbarTransposingButton(int x, int y, int width, int height, float showingRate) {
		float vert = height * 7 / 35f;
		float pX = x * vert + vert / 2;
		float hidingDelta = vert * (1.0f - showingRate);
		return new RectF(pX - vert / 2 + vert / 14, height - vert + vert / 14 + hidingDelta, pX + vert / 2 - vert / 14,
				height - vert / 14 + hidingDelta);
	}

	public static RectF rectOfKeyboardIndicator(int i, int shrink, int width, int height, float showingRate) {
		float vert = height / 35f;
		float hidingDelta = vert * 7 * (1.0f - showingRate);
		RectF r = null;
		switch (i % 12) {
		case 0:
			r = new RectF(width - vert * 21 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 19 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 2:
			r = new RectF(width - vert * 18 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 16 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 4:
			r = new RectF(width - vert * 15 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 13 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 5:
			r = new RectF(width - vert * 12 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 10 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 7:
			r = new RectF(width - vert * 9 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 7 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 9:
			r = new RectF(width - vert * 6 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 4 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;
		case 11:
			r = new RectF(width - vert * 3 + shrink, vert * 4 + shrink - hidingDelta, width - vert * 1 - shrink,
					vert * 6 - shrink - hidingDelta);
			break;

		case 1:
			r = new RectF(width - vert * 19.5f + shrink, vert * 1 + shrink - hidingDelta, width - vert * 17.5f - shrink,
					vert * 3 - shrink - hidingDelta);
			break;
		case 3:
			r = new RectF(width - vert * 16.5f + shrink, vert * 1 + shrink - hidingDelta, width - vert * 14.5f - shrink,
					vert * 3 - shrink - hidingDelta);
			break;
		case 6:
			r = new RectF(width - vert * 10.5f + shrink, vert * 1 + shrink - hidingDelta, width - vert * 8.5f - shrink,
					vert * 3 - shrink - hidingDelta);
			break;
		case 8:
			r = new RectF(width - vert * 7.5f + shrink, vert * 1 + shrink - hidingDelta, width - vert * 5.5f - shrink,
					vert * 3 - shrink - hidingDelta);
			break;
		case 10:
			r = new RectF(width - vert * 4.5f + shrink, vert * 1 + shrink - hidingDelta, width - vert * 2.5f - shrink,
					vert * 3 - shrink - hidingDelta);
			break;
		}

		return r;
	}

	public static RectF rectOfKeyboardIndicators(int shrink, int width, int height, float showingRate) {
		float vert = height / 35f;
		float hidingDelta = vert * 7 * (1.0f - showingRate);
		return new RectF(width - vert * 23 + shrink, 0, width, vert * 7 - shrink - hidingDelta);
	}

	public static int frequencyOfNote(int note, int soundRange) {
		double f = 440.0;
		int n = note;
		while (n < soundRange || n >= soundRange + 12) {
			if (n < soundRange)
				n += 12;
			if (n >= soundRange + 12)
				n -= 12;
		}
		return (int) Math.round(f * Math.pow(2, (n - 9) / 12.0));
	}

	public static Integer[] notesOfChord(int x, int y, int[] tensions) {
		List<Integer> notes = new ArrayList<Integer>();

		if (y >= 1)
			x += 3;
		if (y == -1) {
			if (tensions[1] > 0) {
				notes.add((x * 7 + 360) % 12);
				notes.add((x * 7 + 4 + 360) % 12);
				notes.add((x * 7 + 8 + 360) % 12);
			} else {
				notes.add((x * 7 + 360) % 12);
				notes.add((x * 7 + 5 + 360) % 12);
				notes.add((x * 7 + 7 + 360) % 12);
			}
		} else if (y == 0) {
			notes.add((x * 7 + 360) % 12);
			notes.add((x * 7 + 4 + 360) % 12);
			if (tensions[1] > 0) {
				notes.add((x * 7 + 6 + 360) % 12);
			} else {
				notes.add((x * 7 + 7 + 360) % 12);
			}
		} else if (y == 1) {
			notes.add((x * 7 + 360) % 12);
			notes.add((x * 7 + 3 + 360) % 12);
			if (tensions[1] > 0) {
				notes.add((x * 7 + 6 + 360) % 12);
			} else {
				notes.add((x * 7 + 7 + 360) % 12);
			}
		}
		if (tensions[0] > 0) {
			notes.add((x * 7 + 2 + 360) % 12);
		}

		if (tensions[2] > 0 && tensions[3] > 0) {
			notes.add((x * 7 + 9 + 360) % 12);
		} else if (tensions[2] > 0) {
			notes.add((x * 7 + 10 + 360) % 12);
		} else if (tensions[3] > 0) {
			notes.add((x * 7 + 11 + 360) % 12);
		}
		Integer ns[] = notes.toArray(new Integer[0]);
		return ns;
	}

	public static Integer[] getMidiNotesOfChord(int x, int y, int[] tensions, int soundRange) {
		Integer notesOfChord[] = notesOfChord(x, y, tensions);
		soundRange += 60;
		for (int i = 0; i < notesOfChord.length; i++) {
			int n = notesOfChord[i];
			while (n < soundRange || n >= soundRange + 12) {
				if (n < soundRange)
					n += 12;
				if (n >= soundRange + 12)
					n -= 12;
			}
			notesOfChord[i] = n;
		}
		return notesOfChord;
	}

	public static Integer[] convertNotesToFrequencies(Integer[] notes, int soundRange) {
		List<Integer> freqs = new ArrayList<Integer>();
		for (int i = 0; i < notes.length; i++) {
			freqs.add(frequencyOfNote(notes[i], soundRange));
		}
		Integer fs[] = freqs.toArray(new Integer[0]);
		return fs;
	}

	public static String stringOfScale(int i) {
		if (i < -7 || i > 7)
			return "";
		return SCALES[i + 7];
	}

	public static int preferenceValue(Context context, String key, int def) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		return pref.getInt(key, def);
	}

	public static void setPreferenceValue(Context context, String key, int val) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, val);
		editor.commit();
	}

	public static String stringOfAnimationQuality(int aq, Context context) {
		switch (aq) {
		case -1:
			return context.getString(R.string.settings_animation_quality_low);
		case 0:
			return context.getString(R.string.settings_animation_quality_medium);
		case 1:
			return context.getString(R.string.settings_animation_quality_high);
		default:
			return context.getString(R.string.settings_animation_quality_medium);
		}
	}

	public static String longStringOfScale(int i) {
		switch (i) {
		case -7:
			return "b7 : Cb / Abm";
		case -6:
			return "b6 : Gb / Ebm";
		case -5:
			return "b5 : Db / Bbm";
		case -4:
			return "b4 : Ab / Fm";
		case -3:
			return "b3 : Eb / Cm";
		case -2:
			return "b2 : Bb / Gm";
		case -1:
			return "b1 : F / Dm";
		case 0:
			return "#b0 : C / Am";
		case 1:
			return "#1 : G / Em";
		case 2:
			return "#2 : D / Bm";
		case 3:
			return "#3 : A / F#m";
		case 4:
			return "#4 : E / C#m";
		case 5:
			return "#5 : G / G#m";
		case 6:
			return "#6 : F# / D#m";
		case 7:
			return "#7 : C# / A#m";
		default:
			return "";
		}
	}

	public static String onOrOffString(Context context, int v) {
		if (v > 0)
			return context.getString(R.string.on);
		else
			return context.getString(R.string.off);
	}

	public static int valueOfVolume(int i) {
		return i + 50;
	}

	public static int valueOfSamplingRate(int i) {
		switch (i) {
		case -3:
			return 8000;
		case -2:
			return 16000;
		case -1:
			return 22050;
		case 0:
			return 44100;
		default:
			return 0;
		}
	}

	public static String valueOfWaveform(int i, Context context) {
		switch (i) {
		case 0:
			return context.getString(R.string.settings_waveform_sine_wave);
		case 1:
			return context.getString(R.string.settings_waveform_sawtooth_wave);
		case 2:
			return context.getString(R.string.settings_waveform_triangle_wave);
		case 3:
			return context.getString(R.string.settings_waveform_square_wave);
		case 4:
			return context.getString(R.string.settings_waveform_fourth_pulse_wave);
		case 5:
			return context.getString(R.string.settings_waveform_eighth_pulse_wave);
		case 6:
			return context.getString(R.string.settings_waveform_shepard_tone);
		default:
			return "";
		}
	}

	public static float valueOfAttackDecayReleaseTime(int i) {
		return i / 1000.0f;
	}

	public static String stringOfSoundRange(int soundRange) {
		return shortStringOfSoundRange(soundRange) + " - " + shortStringOfSoundRange(soundRange + 11);
	}

	public static String stringOfSingleTime(int t, Context context) {
		return "" + t / 1000.0f + context.getString(R.string.settings_attack_decay_release_time_seconds);
	}

	public static String stringOfSustainLevel(int s, Context context) {
		return "" + (s + 100) + context.getString(R.string.settings_sustain_level_percent);
	}

	public static String stringOfEnvelope(int e, int a, int d, int s, int r, Context context) {
		if (e > 0) {
			return "" + a / 1000.0f + context.getString(R.string.settings_attack_decay_release_time_seconds) + " - "
					+ d / 1000.0f + context.getString(R.string.settings_attack_decay_release_time_seconds) + " - "
					+ (s + 100) + context.getString(R.string.settings_sustain_level_percent) + " - " + r / 1000.0f
					+ context.getString(R.string.settings_attack_decay_release_time_seconds);
		} else {
			return context.getString(R.string.disabled);
		}
	}

	public static String shortStringOfSoundRange(int soundRange) {
		int octave = 4;
		while (soundRange < 0 || soundRange >= 12) {
			if (soundRange < 0) {
				octave--;
				soundRange += 12;
			}
			if (soundRange >= 12) {
				octave++;
				soundRange -= 12;
			}
		}
		return "" + NOTES_C2B[soundRange] + octave;
	}

	public static Rect RectFToRect(RectF rectf) {
		return new Rect((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom);
	}
}
