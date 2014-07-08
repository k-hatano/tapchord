package jp.nita.tapchord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	Integer[] frequencies=new Integer[0];
	WaveGenerator generator=null;

	int mode;
	long term;
	long modeTerm;

	final int MODE_ATTACK=0;
	final int MODE_DECAY=1;
	final int MODE_SUSTAIN=2;
	final int MODE_RELEASE=3;
	final int MODE_FINISHED=4;

	int volume=0;
	int sampleRate=4000;
	int waveform;
	int waveLength;
	int soundRange;

	int attack=0;
	int decay=0;
	int sustain=0;
	int release=0;
	int length=0;

	int attackLength;
	int decayLength;
	int sustainLength;
	int releaseLength;

	double sustainLevel;

	Context context;

	Object modeProcess = new Object();

	public Sound(Integer[] freqs,Context cont){
		frequencies=freqs;
		context=cont;
	}

	public void play(){
		generator = new WaveGenerator();
		generator.start();
	}

	public void stop(){
		finish();
	}

	public void release(){
		finish();
	}

	public double wave(double t,int which){
		switch(which){
		case 0:
			return Math.sin(2.0*Math.PI*t);
		case 1:
			return (t-Math.floor(t+1/2.0));
		case 2:
		{
			double tt=t-Math.floor(t);
			if(tt<0.25){
				return tt*4;
			}else if(tt<0.50){
				return (0.5-tt)*4;
			}else if(tt<0.75){
				return (-tt+0.5)*4;
			}else{
				return (-1+tt)*4;
			}
		}
		case 3:
			return Math.sin(2.0*Math.PI*t)>0?0.5:-0.5;
		case 4:
			return t-Math.floor(t)<1.0/4.0?0.5:-0.5;
		case 5:
			return t-Math.floor(t)<1.0/8.0?0.5:-0.5;
		default:
			return Math.sin(2.0*Math.PI*t);
		}
	}

	class WaveGenerator extends Thread{
		public void run(){
			volume=Statics.getValueOfVolume(Statics.getPreferenceValue(context,Statics.PREF_VOLUME,0));
			soundRange=Statics.getValueOfVolume(Statics.getPreferenceValue(context,Statics.PREF_SOUND_RANGE,0));
			sampleRate=Statics.getValueOfSamplingRate(Statics.getPreferenceValue(context,Statics.PREF_SAMPLING_RATE,0));
			waveform=Statics.getPreferenceValue(context,Statics.PREF_WAVEFORM,0);

			attack=Statics.getPreferenceValue(context,Statics.PREF_ATTACK_TIME,0);
			decay=Statics.getPreferenceValue(context,Statics.PREF_DECAY_TIME,0);
			sustain=Statics.getPreferenceValue(context,Statics.PREF_SUSTAIN_LEVEL,0)+100;
			release=Statics.getPreferenceValue(context,Statics.PREF_RELEASE_TIME,0);

			attackLength=attack*sampleRate/1000;
			decayLength=decay*sampleRate/1000;
			sustainLength=sampleRate;
			releaseLength=release*sampleRate/1000;

			sustainLevel=(double)sustain/100.0;

			length=AudioTrack.getMinBufferSize(sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT)/100;
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					sampleRate*2,
					AudioTrack.MODE_STREAM);
			mode=MODE_ATTACK;
			term=0;
			modeTerm=0;
			track.play();
			while(mode<=MODE_RELEASE){
				track.write(getWave(length),0,length);
			}
			try{
				sleep(release);
			}catch(InterruptedException ignore){
				
			}
			track.pause();
			track.stop();
			track.release();
		}

		public short[] getWave(int length){
			synchronized(modeProcess){
				short[] w = new short[length];
				for(int i=0;i<length;i++){
					double s=0;
					for(int j=0;j<frequencies.length;j++){
						s+=wave((double)term*frequencies[j]/(double)sampleRate,waveform)*volume/400.0*(Short.MAX_VALUE);
					}

					if(mode==MODE_ATTACK&&modeTerm>=attackLength){
						modeTerm=0;
						mode=MODE_DECAY;
					}
					if(mode==MODE_DECAY&&modeTerm>=decayLength){
						modeTerm=0;
						mode=MODE_SUSTAIN;
					}
					if(mode==MODE_RELEASE&&modeTerm>releaseLength){
						modeTerm=0;
						mode=MODE_FINISHED;
					}

					if(mode==MODE_ATTACK){
						s=s*((double)modeTerm/(double)attackLength);
					}else if(mode==MODE_DECAY){
						s=(s*(double)(decayLength-modeTerm)/(double)decayLength+s*sustainLevel*(double)modeTerm/(double)decayLength);
					}else if(mode==MODE_SUSTAIN){
						s=s*sustainLevel;
					}else if(mode==MODE_RELEASE){
						s=s*((double)(releaseLength-modeTerm)/(double)releaseLength)*sustainLevel;
					}else{
						s=0;
					}

					if(s>=Short.MAX_VALUE) s=(double)Short.MAX_VALUE;
					if(s<=-Short.MAX_VALUE) s=(double)(-Short.MAX_VALUE);
					w[i]=(short)s;

					term++;
					if(mode!=MODE_SUSTAIN) modeTerm++;
					if(term>=sampleRate) term-=sampleRate;
				}
				return w;
			}
		}

	}

	public void finish(){
		synchronized(modeProcess){
			modeTerm=0;
			mode=MODE_RELEASE;
		}
	}

}
