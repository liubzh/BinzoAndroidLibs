package com.binzosoft.lib.exoplayer_demo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private TextView subtitleTextView;
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
            subtitleHandler.doUpdateOnce();
            super.onSeekProcessed();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        subtitleTextView = findViewById(R.id.subtitle);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
         player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        PlayerView playerView = (PlayerView) findViewById(R.id.player_view);
        // Bind the player to the view.
        playerView.setPlayer(player);

        //Uri uri = Uri.parse("/android_asset/test.mp4");
        //Uri uri = Uri.parse("/android_asset/test.m4a");
        Uri uri = Uri.parse("/android_asset/f2.mp4");

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
        subtitleHandler.bindTextView(subtitleTextView);
        try {
            subtitleHandler.loadSrt(getAssets().open("f2.srt"));
        } catch (IOException e) {
            e.printStackTrace();
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
