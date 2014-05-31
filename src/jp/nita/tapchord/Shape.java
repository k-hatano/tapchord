package jp.nita.tapchord;

import android.graphics.PointF;

public class Shape {
	public int style;
	public int rad;
	public int radDelta;
	public int lifetime;
	public PointF center;
	
	public final static int STYLE_LINE=0;
	public final static int STYLE_CIRCLE=1;
	public final static int STYLE_TRIANGLE=2;
	public final static int STYLE_SQUARE=3;
	
	public final static int MAX_LIFETIME=(int)(400.0f/MainActivity.heartBeatInterval);
	
	Shape(PointF pf){
		style=(int)(Math.random()*4);
		rad=(int)(Math.random()*360)-180;
		radDelta=(int)(Math.random()*360)-180;
		if(style==0) radDelta=0;
		lifetime=MAX_LIFETIME;
		center=pf;
	}
}
