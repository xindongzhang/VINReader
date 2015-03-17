package com.example.cggi_04.vinreader;

/**
 * Created by CGGI_04 on 2015/3/17.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class CameraMask extends View {

    private Rect m_Box;
    private Rect mFrame;
    private  Context mContext;

    private final int mMaskColor;
    private final int mFrameColor;

    private Point screenResolution;
    private int screen_width;
    private int screen_height;

//	private Handler mHandler;

    public CameraMask(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        m_Box = new Rect();
        Resources resources = this.getResources();
        mMaskColor = resources.getColor(R.color.maskcolor);
        mFrameColor = resources.getColor(R.color.maskframe);
        screenResolution = getScreenResolution();
        screen_width = screenResolution.x;
        screen_height = screenResolution.y;
        mFrame = getFrameRect();
//		mHandler = getHandler();
    }

    public  Point getScreenResolution(){
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point p = new Point(display.getWidth(),display.getHeight());
        return p;
    }


    public Rect getFrameRect(){
        mFrame = new Rect(screenResolution.x/20,screenResolution.y/8-40,screenResolution.x*19/20,screenResolution.y/8+40);
        return mFrame;
    }




    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (mFrame != null)
            DrawFrame(mFrame,canvas);
    }


    public void setmFrame(Rect mFrame) {
        this.mFrame = mFrame;
    }



    public Rect getmFrame() {
        return mFrame;
    }

    public void DrawFrame(Rect frame,Canvas canvas){
        Paint m_Paint =  new Paint();
        m_Paint.setColor(mMaskColor);
        m_Box.set(0, 0, screen_width, frame.top);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(0, frame.top, frame.left, frame.bottom + 1);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(frame.right + 1, frame.top, screen_width, frame.bottom + 1);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(0, frame.bottom + 1, screen_width, screen_height);
        canvas.drawRect(m_Box, m_Paint);

        m_Paint.setColor(mFrameColor);
        m_Box.set(frame.left, frame.top, frame.right + 1, frame.top + 2);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1);
        canvas.drawRect(m_Box, m_Paint);
        m_Box.set(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1);
        canvas.drawRect(m_Box, m_Paint);
    }


}