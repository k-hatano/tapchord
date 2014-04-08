package jp.nita.tapchord;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;


public class TapChordView extends View {
	int width,height,originalX,originalY,originalScroll;
	int situation,destination,step,scroll,upper;
	int playing,playingX,playingY;
	int playingID;

	int statusbarFlags[]={0,0,0,0};
	int toolbarPressed=-1;

	Integer notesOfChord[]=new Integer[0];
	Sound sound=null;

	SparseIntArray taps=new SparseIntArray();

	public TapChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		situation=Statics.SITUATION_NORMAL;
		destination=1;
		step=0;
		playing=0;
		scroll=0;
		upper=0;
	}

	public void init(Context context){

	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas){
		int x,y;
		float w;
		Paint paint=new Paint();
		Paint textPaint=new Paint();
		RectF rect;
		String str="";

		FontMetrics fontMetrics = textPaint.getFontMetrics();

		width=canvas.getWidth();
		height=canvas.getHeight();

		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		int rad=Statics.getRadiusOfButton(height);

		textPaint.setAntiAlias(true); 
		textPaint.setColor(Statics.getColor(-1,0));
		textPaint.setTextSize(rad/2);

		for(x=-6;x<=6;x++){
			int maj=x+12;
			int min=x+15;
			int xx=(x+72)%12;
			for(y=-1;y<=1;y++){
				int c=0;
				int d=0;
				if(playing>0&&playingX==x&&playingY==y) d=1;
				switch(xx){
				case 11: case 0: case 1:
					c=1;
					break;
				case 2: case 3: case 4:
					c=2;
					break;
				case 5: case 6: case 7:
					c=3;
					break;
				case 8: case 9: case 10:
					c=4;
					break;
				}
				if(situation==Statics.SITUATION_TRANSPORTING) c=0;
				paint.setColor(Statics.getColor(c,d));

				rect=Statics.getRectOfButton(x,y,width,height,scroll);
				canvas.drawOval(rect, paint);

				switch(y){
				case -1:
					str=Statics.NOTES[maj]+Statics.SUS4; break;
				case 0:
					str=Statics.NOTES[maj]; break;
				case 1:
					str=Statics.NOTES[min]+Statics.MINOR; break;
				}
				w=textPaint.measureText(str);
				canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
			}
		}

		paint.setColor(Statics.getColor(0,0));
		canvas.drawRect(Statics.getRectOfStatusBar(width, height),paint);

		paint.setColor(Statics.getColor(0,0));
		canvas.drawRect(Statics.getRectOfToolbar(width, height),paint);

		for(x=0;x<4;x++){
			int d=0;
			if(statusbarFlags[x]>0) d=1;
			paint.setColor(Statics.getColor(5,d));
			rect=Statics.getRectOfStatusBarButton(x,0,width,height);
			canvas.drawOval(rect, paint);

			str=Statics.TENSIONS[x];
			if(x==2&&statusbarFlags[3]>0) str="6";
			if(x==3&&statusbarFlags[2]>0) str="6";
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		for(x=0;x<3;x++){
			int d=0;
			if(toolbarPressed==x) d=1;
			paint.setColor(Statics.getColor(5,d));
			rect=Statics.getRectOfToolbarButton(x,0,width,height);
			canvas.drawOval(rect, paint);

			str=Statics.OPTIONS[x];
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		for(x=0;x<12;x++){
			paint.setColor(Color.LTGRAY);
			rect=Statics.getRectOfKeyboardIndicator(x, 0, width, height);
			canvas.drawOval(rect,paint);
		}

		for(int i=0;i<notesOfChord.length;i++){
			paint.setColor(Color.WHITE);
			rect=Statics.getRectOfKeyboardIndicator(notesOfChord[i], 2, width, height);
			canvas.drawOval(rect,paint);
		}

		paint.setColor(Color.GRAY);
		rect=Statics.getRectOfScrollBar(width, height);
		canvas.drawRect(rect,paint);

		paint.setColor(Color.DKGRAY);
		rect=Statics.getRectOfScrollNob(scroll, upper, width, height);
		canvas.drawRect(rect,paint);
		
		if(situation==Statics.SITUATION_TRANSPORTING){
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(4.0f);
			for(x=-6;x<=6;x++){
				int xx=(x+72)%12;
				for(y=-1;y<=1;y++){
					int c=0;
					switch(xx){
					case 11: case 0: case 1:
						c=1;
						break;
					case 2: case 3: case 4:
						c=2;
						break;
					case 5: case 6: case 7:
						c=3;
						break;
					case 8: case 9: case 10:
						c=4;
						break;
					}
					paint.setColor(Statics.getColor(c,0));

					rect=Statics.getRectOfButton(x,y,width,height,0);
					canvas.drawOval(rect, paint);
				}
			}
		}
	}
	
	public boolean actionDown(int x,int y,int id){
		int i,j;
		RectF rect;
		
		for(i=0;i<4;i++){
			rect=Statics.getRectOfStatusBarButton(i,0,width,height);
			if(rect.contains(x, y)){
				statusbarFlags[i]=1;
				taps.put(id,Statics.STATUSBAR_BUTTON);
			}
		}
		for(i=0;i<3;i++){
			rect=Statics.getRectOfToolbarButton(i,0,width,height);
			if(rect.contains(x, y)){
				toolbarPressed=i;
				taps.put(id,Statics.TOOLBAR_BUTTON);
			}
		}
		if(Statics.getRectOfScrollNob(scroll,upper,width,height).contains(x,y)){
			originalX=x;
			originalY=y;
			originalScroll=scroll;
			taps.put(id,Statics.SCROLL_NOB);
		}else if(Statics.getRectOfScrollBar(width,height).contains(x,y)){
			scroll=0;
			taps.put(id,Statics.SCROLL_BAR);
		}
		if(playing<=0){
			for(j=-6;j<=6;j++){
				for(i=-1;i<=1;i++){
					rect=Statics.getRectOfButton(j,i,width,height,scroll);
					if(rect.contains(x, y)){
						play(j,i);
						playingID=id;
						taps.put(playingID,Statics.CHORD_BUTTON);
					}
				}
			}
		}
		return true;
	}
	
	public boolean actionMove(int x,int y,int id){
		int i,j;
		boolean chordPressed=false;
		RectF rect;
		int kind;
		if(id>=0) kind=taps.get(id);
		else kind=0;
		switch(kind){
		case Statics.SCROLL_NOB:
			if(-y+originalY>height/5){
				scroll=0;
				upper=5;
			}else{
				scroll=(int)(-x+originalX)+originalScroll;
				if(scroll<-Statics.getScrollMax(width,height)) scroll=-Statics.getScrollMax(width,height);
				if(scroll>Statics.getScrollMax(width,height)) scroll=Statics.getScrollMax(width,height);
				upper=0;
			}
			break;
		case Statics.STATUSBAR_BUTTON:
			for(i=0;i<4;i++){
				rect=Statics.getRectOfStatusBarButton(i,0,width,height);
				if(rect.contains(x, y)) statusbarFlags[i]=1;
			}
			break;
		case Statics.TOOLBAR_BUTTON:
			toolbarPressed=-1;
			for(i=0;i<3;i++){
				rect=Statics.getRectOfToolbarButton(i,0,width,height);
				if(rect.contains(x, y)){
					toolbarPressed=i;
					break;
				}
			}
			break;
		case Statics.CHORD_BUTTON:
			if(playing<=0){
				for(j=-6;j<=6;j++){
					for(i=-1;i<=1;i++){
						rect=Statics.getRectOfButton(j,i,width,height,scroll);
						if(rect.contains(x, y)){
							play(j,i);
							chordPressed=true;
							playingID=id;
							taps.put(playingID,Statics.CHORD_BUTTON);
							break;
						}
					}
				}
			}else{
				chordPressed=true;
			}
			break;
		case Statics.SCROLL_BAR:
			break;
		default:
			actionDown(x,y,id);
			break;
		}
		return chordPressed;
	}

	public boolean onTouchEvent(MotionEvent event){
		int x,y,id;
		boolean chordPressed=false;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			Log.i("TapChordView","DOWN Count:"+event.getPointerCount());
			x=(int)event.getX(event.getActionIndex());
			y=(int)event.getY(event.getActionIndex());
			id=event.getPointerId(event.getActionIndex());
			actionDown(x,y,id);
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("TapChordView","MOVE Count:"+event.getPointerCount());
			for(int index=0;index<event.getPointerCount();index++){
				x=(int)event.getX(index);
				y=(int)event.getY(index);
				id=event.getPointerId(index);
				chordPressed|=actionMove(x,y,id);
			}
			if(chordPressed==false){
				playingID=-1;
				stop();
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.i("TapChordView","UP Count:"+event.getPointerCount());
			stop();
			for(int l=0;l<4;l++) statusbarFlags[l]=0;
			if(toolbarPressed>=0) toolbarReleased(toolbarPressed);
			toolbarPressed=-1;
			playingID=-1;
			upper=0;
			taps=new SparseIntArray();
			break;
		default:
			break;
		}
		invalidate();
		return true;
	}
	
	public void toolbarReleased(int which){
		switch(which){
		case 0:
			Intent intent=new Intent((Activity)this.getContext(),SettingsActivity.class);
			this.getContext().startActivity(intent);
	        break;
		case 2:
			situation=1-situation;
			break;
	    default:
	        break;
		}
		invalidate();
	}

	public void play(int x,int y){
		Integer f[]=(Statics.convertNotesToFrequencies(Statics.getNotesOfChord(x,y,statusbarFlags)));
		sound=new Sound(f,0.1f);
		notesOfChord=Statics.getNotesOfChord(x,y,statusbarFlags);
		sound.play();
		switch(y){
		case -1:
			playing=1;
			playingX=x;
			playingY=y;
			break;
		case 0:
			playing=1;
			playingX=x;
			playingY=y;
			break;
		case 1:
			playing=1;
			playingX=x;
			playingY=y;
			break;
		default:
			break;
		}
		invalidate();
	}

	public void stop(){
		if(sound!=null){
			sound.stop();
			sound=null;
		}
		playing=0;
		notesOfChord=new Integer[0];
		invalidate();
	}
	
	public void activityPaused(){
		stop();
	}

}
