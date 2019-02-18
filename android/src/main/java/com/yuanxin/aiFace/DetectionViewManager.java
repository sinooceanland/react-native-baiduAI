package com.yuanxin.aiFace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.face.stat.Ast;
import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.IDetectStrategy;
import com.baidu.idl.face.platform.IDetectStrategyCallback;
import com.baidu.idl.face.platform.utils.APIUtils;
import com.baidu.idl.face.platform.utils.CameraPreviewUtils;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yuanxin.aiFace.utils.CameraUtils;
import com.yuanxin.aiFace.utils.VolumeUtils;
import com.yuanxin.aiFace.widget.FaceDetectRoundView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static com.baidu.idl.face.platform.FaceStatusEnum.OK;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
/**
 * <p>文件描述：<p>
 * <p>作者：Mike<p>
 * <p>创建时间：2019/1/4<p>
 * <p>更改时间：2019/1/4<p>
 */
public class DetectionViewManager extends SimpleViewManager<RelativeLayout> implements
        SurfaceHolder.Callback,
        Camera.PreviewCallback,
        Camera.ErrorCallback,
        VolumeUtils.VolumeCallback,
        IDetectStrategyCallback,
        LifecycleEventListener {

    private static final String TAG = "Detection";

    private final String ViewName = "DetectionView";

    private ThemedReactContext reactContext;

    // View
    protected RelativeLayout mRootView;
    protected FrameLayout mFrameLayout;
    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mSurfaceHolder;
    protected ImageView mSuccessView;
    protected TextView mTipsTopView;
    protected TextView mTipsBottomView;
    protected FaceDetectRoundView mFaceDetectRoundView;
    // 人脸信息
    protected FaceConfig mFaceConfig;
    protected IDetectStrategy mIDetectStrategy;
    // 显示Size
    private Rect mPreviewRect = new Rect();
    protected int mDisplayWidth = 0;
    protected int mDisplayHeight = 0;
    protected int mSurfaceWidth = 0;
    protected int mSurfaceHeight = 0;
    protected Drawable mTipsIcon;
    // 状态标识
    protected volatile boolean mIsEnableSound = true;
    protected boolean mIsCreateSurface = false;
    protected volatile boolean mIsCompletion = false;
    // 相机
    protected Camera mCamera;
    protected Camera.Parameters mCameraParam;
    protected int mCameraId;
    protected int mPreviewWidth;
    protected int mPreviewHight;
    protected int mPreviewDegree;
    // 监听系统音量广播
    protected BroadcastReceiver mVolumeReceiver;

    @Override
    public String getName() {
        return ViewName;
    }

    public static final int START = 1;
    public static final int STOP = 2;
    public static final int RELOAD = 3;
    public static final int DESTROY = 4;

    @Override
    public
    @Nullable
    Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "start", START,
                "stop", STOP,
                "reload", RELOAD,
                "destroy", DESTROY
        );
    }

    @Override
    public void receiveCommand(RelativeLayout root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case START:
                Log.d(TAG, "START");
                reactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mVolumeReceiver = VolumeUtils.registerVolumeReceiver(reactContext, this);
                if (mTipsTopView != null) {
                    mTipsTopView.setText(R.string.detect_face_in);
                }
                startPreview();
                break;
            case STOP:
                Log.d(TAG, "STOP");
                stopPreview();
                break;
            case RELOAD:
                Log.d(TAG, "RELOAD");
                VolumeUtils.unRegisterVolumeReceiver(reactContext, mVolumeReceiver);
                mVolumeReceiver = null;
                if (mIDetectStrategy != null) {
                    mIDetectStrategy.reset();
                }
                stopPreview();


                onRefreshTipsView(false, "");
                mTipsBottomView.setText("");
                mFaceDetectRoundView.processDrawState(true);
                onRefreshSuccessView(false);


                reactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mVolumeReceiver = VolumeUtils.registerVolumeReceiver(reactContext, this);
                if (mTipsTopView != null) {
                    mTipsTopView.setText(R.string.detect_face_in);
                }
                startPreview();
                break;
            case DESTROY:
                Log.d(TAG, "DESTROY");
                reactContext.removeLifecycleEventListener(this);
                VolumeUtils.unRegisterVolumeReceiver(reactContext, mVolumeReceiver);
                mVolumeReceiver = null;
                if (mIDetectStrategy != null) {
                    mIDetectStrategy.reset();
                }
                stopPreview();
                break;
        }
    }

    @javax.annotation.Nullable
    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "onRecongnizeFinish", MapBuilder.of("registrationName", "onRecongnizeFinish"));
    }


    public void onRecongnizeFinish(boolean isSuccess, String imageData) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("isSuccess", isSuccess);
        event.putString("imgData", imageData);
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                mRootView.getId(),
                "onRecongnizeFinish",
                event);
    }


    @Override
    protected RelativeLayout createViewInstance(ThemedReactContext reactContext) {
        Log.d(TAG, "createViewInstance");
        this.reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
        mRootView = (RelativeLayout) LayoutInflater.from(reactContext).inflate(R.layout.activity_face_detect_v3100, null);

        DisplayMetrics dm = new DisplayMetrics();
        Display display = reactContext.getCurrentActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;

        FaceSDKResSettings.initializeResId();
        mFaceConfig = FaceSDKManager.getInstance().getFaceConfig();

        AudioManager am = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        int vol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        mIsEnableSound = vol > 0 ? mFaceConfig.isSound : false;

        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.detect_surface_layout);

        mSurfaceView = new SurfaceView(reactContext);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setSizeFromLayout();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        int w = mDisplayWidth;
        int h = mDisplayHeight;

        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
                (int) (w * FaceDetectRoundView.SURFACE_RATIO), (int) (h * FaceDetectRoundView.SURFACE_RATIO),
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        mSurfaceView.setLayoutParams(cameraFL);
        mFrameLayout.addView(mSurfaceView);

//        mRootView.findViewById(R.id.detect_close).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        mFaceDetectRoundView = (FaceDetectRoundView) mRootView.findViewById(R.id.detect_face_round);
//        mCloseView = (ImageView) mRootView.findViewById(R.id.detect_close);
//        mSoundView = (ImageView) mRootView.findViewById(R.id.detect_sound);
//        mSoundView.setImageResource(mIsEnableSound ?
//                R.mipmap.ic_enable_sound_ext : R.mipmap.ic_disable_sound_ext);
//        mSoundView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mIsEnableSound = !mIsEnableSound;
//                mSoundView.setImageResource(mIsEnableSound ?
//                        R.mipmap.ic_enable_sound_ext : R.mipmap.ic_disable_sound_ext);
//                if (mIDetectStrategy != null) {
//                    mIDetectStrategy.setDetectStrategySoundEnable(mIsEnableSound);
//                }
//            }
//        });
        mTipsTopView = (TextView) mRootView.findViewById(R.id.detect_top_tips);
        mTipsBottomView = (TextView) mRootView.findViewById(R.id.detect_bottom_tips);
        mSuccessView = (ImageView) mRootView.findViewById(R.id.detect_success_image);
        AndPermission.with(reactContext)
                .permission(Permission.CAMERA)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        try {
                            onHostResume();
                        }
                        catch (Exception ex){
                            Toast.makeText(DetectionViewManager.this.reactContext,"申请相机权限失败，请进入手机设置界面手动授权。",Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                        }
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        Toast.makeText(DetectionViewManager.this.reactContext,"申请相机权限失败，请进入手机设置界面手动授权。",Toast.LENGTH_LONG).show();
                        for (int i=0;i<permissions.size();i++){
                            // Log.e(TAG,"拒绝了权限："+permissions.get(i));
                        }
                    }
                }).start();

        return mRootView;
    }


    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume");

        reactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mVolumeReceiver = VolumeUtils.registerVolumeReceiver(reactContext, this);
        if (mTipsTopView != null) {
            mTipsTopView.setText(R.string.detect_face_in);
        }
        startPreview();
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause");
        stopPreview();
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostPause");
        VolumeUtils.unRegisterVolumeReceiver(reactContext, mVolumeReceiver);
        mVolumeReceiver = null;
        if (mIDetectStrategy != null) {
            mIDetectStrategy.reset();
        }
        stopPreview();
    }


    @Override
    public void volumeChanged() {
        try {
            AudioManager am = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int cv = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                mIsEnableSound = cv > 0;
//                mSoundView.setImageResource(mIsEnableSound
//                        ? R.mipmap.ic_enable_sound_ext : R.mipmap.ic_disable_sound_ext);
                if (mIDetectStrategy != null) {
                    mIDetectStrategy.setDetectStrategySoundEnable(mIsEnableSound);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Camera open() {
        Camera camera;
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            return null;
        }

        int index = 0;
        while (index < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                break;
            }
            index++;
        }

        if (index < numCameras) {
            camera = Camera.open(index);
            mCameraId = index;
        } else {
            camera = Camera.open(0);
            mCameraId = 0;
        }
        return camera;
    }

    protected void startPreview() {
        Log.d(TAG, "startPreview");
        mIsCompletion = false;
        if (mSurfaceView != null && mSurfaceView.getHolder() != null) {
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
        }

        if (mCamera == null) {
            try {
                mCamera = open();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mCamera == null) {
            return;
        }
        if (mCameraParam == null) {
            mCameraParam = mCamera.getParameters();
        }

        mCameraParam.setPictureFormat(PixelFormat.JPEG);
        int degree = displayOrientation(reactContext);
        mCamera.setDisplayOrientation(degree);
        // 设置后无效，camera.setDisplayOrientation方法有效
        mCameraParam.set("rotation", degree);
        mPreviewDegree = degree;
        if (mIDetectStrategy != null) {
            mIDetectStrategy.setPreviewDegree(degree);
        }

        Point point = CameraPreviewUtils.getBestPreview(mCameraParam,
                new Point(mDisplayWidth, mDisplayHeight));
        mPreviewWidth = point.x;
        mPreviewHight = point.y;
        // Preview 768,432
        mPreviewRect.set(0, 0, mPreviewHight, mPreviewWidth);

        mCameraParam.setPreviewSize(mPreviewWidth, mPreviewHight);
        mCamera.setParameters(mCameraParam);

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.stopPreview();
            mCamera.setErrorCallback(this);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (RuntimeException e) {
            e.printStackTrace();
            CameraUtils.releaseCamera(mCamera);
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
            CameraUtils.releaseCamera(mCamera);
            mCamera = null;
        }

    }

    protected void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setErrorCallback(null);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CameraUtils.releaseCamera(mCamera);
                mCamera = null;
            }
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(this);
        }
        if (mIDetectStrategy != null) {
            mIDetectStrategy = null;
        }
    }

    private int displayOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }
        int result = (0 - degrees + 360) % 360;
        if (APIUtils.hasGingerbread()) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsCreateSurface = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format,
                               int width,
                               int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (holder.getSurface() == null) {
            return;
        }
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsCreateSurface = false;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mIsCompletion) {
            return;
        }
        Log.d(TAG,"onPreviewFrame");

        if (mIDetectStrategy == null && mFaceDetectRoundView != null && mFaceDetectRoundView.getRound() > 0) {
            Log.d(TAG,"onPreviewFrame1");
            mIDetectStrategy = FaceSDKManager.getInstance().getDetectStrategyModule();
            mIDetectStrategy.setPreviewDegree(mPreviewDegree);
            mIDetectStrategy.setDetectStrategySoundEnable(mIsEnableSound);

            Rect detectRect = FaceDetectRoundView.getPreviewDetectRect(mDisplayWidth, mPreviewHight, mPreviewWidth);
            mIDetectStrategy.setDetectStrategyConfig(mPreviewRect, detectRect, this);
        }
        if (mIDetectStrategy != null) {
            Log.d(TAG,"onPreviewFrame2");
            mIDetectStrategy.detectStrategy(data);
        }

    }

    @Override
    public void onError(int error, Camera camera) {
    }

    @Override
    public void onDetectCompletion(FaceStatusEnum status, String message,
                                   HashMap<String, String> base64ImageMap) {
        if (mIsCompletion) {
            return;
        }

        onRefreshView(status, message);

        if (status == OK) {
            mIsCompletion = true;
            Set<Map.Entry<String, String>> sets = base64ImageMap.entrySet();
            for (Map.Entry<String, String> entry : sets) {
                onRecongnizeFinish(true, entry.getValue());
            }
        }
        Ast.getInstance().faceHit("detect");
    }

    private void onRefreshView(FaceStatusEnum status, String message) {
        switch (status) {
            case OK:
                onRefreshTipsView(false, message);
                mTipsBottomView.setText("");
                mFaceDetectRoundView.processDrawState(false);
                onRefreshSuccessView(true);
                break;
            case Detect_PitchOutOfUpMaxRange:
            case Detect_PitchOutOfDownMaxRange:
            case Detect_PitchOutOfLeftMaxRange:
            case Detect_PitchOutOfRightMaxRange:
                onRefreshTipsView(true, message);
                mTipsBottomView.setText(message);
                mFaceDetectRoundView.processDrawState(true);
                onRefreshSuccessView(false);
                break;
            default:
                onRefreshTipsView(false, message);
                mTipsBottomView.setText("");
                mFaceDetectRoundView.processDrawState(true);
                onRefreshSuccessView(false);
        }
    }

    private void onRefreshTipsView(boolean isAlert, String message) {
        if (isAlert) {
            if (mTipsIcon == null) {
                mTipsIcon = reactContext.getResources().getDrawable(R.mipmap.ic_warning);
                mTipsIcon.setBounds(0, 0, (int) (mTipsIcon.getMinimumWidth() * 0.7f),
                        (int) (mTipsIcon.getMinimumHeight() * 0.7f));
            }
            mRootView.findViewById(R.id.detect_top_tips_bg).setBackgroundResource(R.drawable.bg_tips);
            ((ImageView) mRootView.findViewById(R.id.detect_top_tips_icon)).setImageResource(R.mipmap.ic_warning);
            ((ImageView) mRootView.findViewById(R.id.detect_top_tips_icon)).setVisibility(View.VISIBLE);
            mTipsTopView.setText(R.string.detect_standard);
        } else {
            mRootView.findViewById(R.id.detect_top_tips_bg).setBackgroundResource(R.drawable.bg_tips_no);
            ((ImageView) mRootView.findViewById(R.id.detect_top_tips_icon)).setImageDrawable(null);
            ((ImageView) mRootView.findViewById(R.id.detect_top_tips_icon)).setVisibility(View.GONE);
            if (!TextUtils.isEmpty(message)) {
                mTipsTopView.setText(message);
            }
        }
    }

    /**
     * 得到显示密度
     *
     * @param context Context
     * @return 密度
     */
    public float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * dip转换成px
     *
     * @param context Context
     * @param dip     dip Value
     * @return 换算后的px值
     */
    public int dip2px(float dip) {
        float density = getDensity(reactContext);
        return (int) (dip * density + 0.5f);
    }

    /**
     * px转换成dip
     *
     * @param context Context
     * @param px      px Value
     * @return 换算后的dip值
     */
    public int px2dip(float px) {
        float density = getDensity(reactContext);
        return (int) (px / density + 0.5f);
    }


    private void onRefreshSuccessView(boolean isShow) {
        if (mSuccessView.getTag() == null) {
            Rect rect = mFaceDetectRoundView.getFaceRoundRect();
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mSuccessView.getLayoutParams();
            rlp.setMargins(
                    rect.centerX() - (mSuccessView.getWidth() / 2),
                    rect.top - (mSuccessView.getHeight() / 2),
                    0,
                    0);
            mSuccessView.setLayoutParams(rlp);
            mSuccessView.setTag("setlayout");
        }
        mSuccessView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        Log.d(TAG, "onCatalystInstanceDestroy");
        reactContext.removeLifecycleEventListener(this);
        VolumeUtils.unRegisterVolumeReceiver(reactContext, mVolumeReceiver);
        mVolumeReceiver = null;
        if (mIDetectStrategy != null) {
            mIDetectStrategy.reset();
        }
        stopPreview();
    }

}
