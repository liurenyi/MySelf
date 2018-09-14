package com.example.twovideo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.twovideo.MainActivity;
import com.example.twovideo.R;
import com.example.twovideo.util.PreviewParameter;
import com.example.twovideo.util.RecorderParameter;
import com.example.twovideo.util.SharedUtil;

public class SettingsActivity extends Activity {

    private Context mContext = SettingsActivity.this;

    private Spinner previewSize;
    private Spinner recorderSize;
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> recorederAdapter;
    private String[] previewSizes;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        previewSizes = getResources().getStringArray(R.array.camera_preview_size);
        previewSize = (Spinner) this.findViewById(R.id.preview_size);
        recorderSize = (Spinner) this.findViewById(R.id.recorder_size);
        initData();
    }

    private void initData() {
        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, previewSizes);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        previewSize.setAdapter(adapter);
        //添加事件Spinner事件监听
        previewSize.setOnItemSelectedListener(new SpinnerSelectedListener());
        //设置默认值
        previewSize.setVisibility(View.VISIBLE);
        int selectedValue = SharedUtil.getShared(mContext, PreviewParameter.previewKey, 0);
        previewSize.setSelection(selectedValue, true);

        recorederAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, previewSizes);
        //设置下拉列表的风格
        recorederAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        recorderSize.setAdapter(recorederAdapter);
        //添加事件Spinner事件监听
        recorderSize.setOnItemSelectedListener(new SpinnerSelectedListener1());
        //设置默认值
        recorderSize.setVisibility(View.VISIBLE);
        int recorderValue = SharedUtil.getShared(mContext, RecorderParameter.recorderKey, 0);
        recorderSize.setSelection(recorderValue, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            SharedUtil.saveShared(mContext, PreviewParameter.previewKey, arg2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    //使用数组形式操作
    class SpinnerSelectedListener1 implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            SharedUtil.saveShared(mContext, RecorderParameter.recorderKey, arg2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Override
    public void onBackPressed() {
        int shared = SharedUtil.getShared(mContext, PreviewParameter.previewKey, 0);
        int shared1 = SharedUtil.getShared(mContext, RecorderParameter.recorderKey, 0);
        intent = new Intent();
        Log.i("hahaha", "shared is " + shared + ",shared1 is " + shared1);
        intent.putExtra(PreviewParameter.previewKey, shared);
        intent.putExtra(RecorderParameter.recorderKey, shared1);
        setResult(2, intent);
        finish();
    }
}
