package jp.nita.tapchord;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "1aWGUyDScQifsvlexBt2Oh0Q1bSi8UnsCFYQartfiEAE", mailTo = "kent.ruffle.mgj626@gmail.com", mode = ReportingInteractionMode.DIALOG, resDialogCommentPrompt = R.string.crashed)
public class TapchordApplication extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
}
