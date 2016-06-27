package jp.nita.tapchordplus;

import jp.kshoji.driver.midi.activity.AbstractSingleMidiActivity;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AbstractSingleMidiActivity {

	public static int heartBeatInterval = 5;
	int volume;

	public static MainActivity main = null;

	private Heart heart = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updatePreferenceValues();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		heart = new Heart();
		heart.start();
		main = this;
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
		updatePreferenceValues();
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
		MidiOutputDevice device = getMidiOutputDevice();
		if (device != null) {
			if (on > 0) {
				device.sendMidiNoteOn(0, 0, note, volume * 127 / 100);
			} else {
				device.sendMidiNoteOff(0, 0, note, volume * 127 / 100);
			}
		}
	}

	public void updatePreferenceValues() {
		int animationQuality = Statics.preferenceValue(this, Statics.PREF_ANIMATION_QUALITY, 0);
		setAnimationQuality(animationQuality);
		volume = Statics.valueOfVolume(Statics.preferenceValue(this, Statics.PREF_VOLUME, 0));
	}

	@Override
	public void onDeviceDetached(UsbDevice usbDevice) {
		Toast.makeText(MainActivity.this, getString(R.string.message_midi_device_detached_1)
				+ usbDevice.getDeviceName() + getString(R.string.message_midi_device_detached_2), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDeviceAttached(UsbDevice usbDevice) {
		Toast.makeText(MainActivity.this, getString(R.string.message_midi_device_attached_1)
				+ usbDevice.getDeviceName() + getString(R.string.message_midi_device_attached_2), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onMidiMiscellaneousFunctionCodes(MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiCableEvents(MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiSystemCommonMessage(MidiInputDevice sender, int cable, byte[] bytes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiSystemExclusive(MidiInputDevice sender, int cable, byte[] systemExclusive) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiNoteOff(MidiInputDevice sender, int cable, int channel, int note, int velocity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiNoteOn(MidiInputDevice sender, int cable, int channel, int note, int velocity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiPolyphonicAftertouch(MidiInputDevice sender, int cable, int channel, int note, int pressure) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiControlChange(MidiInputDevice sender, int cable, int channel, int function, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiProgramChange(MidiInputDevice sender, int cable, int channel, int program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiChannelAftertouch(MidiInputDevice sender, int cable, int channel, int pressure) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiPitchWheel(MidiInputDevice sender, int cable, int channel, int amount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMidiSingleByte(MidiInputDevice sender, int cable, int byte1) {
		// TODO Auto-generated method stub

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
