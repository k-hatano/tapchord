package jp.nita.tapchord;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
	AudioTrack track=null;
	final int sampleRate=44100;
	
	public Sound(int[] freqs,float volume){
		track = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_DEFAULT,
		        AudioFormat.ENCODING_DEFAULT,
		        sampleRate,
		        AudioTrack.MODE_STATIC);
		
		byte[] wave=new byte[sampleRate];
		double t=0;
		double dt=1.0/sampleRate;
		for(int i=0;i<sampleRate;i++){
			double s=Math.sin(2.0*Math.PI*t*441);
			wave[i]=(byte)(Byte.MAX_VALUE*s/3);
			t+=dt;
		}
		track.write(wave,0,wave.length);
	}
	
	public void play(){
		track.play();
	}
	
	public void stop(){
		track.stop();
		track.reloadStaticData();
	}
	
}
