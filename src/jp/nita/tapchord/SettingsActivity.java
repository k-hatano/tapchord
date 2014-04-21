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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SettingsActivity extends Activity implements OnClickListener {

	int darken=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		// Intent i=getIntent();
		// darken=i.getIntExtra("darken",0);
		// setTheme(darken>0?android.R.style.Theme_Black:android.R.style.Theme_Light);

		ListView items=(ListView)findViewById(R.id.settings_items);

		List<Map<String,String>> list=new ArrayList<Map<String,String>>();
		{
			Map<String,String> map;
			
			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_scale));
			map.put("value", Statics.SCALES[7]);
			list.add(map);
			
			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_darken));
			map.put("value", getString(R.string.off));
			list.add(map);
			
			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_volume));
			map.put("value", "100");
			list.add(map);
			
			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_sampling_rate));
			map.put("value", getString(R.string.settings_sampling_rate_4000));
			list.add(map);
			
			map=new HashMap<String,String>();
			map.put("key", getString(R.string.settings_waveform));
			map.put("value", getString(R.string.settings_waveform_sine_wave));
			list.add(map);
		}

		SimpleAdapter adapter
		=new SimpleAdapter(this,list
				,android.R.layout.simple_expandable_list_item_2,
new String[]{"key","value"},
				new int[]{android.R.id.text1,android.R.id.text2});
		items.setAdapter(adapter);

		Button button;
		button=(Button)findViewById(R.id.settings_ok);
		button.setOnClickListener(this);
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

}
