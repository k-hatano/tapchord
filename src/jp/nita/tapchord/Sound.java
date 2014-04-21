package jp.nita.tapchord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	AudioTrack track=null;
	int sampleRate=4000;
	int waveLength;
	static AudioTrack lastTrack=null;
	
	public Sound(Integer[] freqs,float volume,Context context){
		sampleRate=Statics.getValueOfSamplingRate(Statics.getPreferenceValue(context,Statics.PREF_SAMPLING_RATE,0));
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_DEFAULT,
		        AudioFormat.ENCODING_DEFAULT,
		        sampleRate,
		        AudioTrack.MODE_STATIC);
		
		byte[] wave=new byte[sampleRate];
		float t=0;
		float dt=1.0f/sampleRate;
		for(int i=0;i<sampleRate;i++){
			float ss=0;
			for(int j=0;j<freqs.length;j++){
				float s=wave(2.0*Math.PI*t*freqs[j],0);
				ss+=s;
			}
			wave[i]=(byte)(Byte.MAX_VALUE*ss*volume);
			t+=dt;
		}
		track.write(wave,0,wave.length);
		waveLength=wave.length/2;
		track.setLoopPoints(0,wave.length,-1);
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
	
	public float wave(double t,int which){
		switch(which){
		case 0:
			return (float)Math.sin(t);
		case 1:
			return Math.sin(t)>0?1:-1;
		default:
			return (float)Math.sin(t);
		}
	}
	
}
