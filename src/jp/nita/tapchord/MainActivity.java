package jp.nita.tapchord;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static int heartBeatInterval = 5;
	public int neverShowAlphaReleased = 0;

	private Heart heart = null;

	public static MainActivity main = null;

	public static MidiDevice midiDevice = null;
	public static MidiInputPort inputPort = null;

	static Object midiProcess = new Object();

	public static SettingsActivity settingsActivity = null;

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

		MidiManager midiManager = (MidiManager)getSystemService(Context.MIDI_SERVICE);
		midiManager.registerDeviceCallback(deviceCallBack, new Handler(Looper.getMainLooper()));

		if (neverShowAlphaReleased <= 0) {
			showAlphaVersionInformationDialog();
		}
	}

	public void showAlphaVersionInformationDialog() {
		TextView messageTextView = new TextView(this);
		messageTextView.setTextAppearance(this, android.R.style.TextAppearance_Inverse);
		messageTextView.setText(getString(R.string.version_201_alpha_released_message));

		TextView cautionTextView = new TextView(this);
		cautionTextView.setTextAppearance(this, android.R.style.TextAppearance_Inverse);
		cautionTextView.setText(getString(R.string.version_201_alpha_released_caution));

		String locale = Locale.getDefault().getLanguage();
		ImageView betaImage = new ImageView(this);
		if (locale.equals("ja")) {
			betaImage.setImageDrawable(getResources().getDrawable(R.drawable.beta_ja));
		} else {
			betaImage.setImageDrawable(getResources().getDrawable(R.drawable.beta_en));
		}
		betaImage.setPadding(getResources().getDimensionPixelSize(R.dimen.beta_image_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_image_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_image_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_image_padding));
		betaImage.setScaleType(ScaleType.FIT_XY);
		betaImage.setAdjustViewBounds(true);
		betaImage.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.beta_image_width_max));
		betaImage.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.beta_image_width_max));

		final CheckBox neverShowAgainCheckBox = new CheckBox(this);
		neverShowAgainCheckBox.setTextAppearance(this, android.R.style.TextAppearance_Inverse);
		neverShowAgainCheckBox.setTextColor(neverShowAgainCheckBox.getTextColors().getDefaultColor());
		neverShowAgainCheckBox.setText(getString(R.string.never_show_again));

		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setPadding(getResources().getDimensionPixelSize(R.dimen.beta_dialog_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_dialog_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_dialog_padding),
				getResources().getDimensionPixelSize(R.dimen.beta_dialog_padding));
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.addView(messageTextView);
		linearLayout.addView(betaImage);
		linearLayout.addView(cautionTextView);
		linearLayout.addView(neverShowAgainCheckBox);

		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(linearLayout);

		final MainActivity finalActivity = this;

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(getString(R.string.version_201_alpha_released_title))
				.setIcon(android.R.drawable.ic_dialog_info).setView(scrollView)
				.setPositiveButton(getString(R.string.remind_me_later), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (neverShowAgainCheckBox.isChecked()) {
							Statics.setPreferenceValue(finalActivity, Statics.PREF_NEVER_SHOW_ALPHA_RELEASED, 1);
						}
					}
				}).setNeutralButton(getString(R.string.go_to_google_play), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (neverShowAgainCheckBox.isChecked()) {
							Statics.setPreferenceValue(finalActivity, Statics.PREF_NEVER_SHOW_ALPHA_RELEASED, 1);
						}
						Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=jp.nita.tapchord");
						Intent i = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(i);
					}
				}).create();
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.show();
	}

	final OnDeviceOpenedListener onDeviceOpenedListener = new MidiManager.OnDeviceOpenedListener() {
	    @Override
	    public void onDeviceOpened(MidiDevice device) {
			synchronized (midiProcess) {
				if (device != null) {
					if (device.getInfo().getInputPortCount() > 0) {

						ProgressDialog progressDialog;
						progressDialog = new ProgressDialog(MainActivity.this);
						progressDialog.setTitle("Connected MIDI device");
						progressDialog.setMessage("Opening input port...");
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.show();

						int devicesMax = device.getInfo().getPorts().length;
						int deviceIndex = 0;
						for (MidiDeviceInfo.PortInfo portInfo : device.getInfo().getPorts()) {
							progressDialog.setMessage(
									"Opening input port (" + (deviceIndex + 1) + "/" + devicesMax + ")");
							if (portInfo.getType() == PortInfo.TYPE_INPUT) {
								MidiInputPort openingPort = device.openInputPort(portInfo.getPortNumber());
								if (openingPort != null) {
									inputPort = openingPort;
									midiDevice = device;
									String deviceName = device.getInfo().getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
									Toast.makeText(MainActivity.this,
											getString(R.string.midi_device_connected) + " " + deviceName,
											Toast.LENGTH_SHORT).show();
									if (MainActivity.settingsActivity != null) {
										MainActivity.settingsActivity.midiDeviceStateChanged(device);
									}
									break;
								}
							}
							deviceIndex++;
						}
						progressDialog.dismiss();
						if (inputPort == null) {
							try {
								device.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
	    }
	};

	final MidiManager.DeviceCallback deviceCallBack = new MidiManager.DeviceCallback() {
		@Override
		public void onDeviceAdded(MidiDeviceInfo device) {
			super.onDeviceAdded(device);
			synchronized (midiProcess) {
				MidiManager midiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
				midiManager.openDevice(device, onDeviceOpenedListener, new Handler(Looper.getMainLooper()));
			}
		}

		@Override
		public void onDeviceRemoved(MidiDeviceInfo device) {
			super.onDeviceRemoved(device);

			if (inputPort != null || midiDevice != null) {
				Toast.makeText(MainActivity.this, getString(R.string.midi_device_disconnected), Toast.LENGTH_SHORT)
						.show();
			}

			synchronized (midiProcess) {
				if (inputPort != null) {
					try {
						inputPort.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputPort = null;
				}
				if (midiDevice != null) {
					try {
						midiDevice.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					midiDevice = null;
				}
			}
			if (MainActivity.settingsActivity != null) {
				MainActivity.settingsActivity.midiDeviceStateChanged(null);
			}
		}

		@Override
		public void onDeviceStatusChanged(MidiDeviceStatus status) {
			super.onDeviceStatusChanged(status);

		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		((TapChordView) findViewById(R.id.tapChordView)).activityPaused(this);
		heart.sleep();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((TapChordView) findViewById(R.id.tapChordView)).activityResumed(this);
		updatePreferences();
		heart.wake();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		((TapChordView) findViewById(R.id.tapChordView)).activityPaused(this);
		heart.die();
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
		updatePreferences();
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
		neverShowAlphaReleased = Statics.preferenceValue(this, Statics.PREF_NEVER_SHOW_ALPHA_RELEASED, 0);
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
			// } else if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode ==
			// KeyEvent.KEYCODE_BACKSLASH) {
			// TapChordView.debugMode = !TapChordView.debugMode;
			// ((TapChordView) findViewById(R.id.tapChordView)).invalidate();
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
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean result = false;
		result = ((TapChordView) findViewById(R.id.tapChordView)).keyLongPressed(keyCode, event);
		if (!result) {
			return super.onKeyLongPress(keyCode, event);
		}
		return super.onKeyLongPress(keyCode, event);
	}

	public static void onAppResumed(Activity activity) {
		MidiManager midiManager = (MidiManager) main.getSystemService(Context.MIDI_SERVICE);
		midiManager.registerDeviceCallback(main.deviceCallBack, new Handler(Looper.getMainLooper()));
	}

	public static void onAppPaused(Activity activity) {
		MidiManager midiManager = (MidiManager) main.getSystemService(Context.MIDI_SERVICE);
		if (midiDevice != null) {
			if (inputPort != null || midiDevice != null) {
				Toast.makeText(main, main.getString(R.string.midi_device_disconnected), Toast.LENGTH_SHORT).show();
			}

			synchronized (midiProcess) {
				if (inputPort != null) {
					try {
						inputPort.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputPort = null;
				}
				if (midiDevice != null) {
					try {
						midiDevice.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					midiDevice = null;
				}
			}
			if (MainActivity.settingsActivity != null) {
				MainActivity.settingsActivity.midiDeviceStateChanged(null);
			}
		}

		midiManager.unregisterDeviceCallback(main.deviceCallBack);
	}

	public static void disconnectMidiDevice(Activity activity) {
		MidiManager midiManager = (MidiManager) main.getSystemService(Context.MIDI_SERVICE);
		if (midiDevice != null) {
			if (inputPort != null || midiDevice != null) {
				Toast.makeText(main, main.getString(R.string.midi_device_disconnected), Toast.LENGTH_SHORT).show();
			}

			synchronized (midiProcess) {
				if (inputPort != null) {
					try {
						inputPort.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputPort = null;
				}
				if (midiDevice != null) {
					try {
						midiDevice.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					midiDevice = null;
				}
			}
			if (MainActivity.settingsActivity != null) {
				MainActivity.settingsActivity.midiDeviceStateChanged(null);
			}
		}

		midiManager.unregisterDeviceCallback(main.deviceCallBack);
	}

}
