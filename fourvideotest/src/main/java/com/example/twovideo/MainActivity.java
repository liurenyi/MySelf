package com.example.twovideo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twovideo.ui.SettingsActivity;
import com.example.twovideo.util.PreviewParameter;
import com.example.twovideo.util.RecorderParameter;

import java.util.ArrayList;
import java.util.List;


//readme
/*for T3 Camera
 * 1:support 2 camera preview and recorder
 * 2:support recorder in background
 * {
 * 		startRender
 * 		stopRender
 * }
 * 3:support watemart turn on and turn off
 * {
 * 		mCameraDevice[index].startWaterMark();
 * }
 *
 * 4:support get cvbs status
 * {
 * 		int status = Camera.getCVBSInStatus(index);
 * }
 * 5:support notification when cvbs out or in
 *
 * {
 * 		IntentFilter filter = new IntentFilter("android.hardware.tvd.state.change");
		mReceiver = new Receiver();
		registerReceiver(mReceiver, filter);
 * }
 * 6:support cvbs set brightness and so on
 * {
 * 		mCameraDevice[index].setAnalogInputColor(67, 50, 100); //setting brightness and so on
 * }
 *
 * 6:mIsSupport2Video true:2 camera false:1 camera
 *
 *
 * CSI or usb camera
 *video0
 *video1
 *video2
 *video3
 *
 *
 *CVBS Camera
 *video4
 *video5
 *video6
 *video7
 *
 *If you use sonix camera with two camera video ,please reference
 *android\device\softwinner\common\prebuild\CarVideo\
 *
 *This apk support:
 *camera0 preview (yuv,mjpeg)
 *camera1 recorder (720P or 1080p)
 *
 *
 *
 *
 *
 *
 *
 * */
public class MainActivity extends Activity implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_TEST = true;
    private static final int MAX_NUM_OF_CAMERAS = 2;
    private static final int HIDDEN_CTL_MENU_BAR = 2;
    private static final int UPDATE_RECORD_TIME = 1;
    private static final int UPDATE_RECORD_TIME1 = 3;
    private static final int UPDATE_RECORD_TIME2 = 5;
    private static final int UPDATE_RECORD_TIME3 = 7;
    private VideoService mService = null;
    private LinearLayout mCtrlMenuBar;
    private ImageButton mRecordButton;
    private ImageButton mRecordButton1;
    private ImageButton mRecordButton2;
    private ImageButton mRecordButton3;
    private ImageButton mBackButton;
    private ImageButton mBackButton1;
    private ImageButton mBackButton2;
    private ImageButton mBackButton3;
    private SurfaceTexture mSurfaceTexture0;
    private SurfaceTexture mSurfaceTexture1;
    private SurfaceTexture mSurfaceTexture2; //
    private SurfaceTexture mSurfaceTexture3; //
    private TextView mRecordTime;
    private TextView mRecordTime1;
    private TextView mRecordTime2;
    private TextView mRecordTime3;
    private ImageView icSetting;
    private BroadcastReceiver mReceiver;
    private static final int VIDEO0 = 0;
    private static final int VIDEO1 = 1;
    private static final int VIDEO2 = 2;
    private static final int VIDEO3 = 3;
    private static final int VIDEO4 = 4;
    private static final int VIDEO5 = 5;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;

//    private int cameraid0 = VIDEO4; // First screen ID left lower
//    private int cameraid1 = VIDEO5; // Second screen ID left upper
//    private int cameraid2 = VIDEO6; // Third screen ID right lower
//    private int cameraid3 = VIDEO7; // Fourth screen ID right upper

    private int cameraid0 = VIDEO0; // First screen ID left lower
    private int cameraid1 = VIDEO1; // Second screen ID left upper
    private int cameraid2 = VIDEO2; // Third screen ID right lower
    private int cameraid3 = VIDEO3; // Fourth screen ID right upper


    private static final boolean mIsSupport2Video = true;
    private List<String> permissionList = new ArrayList<>();

    private final Handler mHandler = new MainHandler();

    private NotificationBack background;

    private Intent intent;
    private int REQUEST_CODE = 1;

    // 更新计时数字显示
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_RECORD_TIME: {
                    mRecordTime.setText((String) msg.obj);
                    break;
                }
                case UPDATE_RECORD_TIME1:
                    mRecordTime1.setText((String) msg.obj);
                    break;
                case UPDATE_RECORD_TIME2:
                    mRecordTime2.setText((String) msg.obj);
                    break;
                case UPDATE_RECORD_TIME3:
                    mRecordTime3.setText((String) msg.obj); //接收UPDATE_RECORD_TIME3信号，来更新time的时间显示
                    break;
                case HIDDEN_CTL_MENU_BAR: {
                }
                break;
                default:
                    break;
            }
        }
    }

    // 发送要更新计时数字的信号
    private IVideoCallback.Stub mVideoCallback = new IVideoCallback.Stub() {
        @Override
        public void onUpdateTimes(int index, String times) throws RemoteException {
            // TODO Auto-generated method stub
            mHandler.removeMessages(UPDATE_RECORD_TIME);
            Message message = new Message();
            if (index != mService.isUVCCameraSonix(index)) {
                message.what = UPDATE_RECORD_TIME;
            } else if (index == cameraid0) {
                message.what = UPDATE_RECORD_TIME;
            } else if (index == cameraid1) {
                message.what = UPDATE_RECORD_TIME1;
            } else if (index == cameraid2) {
                message.what = UPDATE_RECORD_TIME2;
            } else if (index == cameraid3) {
                message.what = UPDATE_RECORD_TIME3;
            }

            message.obj = times;
            mHandler.sendMessage(message);
        }
    };

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {

            mService = ((VideoService.LocalBinder) obj).getService();
            Log.d(TAG, "mService=" + mService);
            mService.registerCallback(mVideoCallback);

        }

        public void onServiceDisconnected(ComponentName classname) {

            if (mService != null) {
                mService.unregisterCallback(mVideoCallback);
            }
            mService = null;
        }
    };

    private void bindVideoService() {
        Log.d(TAG, "bindVideoService###############");
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, mVideoServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        if (mVideoServiceConn != null) {
            unbindService(mVideoServiceConn);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onResume ################");
        super.onResume();
        bindVideoService();
        background.deleteNotification();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause ################");
        super.onPause();
        unbindVideoService();
        if (getRecordingState(cameraid0) || getRecordingState(cameraid1) || getRecordingState(cameraid2) || getRecordingState(cameraid3)) {
            background.createNotification(); // 如果应用还在录制，按home键退出之后开启通知，通知用户应用正在后台运行。
        }
    }


    private class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
        private static final String TAG = "CameraErrorCallback";

        @Override
        public void onError(int error, android.hardware.Camera camera) {
            Log.e(TAG, "Got camera error callback. error=" + error);
            if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {

                throw new RuntimeException("Media server died.");
            }
        }
    }

    private OnLayoutChangeListener mLayoutListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                                   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        }
    };

    private boolean getRecordingState(int index) {
        if (mService != null)
            return mService.getRecordingState(index);
        return false;
    }

    /**
     * 开启预览的method，实际开启预览方法在VideoService中
     *
     * @param cameraId
     * @param surfaceTexture
     */
    private void startPreview(int cameraId, SurfaceTexture surfaceTexture) {

        Log.d(TAG, "mService=" + mService + " surfaceTexture=" + surfaceTexture);
        if (mService != null && (surfaceTexture != null)) {
            mService.startPreview(cameraId, surfaceTexture);
        }
    }

    private void stopPreview(int cameraId) {
        if (mService != null) {
            mService.stopPreview(cameraId);
        }
    }

    public void onCtrlMenuBarClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.recordbutton:
                int cameraid = mService.isUVCCameraSonix(cameraid0);

                if (getRecordingState(cameraid)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid);
                        mRecordTime.setVisibility(View.GONE);
                        mRecordButton.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid, mSurfaceTexture0); // todo ???
                        mRecordTime.setVisibility(View.VISIBLE);
                        mRecordButton.setImageResource(R.drawable.pause_select);
                    }

                }
                break;
            case R.id.recordbutton2:
                if (getRecordingState(cameraid1)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid1);
                        mRecordTime1.setVisibility(View.GONE);
                        mRecordButton1.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                        mRecordTime1.setVisibility(View.VISIBLE);
                        mRecordButton1.setImageResource(R.drawable.pause_select);
                    }
                }
                break;
            case R.id.recordbutton3:
                if (getRecordingState(cameraid2)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid2);
                        mRecordTime2.setVisibility(View.GONE);
                        mRecordButton2.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid2, mSurfaceTexture1);
                        mRecordTime2.setVisibility(View.VISIBLE);
                        mRecordButton2.setImageResource(R.drawable.pause_select);
                    }
                }
                break;
            case R.id.recordbutton4:
                if (getRecordingState(cameraid3)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid3);
                        mRecordTime3.setVisibility(View.GONE);
                        mRecordButton3.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid3, mSurfaceTexture1);
                        mRecordTime3.setVisibility(View.VISIBLE);
                        mRecordButton3.setImageResource(R.drawable.pause_select);
                    }
                }
                break;
            case R.id.settingbutton:
                finish();
                break;
            case R.id.settingbutton2:
                finish();
                break;

        }
    }

    private void startVideoService() {
        Log.d(TAG, "#############startVideoService####################");
        Intent intent = new Intent(MainActivity.this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Log.d(TAG, "###########stopVideoService##################");
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        startVideoService();
        initPermission();
        background = new NotificationBack(MainActivity.this);
        mCtrlMenuBar = (LinearLayout) findViewById(R.id.ctrl_menu_bar);
        mRecordButton = (ImageButton) findViewById(R.id.recordbutton);
        mBackButton = (ImageButton) findViewById(R.id.settingbutton);
        mRecordButton1 = (ImageButton) findViewById(R.id.recordbutton2);
        mBackButton1 = (ImageButton) findViewById(R.id.settingbutton2);
        mRecordButton2 = (ImageButton) findViewById(R.id.recordbutton3);
        mBackButton2 = (ImageButton) findViewById(R.id.settingbutton3);
        mRecordButton3 = (ImageButton) findViewById(R.id.recordbutton4);
        mBackButton3 = (ImageButton) findViewById(R.id.settingbutton4);
        mRecordTime = (TextView) findViewById(R.id.recording_time);
        mRecordTime1 = (TextView) findViewById(R.id.recording_time1);
        mRecordTime2 = (TextView) findViewById(R.id.recording_time2);
        mRecordTime3 = (TextView) findViewById(R.id.recording_time3);
        icSetting = (ImageView) findViewById(R.id.ic_settings);
        icSetting.setOnClickListener(this);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.surface2);
        if (!mIsSupport2Video)
            layout.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mBackButton1.setVisibility(View.GONE);
        mBackButton2.setVisibility(View.GONE);
        mBackButton3.setVisibility(View.GONE);
        initVideoView();
        mReceiver = new BroadcastReceiver() { // 广播似乎没有作用

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                Log.d(TAG, "Intent action=" + arg1.getAction());
                int startRecord = arg1.getIntExtra("start", -1);
                int stopRecord = arg1.getIntExtra("stop", -1);
                Log.d(TAG, "startRecord=" + startRecord + " stopRecord=" + stopRecord);
                if (startRecord == 0) {
                    mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 1) {
                    mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 2) {
                    mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);

                    mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 3) {
                    mService.startVideoRecording(cameraid2, mSurfaceTexture2);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                }


                if (stopRecord == 0) {
                    mService.stopVideoRecording(cameraid0);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);
                } else if (stopRecord == 1) {
                    mService.stopVideoRecording(cameraid1);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);
                } else if (stopRecord == 2) {
                    mService.stopVideoRecording(cameraid0);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);

                    mService.stopVideoRecording(cameraid1);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);
                } else if (stopRecord == 3) {
                    mService.stopVideoRecording(cameraid2);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);
                }

            }
        };
        IntentFilter filter = new IntentFilter("com.android.twovideotest");
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ic_settings:
                if (getRecordingState(cameraid0) || getRecordingState(cameraid1) || getRecordingState(cameraid2) || getRecordingState(cameraid3)) {
                    showAlertDialog(getResources().getString(R.string.dialog_alert_title_1), getResources().getString(R.string.dialog_alert_message_1), 1);
                } else {
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            if (requestCode == 1) {
                int previewExtra = data.getIntExtra(PreviewParameter.previewKey, 0);
                int recorderExtra = data.getIntExtra(RecorderParameter.recorderKey, 0);
                Log.d("hahaha", "previewExtra is " + previewExtra + ",recorderExtra is " + recorderExtra);
                initVideoView();
            }
        }
    }

    // 检测自身权限情况，如果无权限，则去请求权限
    private void initPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }

    }

    /**
     * 请求权限的结果method
     *
     * @param requestCode  请求码
     * @param permissions  哪些权限
     * @param grantResults 结果如何，同意还是禁止
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须同意所有的权限APP才能正常使用", Toast.LENGTH_LONG).show();
                            if (DEBUG_TEST) {
                                Log.d("liu", "必须同意所有的权限APP才能正常使用");
                            }
                            finish();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("heeee", "##########onDestroy#############");

        if (mService != null) {
            int cameraid = mService.isUVCCameraSonix(cameraid0);
            if (cameraid == cameraid0) {
                if (!getRecordingState(cameraid0) && !getRecordingState(cameraid1) && !getRecordingState(cameraid2) && !getRecordingState(cameraid3)) {
                    stopVideoService();
                }
            } else {
                if (!getRecordingState(cameraid) && !getRecordingState(cameraid1) && !getRecordingState(cameraid2) && !getRecordingState(cameraid3)) {
                    stopVideoService();
                }
            }
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void initVideoView() {
        TextureView textureView0 = (TextureView) findViewById(R.id.video0);
        textureView0.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "mVideo0 Available surface=" + surface);

                mSurfaceTexture0 = surface;
                if (mService != null) {
                    int cameraid = mService.isUVCCameraSonix(cameraid0);
                    if (DEBUG) {
                        Log.d(TAG, "cameraid0 = " + cameraid0 + " cameraid = " + cameraid);
                    }
                    if (cameraid == cameraid0) {
                        if (getRecordingState(cameraid0)) {
                            mService.startRender(cameraid0, surface);
                            mRecordButton.setImageResource(R.drawable.pause_select);
                            mRecordTime.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid0, surface);
                            mRecordButton.setImageResource(R.drawable.record_select);
                            mRecordTime.setVisibility(View.GONE);
                        }
                    } else {
                        if (getRecordingState(cameraid)) {
                            startPreview(cameraid0, surface);
                            mRecordButton.setImageResource(R.drawable.pause_select);
                            mRecordTime.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid0, surface);
                            mRecordButton.setImageResource(R.drawable.record_select);
                            mRecordTime.setVisibility(View.GONE);
                        }
                    }
                }

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture0 = null;
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
                if (mService.isUVCCameraSonix(cameraid0) == cameraid0) {
                    if (getRecordingState(cameraid0)) {
                        mService.stopRender(cameraid0);
                    } else {
                        mService.stopPreview(cameraid0);
                        mService.closeCamera(cameraid0);
                    }
                } else {
                    mService.stopPreview(cameraid0);
                    mService.closeCamera(cameraid0);
                }

                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        textureView0.addOnLayoutChangeListener(mLayoutListener);

        if (mIsSupport2Video) {
            TextureView textureView1 = (TextureView) findViewById(R.id.video1);
            textureView1.setSurfaceTextureListener(new SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Log.d(TAG, "mVideo1 Available surface=" + surface);
                    mSurfaceTexture1 = surface;
                    if (mService != null) {
                        int cameraid = mService.isUVCCameraSonix(cameraid1); // liurenyi
                        Log.d("liu", "cameraid = " + cameraid + " cameraid1 = " + cameraid1);
                        if (getRecordingState(cameraid1)) {
                            //startVideoRecording();
                            mService.startRender(cameraid1, surface);
                            mRecordButton1.setImageResource(R.drawable.pause_select);
                            mRecordTime1.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid1, surface);
                            mRecordButton1.setImageResource(R.drawable.record_select);
                            mRecordTime1.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    Log.d(TAG, "onSurfaceTexture1 destroy");
                    mSurfaceTexture1 = null;

                    if (getRecordingState(cameraid1)) {
                        mService.stopRender(cameraid1);

                    } else {
                        mService.stopPreview(cameraid1);
                        mService.closeCamera(cameraid1);
                    }

                    return true;
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
            textureView1.addOnLayoutChangeListener(mLayoutListener);
        }

        /**********start 2017-10-24**********/
        TextureView textureView2 = (TextureView) findViewById(R.id.video2);
        textureView2.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "mVideo2 Available surface=" + surface);

                mSurfaceTexture2 = surface;
                if (mService != null) {

                    int cameraid = mService.isUVCCameraSonix(cameraid2);
                    if (cameraid == cameraid2) {
                        if (getRecordingState(cameraid2)) {
                            //startVideoRecording();
                            mService.startRender(cameraid2, surface);
                            mRecordButton2.setImageResource(R.drawable.pause_select);
                            mRecordTime2.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid2, surface);
                            mRecordButton2.setImageResource(R.drawable.record_select);
                            mRecordTime2.setVisibility(View.GONE);
                        }
                    } else {
                        if (getRecordingState(cameraid)) {
                            //startVideoRecording();
                            startPreview(cameraid2, surface);
                            //mService.startRender(cameraid0, surface);
                            mRecordButton2.setImageResource(R.drawable.pause_select);
                            mRecordTime2.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid2, surface);
                            mRecordButton2.setImageResource(R.drawable.record_select);
                            mRecordTime2.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture0 = null;
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
                if (mService.isUVCCameraSonix(cameraid2) == cameraid2) {
                    if (getRecordingState(cameraid2)) {
                        mService.stopRender(cameraid2);
                    } else {
                        mService.stopPreview(cameraid2);
                        mService.closeCamera(cameraid2);
                    }
                } else {
                    mService.stopPreview(cameraid2);
                    mService.closeCamera(cameraid2);
                }
                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        textureView2.addOnLayoutChangeListener(mLayoutListener);
        /**********end 2017-10-24**********/

        /**********start**********/
        TextureView textureView3 = (TextureView) findViewById(R.id.video3);
        textureView3.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "mVideo3 Available surface=" + surface);

                mSurfaceTexture3 = surface;
                if (mService != null) {
                    int cameraid = mService.isUVCCameraSonix(cameraid3);
                    if (cameraid == cameraid3) {
                        if (getRecordingState(cameraid3)) {
                            //startVideoRecording();
                            mService.startRender(cameraid3, surface);
                            mRecordButton3.setImageResource(R.drawable.pause_select);
                            mRecordTime3.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid3, surface);
                            mRecordButton3.setImageResource(R.drawable.record_select);
                            mRecordTime3.setVisibility(View.GONE);
                        }
                    } else {
                        if (getRecordingState(cameraid)) {
                            //startVideoRecording();
                            startPreview(cameraid3, surface);
                            //mService.startRender(cameraid0, surface);
                            mRecordButton3.setImageResource(R.drawable.pause_select);
                            mRecordTime3.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid3, surface);
                            mRecordButton3.setImageResource(R.drawable.record_select);
                            mRecordTime3.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture0 = null;
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
                if (mService.isUVCCameraSonix(cameraid3) == cameraid3) {
                    if (getRecordingState(cameraid3)) {
                        mService.stopRender(cameraid3);
                    } else {
                        mService.stopPreview(cameraid3);
                        mService.closeCamera(cameraid3);
                    }
                } else {
                    mService.stopPreview(cameraid3);
                    mService.closeCamera(cameraid3);
                }

                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        textureView3.addOnLayoutChangeListener(mLayoutListener);
        /**********end**********/
    }

    @Override
    public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onError(MediaRecorder arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBackPressed() {
        if (!getRecordingState(cameraid0) && !getRecordingState(cameraid1) && !getRecordingState(cameraid2) && !getRecordingState(cameraid3)) {
            stopVideoService();
            finish();
        } else {
            showAlertDialog(getResources().getString(R.string.dialog_alert_title), getResources().getString(R.string.dialog_alert_message), 0);
        }
    }

    private void showAlertDialog(String title, String message, final int type) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.dialog_alert_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getRecordingState(cameraid0)) {
                            if (mService != null) {
                                mService.stopVideoRecording(cameraid0);
                                mRecordTime.setVisibility(View.GONE);
                                mRecordButton.setImageResource(R.drawable.record_select);
                            }
                        }
                        if (getRecordingState(cameraid1)) {
                            if (mService != null) {
                                mService.stopVideoRecording(cameraid1);
                                mRecordTime1.setVisibility(View.GONE);
                                mRecordButton1.setImageResource(R.drawable.record_select);
                            }
                        }
                        if (getRecordingState(cameraid2)) {
                            if (mService != null) {
                                mService.stopVideoRecording(cameraid2);
                                mRecordTime2.setVisibility(View.GONE);
                                mRecordButton2.setImageResource(R.drawable.record_select);
                            }
                        }
                        if (getRecordingState(cameraid3)) {
                            if (mService != null) {
                                mService.stopVideoRecording(cameraid3);
                                mRecordTime3.setVisibility(View.GONE);
                                mRecordButton3.setImageResource(R.drawable.record_select);
                            }
                        }
                        if (type == 0) {
                            finish();
                        } else if (type == 1) {
                            intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            unbindVideoService();
                            finish();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_alert_button_cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }
}