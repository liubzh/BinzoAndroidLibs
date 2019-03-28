package com.binzosoft.lib.video;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.binzosoft.lib.util.PermissionUtil;
import com.binzosoft.lib.util.UiUtil;
import com.binzosoft.lib.util.media.Metadata;
import com.binzosoft.lib.video.subtitle.SubtitleHandler;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
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
import java.util.ArrayList;

public class ExoPlayerAudioActivity extends AppCompatActivity implements Button.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private LinearLayout subtitlesContainer;
    //private TextView subtitleTextView;
    private ImageButton button1, button2;
    private ImageButton playPauseButton;
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
            if (subtitleHandler != null) {
                if (playWhenReady) {
                    subtitleHandler.startToUpdate();
                    playPauseButton.setActivated(true);
                } else {
                    subtitleHandler.stopUpdating();
                    playPauseButton.setActivated(false);
                }
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

    private void setOrientation() {
        if (videoFile == null) {
            return;
        }
        Metadata metadata = Metadata.retrieve(videoFile.getPath());
        Log.i(TAG, "metadata:" + metadata);
        int width = Integer.valueOf(metadata.getVideoWidth());
        int height = Integer.valueOf(metadata.getVideoHeight());
        double quotient = 1.0 * width / height;
        Log.i(TAG, "quotient:" + quotient);
        if (!UiUtil.isTelevision(this)) {
            // 如果不是电视设备，自动判断横竖屏
            if (quotient > 1) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

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

//        setOrientation();

        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.video_exoplayer_audio_activity);

        subtitlesContainer = findViewById(R.id.video_subtitle_container);
        button1 = findViewById(R.id.video_button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.video_button2);
        button2.setOnClickListener(this);
        playPauseButton = findViewById(R.id.video_play_pause_button);
        playPauseButton.setOnClickListener(this);

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
                .createMediaSource(uri);
        // Prepare the player with the source.
        player.prepare(videoSource);

        // 播放器事件监听
        player.addListener(eventListener);

        /*
        subtitleTextView = findViewById(R.id.video_subtitle_text_layout);
        setSelectionActionCallback2(subtitleTextView);
        //subtitleTextView2 = findViewById(R.id.subtitle2);

        String srtPath = videoFile.getParent() + File.separator +
                videoFile.getName().replace(".k.mp4", ".srt");

        Log.i(TAG, "strPath: " + srtPath);
        srtFile = new File(srtPath);


        if (srtFile != null && srtFile.exists()) {
            subtitleTextView = findViewById(R.id.video_subtitle_text_layout);
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
        */
        try {
            searchSrt(videoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 自动开始播放
        player.setPlayWhenReady(true);
    }

    private void searchSrt(File videoFile) throws IOException {
        String dir = videoFile.getParentFile().getPath();
        String fileName = videoFile.getName();
        if (fileName.endsWith(".k.mp4")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".k.mp4"));
        } else if (fileName.endsWith(".mp4")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".mp4"));
        } else if (fileName.endsWith(".wav")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".wav"));
        } else if (fileName.endsWith(".mp3")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".mp3"));
        }
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        ArrayList<String> srtFiles = new ArrayList<>();
        for (String file : videoFile.getParentFile().list()) {
            if (file.startsWith(fileName) && file.endsWith(".srt")) {
                srtFiles.add(file);
            }
        }
        if (srtFiles.size() == 0) {
            return;
        }
        subtitleHandler = new SubtitleHandler(this);
        subtitleHandler.bindExoPlayer(player);
        for (String srtFileName : srtFiles) {
            String srtPath = dir + File.separator + srtFileName;
            Log.i(TAG, "srtPath:" + srtPath);
            View layout = getLayoutInflater().inflate(R.layout.video_subtitle_text_layout, subtitlesContainer, false);
            subtitlesContainer.addView(layout);
            TextView subtitleTextView = layout.findViewById(R.id.video_subtitle_text);
            subtitleTextView.setTextColor(getResources().getColor(R.color.video_colorPrimaryDark));
            setSelectionActionCallback2(subtitleTextView);
            if (srtFileName.equals(fileName + ".srt")) {
                // 与视频文件名完全匹配的就是主字幕
                subtitleHandler.bindSrt(SubtitleHandler.TYPE_MAIN, subtitleTextView, srtPath);
            } else {
                Log.i(TAG, "not equal");
                subtitleHandler.bindSrt(SubtitleHandler.TYPE_SECONDARY, subtitleTextView, srtPath);
            }
        }
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
                    //MenuInflater menuInflater = actionMode.getMenuInflater();
                    //return true;//返回false则不会显示弹窗
                    for (int i = 0; i < menu.size(); i++) {
                        MenuItem item = menu.getItem(i);
                        String title = item.getTitle().toString();
                        Log.i(TAG, title);
                        if ("Copy".equalsIgnoreCase(title)
                                || "Share".equalsIgnoreCase(title)
                                || "复制".equalsIgnoreCase(title)
                                || "分享".equalsIgnoreCase(title)) {
                            item.setVisible(false);
                        }
                    }
                    //menuInflater.inflate(R.menu.vd_selection_action_translate, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    //return false; // 同时显示自定义和系统默认复制、粘贴菜单选项
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menu.clear(); // 清除系统默认复制、粘贴选项后，只显示自定义菜单选项
                    menuInflater.inflate(R.menu.vd_selection_action_translate, menu);
                    //MenuInflater menuInflater = actionMode.getMenuInflater();
                    //menu.clear(); // 清除系统默认复制、粘贴选项后，只显示自定义菜单选项
//                    for (int i = 0; i < menu.size(); i++) {
//                        Log.i(TAG, menu.getItem(i).getTitle().toString());
//                        menu.removeItem(i);
//                    }
//                    menuInflater.inflate(R.menu.vd_selection_action_translate, menu);
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
            if (subtitleHandler != null) {
                subtitleHandler.previous();
            }
        } else if (id == R.id.video_button2) {
            if (subtitleHandler != null) {
                subtitleHandler.next();
            }
        } else if (id == R.id.video_play_pause_button) {
            if (player == null)
                return;
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
            } else {
                player.setPlayWhenReady(true);
            }
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            Log.i(TAG, "KEYCODE_DPAD_LEFT");
            if (subtitleHandler != null) {
                subtitleHandler.previous();
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            Log.i(TAG, "KEYCODE_DPAD_RIGHT");
            if (subtitleHandler != null) {
                subtitleHandler.next();
            }
//        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
//            Log.i(TAG, "KEYCODE_DPAD_UP");
//        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//            Log.i(TAG, "KEYCODE_DPAD_DOWN");
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Log.i(TAG, "KEYCODE_DPAD_CENTER");
            if (player.getPlayWhenReady()) {
                player.setPlayWhenReady(false);
            } else {
                player.setPlayWhenReady(true);
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
