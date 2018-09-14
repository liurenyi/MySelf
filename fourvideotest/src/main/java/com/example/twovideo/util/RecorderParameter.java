package com.example.twovideo.util;

import android.media.MediaRecorder;

import com.example.twovideo.VideoStorage;

/**
 * recorder录制视频的一些默认参数
 */
public class RecorderParameter {

    public static int mOutFormat3GP = MediaRecorder.OutputFormat.THREE_GPP; //控制视频文件的编码格式

    public static int mOutFormatMP4 = MediaRecorder.OutputFormat.MPEG_4; //控制视频文件的编码格式

    public static int mFrameRate = 25; //控制视频文件的帧率

    public static int mRecorderBitRate = 3 * 1024 * 1024; //控制视频文件的大小

    public static int mDefaultWidth = 1920; // 默认设置的宽度

    public static int mDefaultHeight = 1080; // 默认设置的高度

    public static byte[] buffer = new byte[mDefaultHeight * mDefaultWidth * 3 / 2];

    public static String folderName = "RecordVideo";

    public static String recorderKey = "recorder_size";

}
