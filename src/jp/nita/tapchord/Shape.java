package jp.nita.tapchord;

import android.graphics.PointF;

public class Shape {
	public int style;
	public int rad;
	public int lifetime;
	public PointF center;
	
	public final static int STYLE_LINE=0;
	
	public final static int MAX_LIFETIME=32;
	
	Shape(PointF pf){
		style=0;
		rad=(int)(Math.random()*360);
		lifetime=MAX_LIFETIME;
		center=pf;
	}
}
