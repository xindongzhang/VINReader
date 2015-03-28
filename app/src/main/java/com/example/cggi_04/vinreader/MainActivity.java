package com.example.cggi_04.vinreader;


import java.io.File;
import java.io.FileInputStream;
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
import android.net.Uri;
import android.os.AsyncTask;
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
import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends Activity implements  Callback{

    float s=1.0f;
    SurfaceView mpreview;
    private SurfaceHolder mSurfaceHolder;
    private CameraMask maskView;
    private ImageView img;

    private Rect nowframe;
    private Rect tmpRect=new Rect(200,220,600,260);
    private int maxX,maxY,lx,ly,rx,ry;

    private Camera mCamera;
    private Bitmap mBitmap;

    private Camera.Size pictureSize;
    private Camera.Size previewSize;

    //Button bCapture;
    private EditText VinCode;
    //
    private boolean capture_press;

    //Image
    private Bitmap image;


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
        capture_press = false;
        mpreview = (SurfaceView) this.findViewById(R.id.camera_preview);
        VinCode = (EditText) this.findViewById(R.id.et_vincode);
        mSurfaceHolder = mpreview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //bCapture=(Button)findViewById(R.id.bCapture);
        maskView = (CameraMask) this.findViewById(R.id.camera_mask);
        nowframe = maskView.getmFrame();
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = new File(getSDPath()+"/ocr.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("fnf", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("eof", "Error accessing file: " + e.getMessage());
            }
        }
    };


    @Override
    public void onPause(){
        Log.d("Stateinfo","onPause");
        super.onPause();
    }

    @Override
    public void onDestroy(){
        Log.d("Stateinfo","onDestroy");
        super.onDestroy();
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir.toString();
    }

    public static Bitmap createBitmapFromPath(String path, int sampleSize){

        // Creating a bitmap from the given path
        Bitmap bitmap = null;
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = sampleSize;
            options.inPurgeable = true;
            bitmap = BitmapFactory.decodeFile(path, options);

        }
        catch(OutOfMemoryError e){
            //Memory error occured.
        }
        return bitmap;
    }

    public void Capture(View view) throws IOException {
        if (!capture_press){
            mCamera.takePicture(null, null, mPicture);
            /*--------------*/
            /*--------------*/
           // BitmapFactory.Options options = new BitmapFactory.Options();
           // options.inSampleSize = 8;
           // File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
           // FileInputStream fis = new FileInputStream(pictureFile.toString());
           // image = BitmapFactory.decodeStream(fis);
            image =  createBitmapFromPath(getSDPath()+"/ocr.jpg", 2);

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(getSDPath(),"eng");
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890QWERTYUIOPASDFGHJKLZXCVBNM");
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{" +
                                                                   "asdgh;:'\"\\|~`xcvbnm,./<>?");
            baseApi.setImage(image);
            //String recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            /*--------------*/
            VinCode.setText(Integer.toString(image.getHeight()));
            VinCode.setVisibility(View.VISIBLE);
        }else {

        }
    }

    //恢复到采集照片的时候
    public void Refresh(View view) {
        Log.d("Restart","Restarting camera");
        VinCode.setVisibility(View.INVISIBLE);
        mCamera.startPreview();
        capture_press = false;
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
    public void surfaceChanged(SurfaceHolder holder,
                                 int format,
                                 int width,
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

        //picturebili = (float)pictureSize.width/(float)width;
        params.setPictureSize(pictureSize.width-40, pictureSize.height-40);
        Log.v("xinli", pictureSize.width +" * "+pictureSize.height);

        params.setPictureFormat(PixelFormat.JPEG);
        mCamera.setParameters(params);
        //mCamera.setDisplayOrientation(90);

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





