package com.example.cggi_04.vinreader;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cggi_04.vinreader.CameraMask;

public class MainActivity extends Activity implements  Callback{

    float s=1.0f;
    SurfaceView mpreview;
    private SurfaceHolder mSurfaceHolder;
    private ImageButton bigbtn;
    private ImageButton smallbtn;
    private ImageButton photobtn;
    private float picturebili;
    private CameraMask maskView;
    private ImageView img;

    private Rect nowframe;
    private Rect tmpRect=new Rect(200,220,600,260);
    private int maxX,maxY,lx,ly,rx,ry;

    private Camera mCamera;
    private Bitmap mBitmap;

    private Camera.Size pictureSize;
    private Camera.Size previewSize;

    private static final String TESSBASE_PATH ="/sdcard";
    private String uuid;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private float mX;
    private float mY;
    private float mGap;
    private float aX;
    private float bX;
    private float aY;
    private float bY;
    private float nx;
    private float ny;
    private Point	screenResolution;
    private float xx;
    private float yy;
    private float mScaledTouchSlop;
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private boolean hIncrement=false;
    private boolean hDecrement=false;

    private int mode = NONE;
    private float oldDist;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private GestureDetector detector;

    private Handler h=new Handler();

    //Button bCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);
        //设置屏幕旋转
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //设置camera surface holder
        mpreview = (SurfaceView) this.findViewById(R.id.camera_preview);
        mSurfaceHolder = mpreview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //bCapture=(Button)findViewById(R.id.bCapture);
        maskView = (CameraMask) this.findViewById(R.id.camera_mask);
        nowframe = maskView.getmFrame();
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("fnf", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("eof", "Error accessing file: " + e.getMessage());
            }
        }
    };

    public void Capture(View view) {
        showToast("I love Hefang");
        Log.d("capture", "Capturing an image!");
        mCamera.takePicture(null, null,mPicture );
    }

    //没有logcat，用这个来显示调试信息
    private void showToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    //用来判断当前是否对焦
    private boolean hasAutoFocus() {
        PackageManager pm = getPackageManager();
        return  pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Parameters params = mCamera.getParameters();

//		if used in android 1.6
//		set params in default mode

        float camerabili = (float)width/(float)height;
        Log.v("xinli", width + " * "+height);
        //get a good preview size
        List<Camera.Size> supportPreviewSizes = params.getSupportedPreviewSizes();
        for (int i = 0 ; i < supportPreviewSizes.size() ; i++){
            previewSize = supportPreviewSizes.get(i);
            if ( ((float)previewSize.width/(float)previewSize.height) ==  camerabili)
                break;
        }
        params.setPreviewSize(previewSize.width, previewSize.height);
        maxX=previewSize.width-40;
        maxY=previewSize.height-40;
        Log.v("maxX", maxX+"  * "+maxY);

        //get a good picture size : has same ratio and not very big
        List<Camera.Size> supportPictureSizes = params.getSupportedPictureSizes();
        for(int i = 0; i <supportPictureSizes.size();i++){
            pictureSize = supportPictureSizes.get(i);
            if( (pictureSize.width < 1500)&&(((float)pictureSize.width/(float)pictureSize.height) == camerabili))
                break;
        }

        picturebili = (float)pictureSize.width/(float)width;
        params.setPictureSize(pictureSize.width, pictureSize.height);
        Log.v("xinli", pictureSize.width +" * "+pictureSize.height);

        params.setPictureFormat(PixelFormat.JPEG);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public final class AutoFocusCallBack implements android.hardware.Camera.AutoFocusCallback{

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if(success){
                //camera.takePicture(null,null,m_pictureCallback);
            }
        }
    }
}





