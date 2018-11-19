package com.binzosoft.lib.exoplayer_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.binzosoft.lib.exoplayer.subtitle.Caption;
import com.binzosoft.lib.exoplayer.subtitle.FormatSRT;
import com.binzosoft.lib.exoplayer.subtitle.TimedTextObject;

import java.io.FileInputStream;
import java.io.IOException;

public class SubtitleActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle);
        textView = findViewById(R.id.text);

        try {
            TimedTextObject subtitle = new FormatSRT().parseFile("test",
                    getAssets().open("test.srt"));
            StringBuffer sb = new StringBuffer();
            for (Integer key : subtitle.captions.keySet()) {
                Caption caption = subtitle.captions.get(key);
                sb.append(key).append("\n")
                        .append("start:").append(caption.start).append("\n")
                        .append("end:").append(caption.end).append("\n")
                        .append("rawContent:").append(caption.rawContent).append("\n")
                        .append("content:").append(caption.content).append("\n")
                        .append("region:").append(caption.region).append("\n")
                        .append("style:").append(caption.style).append("\n");
            }
            textView.setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
