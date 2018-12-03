package com.kinstalk.her.qchat.homework;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.utils.CountlyConstant;

import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.util.AppUtils;

import ly.count.android.sdk.Countly;

public class WorkImageDetailDialogActivity extends Activity implements View.OnTouchListener {

    private String TAG = WorkImageDetailDialogActivity.class.getSimpleName();
    private ScrollView imageScroll;
    private ImageView workImageView;
    private HorizontalScrollView imageScrollH;
    private ImageView workImageViewH;
    private ImageView imageView;
    private ImageButton close;
    private final String mTTS = "双指捏合图片,可以放大查看哦";
    // These matrixes will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix originMatrix = new Matrix();
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static boolean first = true;
    private boolean mNeedPost = false;
    int mode = NONE;
    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    private float originScale = 0f;
    private float oldDist = 1f;
    private float newDist = 1f;
    private float mScaleSize = 5.0f;

    GuidePageDialog guidePageDialog;
    Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);

        initView();
        initData();
        setAutoSwitchLauncher(true);
        if (!(UIHelper.getHomeGuideAlreadyShow(this))) {
            guidePageDialog = new GuidePageDialog(this);
            guidePageDialog.show();
            UIHelper.setHomeGuideAlreadyShow(this);
            playTTSWithContent(mTTS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (guidePageDialog != null && guidePageDialog.isShowing()) {
            guidePageDialog.stopAnimation();
            guidePageDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        first = true;
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }
    }

    public void initView() {

        close = (ImageButton) findViewById(R.id.work_detail_close_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageView = (ImageView) findViewById(R.id.image_work_detail1);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // make the image fit to the center.
        imageView.setOnTouchListener(this);
        Log.d(TAG, "ORIGINAL MATRIX" + imageView.getImageMatrix());
        /*workImageView = (ImageView) findViewById(R.id.image_work_detail);
        workImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/


    }

    public void initData() {
        first = true;
        Intent intent = this.getIntent();
        if (null != intent) {
            String imageUrl = intent.getStringExtra("imageUrl");
            setImageUrl(imageUrl);
        }
    }

    public void isHorizontal(boolean horizontal) {
        if (horizontal) {
            imageScrollH.setVisibility(View.VISIBLE);
            workImageViewH.setVisibility(View.VISIBLE);
        } else {
            imageScroll.setVisibility(View.VISIBLE);
            workImageView.setVisibility(View.VISIBLE);
        }
    }

    public static float dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (dipValue * scale + 0.5f);
    }

    public void setImageUrl(String imageUrl) {

        if (imageUrl.substring(0, 8).equals("/sdcard/")) {
/*            Glide.with(this)
                    .load(imageUrl)
                    .skipMemoryCache(false)
                    .into(imageView);
  */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bm = BitmapFactory.decodeFile(imageUrl, options);
            if (bm == null) {
                Log.e(TAG, "NULL BM");
            }
            if (bm.getWidth() * bm.getHeight() > 0) {
                imageView.setImageBitmap(bm);
            }
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_detail_pic);
        } else {
            Log.d(TAG, "setImageUrl: NO SDCard");
            finish();
        }
    }

    /**
     * 取消自动回到首页方法
     *
     * @param auto true 30s自动回到首页 false 取消自动回到首页
     */
    public void setAutoSwitchLauncher(boolean auto) {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), auto);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private long lastClickTime;

    private boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 600) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 判断触摸时间派发间隔
     */
 /*   @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isFastDoubleClick()) {
                finish();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    */
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        float scale;
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //first finger down only
                savedMatrix.set(view.getImageMatrix());
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: //first finger lifted
            case MotionEvent.ACTION_POINTER_UP: //second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //second finger down
                // calculates the distance between two points where user touched.
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                // minimal distance between both the fingers
                if (oldDist > 5f) {
                    savedMatrix.set(view.getImageMatrix());
                    // sets the mid-point of the straight line between two points where user touched.
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                float locatMatrix[] = new float[12];
                matrix.getValues(locatMatrix);
                if (locatMatrix[0] != 1 && first) {
                    first = false;
                    originMatrix.set(matrix);
                    originScale = locatMatrix[0];
                    Log.d(TAG, "here");
                }
                if (mode == DRAG) {
                    //movement of first finger
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    Log.d(TAG, "movement ");
                } else if (mode == ZOOM) { //pinch zooming
                    newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) {

                        matrix.set(savedMatrix);
                        //thinking I need to play around with this value to limit it**
                        scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        float localMatrix[] = new float[12];
                        matrix.getValues(localMatrix);
                        mScaleSize = localMatrix[0];
                        if ((localMatrix[0] >= originScale && localMatrix[0] < 5 && scale >= 1) || (scale < 1 && localMatrix[0] > originScale)) {
                            Log.d(TAG, "actually zoom scale" + scale + " " + localMatrix[0]);
                        } else if (!first) {
                            mNeedPost = true;
                            float savedLocalMatrix[] = new float[12];
                            savedMatrix.getValues(savedLocalMatrix);
                            matrix.set(savedMatrix);
                            if (mScaleSize > 4.5) {
                                matrix.postScale((4.5f / savedLocalMatrix[0]), 4.5f / savedLocalMatrix[0], mid.x, mid.y);
                                break;
                            } else if (mScaleSize < 1) {
                                matrix.set(originMatrix);
                            }

                            matrix.setValues(savedLocalMatrix);
                            //            return true;
                            //   matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                }
                break;
        }

        // Perform the transformation
        if (oldDist > 1f && newDist > 1f) {
            Log.d(TAG, "setImageMatrix " + oldDist + matrix.toString() + savedMatrix.toString());
            // make the image scalable as a matrix

            view.setScaleType(ImageView.ScaleType.MATRIX);
            view.setImageMatrix(matrix);

        } else {
            Log.d(TAG, "NOT setImageMatrix " + oldDist + matrix.toString() + savedMatrix.toString());
        }
        return true; // indicate event was handled
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void playTTSWithContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
