package com.binzosoft.lib.util_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.binzosoft.lib.util.ToastUtil;

public class ToastActivity extends AppCompatActivity implements
        Button.OnClickListener, CheckBox.OnCheckedChangeListener {

    private CheckBox checkBox;
    private Button button1, button2, button3, button4;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);
        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(this);
        checkBox.setChecked(ToastUtil.isShowToast());
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = findViewById(R.id.button4);
        button4.setOnClickListener(this);
        inflater = getLayoutInflater();
    }

    private String currentTimeMillis() {
        return "currentTimeMillis:" + System.currentTimeMillis();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button1:
                ToastUtil.showShort(this, currentTimeMillis());
                break;
            case R.id.button2:
                ToastUtil.showLong(this, currentTimeMillis());
                break;
            case R.id.button3:
                ToastUtil.showWithIcon(this, currentTimeMillis(),
                        R.mipmap.ic_launcher_round, Toast.LENGTH_LONG);
                break;
            case R.id.button4:
                ToastUtil.showCustom(this, inflater.inflate(R.layout.activity_toast, null),
                        Toast.LENGTH_LONG);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ToastUtil.setShowToast(isChecked);
    }
}
