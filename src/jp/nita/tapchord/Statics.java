package jp.nita.tapchord;

import android.graphics.Color;
import android.graphics.RectF;

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
	
	public static String TENSIONS[]={"add9","-5/aug","7","M7"};
	public static String OPTIONS[]={"Sine","Stroke","#b0"};
	
	public static RectF getRectOfButton(int x,int y,int width,int height,int scroll){
		float vert=height*7/35f;
		float pX=width/2+x*vert;
		float pY=height/2+y*vert;
		return new RectF(pX-vert/2+vert/14+scroll*vert/7, pY-vert/2+vert/14, pX+vert/2-vert/14+scroll*vert/7, pY+vert/2-vert/14);
	}
	
	public static int getRadiusOfButton(int height){
		return height*7/70-8;
	}
	
	public static RectF getRectOfStatusBar(int width,int height){
		return new RectF(0,0,width,height*7/35);
	}
	
	public static RectF getRectOfStatusBarButton(int x,int y,int width,int height){
		float vert=height*7/35f;
		float pX=x*vert+vert/2;
		return new RectF(pX-vert/2+vert/14,0+vert/14,pX+vert/2-vert/14,vert-vert/14);
	}
	
	public static RectF getRectOfToolbar(int width,int height){
		return new RectF(0,height*28/35,width,height);
	}
	
	public static RectF getRectOfToolbarButton(int x,int y,int width,int height){
		float vert=height*7/35f;
		float pX=x*vert+vert/2;
		return new RectF(width-(pX+vert/2)+vert/14,height-vert+vert/14,width-(pX-vert/2)-vert/14,height-vert/14);
	}
	
	public static RectF getRectOfKeyboardIndicator(int i,int shrink,int width,int height){
		float vert=height/35f;
		RectF r=null;
		switch(i%12){
		case 0:
			r=new RectF(width-vert*21,vert*4,width-vert*19,vert*6);
			break;
		case 2:
			r=new RectF(width-vert*18,vert*4,width-vert*16,vert*6);
			break;
		case 4:
			r=new RectF(width-vert*15,vert*4,width-vert*13,vert*6);
			break;
		case 5:
			r=new RectF(width-vert*12,vert*4,width-vert*10,vert*6);
			break;
		case 7:
			r=new RectF(width-vert*9,vert*4,width-vert*7,vert*6);
			break;
		case 9:
			r=new RectF(width-vert*6,vert*4,width-vert*4,vert*6);
			break;
		case 11:
			r=new RectF(width-vert*3,vert*4,width-vert*1,vert*6);
			break;
			
		case 1:
			r=new RectF(width-vert*19.5f,vert*1,width-vert*17.5f,vert*3);
			break;
		case 3:
			r=new RectF(width-vert*16.5f,vert*1,width-vert*14.5f,vert*3);
			break;
		case 6:
			r=new RectF(width-vert*10.5f,vert*1,width-vert*8.5f,vert*3);
			break;
		case 8:
			r=new RectF(width-vert*7.5f,vert*1,width-vert*5.5f,vert*3);
			break;
		case 10:
			r=new RectF(width-vert*4.5f,vert*1,width-vert*2.5f,vert*3);
			break;
		}
		
		return r;
	}
	
	public static RectF getRectOfScrollBar(int width,int height){
		float vert=height/35f;
		return new RectF(vert*2,vert*30.5f,vert*2+vert*13,vert*32.5f);
	}
	
	public static RectF getRectOfScrollNob(int pos,int upper,int width,int height){
		float vert=height/35f;
		float max=(vert*7)*13;
		float nob=width/max;
		float x=vert*2+vert*13/2-pos;
		return new RectF(x-(nob*vert*13)/2,vert*30f-upper,x+(nob*vert*13)/2,vert*33f-upper);
	}
	
}
