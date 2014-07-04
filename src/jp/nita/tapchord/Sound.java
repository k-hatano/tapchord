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
					AudioFormat.ENCODING_PCM_16BIT);
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					sampleRate*2,
					AudioTrack.MODE_STREAM);
			mode=0;
			term=0;
			track.play();
			while(mode<=3){
				track.write(getWave(length),0,length);
			}
			track.stop();
			track.release();
		}

		public short[] getWave(int length){
			short[] w = new short[length];
			for(int i=0;i<length;i++){
				double s=0;
				for(int j=0;j<frequencies.length;j++){
					s+=wave((double)term*frequencies[j]/(double)sampleRate,waveform)*volume/400.0*(Short.MAX_VALUE);
				}
				if(s>=Short.MAX_VALUE) s=(double)Short.MAX_VALUE;
				if(s<=-Short.MAX_VALUE) s=(double)(-Short.MAX_VALUE);
				w[i]=(short)s;
				term++;
				if(term>=sampleRate) term-=sampleRate;
			}
			return w;
		}

	}
	
	public void finish(){
		mode=4;
	}

}
