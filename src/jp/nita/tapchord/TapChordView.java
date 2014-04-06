package jp.nita.tapchord;

import android.annotation.SuppressLint;
import android.content.Context;
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

	int toolbarFlags[]={0,0,0,0};

	private final int SITUATION_NORMAL=0;
	private final int SITUATION_SCROLLING=1;

	Integer notesOfChord[]=new Integer[0];
	Sound sound=null;

	SparseIntArray taps=new SparseIntArray();

	public TapChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		situation=SITUATION_NORMAL;
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
			int xx=(x+12)%12;
			for(y=-1;y<=1;y++){
				int d=0;
				if(playing>0&&playingX==x&&playingY==y) d=1;
				switch(xx){
				case 11: case 0: case 1:
					paint.setColor(Statics.getColor(1,d));
					break;
				case 2: case 3: case 4:
					paint.setColor(Statics.getColor(2,d));
					break;
				case 5: case 6: case 7:
					paint.setColor(Statics.getColor(3,d));
					break;
				case 8: case 9: case 10:
					paint.setColor(Statics.getColor(4,d));
					break;
				}

				rect=Statics.getRectOfButton(x,y,width,height,scroll);
				canvas.drawOval(rect, paint);

				switch(y){
				case -1:
					str=Statics.SUS4S[xx]; break;
				case 0:
					str=Statics.MAJORS[xx]; break;
				case 1:
					str=Statics.MINORS[xx]; break;
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
			if(toolbarFlags[x]>0) d=1;
			paint.setColor(Statics.getColor(5,d));
			rect=Statics.getRectOfStatusBarButton(x,0,width,height);
			canvas.drawOval(rect, paint);

			str=Statics.TENSIONS[x];
			if(x==2&&toolbarFlags[3]>0) str="6";
			if(x==3&&toolbarFlags[2]>0) str="6";
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		for(x=0;x<3;x++){
			paint.setColor(Statics.getColor(5,0));
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
	}

	public boolean onTouchEvent(MotionEvent event){
		int i,j;
		int id,kind;
		int x,y;
		RectF rect;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			Log.i("TapChordView","DOWN id:"+event.getPointerId(event.getActionIndex()));
			x=(int)event.getX(event.getActionIndex());
			y=(int)event.getY(event.getActionIndex());
			for(i=0;i<4;i++){
				rect=Statics.getRectOfStatusBarButton(i,0,width,height);
				if(rect.contains(x, y)){
					toolbarFlags[i]=1;
					taps.put(event.getPointerId(event.getActionIndex()),Statics.STATUSBAR_BUTTON);
				}
			}
			if(Statics.getRectOfScrollNob(scroll,upper,width,height).contains(x,y)){
				situation=SITUATION_SCROLLING;
				originalX=x;
				originalY=y;
				originalScroll=scroll;
				taps.put(event.getPointerId(event.getActionIndex()),Statics.SCROLL_NOB);
			}
			if(playing<=0){
				for(j=-6;j<=6;j++){
					for(i=-1;i<=1;i++){
						rect=Statics.getRectOfButton(j,i,width,height,scroll);
						if(rect.contains(x, y)){
							play(j,i);
							playingID=event.getPointerId(event.getActionIndex());
							taps.put(event.getPointerId(event.getActionIndex()),Statics.CHORD_BUTTON);
						}
					}
				}
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("TapChordView","MOVE id:"+event.getPointerId(event.getActionIndex()));
			id=event.getPointerId(event.getActionIndex());
			if(id>=0) kind=taps.get(id);
			else kind=0;
			x=(int)event.getX(event.getActionIndex());
			y=(int)event.getY(event.getActionIndex());
			switch(kind){
			case Statics.SCROLL_NOB:
				if(event.getHistorySize()>0||event.getPointerCount()>0){
					if(-event.getY(event.getActionIndex())+originalY>height/5){
						scroll=0;
						upper=5;
					}else{
						scroll=(int)(-event.getX(event.getActionIndex())+originalX)+originalScroll;
						upper=0;
					}
				}
				break;
			default:
				for(i=0;i<4;i++){
					rect=Statics.getRectOfStatusBarButton(i,0,width,height);
					if(rect.contains(x, y)) toolbarFlags[i]=1;
				}
				if(playing<=0){
					for(j=-6;j<=6;j++){
						for(i=-1;i<=1;i++){
							rect=Statics.getRectOfButton(j,i,width,height,scroll);
							if(rect.contains(x, y)){
								play(j,i);
								playingID=event.getPointerId(event.getActionIndex());
								taps.put(event.getPointerId(event.getActionIndex()),Statics.CHORD_BUTTON);
								break;
							}
						}
					}
				}
				break;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			Log.i("TapChordView","UP   id:"+event.getPointerId(event.getActionIndex()));
			id=event.getPointerId(event.getActionIndex());
			kind=taps.get(id);
			switch(kind){
			case Statics.STATUSBAR_BUTTON:
			case Statics.CHORD_BUTTON:
				for(int l=0;l<4;l++) toolbarFlags[l]=0;
				stop();
				playingID=-1;
				break;
			case Statics.SCROLL_NOB:
				situation=SITUATION_NORMAL;
				upper=0;
				break;
			default:
				break;
			}
			taps.delete(id);
			break;
		}
		Log.i("TapChordView","Count:"+event.getPointerCount());
		invalidate();
		return true;
	}

	public void play(int x,int y){
		Integer f[]=(Statics.convertNotesToFrequencies(Statics.getNotesOfChord(x,y,toolbarFlags)));
		sound=new Sound(f,0.1f);
		notesOfChord=Statics.getNotesOfChord(x,y,toolbarFlags);
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

}
