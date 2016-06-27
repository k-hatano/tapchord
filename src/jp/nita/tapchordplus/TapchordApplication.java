package jp.nita.tapchordplus;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "1Hw5GCMynB_Hs2lQMC6YdG9VjG_QYZxYyvn1N8UwIf-0", mailTo = "kent.ruffle.mgj626@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crashed)
public class TapchordApplication extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
}
