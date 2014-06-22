package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Sound {
	static List<AudioTrack> tracks=new ArrayList<AudioTrack>();

	int volume=0;
	int sampleRate=4000;
	int waveform;
	int waveLength;
	int soundRange;

	int attack=0;
	int decay=0;
	int release=0;

	public Sound(Integer[] freqs,Context context){
		volume=Statics.getValueOfVolume(Statics.getPreferenceValue(context,Statics.PREF_VOLUME,0));
		soundRange=Statics.getValueOfVolume(Statics.getPreferenceValue(context,Statics.PREF_SOUND_RANGE,0));
		sampleRate=Statics.getValueOfSamplingRate(Statics.getPreferenceValue(context,Statics.PREF_SAMPLING_RATE,0));
		waveform=Statics.getPreferenceValue(context,Statics.PREF_WAVEFORM,0);

		attack=Statics.getPreferenceValue(context,Statics.PREF_ATTACK_TIME,0);
		decay=Statics.getPreferenceValue(context,Statics.PREF_DECAY_TIME,0);
		release=Statics.getPreferenceValue(context,Statics.PREF_RELEASE_TIME,0);

		if(tracks.size()>0){
			for(int j=0;j<tracks.size();j++){
				tracks.get(j).pause();
				tracks.get(j).stop();
				tracks.get(j).release();
			}
			tracks=new ArrayList<AudioTrack>();
		}

		for(int j=0;j<freqs.length;j++){
			int attackLength=attack*sampleRate/1000;
			// int decayLength=decay*sampleRate/1000;
			int sustainLength=sampleRate/freqs[j];
			int releaseLength=release*sampleRate/1000;
			int length=attackLength+sustainLength+releaseLength;

			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					length*2,
					AudioTrack.MODE_STATIC);

			short[] wave=new short[length];
			for(int i=0;i<length;i++){
				double t=(double)i/sampleRate;
				double s=wave(t*freqs[j],waveform);
				double ss=(Short.MAX_VALUE*s*volume/400.0);
				if(ss>=Short.MAX_VALUE) ss=(double)Short.MAX_VALUE;
				if(ss<=-Short.MAX_VALUE) ss=(double)(-Short.MAX_VALUE);
				if(i<attackLength){
					ss=(ss*i/attackLength);
				}
				if(i>attackLength+sustainLength){
					ss=(ss*(length-i)/(releaseLength));
				}
				wave[i]=(short)ss;
			}
			track.write(wave,0,wave.length);
			waveLength=wave.length;
			Log.i("tapchord.Sound",""+attackLength+","+sustainLength+","+releaseLength+";"+waveLength);
			track.setLoopPoints(attackLength,attackLength+sustainLength,-1);

			tracks.add(track);
		}
	}

	public void play(){
		for(int j=0;j<tracks.size();j++){
			tracks.get(j).play();
		}
	}

	public void stop(){
		for(int j=0;j<tracks.size();j++){
			tracks.get(j).stop();
		}
	}

	public void release(){
		for(int j=0;j<tracks.size();j++){
			tracks.get(j).pause();
			tracks.get(j).stop();
			tracks.get(j).release();
		}
		tracks=new ArrayList<AudioTrack>();
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

}
