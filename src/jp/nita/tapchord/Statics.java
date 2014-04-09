package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.RectF;

public class Statics {

	final public static int NIHIL=0;
	final public static int FARAWAY=256;

	final public static int SITUATION_NORMAL=0;
	final public static int SITUATION_TRANSPOSE=1;

	final public static int CHORD_BUTTON=1;
	final public static int STATUSBAR_BUTTON=2;
	final public static int SCROLL_NOB=3;
	final public static int TOOLBAR_BUTTON=4;
	final public static int SCROLL_BAR=5;
	final public static int TRANSPOSE_SCALE_BUTTON=6;
	
	final public static int COLOR_BLACK=-5;
	final public static int COLOR_DARKGRAY=-4;
	final public static int COLOR_GRAY=-3;
	final public static int COLOR_LIGHTGRAY=-2;
	final public static int COLOR_WHITE=-1;
	final public static int COLOR_ABSOLUTE_LIGHT=0;
	final public static int COLOR_RED=1;
	final public static int COLOR_YELLOW=2;
	final public static int COLOR_GREEN=3;
	final public static int COLOR_BLUE=4;
	final public static int COLOR_ORANGE=5;

	final public static String SUS4="sus4";
	final public static String MINOR="m";

	public static int getColor(int which,int pressed,int dark){
		int r,g,b;
		if(dark==0){
			switch(which){
			case COLOR_BLACK:
				r=0;
				g=0;
				b=0;
				break;
			case COLOR_DARKGRAY:
				r=64;
				g=64;
				b=64;
				break;
			case COLOR_GRAY:
				r=128;
				g=128;
				b=128;
				break;
			case COLOR_LIGHTGRAY:
				r=240;
				g=240;
				b=240;
				break;
			case COLOR_ABSOLUTE_LIGHT:
				r=255;
				g=255;
				b=255;
				break;
			case COLOR_RED:
				r=255;
				g=160;
				b=224;
				break;
			case COLOR_YELLOW:
				r=255;
				g=255;
				b=128;
				break;
			case COLOR_GREEN:
				r=160;
				g=255;
				b=160;
				break;
			case COLOR_BLUE:
				r=160;
				g=224;
				b=255;
				break;
			case COLOR_ORANGE:
				r=255;
				g=192;
				b=128;
				break;
			default:
				r=255;
				g=255;
				b=255;
				break;
			}
			switch(pressed){
			case 1:
				r/=2;
				g/=2;
				b/=2;
				break;
			case -1:
				r=256-(256-r)/2;
				g=256-(256-g)/2;
				b=256-(256-b)/2;
				break;
			default:
				break;
			}
		}else{
			switch(which){
			case COLOR_BLACK:
				r=0;
				g=64;
				b=64;
				break;
			case COLOR_DARKGRAY:
				r=0;
				g=48;
				b=48;
				break;
			case COLOR_GRAY:
				r=0;
				g=32;
				b=32;
				break;
			case COLOR_LIGHTGRAY:
				r=0;
				g=16;
				b=16;
				break;
			case COLOR_ABSOLUTE_LIGHT:
				r=80;
				g=80;
				b=80;
				break;
			case COLOR_RED:
			case COLOR_YELLOW:
			case COLOR_GREEN:
			case COLOR_BLUE:
			case COLOR_ORANGE:
				r=0;
				g=32;
				b=32;
				break;
			default: // WHITE
				r=0;
				g=0;
				b=0;
				break;
			}
			switch(pressed){
			case -1:
				r/=2;
				g/=2;
				b/=2;
				break;
			case 1:
				r=256-(256-r)/2;
				g=256-(256-g)/2;
				b=256-(256-b)/2;
				break;
			default:
				break;
			}
		}
		if(r<0) r=0;
		if(r>255) r=255;
		if(g<0) g=0;
		if(g>255) g=255;
		if(b<0) b=0;
		if(b>255) b=255;
		return Color.argb(255,r,g,b);
	}

	public static String NOTES[]={"Fbb","Cbb","Gbb","Dbb","Abb","Ebb","Bbb","Fb","Cb","Gb","Db","Ab","Eb","Bb","F",
		"C","G","D","A","E","B","F#","C#","G#","D#","A#","E#",
		"B#","F#","Cx","Gx","Dx","Ax","Ex","Gx","Fx"};

	public static String SCALES[]={"b7","b6","b5","b4","b3","b2","b1","#b0","#1","#2","#3","#4","#5","#6","#7"};

	public static String TENSIONS[]={"add9","-5/aug","7","M7"};
	public static String OPTIONS[]={"Settings","Light","0#b"};

	public static RectF getRectOfButton(int x,int y,int width,int height,int scroll){
		float vert=height*7/35f;
		float pX=width/2+x*vert;
		float pY=height/2+y*vert;
		return new RectF(pX-vert/2+vert/14+scroll*4, pY-vert/2+vert/14, pX+vert/2-vert/14+scroll*4, pY+vert/2-vert/14);
	}

	public static int getScrollMax(int width,int height){
		float vert=height/35f;
		float max=(vert*7)*13;
		float nob=width/4;
		return (int)(max/4-nob)/2;
	}

	public static RectF getRectOfScrollBar(int width,int height){
		float vert=height/35f;
		float max=(vert*7)*13;
		return new RectF(vert*2,vert*30.5f,vert*2+max/4,vert*32.5f);
	}

	public static RectF getRectOfScrollNob(int pos,int upper,int width,int height){
		float vert=height/35f;
		float max=(vert*7)*13;
		float nob=width/4;
		float x=vert*2+max/8-pos;
		return new RectF(x-nob/2,vert*30f-upper,x+nob/2,vert*33f-upper);
	}

	public static int getRadiusOfButton(int height){
		return height*7/70-8;
	}

	public static RectF getRectOfStatusBar(int width,int height,float showingRate){
		float vert=height*7/35f;
		float hidingDelta=vert*(1.0f-showingRate);
		return new RectF(0,0-hidingDelta,width,height*7/35-hidingDelta);
	}

	public static RectF getRectOfStatusBarButton(int x,int y,int width,int height,float showingRate){
		float vert=height*7/35f;
		float pX=x*vert+vert/2;
		float hidingDelta=vert*(1.0f-showingRate);
		return new RectF(pX-vert/2+vert/14,0+vert/14-hidingDelta,pX+vert/2-vert/14,vert-vert/14-hidingDelta);
	}

	public static RectF getRectOfToolbar(int width,int height,float showingRate){
		return new RectF(0,height*28/35,width,height);
	}

	public static RectF getRectOfToolbarButton(int x,int y,int width,int height,float showingRate){
		float vert=height*7/35f;
		float pX=x*vert+vert/2;
		return new RectF(width-(pX+vert/2)+vert/14,height-vert+vert/14,
				width-(pX-vert/2)-vert/14,height-vert/14);
	}

	public static RectF getRectOfKeyboardIndicator(int i,int shrink,int width,int height,float showingRate){
		float vert=height/35f;
		float hidingDelta=vert*7*(1.0f-showingRate);
		RectF r=null;
		switch(i%12){
		case 0:
			r=new RectF(width-vert*21+shrink,vert*4+shrink-hidingDelta,
					width-vert*19-shrink,vert*6-shrink-hidingDelta);
			break;
		case 2:
			r=new RectF(width-vert*18+shrink,vert*4+shrink-hidingDelta,
					width-vert*16-shrink,vert*6-shrink-hidingDelta);
			break;
		case 4:
			r=new RectF(width-vert*15+shrink,vert*4+shrink-hidingDelta,
					width-vert*13-shrink,vert*6-shrink-hidingDelta);
			break;
		case 5:
			r=new RectF(width-vert*12+shrink,vert*4+shrink-hidingDelta,
					width-vert*10-shrink,vert*6-shrink-hidingDelta);
			break;
		case 7:
			r=new RectF(width-vert*9+shrink,vert*4+shrink-hidingDelta,
					width-vert*7-shrink,vert*6-shrink-hidingDelta);
			break;
		case 9:
			r=new RectF(width-vert*6+shrink,vert*4+shrink-hidingDelta,
					width-vert*4-shrink,vert*6-shrink-hidingDelta);
			break;
		case 11:
			r=new RectF(width-vert*3+shrink,vert*4+shrink-hidingDelta,
					width-vert*1-shrink,vert*6-shrink-hidingDelta);
			break;

		case 1:
			r=new RectF(width-vert*19.5f+shrink,vert*1+shrink-hidingDelta,
					width-vert*17.5f-shrink,vert*3-shrink-hidingDelta);
			break;
		case 3:
			r=new RectF(width-vert*16.5f+shrink,vert*1+shrink-hidingDelta,
					width-vert*14.5f-shrink,vert*3-shrink-hidingDelta);
			break;
		case 6:
			r=new RectF(width-vert*10.5f+shrink,vert*1+shrink-hidingDelta,
					width-vert*8.5f-shrink,vert*3-shrink-hidingDelta);
			break;
		case 8:
			r=new RectF(width-vert*7.5f+shrink,vert*1+shrink-hidingDelta,
					width-vert*5.5f-shrink,vert*3-shrink-hidingDelta);
			break;
		case 10:
			r=new RectF(width-vert*4.5f+shrink,vert*1+shrink-hidingDelta,
					width-vert*2.5f-shrink,vert*3-shrink-hidingDelta);
			break;
		}

		return r;
	}

	public static int getFrequencyOfNote(int note){
		double f=440.0;
		int n=note-9;
		return (int)(f*Math.pow(2,n/12.0));
	}

	public static Integer[] getNotesOfChord(int x,int y,int[] tensions){
		List<Integer> notes=new ArrayList<Integer>();

		if(y>=1) x+=3;

		if(y==-1){
			if(tensions[1]>0){
				notes.add((x*7+360)%12);
				notes.add((x*7+4+360)%12);
				notes.add((x*7+8+360)%12);
			}else{
				notes.add((x*7+360)%12);
				notes.add((x*7+5+360)%12);
				notes.add((x*7+7+360)%12);
			}
		}else if(y==0){
			notes.add((x*7+360)%12);
			notes.add((x*7+4+360)%12);
			if(tensions[1]>0){
				notes.add((x*7+6+360)%12);
			}else{
				notes.add((x*7+7+360)%12);
			}
		}else if(y==1){
			notes.add((x*7+360)%12);
			notes.add((x*7+3+360)%12);
			if(tensions[1]>0){
				notes.add((x*7+6+360)%12);
			}else{
				notes.add((x*7+7+360)%12);
			}
		}

		if(tensions[0]>0){
			notes.add((x*7+2+360)%12);
		}

		if(tensions[2]>0&&tensions[3]>0){
			notes.add((x*7+9+360)%12);
		}else if(tensions[2]>0){
			notes.add((x*7+10+360)%12);
		}else if(tensions[3]>0){
			notes.add((x*7+11+360)%12);
		}

		Integer ns[]=notes.toArray(new Integer[0]);
		return ns;
	}

	public static Integer[] convertNotesToFrequencies(Integer[] notes){
		List<Integer> freqs=new ArrayList<Integer>();

		for(int i=0;i<notes.length;i++){
			freqs.add(getFrequencyOfNote(notes[i]));
		}

		Integer fs[]=freqs.toArray(new Integer[0]);
		return fs;
	}

	public static String getStringOfScale(int i){
		if(i<-7||i>7) return "";
		return SCALES[i+7];
	}
}
