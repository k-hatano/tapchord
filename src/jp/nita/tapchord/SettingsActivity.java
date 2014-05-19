package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SettingsActivity extends Activity implements OnClickListener,OnItemClickListener {

	int darken=0;
	int scale;
	int volume;
	int samplingRate;
	int waveform;
	int vibration;
	
	int selection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		Intent i=getIntent();
		darken=i.getIntExtra("darken",0);
		setTheme(darken>0?android.R.style.Theme_Black:android.R.style.Theme_Light);

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
			map.put("key", getString(R.string.settings_waveform));
			map.put("value", Statics.getValueOfWaveform(waveform,this));
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
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setScale(selected-7);
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
		case 1:{
			CharSequence list[]=new String[2];
			list[0]=getString(R.string.off);
			list[1]=getString(R.string.on);
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_darken))
			.setSingleChoiceItems(list,darken,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setDarken(selected);
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
		case 2:{
			CharSequence list[]=new String[2];
			list[0]=getString(R.string.off);
			list[1]=getString(R.string.on);
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_vibration))
			.setSingleChoiceItems(list,vibration,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setVibration(selected);
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
		case 3:{
			CharSequence list[]=new String[5];
			for(int i=-1;i<4;i++){
				list[i+1]=""+Statics.getValueOfVolume(i);
			}
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_volume))
			.setSingleChoiceItems(list,volume+1,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setVolume(selected-1);
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
		case 4:{
			CharSequence list[]=new String[6];
			for(int i=0;i<6;i++){
				list[i]=Statics.getValueOfWaveform(i,this);
			}
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_waveform))
			.setSingleChoiceItems(list,waveform,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setWaveform(selected);
					((ListView)findViewById(R.id.settings_items)).setSelection(4);
				}
			})
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ListView)findViewById(R.id.settings_items)).setSelection(4);
				}
			})
			.show();
			break;
		}
		case 5:{
			CharSequence list[]=new String[4];
			for(int i=0;i<4;i++){
				list[i]=""+Statics.getValueOfSamplingRate(i)+" "+getString(R.string.settings_sampling_rate_hz);
			}
			new AlertDialog.Builder(SettingsActivity.this)
			.setTitle(getString(R.string.settings_sampling_rate))
			.setSingleChoiceItems(list,samplingRate,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					selected=arg1;
				}
			})
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(selected>=0) setSamplingRate(selected);
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

	public void setWaveform(int wf){
		waveform=wf;
		Statics.setPreferenceValue(this,Statics.PREF_WAVEFORM,waveform);
		getPreferenceValues();
		updateSettingsListView();
	}
}
