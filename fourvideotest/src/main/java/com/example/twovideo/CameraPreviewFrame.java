package com.example.twovideo;

import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;

import com.example.twovideo.util.ImageProcess;

/**
 * Created by liurenyi on 2018/6/6.
 */

public class CameraPreviewFrame implements Camera.PreviewCallback {

    /**
     *
     * @param bytes bytes是从camera传过来的一帧帧的原始数据，格式为yuv格式
     * @param camera 此时正在工作的camera
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (camera == null) {
            return;
        }
        long realtime = SystemClock.elapsedRealtime();
        Log.i("liu", "realtime:" + realtime);
        //Log.i("liu", "camera width:" + camera.getParameters().getPreviewSize().width + ",height:" + camera.getParameters().getPreviewSize().height);
        //Log.i("liu", "---bytes:" + bytes.length);
        camera.addCallbackBuffer(bytes);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //ImageProcess.saveYUV(bytes);
            }
        }).start();
    }
}
