package com.binzosoft.lib.video.subtitle;

import android.util.Log;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;

public class Player {

    private VideoView videoView;
    private ExoPlayer exoPlayer;

    public Player(VideoView videoView) {
        this.videoView = videoView;
    }

    public Player(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public void pause() {
        if (videoView != null) {
            videoView.pause();
        }
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public void start() {
        if (videoView != null) {
            videoView.start();
        }
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public long getCurrentPosition() {
        long position = 0;
        if (videoView != null) {
            position = videoView.getCurrentPosition();
        }
        if (exoPlayer != null) {
            position = exoPlayer.getCurrentPosition();
        }
        return position;
    }

    public void seekTo(long position) {
        if (videoView != null) {
            if (position > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Integer is to large.");
            } else {
                videoView.seekTo((int) position);
            }
        }
        if (exoPlayer != null) {
            exoPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        if (videoView != null) {
            return videoView.isPlaying();
        }
        if (exoPlayer != null) {
            return exoPlayer.getPlayWhenReady();
        }
        return false;
    }

}
