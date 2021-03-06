package jp.nita.tapchord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	static long tappedTime = 0;
	static long startedPlayingTime = 0;
	static long requiredTime = 0;

	Integer[] notesInRange = new Integer[0];
	Integer[] frequencies = new Integer[0];
	WaveGenerator generator = null;
	static AudioTrack track = null;
	static double gaussianTable[] = new double[100];

	int mode;
	long term, modeTerm;

	final int MODE_ATTACK = 0;
	final int MODE_DECAY = 1;
	final int MODE_SUSTAIN = 2;
	final int MODE_RELEASE = 3;
	final int MODE_FINISHED = 4;

	int volume = 0;
	int sampleRate = 4000;
	int waveform;
	int waveLength;
	int soundRange;

	int length;
	int attackLength, decayLength, sustainLength, releaseLength;
	boolean enableEnvelope;

	double sustainLevel;

	Context context;

	static Object modeProcess = new Object();

	public Sound(Integer[] n, Context cont) {
		notesInRange = n;
		frequencies = Statics.convertRawNotesToFrequencies(n);

		context = cont;

		for (int i = 0; i < gaussianTable.length; i++) {
			gaussianTable[i] = gaussian(i - gaussianTable.length / 2);
		}
	}

	public void play() {
		generator = new WaveGenerator();
		generator.start();
	}

	public void stop() {
		finish(MODE_RELEASE);
	}

	public void release() {
		finish(MODE_FINISHED);
	}

	public static double wave(double t, int which) {
//		Log.i("Sound", "t:"+t);
		switch (which) {
		case 0:
			return Math.sin(2.0 * Math.PI * t);
		case 1:
			return (t - Math.floor(t + 1 / 2.0));
		case 2: {
			double tt = t - Math.floor(t);
			if (tt < 0.25) {
				return tt * 4;
			} else if (tt < 0.50) {
				return (0.5 - tt) * 4;
			} else if (tt < 0.75) {
				return (-tt + 0.5) * 4;
			} else {
				return (-1 + tt) * 4;
			}
		}
		case 3:
			return Math.sin(2.0 * Math.PI * t) > 0 ? 0.5 : -0.5;
		case 4:
			return t - Math.floor(t) < 1.0 / 4.0 ? 0.5 : -0.5;
		case 5:
			return t - Math.floor(t) < 1.0 / 8.0 ? 0.5 : -0.5;
		default:
			return Math.sin(2.0 * Math.PI * t);
		}
	}

	final static double LOG_2 = Math.log(2.0);

	public static double shepardTone(long term, int noteInRange, int frequency, int sampleRate, int soundRange, int which) {
		switch (which) {
		case 6: {
			double r = 0, g = 0;
			double t = (double)term * frequency / sampleRate;
			int n = (int)(noteInRange - soundRange - 6);

			g = gaussianTable[n - 24 + gaussianTable.length / 2];
			r += Math.sin(0.5 * Math.PI * t) * g;

			g = gaussianTable[n - 12 + gaussianTable.length / 2];
			r += Math.sin(1.0 * Math.PI * t) * g;

			g = gaussianTable[n + gaussianTable.length / 2];
			r += Math.sin(2.0 * Math.PI * t) * g;

			g = gaussianTable[n + 12 + gaussianTable.length / 2];
			r += Math.sin(4.0 * Math.PI * t) * g;

			g = gaussianTable[n + 24 + gaussianTable.length / 2];
			r += Math.sin(8.0 * Math.PI * t) * g;

			return r;
		}
		default:
			return Math.sin(2.0 * Math.PI * term * frequency / sampleRate);
		}
	}

	final static double SIGMA = 0.45;
	final static double SIGMA_SQRT_PI = SIGMA * Math.sqrt(2 * Math.PI);
	final static double SIGMA_SQUARED_2 = 2 * SIGMA * SIGMA;

	public static double gaussian(double t) {
		double tOn12 = t / 12.0;
		return (1.0 / SIGMA_SQRT_PI) * Math.exp(- tOn12 * tOn12 / SIGMA_SQUARED_2);
	}

	public short[] getWave(int length) {
		short[] w = new short[length];
		for (int i = 0; i < length; i++) {
			double s = 0;
			switch (waveform) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				for (int j = 0; j < frequencies.length; j++) {
					s += wave((double) term * frequencies[j] / sampleRate, waveform);
				}
				break;
			case 6:
				for (int j = 0; j < notesInRange.length; j++) {
					s += shepardTone(term, notesInRange[j], frequencies[j], sampleRate, soundRange, waveform);
				}
				break;
			}

			s *= volume / 400.0 * (Short.MAX_VALUE);

			if (enableEnvelope) {
				if (mode == MODE_ATTACK && modeTerm >= attackLength) {
					modeTerm = 0;
					mode = MODE_DECAY;
				}
				if (mode == MODE_DECAY && modeTerm >= decayLength) {
					modeTerm = 0;
					mode = MODE_SUSTAIN;
				}
				if (mode == MODE_RELEASE && modeTerm > releaseLength) {
					modeTerm = 0;
					mode = MODE_FINISHED;
				}

				if (mode == MODE_ATTACK) {
					s = s * ((double) modeTerm / (double) attackLength);
				} else if (mode == MODE_DECAY) {
					s = (s * (double) (decayLength - modeTerm) / (double) decayLength
							+ s * sustainLevel * (double) modeTerm / (double) decayLength);
				} else if (mode == MODE_SUSTAIN) {
					s = s * sustainLevel;
				} else if (mode == MODE_RELEASE) {
					s = s * ((double) (releaseLength - modeTerm) / (double) releaseLength) * sustainLevel;
				} else {
					s = 0;
				}
			}

			if (s >= Short.MAX_VALUE)
				s = (double) Short.MAX_VALUE;
			if (s <= -Short.MAX_VALUE)
				s = (double) (-Short.MAX_VALUE);
			w[i] = (short) s;

			term++;
			if (mode != MODE_SUSTAIN)
				modeTerm++;
			if (term >= sampleRate)
				term -= sampleRate;
		}
		return w;
	}

	class WaveGenerator extends Thread {
		public void run() {
			synchronized (modeProcess) {
				if (track != null) {
					track.pause();
					track.stop();
					track.release();
					track = null;
				}

				volume = Statics.valueOfVolume(Statics.preferenceValue(context, Statics.PREF_VOLUME, 30));
				soundRange = Statics.preferenceValue(context, Statics.PREF_SOUND_RANGE, 0);
				sampleRate = Statics
						.valueOfSamplingRate(Statics.preferenceValue(context, Statics.PREF_SAMPLING_RATE, 0));
				waveform = Statics.preferenceValue(context, Statics.PREF_WAVEFORM, 0);
				enableEnvelope = Statics.preferenceValue(context, Statics.PREF_ENABLE_ENVELOPE, 0) > 0;

				if (enableEnvelope) {
					final int attack = Statics.preferenceValue(context, Statics.PREF_ATTACK_TIME, 0);
					final int decay = Statics.preferenceValue(context, Statics.PREF_DECAY_TIME, 0);
					final int sustain = Statics.preferenceValue(context, Statics.PREF_SUSTAIN_LEVEL, 0) + 100;
					final int release = Statics.preferenceValue(context, Statics.PREF_RELEASE_TIME, 0);

					attackLength = attack * sampleRate / 1000;
					decayLength = decay * sampleRate / 1000;
					sustainLength = sampleRate;
					releaseLength = release * sampleRate / 1000;

					sustainLevel = (double) sustain / 100.0;

					length = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
							AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, length,
							AudioTrack.MODE_STREAM);

					mode = MODE_ATTACK;
					term = 0;
					modeTerm = 0;
					startedPlayingTime = System.currentTimeMillis();
					requiredTime = startedPlayingTime - tappedTime;
					track.play();
					while (mode <= MODE_RELEASE) {
						track.write(getWave(length), 0, length);
					}
					try {
						sleep(release);
					} catch (InterruptedException ignore) {

					}
					track.stop();
					startedPlayingTime = System.currentTimeMillis();
					requiredTime = startedPlayingTime - tappedTime;
					track.release();
					track = null;
				} else {
					attackLength = 0;
					decayLength = 0;
					sustainLength = sampleRate;
					releaseLength = 0;

					sustainLevel = 1.0;
					length = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
							AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, length,
							AudioTrack.MODE_STREAM);

					mode = MODE_SUSTAIN;
					term = 0;
					modeTerm = 0;

					startedPlayingTime = System.currentTimeMillis();
					requiredTime = startedPlayingTime - tappedTime;
					track.play();
					while (mode <= MODE_SUSTAIN) {
						track.write(getWave(length), 0, length);
					}
					track.stop();
					startedPlayingTime = System.currentTimeMillis();
					requiredTime = startedPlayingTime - tappedTime;
					track.release();
					track = null;
				}
			}
		}

	}

	public void finish(int modeParam) {
		if (track != null && track.getState() == AudioTrack.STATE_INITIALIZED && !enableEnvelope) {
			try {
				// できるだけ早く音を止めるためだけのpauseなので、例外が発生しても無視する
				// TODO: 何とかした方がいいと思う
				track.pause();
			} catch(IllegalStateException ignore) {

			}
		}
		modeTerm = 0;
		mode = modeParam;
	}

}
