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
import android.view.MotionEvent;
import android.view.View;

public class TapChordView extends View {
	int width,height;
	int situation,destination,step;
	int playing,playingX,playingY;
	int playingID;
	
	int toolbarFlags[]={0,0,0,0};

	public TapChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		situation=1;
		destination=1;
		step=0;
		playing=0;
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

				rect=Statics.getRectOfButton(x,y,width,height);
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
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}
		
		for(x=0;x<3;x++){
			int d=0;
			if(toolbarFlags[x]>0) d=1;
			paint.setColor(Statics.getColor(5,d));
			rect=Statics.getRectOfToolbarButton(x,0,width,height);
			canvas.drawOval(rect, paint);
			
			str=Statics.OPTIONS[x];
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}
		
		for(x=0;x<12;x++){
			paint.setColor(Color.WHITE);
			rect=Statics.getRectOfKeyboardIndicator(x, 0, width, height);
			canvas.drawOval(rect,paint);
		}
		
		paint.setColor(Color.GRAY);
		rect=Statics.getRectOfScrollBar(width, height);
		canvas.drawRect(rect,paint);
		
		paint.setColor(Color.DKGRAY);
		rect=Statics.getRectOfScrollNob(0, width, height);
		canvas.drawRect(rect,paint);
	}

	public boolean onTouchEvent(MotionEvent event){
		int i,j,k;
		int x,y;
		RectF rect;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			for(k=0;k<event.getPointerCount();k++){
				x=(int)event.getX(k);
				y=(int)event.getY(k);
				for(i=0;i<4;i++){
					rect=Statics.getRectOfStatusBarButton(i,0,width,height);
					if(rect.contains(x, y)) toolbarFlags[i]=1;
				}
			}
			if(playing<=0){
				for(k=0;k<event.getPointerCount();k++){
					x=(int)event.getX(k);
					y=(int)event.getY(k);
					for(j=-6;j<=6;j++){
						for(i=-1;i<=1;i++){
							rect=Statics.getRectOfButton(j,i,width,height);
							if(rect.contains(x, y)){
								play(j,i);
								playingID=event.getPointerId(k);
							}
						}
					}
				}
			}
			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			for(k=0;k<event.getPointerCount();k++){
				x=(int)event.getX(k);
				y=(int)event.getY(k);
				for(i=0;i<4;i++){
					rect=Statics.getRectOfStatusBarButton(i,0,width,height);
					if(rect.contains(x, y)) toolbarFlags[i]=0;
				}
			}
			for(k=0;k<event.getPointerCount();k++){
				x=(int)event.getHistoricalX(k);
				y=(int)event.getHistoricalX(k);
				for(j=-6;j<=6;j++){
					for(i=-1;i<=1;i++){
						rect=Statics.getRectOfButton(j,i,width,height);
						if(rect.contains(x, y)){
							stop();
							playingID=0;
						}
					}
				}
				x=(int)event.getX(k);
				y=(int)event.getY(k);
				for(j=-6;j<=6;j++){
					for(i=-1;i<=1;i++){
						rect=Statics.getRectOfButton(j,i,width,height);
						if(rect.contains(x, y)){
							stop();
							playingID=0;
						}
					}
				}
				if(event.getPointerId(k)==playingID){
					stop();
					playingID=0;
				}
			}
			invalidate();
			break;
		}
		return true;
	}
	
	public void play(int x,int y){
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
		
	}
	
	public void stop(){
		playing=0;
	}

}
