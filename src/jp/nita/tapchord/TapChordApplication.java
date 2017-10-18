package jp.nita.tapchord;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public class TapChordApplication extends Application {

	private AppStatus mAppStatus = AppStatus.FOREGROUND;

	public void onCreate() {
		super.onCreate();

		registerActivityLifecycleCallbacks(new TapChordActivityLifecycleCallbacks());
	}

	public static TapChordApplication get(Context context) {
		return (TapChordApplication)context.getApplicationContext();
	}

	public AppStatus getAppStatus() {
		return mAppStatus;
	}

	public boolean ifForeground() {
		return mAppStatus.ordinal() > AppStatus.BACKGROUND.ordinal();
	}

	public enum AppStatus {
		BACKGROUND, RETURNED_TO_FOREGROUND, FOREGROUND
	}

	public class TapChordActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

		private int running = 0;

		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

		}

		@Override
		public void onActivityDestroyed(Activity activity) {

		}

		@Override
		public void onActivityPaused(Activity activity) {

		}

		@Override
		public void onActivityResumed(Activity activity) {

		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

		}

		@Override
		public void onActivityStarted(Activity activity) {
			running++;
			if (running == 1) {
				mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
			} else if (running > 1) {
				mAppStatus = AppStatus.FOREGROUND;
			}
		}

		@Override
		public void onActivityStopped(Activity activity) {
			running--;
			if (running <= 0) {
				mAppStatus = AppStatus.BACKGROUND;
			}
		}

	}

}
