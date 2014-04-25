package jp.nita.tapchord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	AudioTrack track=null;
	int volume=0;
	int sampleRate=4000;
	int waveform;
	int waveLength;
	static AudioTrack lastTrack=null;
	
	public Sound(Integer[] freqs,Context context){
		volume=Statics.getValueOfVolume(Statics.getPreferenceValue(context,Statics.PREF_VOLUME,0));
		sampleRate=Statics.getValueOfSamplingRate(Statics.getPreferenceValue(context,Statics.PREF_SAMPLING_RATE,0));
		waveform=Statics.getPreferenceValue(context,Statics.PREF_WAVEFORM,0);
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
		        AudioFormat.ENCODING_PCM_16BIT,
		        sampleRate,
		        AudioTrack.MODE_STATIC);
		
		byte[] wave=new byte[sampleRate];
		double t=0;
		double dt=1.0/sampleRate;
		for(int i=0;i<sampleRate;i++){
			double ss=0;
			for(int j=0;j<freqs.length;j++){
				double s=wave(2.0*Math.PI*t*freqs[j],waveform);
				ss+=s;
			}
			double sss=(Byte.MAX_VALUE*ss*(volume/400.0));
			if(sss>=127) sss=127;
			if(sss<=-127) sss=-127;
			wave[i]=(byte)sss;
			t=dt*i;
		}
		track.write(wave,0,wave.length);
		waveLength=wave.length/2;
		track.setLoopPoints(0,waveLength,-1);
	}
	
	public void play(){
		if(lastTrack!=null) lastTrack.release();
		track.stop();
		track.reloadStaticData();
		track.setLoopPoints(0,waveLength,-1);
		track.play();
		lastTrack=track;
	}
	
	public void stop(){
		track.pause();
		track.stop();
		track.release();
		lastTrack=null;
	}
	
	public double wave(double t,int which){
		switch(which){
		case 0:
			return Math.sin(t);
		case 1:
			return (t/(2*Math.PI)-Math.floor(t/(Math.PI*2)+1/2.0))*2;
		case 2:
			return Math.sin(t)>0?1:-1;
		case 3:
		{
			double tt=t/Math.PI/2-Math.floor(t/Math.PI/2);
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
		case 4:
			return (t/Math.PI/2)-Math.floor(t/Math.PI/2)<1.0/4.0?1:-1;
		case 5:
			return (t/Math.PI/2)-Math.floor(t/Math.PI/2)<1.0/8.0?1:-1;
		default:
			return Math.sin(t);
		}
	}
	
}
