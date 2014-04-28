package jp.nita.tapchord;

import android.graphics.PointF;

public class Shape {
	public int style;
	public int rad;
	public int lifetime;
	public PointF center;
	
	public final static int STYLE_LINE=0;
	public final static int STYLE_CIRCLE=1;
	public final static int STYLE_TRIANGLE=2;
	public final static int STYLE_SQUARE=3;
	
	public final static int MAX_LIFETIME=36;
	
	Shape(PointF pf){
		style=(int)(Math.random()*4);
		rad=(int)(Math.random()*360);
		lifetime=MAX_LIFETIME;
		center=pf;
	}
}
