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
		double t=0;
		double dt=1.0/sampleRate;
		for(int i=0;i<sampleRate;i++){
			double ss=0;
			for(int j=0;j<freqs.length;j++){
				double s=Math.sin(2.0*Math.PI*t*freqs[j]);
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
		track.stop();
	}
	
}
