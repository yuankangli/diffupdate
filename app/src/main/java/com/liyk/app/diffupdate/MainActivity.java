package com.liyk.app.diffupdate;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.liyk.app.diffupdate.listener.OnToastListener;
import com.liyk.app.diffupdate.network.GetUpdateInfo;
import com.liyk.app.diffupdate.util.APKVersionCodeUtils;
import com.liyk.app.diffupdate.util.UpdateUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;

    private TextView updateLog;

    private int lineIndex = 1;

    private OnToastListener onToastListener = new OnToastListener() {
        @Override
        public void showToastMessage(String message) {
            runOnUiThread(() -> {
                updateLog.setText(updateLog.getText() + (lineIndex + ". ") + message + "\r\n");
                lineIndex++;
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }

    private void initView() {
        Button updateBtn = findViewById(R.id.update_btn);
        updateBtn.setOnClickListener(this);
        TextView versionTxt = findViewById(R.id.version_txt);
        versionTxt.setText("当前版本号: v" + APKVersionCodeUtils.getVerName(mContext));
        updateLog = findViewById(R.id.update_log);
        UpdateUtil.setOnToastListener(onToastListener);
        GetUpdateInfo.setOnToastListener(onToastListener);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.update_btn) {
            updateLog.setText("");
            lineIndex = 1;
            /**
             * 参数说明详见注释
             */
            UpdateUtil.updateApp(mContext, true);
        }
    }


}