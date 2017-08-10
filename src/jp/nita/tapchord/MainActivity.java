package jp.nita.tapchord;

import android.media.AudioManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceInfo.PortInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiManager.OnDeviceOpenedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static int heartBeatInterval = 5;

	private Heart heart = null;
	
	public static MainActivity main = null;
	
	MidiDevice midiDevice = null;
	MidiInputPort inputPort = null;
	
	int volume;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updatePreferences();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		heart = new Heart();
		heart.start();
		main = this;
		
		final OnDeviceOpenedListener onDeviceOpenedListener = new MidiManager.OnDeviceOpenedListener() {
		    @Override
		    public void onDeviceOpened(MidiDevice device) {
		        if (device == null) {
		        	Toast.makeText(MainActivity.this, "Opening device failed", Toast.LENGTH_SHORT).show();
		        } else {
		        	Toast.makeText(MainActivity.this, "Opened device : " + device.toString(), Toast.LENGTH_SHORT).show();
		        	if (device.getInfo().getInputPortCount() == 0) {
		        		Toast.makeText(MainActivity.this, "but no input ports available", Toast.LENGTH_SHORT).show();
		        	} else {
		        		for (MidiDeviceInfo.PortInfo portInfo : device.getInfo().getPorts()) {
		        			if (portInfo.getType() == PortInfo.TYPE_INPUT) {
		        				inputPort = device.openInputPort(portInfo.getPortNumber());
		        				if (inputPort != null) {
		        					Toast.makeText(MainActivity.this, "Opened input port : " + portInfo.getName(), Toast.LENGTH_SHORT).show();
		        					break;
		        				}
		        			}
		        		}
		        		if (inputPort == null) {
		        			Toast.makeText(MainActivity.this, "but no input ports available", Toast.LENGTH_SHORT).show();
		        		}
		        	}
		        	midiDevice = device;
		        }
		    }
		};
		
		MidiManager.DeviceCallback deviceCallBack = new MidiManager.DeviceCallback() {
		    @Override
		    public void onDeviceAdded(MidiDeviceInfo device) {
		        super.onDeviceAdded(device);
		        Toast.makeText(MainActivity.this, "MIDI device added : " + device.toString(), Toast.LENGTH_SHORT).show();
		        MidiManager midiManager = (MidiManager)getSystemService(Context.MIDI_SERVICE);
		        midiManager.openDevice(device, onDeviceOpenedListener, new Handler(Looper.getMainLooper()));
		    }

		    @Override
		    public void onDeviceRemoved(MidiDeviceInfo device) {
		        super.onDeviceRemoved(device);
		        midiDevice = null;
		        inputPort = null;
		        Toast.makeText(MainActivity.this, "MIDI device removed : " + device.toString(), Toast.LENGTH_SHORT).show();
		    }

		    @Override
		    public void onDeviceStatusChanged(MidiDeviceStatus status) {
		        super.onDeviceStatusChanged(status);
		        
		    }
		};
		
		MidiManager midiManager = (MidiManager)getSystemService(Context.MIDI_SERVICE);
		midiManager.registerDeviceCallback(deviceCallBack, new Handler(Looper.getMainLooper()));
	}

	@Override
	protected void onPause() {
		super.onPause();
		((TapChordView) findViewById(R.id.tapChordView)).activityPaused();
		heart.sleep();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((TapChordView) findViewById(R.id.tapChordView)).activityResumed();
		heart.wake();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_quit:
			new AlertDialog.Builder(this).setTitle(getString(R.string.action_quit))
					.setMessage(getString(R.string.message_quit))
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
		}
		return false;
	}

	public static void setAnimationQuality(int aq) {
		switch (aq) {
		case -1:
			heartBeatInterval = 25;
			break;
		case 0:
			heartBeatInterval = 5;
			break;
		case 1:
			heartBeatInterval = 1;
			break;
		default:
			heartBeatInterval = 5;
			break;
		}
	}

	class Heart extends Thread implements Runnable {
		private boolean awake = true;
		private boolean alive = true;

		public void run() {
			TapChordView view = ((TapChordView) findViewById(R.id.tapChordView));
			while (alive) {
				try {
					Thread.sleep(heartBeatInterval);
					if (awake)
						view.heartbeat(heartBeatInterval);
				} catch (InterruptedException e) {
					die();
				}
			}
		}

		public void wake() {
			awake = true;
		}

		public void sleep() {
			awake = false;
		}

		public void die() {
			alive = false;
		}
	}
	
	public void sendMidiEventToDevice(int on, int note) {
		if (midiDevice == null) {
			return;
		}
		if (inputPort == null) {
			return;
		}
		byte[] buffer = new byte[32];
		int numBytes = 0;
		int channel = 1;
		buffer[numBytes++] = (byte)((on > 0 ? 0x90 : 0x80) + (channel - 1));
		buffer[numBytes++] = (byte)(note);
		buffer[numBytes++] = (byte)(volume * 127 / 100);
		int offset = 0;
		try {
		    inputPort.send(buffer, offset, numBytes);
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		    e.printStackTrace();
		}
	}

	public void updatePreferences() {
		int animationQuality = Statics.preferenceValue(this, Statics.PREF_ANIMATION_QUALITY, 0);
		setAnimationQuality(animationQuality);
		volume = Statics.valueOfVolume(Statics.preferenceValue(this, Statics.PREF_VOLUME, 0));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this).setTitle(getString(R.string.action_quit))
					.setMessage(getString(R.string.message_quit))
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
//		} else if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_BACKSLASH) {
//			TapChordView.debugMode = !TapChordView.debugMode;
//			((TapChordView) findViewById(R.id.tapChordView)).invalidate();
		} else {
			boolean result = false;
			result = ((TapChordView) findViewById(R.id.tapChordView)).keyPressed(keyCode, event);
			if (!result) {
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean result = false;
		result = ((TapChordView) findViewById(R.id.tapChordView)).keyReleased(keyCode, event);
		if (!result) {
			return super.onKeyUp(keyCode, event);
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event){
		boolean result = false;
		result = ((TapChordView) findViewById(R.id.tapChordView)).keyLongPressed(keyCode, event);
		if (!result) {
			return super.onKeyLongPress(keyCode, event);
		}
	    return super.onKeyLongPress(keyCode, event);
	}

}
