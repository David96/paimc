package me.bionicsheep.paimc;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import me.bionicsheep.paimc.R;
import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class TriggerService extends Service{

    ImageView detectorArea, background;
    WindowManager wm;
    WindowManager.LayoutParams tparams, bparams;
    int twidth, theight;
    int bwidth, bheight;

    Display display;
    int displayWidth, displayHeight;

    boolean scanning = true;
    int dragY;
    int shadow_threshold;

    Service currentActivity = this;
    Handler handler;
    Toast toast;

    Canvas canvas;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        detectorArea = new ImageView(this);
        background = new ImageView(this);

        handler = new Handler();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        getDisplaySize();

        startTrigger();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private OnTouchListener triggerTouchListener = new OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                startBackground();
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                scanning = true;
                wm.removeView(background);
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                dragY = (int) -event.getY();
                if(scanning && dragY > shadow_threshold){
                    scanning = false;
                    fadeToDark();
                }
            }
            return true;
        }
    };

    @Override
    public void onDestroy(){
        if(detectorArea != null) {
            wm.removeView(detectorArea);
        }
        super.onDestroy();
    }

    private void fadeToDark(){
        (new Thread(){
            int n = 0;
            public void run(){

                for(n = 0; n < 175; n++){
                    try{
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                background.setBackgroundColor(Color.argb(n, n, 0, 0));
                            }
                        });
                        sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Called if rotation or something like that has changed
        getDisplaySize();
    }

    private void getDisplaySize() {
        int sysui = detectorArea.getSystemUiVisibility();
        Point size = new Point();
        if ((sysui & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
            display.getRealSize(size);
        } else {
            display.getSize(size);
        }
        displayWidth = size.x;
        displayHeight = size.y;
    }

    private void startTrigger(){
        detectorArea.setImageResource(R.drawable.detector);
        detectorArea.setOnTouchListener(triggerTouchListener);
        detectorArea.setScaleType(ImageView.ScaleType.FIT_XY);
        detectorArea.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int sysui) {
                getDisplaySize();
            }
        });
        twidth = displayWidth / 2;
        theight = 10;
        shadow_threshold = displayHeight / 3;

        tparams = new WindowManager.LayoutParams(
                twidth,
                theight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
                );

        tparams.gravity = Gravity.CENTER | Gravity.BOTTOM;

        wm.addView(detectorArea, tparams);
    }

    private void startBackground(){
        bwidth = displayWidth;
        bheight = displayHeight;
        Log.d("display","height " + displayHeight);

        bparams = new WindowManager.LayoutParams(
                bwidth,
                bheight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT
                );

        bparams.gravity = Gravity.CENTER | Gravity.BOTTOM;

        wm.addView(background, bparams);
        background.setBackgroundColor(Color.argb(0, 0, 0, 0));
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

}