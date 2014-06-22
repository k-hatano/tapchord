package jp.nita.tapchord;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	
	public static int heartBeatInterval=5;
	
	private Heart heart=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		heart=new Heart();
		heart.start();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		((TapChordView)findViewById(R.id.tapChordView)).activityPaused();
		heart.sleep();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		((TapChordView)findViewById(R.id.tapChordView)).activityResumed();
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
	        Intent intent=new Intent(this,SettingsActivity.class);
	        TapChordView view=(TapChordView)findViewById(R.id.tapChordView);
	        intent.putExtra("darken",view.getDarken());
	        startActivity(intent);
	        return true;
	    case R.id.action_quit:
	    	new AlertDialog.Builder(this)
			.setTitle(getString(R.string.action_quit))
			.setMessage(getString(R.string.message_quit))
			.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).show();
	    }
	    return false;
	}
	
	class Heart extends Thread implements Runnable{
		private boolean awake=true;
		private boolean alive=true;
		public void run(){
			TapChordView view=((TapChordView)findViewById(R.id.tapChordView));
			while(alive){
				try{
					Thread.sleep(heartBeatInterval);
					if(awake) view.heartbeat(heartBeatInterval);
				}catch(InterruptedException e){
					die();
				}
			}
		}
		public void wake(){
			awake=true;
		}
		public void sleep(){
			awake=false;
		}
		public void die(){
			alive=false;
		}
	}

}
