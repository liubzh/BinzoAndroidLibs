package com.binzosoft.lib.exoplayer_demo;

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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.binzosoft.lib.exoplayer.subtitle.SubtitleHandler;
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

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private EditText subtitleTextView1, subtitleTextView2;
    private SubtitleHandler subtitleHandler;
    private SimpleExoPlayer player;

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
            if (playbackState == Player.STATE_READY && playWhenReady) {
                subtitleHandler.startUpdating();
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
            subtitleHandler.updateOnce();
            super.onSeekProcessed();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        subtitleTextView1 = findViewById(R.id.subtitle1);
        setSelectionActionCallback2(subtitleTextView1);
        //subtitleTextView2 = findViewById(R.id.subtitle2);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        PlayerView playerView = (PlayerView) findViewById(R.id.player_view);
        // Bind the player to the view.
        playerView.setPlayer(player);

        //Uri uri = Uri.parse("/android_asset/test.mp4");
        //Uri uri = Uri.parse("/android_asset/test.m4a");
        //Uri uri = Uri.parse("/android_asset/f2.mp4");
        //Uri uri = Uri.parse("/android_asset/test.mp3");
        Uri uri = Uri.parse("file:///sdcard/The Simpsons S01E01 - Simpsons Roasting on an Open Fire.mp4");

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getPackageName()));
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        // Prepare the player with the source.
        player.prepare(videoSource);

        // 播放器事件监听
        player.addListener(eventListener);

        subtitleHandler = new SubtitleHandler(this);
        subtitleHandler.bindPlayer(player);
        try {
            //subtitleHandler.bindSrt(subtitleTextView, getAssets().open("f2.srt"));
            subtitleHandler.bindSrt(subtitleTextView1, "/sdcard/The Simpsons S01E01 - Simpsons Roasting on an Open Fire-en.srt");
            //subtitleHandler.bindSrt(subtitleTextView2, "/sdcard/Movies/LegallyBlonde1.Chs.srt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSelectionActionCallback(TextView textView) {

    }

    private void setSelectionActionCallback2(final EditText editText) {
        ActionMode.Callback2 callback2;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            callback2 = new ActionMode.Callback2() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    /*
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menuInflater.inflate(R.menu.selection_action_google_translate, menu);
                    return true;//返回false则不会显示弹窗
                    */
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    //return false; // 同时显示自定义和系统默认复制、粘贴菜单选项
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menu.clear(); // 清楚系统默认复制、粘贴选项后，只显示自定义菜单选项
                    menuInflater.inflate(R.menu.selection_action_google_translate, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    if (editText == null || !editText.hasSelection()) {
                        return false;
                    }
                    String selection = editText.getText()
                            .subSequence(editText.getSelectionStart(), editText.getSelectionEnd())
                            .toString()
                            .replace("<br>", " ")
                            .replace("\n", " ");

                    //根据item的ID处理点击事件
                    switch (menuItem.getItemId()) {
                        case R.id.GoogleTranslate:
                            actionMode.finish();//收起操作菜单

                            Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.setPackage("com.google.android.apps.translate");
                            intent.setType("text/plain");

                            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, selection);
                            //intent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, selection);

                            startActivity(intent);
                            break;

                        case R.id.SelectAll:
                            editText.selectAll();
                            break;
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
            editText.setCustomSelectionActionModeCallback(callback2);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        subtitleHandler.stopUpdating();
        player.release();
        subtitleHandler.destroy();
        super.onDestroy();
    }
}
