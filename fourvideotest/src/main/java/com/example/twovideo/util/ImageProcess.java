package com.example.twovideo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageProcess {

    public static ByteArrayOutputStream baos;

    public static byte[] rawImage;

    public static Bitmap bitmap;

    public static FileOutputStream outputStream;

    public static BufferedOutputStream bufferedOutputStream;

    public static  File h264writer = null;

    public static void saveImage(final byte[] bytes, final Camera camera) {

        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inJustDecodeBounds = true;
        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
        rawImage = baos.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        if (bitmap != null) {
            Log.i("liu", "bitmap is not null!");
            saveBitmap(bitmap);
        } else {
            Log.i("liu", "bitmap is null!");
        }
    }

    private static String generateFileName() {
        return SystemClock.elapsedRealtime() + "";
    }

    public static void saveBitmap(Bitmap mBitmap) {
        File filePic = new File("mnt/sdcard/Image/" + generateFileName() + ".jpg");
        if (!filePic.exists()) {
            filePic.getParentFile().mkdirs();
            try {
                filePic.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePic);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存为yuv原始数据，合成视频文件。使用YUV播放器工具播放
     * @param bytes
     */
    public static void saveYUV(byte[] bytes) {
        try {
            if (h264writer == null) {
                h264writer = new File(Environment.getExternalStorageDirectory(),
                        "hellowrold222.yuv");
                h264writer.createNewFile();
                // 获取FileOutputStream对象
                outputStream = new FileOutputStream(h264writer);
                // 获取BufferedOutputStream对象
                bufferedOutputStream = new BufferedOutputStream(outputStream);
            }
            if (bufferedOutputStream != null) {
                // 往文件所在的缓冲输出流中写byte数据
                bufferedOutputStream.write(bytes);
                // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
                bufferedOutputStream.flush();
                Log.d("hello", "did write h264….");
            }
        } catch (Exception e) {
            Log.d("hello", "write h264 failed….");
        } finally {}

    }

}
