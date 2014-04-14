package jp.nita.tapchord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
		String[] array=getResources().getStringArray(R.array.settings_items);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,array);
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

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.settings_ok:
			finish();
			break;
		}
	}

}
