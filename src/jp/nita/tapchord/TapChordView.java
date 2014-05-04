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
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;


public class TapChordView extends View {
	int width,height,originalX,originalY,originalScroll;
	int situation,destination,step,scroll,upper,darken,destScale;
	int playing,playingX,playingY,tappedX,virtualScroll;
	int playingID;

	int scale=0;
	int pulling=0;

	int statusbarFlags[]={0,0,0,0};
	int toolbarPressed=-1;
	int scalePressed=Statics.FARAWAY;

	float barsShowingRate=1.0f;

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

		delta=scroll/(height/5);
		for(x=-6-delta;x<=6-delta;x++){
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
		for(x=-6-delta;x<=6-delta;x++){
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

		if(situation==Statics.SITUATION_TRANSPOSE || destination==Statics.SITUATION_TRANSPOSE){
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height/200);
			for(x=-6;x<=6;x++){
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
		canvas.drawRect(Statics.getRectOfToolbar(width, height,1.0f),paint);

		for(x=0;x<4;x++){
			int d=0;
			if(statusbarFlags[x]>0) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_ORANGE,d,darken));
			rect=Statics.getRectOfStatusBarButton(x,0,width,height,barsShowingRate);
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
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarButton(x,0,width,height,1.0f);
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
		rect=Statics.getRectOfScrollBar(width, height);
		canvas.drawRect(rect,paint);

		paint.setColor(Statics.getColor(Statics.COLOR_DARKGRAY,0,darken));
		rect=Statics.getRectOfScrollNob(scroll, upper, width, height);
		canvas.drawRect(rect,paint);

		if(darken>0){
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(height/25);
			paint.setColor(Statics.getColor(Statics.COLOR_ABSOLUTE_CYAN,1,darken));
			for(int i=0;i<shapes.size();i++){
				Shape shape=shapes.get(i);
				paint.setAlpha(255*shape.lifetime/Shape.MAX_LIFETIME);

				if(shape.style==Shape.STYLE_LINE){
					float ax=shape.center.x-(float)(Math.cos(shape.rad)*width);
					float ay=shape.center.y-(float)(Math.sin(shape.rad)*width);
					float bx=shape.center.x+(float)(Math.cos(shape.rad)*width);
					float by=shape.center.y+(float)(Math.sin(shape.rad)*width);
					canvas.drawLine(ax,ay,bx,by,paint);
				}if(shape.style==Shape.STYLE_CIRCLE){
					float cx=shape.center.x;
					float cy=shape.center.y;
					canvas.drawCircle(cx,cy,(float)(height*(0.3f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.7f),paint);
				}if(shape.style==Shape.STYLE_TRIANGLE){
					float l=(float)(height*(0.3f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.7f);
					float ax=shape.center.x+(float)(Math.cos((shape.rad)/360.0*Math.PI*2)*l);
					float ay=shape.center.y+(float)(Math.sin((shape.rad)/360.0*Math.PI*2)*l);
					float bx=shape.center.x+(float)(Math.cos((shape.rad+120)/360.0*Math.PI*2)*l);
					float by=shape.center.y+(float)(Math.sin((shape.rad+120)/360.0*Math.PI*2)*l);
					float cx=shape.center.x+(float)(Math.cos((shape.rad+240)/360.0*Math.PI*2)*l);
					float cy=shape.center.y+(float)(Math.sin((shape.rad+240)/360.0*Math.PI*2)*l);
					canvas.drawLine(ax,ay,bx,by,paint);
					canvas.drawLine(bx,by,cx,cy,paint);
					canvas.drawLine(cx,cy,ax,ay,paint);
				}if(shape.style==Shape.STYLE_SQUARE){
					float l=(float)(height*(0.3f+(float)(Shape.MAX_LIFETIME-shape.lifetime)/Shape.MAX_LIFETIME)*0.7f);
					float ax=shape.center.x+(float)(Math.cos((shape.rad)/360.0*Math.PI*2)*l);
					float ay=shape.center.y+(float)(Math.sin((shape.rad)/360.0*Math.PI*2)*l);
					float bx=shape.center.x+(float)(Math.cos((shape.rad+90)/360.0*Math.PI*2)*l);
					float by=shape.center.y+(float)(Math.sin((shape.rad+90)/360.0*Math.PI*2)*l);
					float cx=shape.center.x+(float)(Math.cos((shape.rad+180)/360.0*Math.PI*2)*l);
					float cy=shape.center.y+(float)(Math.sin((shape.rad+180)/360.0*Math.PI*2)*l);
					float dx=shape.center.x+(float)(Math.cos((shape.rad+270)/360.0*Math.PI*2)*l);
					float dy=shape.center.y+(float)(Math.sin((shape.rad+270)/360.0*Math.PI*2)*l);
					canvas.drawLine(ax,ay,bx,by,paint);
					canvas.drawLine(bx,by,cx,cy,paint);
					canvas.drawLine(cx,cy,dx,dy,paint);
					canvas.drawLine(dx,dy,ax,ay,paint);
				}
			}
		}

		paint.setStyle(Style.FILL);
		paint.setColor(Statics.getColor(Statics.COLOR_PASTELGRAY,0,darken));
		canvas.drawRect(Statics.getRectOfToolbar(width, height,1.0f-barsShowingRate),paint);

		if(situation==Statics.SITUATION_TRANSPOSE||situation==Statics.SITUATION_TRANSPOSING){
			int d;
			d=0;
			if(toolbarPressed==0) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarButton(0,0,width,height,1.0f-barsShowingRate);
			canvas.drawOval(rect, paint);
			str=getContext().getString(R.string.ok);
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);

			d=0;
			if(toolbarPressed==1) d=1;
			paint.setColor(Statics.getColor(Statics.COLOR_PURPLE,d,darken));
			rect=Statics.getRectOfToolbarTransposingButton(0,0,width,height,1.0f-barsShowingRate);
			canvas.drawOval(rect, paint);
			str=Statics.SCALES[7];
			w=textPaint.measureText(str);
			canvas.drawText(str,rect.centerX()-w/2,rect.centerY()-(fontMetrics.ascent+fontMetrics.descent)/2,textPaint);
		}

	}

	public boolean actionDown(int x,int y,int id){
		int i,j;
		RectF rect;

		if(situation==Statics.SITUATION_TRANSPOSE||situation==Statics.SITUATION_TRANSPOSING){
			rect=Statics.getRectOfToolbarButton(0,0,width,height,1.0f);
			if(rect.contains(x, y)){
				toolbarPressed=0;
				taps.put(id,Statics.TOOLBAR_BUTTON);
				return true;
			}
			rect=Statics.getRectOfToolbarTransposingButton(0,0,width,height,1.0f);
			if(rect.contains(x, y)){
				toolbarPressed=1;
				taps.put(id,Statics.TOOLBAR_BUTTON);
				return true;
			}
		}else{
			for(i=0;i<3;i++){
				rect=Statics.getRectOfToolbarButton(i,0,width,height,barsShowingRate);
				if(rect.contains(x, y)){
					toolbarPressed=i;
					taps.put(id,Statics.TOOLBAR_BUTTON);
					return true;
				}
			}
		}

		if(situation==Statics.SITUATION_TRANSPOSE){
			for(i=-6;i<=6;i++){
				rect=Statics.getRectOfButton(i,-2,width,height,scroll);
				if(rect.contains(x, y)){
					scalePressed=i;
					taps.put(id,Statics.TRANSPOSE_SCALE_BUTTON);
					return true;
				}
			}
		}else{
			for(i=0;i<4;i++){
				rect=Statics.getRectOfStatusBarButton(i,0,width,height,barsShowingRate);
				if(rect.contains(x, y)){
					statusbarFlags[i]=1;
					taps.put(id,Statics.STATUSBAR_BUTTON);
					return true;
				}
			}
			if(Statics.getRectOfScrollNob(scroll,upper,width,height).contains(x,y)){
				originalX=x;
				originalY=y;
				originalScroll=scroll;
				taps.put(id,Statics.SCROLL_NOB);
				return true;
			}else if(Statics.getRectOfToolbar(width,height,1.0f).contains(x,y)){
				scroll=0;
				taps.put(id,Statics.SCROLL_BAR);
				return true;
			}
		}

		if(playing<=0){
			for(j=-6;j<=6;j++){
				for(i=-1;i<=1;i++){
					rect=Statics.getRectOfButton(j,i,width,height,scroll);
					if(rect.contains(x, y)){
						play(j,i);
						originalScroll=scroll;
						tappedX=x;
						playingID=id;
						taps.put(playingID,Statics.CHORD_BUTTON);
						if(darken>0){
							shapes.add(new Shape(new PointF(x,y)));
						}
						return true;
					}
				}
			}
		}

		return false;
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
				if(rect.contains(x, y)) statusbarFlags[i]=1;
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
						virtualScroll=originalScroll+(x-tappedX);
						scroll=(virtualScroll+scroll)/2;
						if(scroll<-Statics.getScrollMax(width,height)) scroll=-Statics.getScrollMax(width,height);
						if(scroll>Statics.getScrollMax(width,height)) scroll=Statics.getScrollMax(width,height);
					}else if(Math.abs(x-tappedX)>height/5){
						virtualScroll=originalScroll+(x-tappedX);
						pulling=1;
						startPullingAnimation();
					}
				}
				chordPressed=true;
				break;
			}
			if(playing<=0){
				for(j=-6;j<=6;j++){
					for(i=-1;i<=1;i++){
						rect=Statics.getRectOfButton(j,i,width,height,scroll);
						if(rect.contains(x, y)){
							play(j,i);
							chordPressed=true;
							originalScroll=scroll;
							tappedX=x;
							playingID=id;
							taps.put(playingID,Statics.CHORD_BUTTON);
							if(darken>0){
								shapes.add(new Shape(new PointF(x,y)));
							}
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
		case Statics.TRANSPOSE_SCALE_BUTTON:
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
			for(int l=0;l<4;l++) statusbarFlags[l]=0;
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
		Integer f[]=(Statics.convertNotesToFrequencies(notesOfChord));
		sound=new Sound(f,this.getContext());
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

	public void activityResumed(){
		getPreferenceValues();
		invalidate();
	}

	public void heartbeat(){
		if(step>0){
			step--;
			if(situation==Statics.SITUATION_NORMAL){
				switch(destination){
				case Statics.SITUATION_TRANSPOSE:
					scroll=(int)(originalScroll*step/10.0f);
					barsShowingRate=step/10.0f;
					break;
				case Statics.SITUATION_PULLING:
					break;
				}
			}else if(situation==Statics.SITUATION_TRANSPOSE){
				switch(destination){
				case Statics.SITUATION_NORMAL:
					barsShowingRate=(10-step)/10.0f;
					break;
				}
			}else if(situation==Statics.SITUATION_TRANSPOSING){
				switch(destination){
				case Statics.SITUATION_TRANSPOSE:
					scroll=((scale-destScale)*(height/5)*(10-step)/10);
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
			if(step==0&&pulling==1){
				pulling=2;
			}
			handler.post(new Repainter());
		}
		if(shapes.size()>0){
			for(int i=shapes.size()-1;i>=0;i--){
				shapes.get(i).rad+=shapes.get(i).radDelta;
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
		step=10;
	}

	public void startTransposingAnimation(int ds){
		situation=Statics.SITUATION_TRANSPOSING;
		step=10;
		destScale=ds;
	}

	public void startPullingAnimation(){
		situation=Statics.SITUATION_PULLING;
		step=10;
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
	}
}
