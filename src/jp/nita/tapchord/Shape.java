package jp.nita.tapchord;

import android.graphics.PointF;

public class Shape {
	public int style;
	public int radStart;
	public int radEnd;
	public int lifetime;
	public PointF center;
	
	public final static int STYLE_LINE=0;
	public final static int STYLE_CIRCLE=1;
	public final static int STYLE_TRIANGLE=2;
	public final static int STYLE_SQUARE=3;
	
	public static int MAX_LIFETIME=75;
	
	Shape(PointF pf){
		style=(int)(Math.random()*4);
		radStart=(int)(Math.random()*360)-180;
		radEnd=radStart+(int)(Math.random()*180)-90;
		lifetime=getMaxLifetime();
		center=pf;
	}
	
	public static int getMaxLifetime(){
		return MAX_LIFETIME/MainActivity.heartBeatInterval;
	}
}
