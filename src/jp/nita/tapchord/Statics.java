package jp.nita.tapchord;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

public class Statics {
	
	public static int getColor(int which,int darkness){
		int r,g,b;
		switch(which){
		case -1: // DARKGRAY
			r=16;
			g=16;
			b=16;
			break;
		case 0: // LIGHTGRAY
			r=240;
			g=240;
			b=240;
			break;
		case 1: // RED
			r=255;
			g=160;
			b=224;
			break;
		case 2: // YELLOW
			r=255;
			g=255;
			b=128;
			break;
		case 3: // GREEN
			r=160;
			g=255;
			b=160;
			break;
		case 4: // BLUE
			r=160;
			g=224;
			b=255;
			break;
		case 5: // ORANGE
			r=255;
			g=192;
			b=128;
			break;
		default: // WHITE
			r=255;
			g=255;
			b=255;
			break;
		}
		switch(darkness){
		case 1:
			r/=2;
			g/=2;
			b/=2;
			break;
		case -1:
			r=256-(256-r/2);
			g=256-(256-r/2);
			b=256-(256-r/2);
			break;
		default:
			break;
		}
		if(r<0) r=0;
		if(r>255) r=255;
		if(g<0) g=0;
		if(g>255) g=255;
		if(b<0) b=0;
		if(b>255) b=255;
		return Color.argb(255,r,g,b);
	}
	
	public static String SUS4S[]={"Csus4","Gsus4","Dsus4","Asus4","Esus4","Bsus4","F#sus4","C#sus4","G#sus4","D#sus4","A#sus4","Fsus4"};
	public static String MAJORS[]={"C","G","D","A","E","B","F#","C#","G#","D#","A#","F"};
	public static String MINORS[]={"Am","Em","Bm","F#m","C#m","G#m","D#m","A#m","Fm","Cm","Gm","Dm"};
	
	public static String TENSIONS[]={"7","M7","add9","-5/aug"};
	
	public static Rect getRectOfButton(int x,int y,int width,int height){
		int vert=height*7/35;
		int pX=width/2+x*vert;
		int pY=height/2+y*vert;
		return new Rect(pX-vert/2, pY-vert/2, pX+vert/2, pY+vert/2);
	}
	
	public static Point getPointOfButton(int x,int y,int width,int height){
		int vert=height*7/35;
		int pX=width/2+x*vert;
		int pY=height/2+y*vert;
		return new Point(pX, pY);
	}
	
	public static int getRadiusOfButton(int height){
		return height*7/70-8;
	}
	
	public static Rect getRectOfStatusBar(int width,int height){
		return new Rect(0,0,width,height*7/35);
	}
	
	public static Point getPointOfStatusBarButton(int x,int y,int width,int height){
		int vert=height*7/35;
		int pX=x*vert+vert/2;
		return new Point(pX,vert/2);
	}
	
	public static Rect getRectOfStatusBarButton(int x,int y,int width,int height){
		int vert=height*7/35;
		int pX=x*vert+vert/2;
		return new Rect(pX-vert/2,0,pX+vert/2,vert);
	}
	
	public static Rect getRectOfToolbar(int width,int height){
		return new Rect(0,height*28/35,width,height);
	}
}
