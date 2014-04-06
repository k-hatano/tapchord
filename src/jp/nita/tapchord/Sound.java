package jp.nita.tapchord;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	AudioTrack track=null;
	final int sampleRate=8000;
	int waveLength;
	
	public Sound(Integer[] freqs,float volume){
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
		track.stop();
		track.reloadStaticData();
		track.setLoopPoints(0,waveLength,-1);
		track.play();
	}
	
	public void stop(){
		track.pause();
		track.stop();
		track.release();
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
