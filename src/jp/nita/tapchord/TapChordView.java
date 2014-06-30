package jp.nita.tapchord;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

public class TapChordView extends View {
	int width,height,originalX,originalY,originalScroll;
	int situation,destination,step,scroll,upper,darken,destScale;
	int playing,playingX,playingY,tappedX,destinationScroll;
	int playingID;

	int scale=0;
	int vibration=0;
	int soundRange=0;
	int attackTime=0;
	int decayTime=0;
	int releaseTime=0;
	int pulling=0;

	int statusbarFlags[]={0,0,0,0};
	int lastTapped=-1;
	long lastTappedTime=-1;
	int toolbarPressed=-1;
	int scalePressed=Statics.FARAWAY;
	int heartBeatInterval=MainActivity.heartBeatInterval;
	float stepMax=100.0f/heartBeatInterval;

	float volumeRate;
	float barsShowingRate=1.0f;

	private Vibrator vib;

	List<Shape> shapes=new ArrayList<Shape>();

	Handler handler=new Handler();

	Integer notesOfChord[]=new Integer[0];
	Sound sound=null;

	SparseIntArray taps=new SparseIntArray();

	public TapChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		situation=Statics.SITUATION_NORMAL;
		destination=Statics.SITUATION_NORMAL;
		step=0;
		playing=0;
		scroll=0;
		upper=0;
		darken=0;
		vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void init(Context context){

	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas){
		int x,y,delta;
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
		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK,0,darken));
		textPaint.setTextSize(rad/2);

		rect=new RectF(0,0,width,height);
		paint.setColor(Statics.getColor(Statics.COLOR_WHITE,0,darken));
		canvas.drawRect(rect,paint);

		if(situation==Statics.SITUATION_TRANSPOSE||situation==Statics.SITUATION_TRANSPOSING
				||destination==Statics.SITUATION_TRANSPOSE||destination==Statics.SITUATION_TRANSPOSING){
			paint.setStyle(Style.FILL);
			paint.setColor(Statics.getColor(Statics.COLOR_PASTELGRAY,0,darken));
			canvas.drawRect(Statics.getRectOfToolbar(width, height,1.0f),paint);

			int d;
			d=0;
			if(toolbarPressed==0) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarButton(0,0,width,height,1.0f);
			canvas.drawOval(rect, paint);
			str=getContext().getString(R.string.ok);
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);

			d=0;
			if(toolbarPressed==1) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarTransposingButton(0,0,width,height,1.0f);
			canvas.drawOval(rect, paint);
			str=Statics.SCALES[7];
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		delta=scroll/(height/5);
		for(x=-7-delta;x<=7-delta;x++){
			int d=0;
			if(x==scalePressed) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY,d,darken));

			rect=Statics.getRectOfButton(x,-2,width,height,scroll);
			canvas.drawOval(rect, paint);

			if(x+scale<-7){
				textPaint.setColor(Statics.getColor(Statics.COLOR_GRAY,0,darken));
				str=Statics.getStringOfScale(x+scale+12);
				w=textPaint.measureText(str);
				canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
			}else if(x+scale>7){
				textPaint.setColor(Statics.getColor(Statics.COLOR_GRAY,0,darken));
				str=Statics.getStringOfScale(x+scale-12);
				w=textPaint.measureText(str);
				canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
			}else{
				textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK,0,darken));
				str=Statics.getStringOfScale(x+scale);
				w=textPaint.measureText(str);
				canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
			}
		}

		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK,0,darken));

		delta=scroll/(height/5);
		for(x=-7-delta;x<=7-delta;x++){
			int maj=x+15+scale;
			if(maj<0) maj+=12;
			if(maj>=36) maj-=12;
			int min=x+18+scale;
			if(min<0) min+=12;
			if(min>=36) min-=12;
			int xx=(x+360)%12;
			for(y=-1;y<=1;y++){
				int c=0;
				int d=0;
				if(playing>0&&playingX==x&&playingY==y) d=1;
				switch(xx){
				case 11: case 0: case 1:
					c=Statics.COLOR_RED;
					break;
				case 2: case 3: case 4:
					c=Statics.COLOR_YELLOW;
					break;
				case 5: case 6: case 7:
					c=Statics.COLOR_GREEN;
					break;
				case 8: case 9: case 10:
					c=Statics.COLOR_BLUE;
					break;
				}
				if(situation==Statics.SITUATION_TRANSPOSE || destination==Statics.SITUATION_TRANSPOSE){
					c=Statics.COLOR_LIGHTGRAY;
				}
				paint.setColor(Statics.getColor(c,d,darken));

				rect=Statics.getRectOfButton(x,y,width,height,scroll);
				canvas.drawOval(rect, paint);

				switch(y){
				case -1:
					str=Statics.NOTES_5TH[maj]+Statics.SUS4; break;
				case 0:
					str=Statics.NOTES_5TH[maj]; break;
				case 1:
					str=Statics.NOTES_5TH[min]+Statics.MINOR; break;
				}
				w=textPaint.measureText(str);
				canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
			}
		}

		if(situation==Statics.SITUATION_TRANSPOSE || destination==Statics.SITUATION_TRANSPOSE){
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height/160.0f);
			for(x=-7;x<=7;x++){
				int xx=(x+360)%12;
				for(y=-1;y<=1;y++){
					int c=0;
					switch(xx){
					case 11: case 0: case 1:
						c=Statics.COLOR_RED;
						break;
					case 2: case 3: case 4:
						c=Statics.COLOR_YELLOW;
						break;
					case 5: case 6: case 7:
						c=Statics.COLOR_GREEN;
						break;
					case 8: case 9: case 10:
						c=Statics.COLOR_BLUE;
						break;
					}
					paint.setColor(Statics.getColor(c,0,darken));

					int sc=scroll;
					if(situation==Statics.SITUATION_TRANSPOSING) sc=0;
					rect=Statics.getRectOfButton(x,y,width,height,sc);
					canvas.drawOval(rect, paint);
				}
			}

			paint.setColor(Statics.getColor(Statics.COLOR_RED,0,darken));

			int sc=scroll;
			if(situation==Statics.SITUATION_TRANSPOSING) sc=0;
			rect=Statics.getRectOfButton(0,-2,width,height,sc);
			canvas.drawOval(rect, paint);
		}

		paint.setStyle(Style.FILL);

		paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY,0,darken));
		canvas.drawRect(Statics.getRectOfStatusBar(width, height,barsShowingRate),paint);

		paint.setColor(Statics.getColor(Statics.COLOR_LIGHTGRAY,0,darken));
		canvas.drawRect(Statics.getRectOfToolbar(width, height,barsShowingRate),paint);

		for(x=0;x<4;x++){
			int d=0;
			if(statusbarFlags[x]>0) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_ORANGE,d,darken));
			rect=Statics.getRectOfStatusBarButton(x,0,width,height,barsShowingRate);
			canvas.drawOval(rect, paint);

			if(statusbarFlags[x]>=2)	textPaint.setColor(Statics.getColor(Statics.COLOR_ORANGE,0,darken));
			else 						textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK,0,darken));
			str=Statics.TENSIONS[x];
			if(x==2&&statusbarFlags[3]>0) str="6";
			if(x==3&&statusbarFlags[2]>0) str="6";
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		textPaint.setColor(Statics.getColor(Statics.COLOR_BLACK,0,darken));

		for(x=0;x<3;x++){
			int d=0;
			if(toolbarPressed==x) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarButton(x,0,width,height,barsShowingRate);
			canvas.drawOval(rect, paint);

			switch(x){
			case 0:
				str=getContext().getString(R.string.action_settings);
				break;
			case 1:
				str=getContext().getString(R.string.darken);
				break;
			case 2:
				str=Statics.getStringOfScale(scale);
				break;
			default:
				str="";
			}
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

		if(Statics.getRectOfStatusBarButton(3, 0, width, height, barsShowingRate).right
				< Statics.getRectOfKeyboardIndicator(0, 0, width, height, barsShowingRate).left){

			for(x=0;x<12;x++){
				paint.setColor(Statics.getColor(Statics.COLOR_GRAY,0,darken));
				rect=Statics.getRectOfKeyboardIndicator(x, 0, width, height, barsShowingRate);
				canvas.drawOval(rect,paint);
			}

			for(int i=0;i<notesOfChord.length;i++){
				paint.setColor(Statics.getColor(Statics.COLOR_ABSOLUTE_LIGHT,0,darken));
				rect=Statics.getRectOfKeyboardIndicator(notesOfChord[i], 2, width, height, barsShowingRate);
				canvas.drawOval(rect,paint);
			}

		}

		paint.setColor(Statics.getColor(Statics.COLOR_GRAY,0,darken));
		rect=Statics.getRectOfScrollBar(width, height, barsShowingRate);
		canvas.drawRect(rect,paint);

		paint.setColor(Statics.getColor(Statics.COLOR_DARKGRAY,0,darken));
		rect=Statics.getRectOfScrollNob(scroll, upper, width, height, barsShowingRate);
		canvas.drawRect(rect,paint);

		if(darken>0){
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height/25);
			paint.setColor(Statics.getColor(Statics.COLOR_ABSOLUTE_CYAN,1,darken));
			for(int i=0;i<shapes.size();i++){
				Shape shape=shapes.get(i);
				paint.setAlpha(255*shape.lifetime/Shape.MAX_LIFETIME);

				if(shape.style==Shape.STYLE_LINE){
					float r=shape.rad;
					float ax=shape.center.x-(float)(Math.cos(r/360.0*Math.PI*2)*width);
					float ay=shape.center.y-(float)(Math.sin(r/360.0*Math.PI*2)*width);
					float bx=shape.center.x+(float)(Math.cos(r/360.0*Math.PI*2)*width);
					float by=shape.center.y+(float)(Math.sin(r/360.0*Math.PI*2)*width);
					canvas.drawLine(ax,ay,bx,by,paint);
				}if(shape.style==Shape.STYLE_CIRCLE){
					float cx=shape.center.x;
					float cy=shape.center.y;
					canvas.drawCircle(cx,cy,(float)(height*(0.2f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.8f),paint);
				}if(shape.style==Shape.STYLE_TRIANGLE){
					float l=(float)(height*(0.3f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.7f);
					float r=((shape.rad*shape.lifetime)+(shape.radDelta*(Shape.MAX_LIFETIME-shape.lifetime)))/Shape.MAX_LIFETIME;
					float ax=shape.center.x+(float)(Math.cos((r)/360.0*Math.PI*2)*l);
					float ay=shape.center.y+(float)(Math.sin((r)/360.0*Math.PI*2)*l);
					float bx=shape.center.x+(float)(Math.cos((r+120)/360.0*Math.PI*2)*l);
					float by=shape.center.y+(float)(Math.sin((r+120)/360.0*Math.PI*2)*l);
					float cx=shape.center.x+(float)(Math.cos((r+240)/360.0*Math.PI*2)*l);
					float cy=shape.center.y+(float)(Math.sin((r+240)/360.0*Math.PI*2)*l);
					canvas.drawLine(ax,ay,bx,by,paint);
					canvas.drawLine(bx,by,cx,cy,paint);
					canvas.drawLine(cx,cy,ax,ay,paint);
				}if(shape.style==Shape.STYLE_SQUARE){
					float l=(float)(height*(0.3f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.7f);
					float r=((shape.rad*shape.lifetime)+(shape.radDelta*(Shape.MAX_LIFETIME-shape.lifetime)))/Shape.MAX_LIFETIME;
					float ax=shape.center.x+(float)(Math.cos((r)/360.0*Math.PI*2)*l);
					float ay=shape.center.y+(float)(Math.sin((r)/360.0*Math.PI*2)*l);
					float bx=shape.center.x+(float)(Math.cos((r+90)/360.0*Math.PI*2)*l);
					float by=shape.center.y+(float)(Math.sin((r+90)/360.0*Math.PI*2)*l);
					float cx=shape.center.x+(float)(Math.cos((r+180)/360.0*Math.PI*2)*l);
					float cy=shape.center.y+(float)(Math.sin((r+180)/360.0*Math.PI*2)*l);
					float dx=shape.center.x+(float)(Math.cos((r+270)/360.0*Math.PI*2)*l);
					float dy=shape.center.y+(float)(Math.sin((r+270)/360.0*Math.PI*2)*l);
					canvas.drawLine(ax,ay,bx,by,paint);
					canvas.drawLine(bx,by,cx,cy,paint);
					canvas.drawLine(cx,cy,dx,dy,paint);
					canvas.drawLine(dx,dy,ax,ay,paint);
				}
			}
		}

	}

	public boolean actionDown(int x,int y,int id){
		int i;
		RectF rect;

		if(situation==Statics.SITUATION_TRANSPOSE||situation==Statics.SITUATION_TRANSPOSING){
			rect=Statics.getRectOfToolbarButton(0,0,width,height,1.0f);
			if(rect.contains(x, y)){
				toolbarPressed=0;
				taps.put(id,Statics.TOOLBAR_BUTTON);
				if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
				return false;
			}
			rect=Statics.getRectOfToolbarTransposingButton(0,0,width,height,1.0f);
			if(rect.contains(x, y)){
				toolbarPressed=1;
				taps.put(id,Statics.TOOLBAR_BUTTON);
				if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
				return false;
			}
		}else{
			for(i=0;i<3;i++){
				rect=Statics.getRectOfToolbarButton(i,0,width,height,barsShowingRate);
				if(rect.contains(x, y)){
					toolbarPressed=i;
					taps.put(id,Statics.TOOLBAR_BUTTON);
					if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
					return false;
				}
			}
		}

		if(situation==Statics.SITUATION_TRANSPOSE){
			for(i=-7;i<=7;i++){
				rect=Statics.getRectOfButton(i,-2,width,height,scroll);
				if(rect.contains(x, y)){
					scalePressed=i;
					taps.put(id,Statics.TRANSPOSE_SCALE_BUTTON);
					if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
					return false;
				}
			}
		}else{
			for(i=0;i<4;i++){
				rect=Statics.getRectOfStatusBarButton(i,0,width,height,barsShowingRate);
				if(rect.contains(x, y)){
					if(lastTapped==i&&System.currentTimeMillis()-lastTappedTime<500) statusbarFlags[i]=2;
					else statusbarFlags[i]=1;
					taps.put(id,Statics.STATUSBAR_BUTTON);
					if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
					lastTapped=i;
					lastTappedTime=System.currentTimeMillis();
					return false;
				}
			}
			if(Statics.getRectOfScrollNob(scroll,upper,width,height,barsShowingRate).contains(x,y)){
				originalX=x;
				originalY=y;
				originalScroll=scroll;
				taps.put(id,Statics.SCROLL_NOB);
				if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
				return false;
			}else if(Statics.getRectOfToolbar(width,height,1.0f).contains(x,y)){
				if(scroll==0){
					for(i=0;i<4;i++){
						if(statusbarFlags[i]>=2) statusbarFlags[i]=0;
					}
				}else{
					scroll=0;
				}
				taps.put(id,Statics.SCROLL_BAR);
				if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
				return false;
			}
		}

		if(playing<=0){
			if(Statics.getRectOfButtonArea(width,height).contains(x, y)){
				Point buttonXY=Statics.getXYOfButton(x,y,width,height,scroll);
				if(buttonXY.y>=-1&&buttonXY.y<=1){
					play(buttonXY.x,buttonXY.y);
					originalScroll=scroll;
					tappedX=x;
					playingID=id;
					taps.put(playingID,Statics.CHORD_BUTTON);
					if(vibration>0) vib.vibrate(Statics.VIBRATION_LENGTH);
					if(darken>0){
						shapes.add(new Shape(new PointF(x,y)));
					}
					return true;
				}
			}
		}else{
			if(Statics.getRectOfButtonArea(width,height).contains(x, y)){
				destinationScroll=originalScroll+(x-tappedX);
				pulling=1;
				startPullingAnimation();
				return true;
			}
		}

		return false;
	}

	public boolean actionMove(int x,int y,int id){
		int i;
		boolean chordPressed=false;
		RectF rect;
		int kind;
		if(id>=0) kind=taps.get(id);
		else kind=0;
		switch(kind){
		case Statics.SCROLL_NOB:
			if(-y+originalY>height/5){
				if(vibration>0&&scroll!=0) vib.vibrate(Statics.VIBRATION_LENGTH);
				scroll=0;
				upper=height/35/5*2;
			}else{
				scroll=(int)(-x+originalX)*5+originalScroll;
				if(scroll<-Statics.getScrollMax(width,height)) scroll=-Statics.getScrollMax(width,height);
				if(scroll>Statics.getScrollMax(width,height)) scroll=Statics.getScrollMax(width,height);
				upper=0;
			}
			break;
		case Statics.STATUSBAR_BUTTON:
			for(i=0;i<4;i++){
				rect=Statics.getRectOfStatusBarButton(i,0,width,height,barsShowingRate);
				if(rect.contains(x, y)){
					if(statusbarFlags[i]>=2&&lastTapped==i) continue;
					if(vibration>0&&statusbarFlags[i]==0) vib.vibrate(Statics.VIBRATION_LENGTH);
					statusbarFlags[i]=1;
				}
			}
			break;
		case Statics.TOOLBAR_BUTTON:
			toolbarPressed=-1;
			if(situation==Statics.SITUATION_TRANSPOSE||situation==Statics.SITUATION_TRANSPOSING){
				rect=Statics.getRectOfToolbarButton(0,0,width,height,1.0f);
				if(rect.contains(x, y)){
					toolbarPressed=0;
					taps.put(id,Statics.TOOLBAR_BUTTON);
				}
				rect=Statics.getRectOfToolbarTransposingButton(0,0,width,height,1.0f);
				if(rect.contains(x, y)){
					toolbarPressed=1;
					taps.put(id,Statics.TOOLBAR_BUTTON);
				}
			}else{
				for(i=0;i<3;i++){
					rect=Statics.getRectOfToolbarButton(i,0,width,height,barsShowingRate);
					if(rect.contains(x, y)){
						toolbarPressed=i;
						taps.put(id,Statics.TOOLBAR_BUTTON);
					}
				}
			}
			break;
		case Statics.CHORD_BUTTON:
			if(id==playingID){
				if(situation==Statics.SITUATION_NORMAL){
					if(pulling==2){
						scroll=originalScroll+(x-tappedX);
						if(y>height*4/5){
							int vert=height/5;
							scroll=(scroll/vert)*vert;
						}
						if(scroll<-Statics.getScrollMax(width,height)) scroll=-Statics.getScrollMax(width,height);
						if(scroll>Statics.getScrollMax(width,height)) scroll=Statics.getScrollMax(width,height);
					}else if(pulling==1){
						destinationScroll=originalScroll+(x-tappedX);
					}else if(Math.abs(x-tappedX)>height/5){
						originalScroll=scroll;
						destinationScroll=originalScroll+(x-tappedX);
						pulling=1;
						step=100/heartBeatInterval;
						startPullingAnimation();
					}
				}
				chordPressed=true;
				break;
			}else{
				chordPressed=actionDown(x,y,id);
			}
			break;
		case Statics.SCROLL_BAR:
			break;
		case Statics.TRANSPOSE_SCALE_BUTTON:
			break;
		default:
			chordPressed=actionDown(x,y,id);
			break;
		}
		return chordPressed;
	}

	public boolean onTouchEvent(MotionEvent event){
		int x,y,id;
		boolean chordPressed=false;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			// Log.i("TapChordView","DOWN Count:"+event.getPointerCount());
			x=(int)event.getX(event.getActionIndex());
			y=(int)event.getY(event.getActionIndex());
			id=event.getPointerId(event.getActionIndex());
			actionDown(x,y,id);
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i("TapChordView","MOVE Count:"+event.getPointerCount());
			for(int index=0;index<event.getPointerCount();index++){
				x=(int)event.getX(index);
				y=(int)event.getY(index);
				id=event.getPointerId(index);
				chordPressed|=actionMove(x,y,id);
			}
			if(chordPressed==false){
				playingID=-1;
				pulling=0;
				stop();
			}
			break;
		case MotionEvent.ACTION_UP:
			// Log.i("TapChordView","UP Count:"+event.getPointerCount());
			stop();
			for(int l=0;l<4;l++){
				if(statusbarFlags[l]==1) statusbarFlags[l]=0;
			}
			if(toolbarPressed>=0) toolbarReleased(toolbarPressed);
			if(scalePressed!=Statics.FARAWAY) scaleReleased(scalePressed);
			toolbarPressed=-1;
			scalePressed=Statics.FARAWAY;
			playingID=-1;
			pulling=0;
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
		if(situation==Statics.SITUATION_TRANSPOSE){
			switch(which){
			case 0:
				originalScroll=scroll;
				startAnimation(1-situation);
				break;
			case 1:
				startTransposingAnimation(0);
				break;
			}
		}else{
			switch(which){
			case 0:
				Intent intent=new Intent((Activity)this.getContext(),SettingsActivity.class);
				intent.putExtra("darken",getDarken());
				this.getContext().startActivity(intent);
				break;
			case 1:
				setDarken(1-darken);
				break;
			case 2:
				originalScroll=scroll;
				startAnimation(1-situation);
				break;
			default:
				break;
			}
		}
		toolbarPressed=-1;
		invalidate();
	}

	public void scaleReleased(int which){
		int ds;
		ds=which+scale;
		if(ds<-7) ds+=12;
		if(ds>7) ds-=12;
		startTransposingAnimation(ds);
		invalidate();
	}

	public void play(int x,int y){
		notesOfChord=Statics.getNotesOfChord(x+scale,y,statusbarFlags);
		Integer f[]=(Statics.convertNotesToFrequencies(notesOfChord,soundRange));
		sound=new Sound(f,this.getContext());
		playing=1;
		playingX=x;
		playingY=y;
		volumeRate=1.0f;
		sound.play();
		invalidate();
	}

	public void stop(){
		if(sound!=null){
			sound.stop();
		}
		playing=0;
		notesOfChord=new Integer[0];
		invalidate();
	}

	public void release(){
		if(sound!=null){
			sound.release();
			sound=null;
		}
		playing=0;
		notesOfChord=new Integer[0];
		invalidate();
	}

	public void activityPaused(){
		release();
	}

	public void activityResumed(){
		getPreferenceValues();
		invalidate();
	}

	public void heartbeat(int interval){
		if(step>0){
			step--;
			if(situation==Statics.SITUATION_NORMAL){
				switch(destination){
				case Statics.SITUATION_TRANSPOSE:
					scroll=(int)(originalScroll*step/stepMax);
					barsShowingRate=step/stepMax;
					break;
				case Statics.SITUATION_PULLING:
					break;
				}
			}else if(situation==Statics.SITUATION_TRANSPOSE){
				switch(destination){
				case Statics.SITUATION_NORMAL:
					barsShowingRate=(stepMax-step)/stepMax;
					break;
				}
			}else if(situation==Statics.SITUATION_TRANSPOSING){
				switch(destination){
				case Statics.SITUATION_TRANSPOSE:
					scroll=(int)((scale-destScale)*(height/5)*(stepMax-step)/stepMax);
					break;
				}
			}
			if(step==0&&situation!=destination){
				if(situation==Statics.SITUATION_TRANSPOSING){
					setScale(destScale);
					scroll=0;
				}
				situation=destination;
			}
			handler.post(new Repainter());
		}
		if(pulling==1){
			int max=100/heartBeatInterval;
			scroll=(destinationScroll*(max-step)+scroll*step)/max;
			if(scroll<-Statics.getScrollMax(width,height)) scroll=-Statics.getScrollMax(width,height);
			if(scroll>Statics.getScrollMax(width,height)) scroll=Statics.getScrollMax(width,height);
			handler.post(new Repainter());
		}
		if(shapes.size()>0){
			for(int i=shapes.size()-1;i>=0;i--){
				shapes.get(i).lifetime--;
				if(shapes.get(i).lifetime<=0) shapes.remove(i);
			}
			handler.post(new Repainter());
		}
	}

	private class Repainter implements Runnable{
		@Override
		public void run() {
			invalidate();
		}
	}

	public void startAnimation(int dest){
		destination=dest;
		step=(int)stepMax;
	}

	public void startTransposingAnimation(int ds){
		situation=Statics.SITUATION_TRANSPOSING;
		step=(int)stepMax;
		destScale=ds;
	}

	public void startPullingAnimation(){
		situation=Statics.SITUATION_PULLING;
		step=(int)stepMax;
	}

	public int getDarken(){
		return darken;
	}

	public void setScale(int s){
		scale=s;
		Statics.setPreferenceValue(this.getContext(),Statics.PREF_SCALE,scale);
	}

	public void setDarken(int d){
		darken=d;
		Statics.setPreferenceValue(this.getContext(),Statics.PREF_DARKEN,darken);
	}

	public void getPreferenceValues(){
		scale=Statics.getPreferenceValue(this.getContext(),Statics.PREF_SCALE,0);
		darken=Statics.getPreferenceValue(this.getContext(),Statics.PREF_DARKEN,0);
		soundRange=Statics.getPreferenceValue(this.getContext(),Statics.PREF_SOUND_RANGE,0);
		vibration=Statics.getPreferenceValue(this.getContext(),Statics.PREF_VIBRATION,0);
		attackTime=Statics.getPreferenceValue(this.getContext(),Statics.PREF_ATTACK_TIME,0);
		decayTime=Statics.getPreferenceValue(this.getContext(),Statics.PREF_DECAY_TIME,0);
		releaseTime=Statics.getPreferenceValue(this.getContext(),Statics.PREF_RELEASE_TIME,0);
	}


}
