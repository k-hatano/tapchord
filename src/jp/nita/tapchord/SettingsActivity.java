package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnClickListener,OnItemClickListener {

	int darken=0;
	int scale;
	int volume;
	int samplingRate;
	int waveform;
	int vibration;
	int soundRange;
	int attackTime;
	int decayTime;
	int sustainLevel;
	int releaseTime;

	int selection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i=getIntent();
		darken=i.getIntExtra("darken",0);
		setTheme(darken>0?android.R.style.Theme_Black:android.R.style.Theme_Light);

		setContentView(R.layout.activity_settings);

		Button button;
		button=(Button)findViewById(R.id.settings_ok);
		button.setOnClickListener(this);
	}

	public void getPreferenceValues(){
		scale=Statics.getPreferenceValue(this,Statics.PREF_SCALE,0);
		darken=Statics.getPreferenceValue(this,Statics.PREF_DARKEN,0);
		vibration=Statics.getPreferenceValue(this,Statics.PREF_VIBRATION,0);
		volume=Statics.getPreferenceValue(this,Statics.PREF_VOLUME,0);
		samplingRate=Statics.getPreferenceValue(this,Statics.PREF_SAMPLING_RATE,0);
		waveform=Statics.getPreferenceValue(this,Statics.PREF_WAVEFORM,0);
		soundRange=Statics.getPreferenceValue(this,Statics.PREF_SOUND_RANGE,0);
		attackTime=Statics.getPreferenceValue(this,Statics.PREF_ATTACK_TIME,0);
		decayTime=Statics.getPreferenceValue(this,Statics.PREF_DECAY_TIME,0);
		sustainLevel=Statics.getPreferenceValue(this,Statics.PREF_SUSTAIN_LEVEL,0);
		releaseTime=Statics.getPreferenceValue(this,Statics.PREF_RELEASE_TIME,0);
	}

	public void updateSettingsListView(){
		ListView items=(ListView)findViewById(R.id.settings_items);
		List<Map<String,String>> list=new ArrayList<Map<String,String>>();
		{
			Map<String,String> map;

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_scale));
			map.put("value", Statics.getLongStringOfScale(scale));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_darken));
			map.put("value", Statics.getOnOrOffString(this,darken));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_vibration));
			map.put("value", Statics.getOnOrOffString(this,vibration));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_volume));
			map.put("value", ""+Statics.getValueOfVolume(volume));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_sound_range));
			map.put("value", ""+Statics.getStringOfSoundRange(soundRange));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_waveform));
			map.put("value", Statics.getValueOfWaveform(waveform,this));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_envelope));
			map.put("value", ""+Statics.getStringOfEnvelope(attackTime,decayTime,sustainLevel,releaseTime,this));
			list.add(map);

			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_sampling_rate));
			map.put("value", ""+Statics.getValueOfSamplingRate(samplingRate)+" "+getString(R.string.settings_sampling_rate_hz));
			list.add(map);
		}

		SimpleAdapter adapter
		=new SimpleAdapter(this,list
				,android.R.layout.simple_expandable_list_item_2,
				new String[]{"key","value"},
				new int[]{android.R.id.text1,android.R.id.text2});
		items.setAdapter(adapter);

		items.setOnItemClickListener(this);
	}

	@Override
	public void onResume(){
		super.onResume();

		getPreferenceValues();
		updateSettingsListView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_ok:
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.settings_ok:
			finish();
			break;
		}
	}

	int selected=-1;

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		selected=-1;
		switch(arg2){
		case 0:{
			CharSequence list[]=new String[15];
			for(int i=-7;i<=7;i++) list[i+7]=Statics.getLongStringOfScale(i);
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_scale))
			.setSingleChoiceItems(list,scale+7,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					setScale(arg1-7);
					arg0.dismiss();
				}
			}).show();
			break;
		}
		case 1:{
			CharSequence list[]=new String[1];
			list[0]=getString(R.string.on);
			boolean checks[]=new boolean[1];
			checks[0]=darken>0;
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_darken))
			.setMultiChoiceItems(list,checks,new OnMultiChoiceClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
					switch(arg1){
					case 0:
						selected=arg2?1:0;
						break;
					default:
						break;
					}
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0&&selected!=darken){
						setDarken(selected);
						SettingsActivity.this.finish();
					}
					((ListView)findViewById(R.id.settings_items)).setSelection(0);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(0);
				}
			})
			.show();
			break;
		}
		case 2:{
			CharSequence list[]=new String[1];
			list[0]=getString(R.string.on);
			boolean checks[]=new boolean[1];
			checks[0]=vibration>0;
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_vibration))
			.setMultiChoiceItems(list,checks,new OnMultiChoiceClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
					switch(arg1){
					case 0:
						selected=arg2?1:0;
						break;
					default:
						break;
					}
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setVibration(selected);
					((ListView)findViewById(R.id.settings_items)).setSelection(1);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(1);
				}
			})
			.show();
			break;
		}
		case 3:{
			int vol=volume+50;
			final TextView volumeView = new TextView(this);
			volumeView.setText(""+vol);
			volumeView.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			final SeekBar seekBar = new SeekBar(this);
			seekBar.setProgress(vol);
			seekBar.setMax(100);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					volumeView.setText(""+progress);
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
			});
			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(volumeView);
			layout.addView(seekBar);
			layout.setPadding(8,8,8,8);
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_volume))
			.setView(layout)
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setVolume(seekBar.getProgress()-50);
					((ListView)findViewById(R.id.settings_items)).setSelection(2);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(2);
				}
			})
			.show();
			break;
		}
		case 4:{
			final TextView rangeView = new TextView(this);
			rangeView.setText(""+Statics.getStringOfSoundRange(soundRange));
			rangeView.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			final SeekBar seekBar = new SeekBar(this);
			seekBar.setProgress(soundRange+24);
			seekBar.setMax(48);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					rangeView.setText(""+Statics.getStringOfSoundRange(seekBar.getProgress()-24));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
			});
			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(rangeView);
			layout.addView(seekBar);
			layout.setPadding(8,8,8,8);
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_sound_range))
			.setView(layout)
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setSoundRange(seekBar.getProgress()-24);
					((ListView)findViewById(R.id.settings_items)).setSelection(3);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(3);
				}
			})
			.show();
			break;
		}
		case 5:{
			CharSequence list[]=new String[6];
			for(int i=0;i<6;i++){
				list[i]=Statics.getValueOfWaveform(i,this);
			}
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_waveform))
			.setSingleChoiceItems(list,waveform,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					setWaveform(arg1);
					arg0.dismiss();
					((ListView)findViewById(R.id.settings_items)).setSelection(4);
				}
			})
			.show();
			break;
		}
		case 6:{
			final SeekBar attackSeekBar = new SeekBar(this);
			final SeekBar decaySeekBar = new SeekBar(this);
			final SeekBar sustainSeekBar = new SeekBar(this);
			final SeekBar releaseSeekBar = new SeekBar(this);
			final TextView attackLabel = new TextView(this);
			final TextView decayLabel = new TextView(this);
			final TextView sustainLabel = new TextView(this);
			final TextView releaseLabel = new TextView(this);
			TableRow.LayoutParams tableRowParams;
			attackSeekBar.setProgress(attackTime);
			attackSeekBar.setMax(100);
			attackSeekBar.setPadding(0,0,0,8);
			attackSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					attackLabel.setText(getString(R.string.settings_attack)+" : "+Statics.getStringOfSingleTime(progress,SettingsActivity.this));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
			decaySeekBar.setProgress(decayTime);
			decaySeekBar.setMax(100);
			decaySeekBar.setPadding(0,0,0,8);
			decaySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					decayLabel.setText(getString(R.string.settings_decay)+" : "+Statics.getStringOfSingleTime(progress,SettingsActivity.this));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
			sustainSeekBar.setProgress(sustainLevel+100);
			sustainSeekBar.setMax(100);
			sustainSeekBar.setPadding(0,0,0,8);
			sustainSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					sustainLabel.setText(getString(R.string.settings_sustain)+" : "+Statics.getStringOfSustainLevel(progress-100,SettingsActivity.this));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
			releaseSeekBar.setProgress(releaseTime);
			releaseSeekBar.setMax(100);
			releaseSeekBar.setPadding(0,0,0,8);
			releaseSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					releaseLabel.setText(getString(R.string.settings_release)+" : "+Statics.getStringOfSingleTime(progress,SettingsActivity.this));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});
			final TableLayout tableLayout = new TableLayout(this);
			TableRow row1=new TableRow(SettingsActivity.this);
			attackLabel.setText(getString(R.string.settings_attack)+" : "+Statics.getStringOfSingleTime(attackTime,SettingsActivity.this));
			attackLabel.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			row1.addView(attackLabel);
			row1.addView(attackSeekBar);
			tableLayout.addView(row1);
			TableRow row2=new TableRow(SettingsActivity.this);
			decayLabel.setText(getString(R.string.settings_decay)+" : "+Statics.getStringOfSingleTime(decayTime,SettingsActivity.this));
			decayLabel.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			row2.addView(decayLabel);
			row2.addView(decaySeekBar);
			tableLayout.addView(row2);
			TableRow row3=new TableRow(SettingsActivity.this);
			sustainLabel.setText(getString(R.string.settings_sustain)+" : "+Statics.getStringOfSustainLevel(sustainLevel,SettingsActivity.this));
			sustainLabel.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			row3.addView(sustainLabel);
			row3.addView(sustainSeekBar);
			tableLayout.addView(row3);
			TableRow row4=new TableRow(SettingsActivity.this);
			releaseLabel.setText(getString(R.string.settings_release)+" : "+Statics.getStringOfSingleTime(releaseTime,SettingsActivity.this));
			releaseLabel.setTextAppearance(this,darken>0?android.R.style.TextAppearance:android.R.style.TextAppearance_Inverse);
			row4.addView(releaseLabel);
			tableLayout.addView(row4);
			row4.addView(releaseSeekBar);

			tableRowParams = (TableRow.LayoutParams)attackSeekBar.getLayoutParams();
			tableRowParams.span = 3; 
			attackSeekBar.setLayoutParams(tableRowParams);
			tableRowParams = (TableRow.LayoutParams)decaySeekBar.getLayoutParams();
			tableRowParams.span = 3; 
			decaySeekBar.setLayoutParams(tableRowParams);
			tableRowParams = (TableRow.LayoutParams)sustainSeekBar.getLayoutParams();
			tableRowParams.span = 3; 
			sustainSeekBar.setLayoutParams(tableRowParams);
			tableRowParams = (TableRow.LayoutParams)releaseSeekBar.getLayoutParams();
			tableRowParams.span = 3; 
			releaseSeekBar.setLayoutParams(tableRowParams);

			FrameLayout.LayoutParams layoutParams=(FrameLayout.LayoutParams)tableLayout.getLayoutParams();
			layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
			tableLayout.setLayoutParams(layoutParams);
			tableLayout.setPadding(8,8,8,8);
			tableLayout.setStretchAllColumns(true);

			ScrollView scrollView = new ScrollView(SettingsActivity.this);
			scrollView.addView(tableLayout);

			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_envelope))
			.setView(scrollView)
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setAttackTime(attackSeekBar.getProgress());
					setDecayTime(decaySeekBar.getProgress());
					setSustainLevel(sustainSeekBar.getProgress());
					setReleaseTime(releaseSeekBar.getProgress());
					((ListView)findViewById(R.id.settings_items)).setSelection(5);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(5);
				}
			})
			.show();
			break;
		}
		case 7:{
			CharSequence list[]=new String[4];
			for(int i=0;i<4;i++){
				list[i]=""+Statics.getValueOfSamplingRate(i-3)+" "+getString(R.string.settings_sampling_rate_hz);
			}
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_sampling_rate))
			.setSingleChoiceItems(list,samplingRate+3,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					setSamplingRate(arg1-3);
					arg0.dismiss();
					((ListView)findViewById(R.id.settings_items)).setSelection(6);
				}
			})
			.show();
			break;
		}
		default:
			return;
		}
	}

	public void setScale(int s){
		scale=s;
		Statics.setPreferenceValue(this,Statics.PREF_SCALE,scale);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setDarken(int d){
		darken=d;
		Statics.setPreferenceValue(this,Statics.PREF_DARKEN,darken);
		getPreferenceValues();
		updateSettingsListView();
	}
	public void setVibration(int v){
		vibration=v;
		Statics.setPreferenceValue(this,Statics.PREF_VIBRATION,vibration);
		getPreferenceValues();
		updateSettingsListView();
	}
	public void setSamplingRate(int sr){
		samplingRate=sr;
		Statics.setPreferenceValue(this,Statics.PREF_SAMPLING_RATE,samplingRate);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setVolume(int v){
		volume=v;
		Statics.setPreferenceValue(this,Statics.PREF_VOLUME,volume);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setSoundRange(int sr){
		soundRange=sr;
		Statics.setPreferenceValue(this,Statics.PREF_SOUND_RANGE,sr);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setAttackTime(int at){
		attackTime=at;
		Statics.setPreferenceValue(this,Statics.PREF_ATTACK_TIME,at);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setDecayTime(int dt){
		decayTime=dt;
		Statics.setPreferenceValue(this,Statics.PREF_DECAY_TIME,dt);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setSustainLevel(int sl){
		sustainLevel=sl;
		Statics.setPreferenceValue(this,Statics.PREF_SUSTAIN_LEVEL,sl-100);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setReleaseTime(int rt){
		releaseTime=rt;
		Statics.setPreferenceValue(this,Statics.PREF_RELEASE_TIME,rt);
		getPreferenceValues();
		updateSettingsListView();
	}

	public void setWaveform(int wf){
		waveform=wf;
		Statics.setPreferenceValue(this,Statics.PREF_WAVEFORM,waveform);
		getPreferenceValues();
		updateSettingsListView();
	}
}
