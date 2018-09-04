package com.example.twovideo;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by liurenyi on 2018/6/6.
 */

public class CameraPreviewFrame implements Camera.PreviewCallback {

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (camera == null) {
            return;
        }
        //Log.i("liu", "---bytes:" + bytes.length);
        camera.addCallbackBuffer(bytes);

    }
}
