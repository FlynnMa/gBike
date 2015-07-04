package com.vehicle.uart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.vehicle.uart.R;

public class SpeedView extends SurfaceView implements Callback,Runnable{
	private SurfaceHolder holder;
	private Thread thread;
	private Paint paint;
	private Canvas canvas;
	private int screenW;
	private Bitmap bigDialBmp,bigPointerBmp,bgBmp;
	private boolean flag;
	private int bigDialX,bigDialY,bigPointerX,bigPointerY,
				smallPointerX,smallPointerY;
	private Rect bgRect;
	public int bigDialDegrees;
	private String percentageText="";
	private int percentageX,percentageY;
	public SpeedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder=getHolder();
		holder.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setColor(Color.argb(255, 207, 60, 11));
		paint.setTextSize(22);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}
	public void myDraw(){
		try {
			canvas=holder.lockCanvas(bgRect);
			canvas.drawColor(Color.WHITE);
			drawBigDial();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			holder.unlockCanvasAndPost(canvas);
		}
	}
    public void drawBigDial(){
		canvas.drawBitmap(bigDialBmp, bigDialX, bigDialY, paint);
		canvas.save();
		canvas.rotate(bigDialDegrees,bigPointerX+bigPointerBmp.getWidth()/2, bigPointerY+bigPointerBmp.getHeight()/2);
		canvas.drawBitmap(bigPointerBmp,bigPointerX,bigPointerY,paint);
		canvas.restore();
	}

	public void logic(){
		bigDialDegrees++;
		if (bigDialDegrees>180)
		{
			bigDialDegrees=bigDialDegrees-180;
		}
	}
	public void run() {
		while(flag){
			long start = System.currentTimeMillis();
	        myDraw();
	        logic();
	        long end = System.currentTimeMillis();
	        try {
	            if (end - start < 50)
	           Thread.sleep(50 - (end - start));
	        } catch (Exception e) {
	           e.printStackTrace();
	        }
		}
	}
	public void surfaceCreated(SurfaceHolder holder) {
		bigDialBmp = BitmapFactory.decodeResource(getResources(), R.drawable.signsec_dashboard);
		bigPointerBmp = BitmapFactory.decodeResource(getResources(), R.drawable.signsec_pointer);
		bgBmp = BitmapFactory.decodeResource(getResources(), R.drawable.signsec_dj_ll_blue);
		screenW=getWidth();
		bgRect=new Rect(0, 0,screenW , bgBmp.getHeight());
		bigDialX =(getWidth()-bigDialBmp.getWidth())/2; // 20;
		bigDialY =0;
		bigPointerX = (getWidth()-bigPointerBmp.getWidth())/2;// 20/2+bigDialBmp.getWidth()/2-bigPointerBmp.getWidth()/2+10;
		bigPointerY = 0;
		
		flag=true;
		thread= new Thread(this);
		thread.start();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		flag=false;
	}
	public int getBigDialDegrees() {
		return bigDialDegrees;
	}
	public void setBigDialDegrees(int bigDialDegrees) {
		this.bigDialDegrees = bigDialDegrees;
	}
}
