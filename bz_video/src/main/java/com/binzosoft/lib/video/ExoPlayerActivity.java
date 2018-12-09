package com.binzosoft.lib.video;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.binzosoft.lib.util.PermissionUtil;
import com.binzosoft.lib.video.subtitle.SubtitleHandler;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;

public class ExoPlayerActivity extends AppCompatActivity implements Button.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private TextView subtitleTextView;
    private Button button1, button2;
    private SubtitleHandler subtitleHandler;
    private SimpleExoPlayer player;
    private PlayerView playerView;

    private Uri uri;
    private File videoFile;
    private File srtFile;

    private Player.EventListener eventListener = new Player.DefaultEventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Log.i(TAG, "onTimelineChanged:" + timeline.toString());
            super.onTimelineChanged(timeline, manifest, reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, String.format("onTracksChanged"));
            super.onTracksChanged(trackGroups, trackSelections);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, String.format("onLoadingChanged(%b)", isLoading));
            super.onLoadingChanged(isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, String.format("onPlayerStateChanged(%b,%d)", playWhenReady, playbackState));
            if (playWhenReady) {
                subtitleHandler.startToUpdate();
            } else {
                subtitleHandler.stopUpdating();
            }
            super.onPlayerStateChanged(playWhenReady, playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.i(TAG, String.format("onRepeatModeChanged(%d)", repeatMode));
            super.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.i(TAG, String.format("onShuffleModeEnabledChanged(%b)", shuffleModeEnabled));
            super.onShuffleModeEnabledChanged(shuffleModeEnabled);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, String.format("onPlayerError(%s)", error.getMessage()));
            super.onPlayerError(error);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, String.format("onPositionDiscontinuity(%d)", reason));
            super.onPositionDiscontinuity(reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.i(TAG, String.format("onPlaybackParametersChanged()"));
            super.onPlaybackParametersChanged(playbackParameters);
        }

        @Override
        public void onSeekProcessed() {
            Log.i(TAG, String.format("onSeekProcessed()"));
            player.setPlayWhenReady(false);
            super.onSeekProcessed();
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                Log.i(TAG, "onTouch");
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                } else {
                    player.setPlayWhenReady(true);
                }
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            uri = getIntent().getData();
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                videoFile = new File(uri.getPath());
            }
            Log.i(TAG, "video path: " + videoFile.getPath());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (videoFile == null || !videoFile.exists()) {
            Log.e(TAG, "invalid uri");
            finish();
        }

        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.video_exoplayer_activity);

        button1 = findViewById(R.id.video_button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.video_button2);
        button2.setOnClickListener(this);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        playerView = findViewById(R.id.video_playerView);
        // Bind the player to the view.
        playerView.setPlayer(player);
        playerView.setUseController(false);
        playerView.setOnTouchListener(onTouchListener);

        playerView.setOnClickListener(this);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);

        String title = videoFile.getName();
        if (title.contains(".k.mp4")) {
            title = title.substring(0, title.lastIndexOf(".k.mp4"));
        }
        if (title.contains(" - ")) {
            title = title.substring(title.indexOf(" - ") + 3);
        }
        setTitle(title);

        /* 官方示例的: DefaultDataSourceFactory */
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getPackageName()));
        /* 自定义的 MyDataSourceFactory，可解析加密资源
        DataSource.Factory dataSourceFactory = new MyDataSourceFactory(this,
                Util.getUserAgent(this, getPackageName()));
                 */
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(Mp4Extractor.FACTORY)
                .createMediaSource(uri);
        // Prepare the player with the source.
        player.prepare(videoSource);

        // 播放器事件监听
        player.addListener(eventListener);

        subtitleTextView = findViewById(R.id.video_subtitle_text);
        setSelectionActionCallback2(subtitleTextView);
        //subtitleTextView2 = findViewById(R.id.subtitle2);

        String srtPath = videoFile.getParent() + File.separator +
                videoFile.getName().replace(".k.mp4", ".srt");
        Log.i(TAG, "strPath: " + srtPath);
        srtFile = new File(srtPath);


        if (srtFile != null && srtFile.exists()) {
            subtitleTextView = findViewById(R.id.video_subtitle_text);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                setSelectionActionCallback2(subtitleTextView);
            }
            subtitleHandler = new SubtitleHandler(this);
            subtitleHandler.bindExoPlayer(player);
            //subtitleHandler.setPlayMode(SubtitleHandler.PlayMode.PAUSE_SINGLE_SENTENCE);
            try {
                subtitleHandler.bindSrt(SubtitleHandler.TYPE_MAIN, subtitleTextView,
                        srtFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 自动开始播放
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setSelectionActionCallback(TextView textView) {

    }

    private void setSelectionActionCallback2(final TextView textView) {
        ActionMode.Callback2 callback2;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            callback2 = new ActionMode.Callback2() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    /*
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menuInflater.inflate(R.menu.vd_selection_action_translate, menu);
                    return true;//返回false则不会显示弹窗
                    */
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    //return false; // 同时显示自定义和系统默认复制、粘贴菜单选项
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menu.clear(); // 清除系统默认复制、粘贴选项后，只显示自定义菜单选项
                    menuInflater.inflate(R.menu.vd_selection_action_translate, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    if (textView == null || !textView.hasSelection()) {
                        return false;
                    }
                    String selection = textView.getText()
                            .subSequence(textView.getSelectionStart(), textView.getSelectionEnd())
                            .toString()
                            .replace("<br>", " ")
                            .replace("\n", " ");

                    //根据item的ID处理点击事件
                    int menuItemId = menuItem.getItemId();
                    if (menuItemId == R.id.GoogleTranslate) {
                        actionMode.finish();//收起操作菜单

                        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.setPackage("com.google.android.apps.translate");
                        intent.setType("text/plain");

                        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, selection);
                        //intent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, selection);

                        startActivity(intent);
                    }
                    return false;//返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {

                }

                @Override
                public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                    //可选  用于改变弹出菜单的位置
                    super.onGetContentRect(mode, view, outRect);
                }
            };
            textView.setCustomSelectionActionModeCallback(callback2);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        player.release();
        if (subtitleHandler != null) {
            subtitleHandler.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.i(TAG, "onClick() id=" + id);
        if (id == R.id.video_playerView) {
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
                //subtitleHandler.pause();
            } else {
                player.setPlayWhenReady(true);
                //subtitleHandler.play();
            }
        } else if (id == R.id.video_button1) {
            subtitleHandler.previous();
        } else if (id == R.id.video_button2) {
            subtitleHandler.next();
        }
    }
}
