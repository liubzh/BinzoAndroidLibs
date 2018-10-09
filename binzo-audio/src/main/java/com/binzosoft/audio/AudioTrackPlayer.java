package com.binzosoft.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.binzosoft.audio.model.Lyric;
import com.binzosoft.audio.model.LyricItem;
import com.binzosoft.audio.model.WavInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AudioTrackPlayer implements AudioTrack.OnPlaybackPositionUpdateListener {

    private String TAG = getClass().getSimpleName();
    private static final int LYRIC_CHECK_FREQUENCY = 100; //ms: 100ms = 1秒十次

    public enum PLAY_MODE {
        SINGLE_SONG,              // 单曲播放完立即停止
        LOOP_SINGLE_SONG,         // 单曲循环
        LOOP_SINGLE_SENTENCE,     // 单句循环
        RANDOM,                   // 随机播放          (需在 Player 外部实现)
        LOOP_LIST,                // 列表循环          (需在 Player 外部实现)
    }
    private PLAY_MODE playMode = PLAY_MODE.SINGLE_SONG; // 默认为 SINGLE_SONG

    private WavInfo audioInfo;
    private WavDecoder audioDecoder;
    private InputStream audioInputStream;

    private int bufferSizeInBytes;
    private AudioTrack mAudioTrack;

    private Lyric lyric;

    private int positionOffset = 0;

    private OnCompletionListener completionListener;
    private OnLyricUpdateListener lyricUpdateListener;
    private OnPositionChangedListener positionChangedListener;

    /**
     * 实现 player 时，将音频结束位置做标记，所以触发此方法标志着整首音频播放完成。
     * @param audioTrack
     */
    @Override
    public void onMarkerReached(AudioTrack audioTrack) {
        Log.i(TAG, "onMarkerReached: " + audioTrack.getNotificationMarkerPosition());
        if (playMode == PLAY_MODE.LOOP_SINGLE_SONG) {
            seekTo(0); // 单曲循环模式再次从头开始播放
        } else if (playMode == PLAY_MODE.SINGLE_SONG) {
            mAudioTrack.setPositionNotificationPeriod(0); // 关闭 PeriodicNotification
            mAudioTrack.stop();
            if (completionListener != null) {
                completionListener.onCompletion(mAudioTrack);
            }
            if (lyricUpdateListener != null) {
                lyricUpdateListener.onLyricUpdate(null);
            }
        }
    }

    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        int position;
        try {
            position = audioTrack.getPlaybackHeadPosition() + positionOffset;
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        //Log.i(TAG, "position: " + position);
        int msec = position / (audioTrack.getSampleRate() / 1000); // ms
        msec = msec - LYRIC_CHECK_FREQUENCY; // 减去歌词检查刷新频率的误差
        //Log.i(TAG, "msec: " + msec + "ms");
        if (positionChangedListener != null) {
            positionChangedListener.onPositionChanged(msec);
        }

        if (lyricUpdateListener != null && lyric != null) {
            LyricItem nextItem = lyric.getNextItem();
            //Log.i(TAG, "nextItem:" + nextItem);
            if (nextItem != null && msec >= nextItem.getTimestamp()) {
                if (playMode == PLAY_MODE.LOOP_SINGLE_SENTENCE) {
                    Log.i(TAG, "loop single sentence");
                    seekTo(lyric.getCurrentItem().getTimestamp());
                } else {
                    lyric.next();
                    Log.i(TAG, "onLyricUpdate: " + lyric.getCurrentItem());
                    lyricUpdateListener.onLyricUpdate(lyric);
                }
            }
        }
    }

    public void setAudioData(InputStream inputStream) throws IOException, WavDecoder.DecoderException {
        Log.i(TAG, "setDataSource()");
        completionListener = null;
        this.audioInputStream = inputStream;
        audioDecoder = new WavDecoder(this.audioInputStream);
        this.audioInfo = audioDecoder.readHeader();

        /*
        bufferSizeInBytes = AudioTrack.getMinBufferSize(audioInfo.getRate(),
                audioInfo.isStereo()? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
                */
        bufferSizeInBytes = audioInfo.getDataSize();

        if (mAudioTrack != null) {
            // 若是复用 AudioTrackPlayer，需释放资源
            mAudioTrack.release();
        }
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                audioInfo.getRate(),// 设置音频数据的采样率
                audioInfo.isStereo() ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,// 设置音频数据块是8位还是16位
                bufferSizeInBytes,
                AudioTrack.MODE_STATIC //AudioTrack.MODE_STREAM // 设置模式类型
        );

        mAudioTrack.setPlaybackPositionUpdateListener(this);
        // 播放到结束位置触发监听：onMarkerReached()，seekTo()之后，需要重新设置这个值，注意偏移量
        mAudioTrack.setNotificationMarkerPosition(getDuration());

        prepare();
    }

    public void setAudioData(String filePath) throws IOException, WavDecoder.DecoderException {
        setAudioData(new FileInputStream(filePath));
    }

    public void setLyricData(InputStream inputStream) {
        Log.i(TAG, "setLyricData()");
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            isr = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(isr);
            lyric = new Lyric(getDuration());
            String str;
            int timestamp;
            String content;
            while ((str = reader.readLine()) != null) {
                //Log.i(TAG, str);
                content = str.substring(str.indexOf("]") + 1).trim();
                //Log.i(TAG, "content: " + content);
                str = str.substring(1, str.indexOf("]"));
                timestamp = (int) (
                        Integer.valueOf(str.substring(0, str.indexOf(":"))) * 60 * 1000 +
                        Double.valueOf(str.substring(str.indexOf(":") + 1)) * 1000
                );
                LyricItem item = new LyricItem(timestamp, content);
                //Log.i(TAG, item.toString());
                lyric.addItem(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLyricData(String filePath) throws IOException {
        setLyricData(new FileInputStream(filePath));
    }

    public int getDuration() {
        if (audioInfo == null) {
            return 0;
        } else {
            return audioInfo.getDataSize() / audioInfo.getChannels() / 2;
        }
    }

    private void prepare() throws IOException {
        byte[] data = new byte[audioInfo.getDataSize()];
        audioInputStream.read(data, 0, data.length);
        mAudioTrack.write(data, 0, data.length);
        audioInputStream.close();
    }

    public void start() {
        Log.i(TAG, "start()");

        // 每隔一段时间触发一次监听：onPeriodicNotification()，仅在歌词有效情况下注册
        int period = audioInfo.getRate() * LYRIC_CHECK_FREQUENCY / 1000;
        mAudioTrack.setPositionNotificationPeriod(period);

        mAudioTrack.play();
        //new WriteThread().start();
    }

    public void pause() {
        Log.i(TAG, "pause()");
        mAudioTrack.pause();
    }

    public void stop() {
        Log.i(TAG, "stop()");
        mAudioTrack.flush();
        mAudioTrack.stop();
    }

    public void release() {
        Log.i(TAG, "release()");
        try {
            if (audioInputStream != null) {
                audioInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        completionListener = null;
        mAudioTrack.release(); // 关闭并释放资源
    }

    /**
     * @return 返回当前是否正在播放
     */
    public boolean isPlaying() {
        boolean playing = mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        Log.i(TAG, "isPlaying() ? " + playing);
        return playing;
    }

    /**
     * 设置播放进度
     * @param msec 参数单位为毫秒，如音频时长为5分钟，设置中间位置：seekTo(5 * 60 * 1000 / 2)
     * @return 执行是否成功。
     */
    public boolean seekTo(int msec) {
        int position = (int)(1l * msec * audioInfo.getRate() / 1000);
        Log.i(TAG, "position: " + position);
        boolean playing = isPlaying();
        mAudioTrack.stop();
        int result = mAudioTrack.setPlaybackHeadPosition(position);
        boolean success = result == AudioTrack.SUCCESS;
        Log.i(TAG, "seekTo: " + msec + ", result: " + result + ", success: " + success);
        if (playing) {
            mAudioTrack.play();
        }
        if (success && lyric != null) {
            positionOffset = position;
            mAudioTrack.setNotificationMarkerPosition(getDuration() - positionOffset);
            lyric.target(msec);
            if (lyricUpdateListener != null) {
                lyricUpdateListener.onLyricUpdate(lyric);
            }
        }
        Log.i(TAG, "target item:" + lyric.getCurrentItem());
        return success;
    }

    /**
     * 根据歌词，快进到下一句歌词的时间戳位置。
     * @return 执行是否成功。
     */
    public boolean seekToNext() {
        if (lyric == null) {
            return false;
        }
        LyricItem nextItem = lyric.getNextItem();
        if (nextItem != null) {
            return seekTo(nextItem.getTimestamp());
        } else {
            return false;
        }
    }

    /**
     * 根据歌词，快退到上一句歌词的时间戳位置
     * @return 执行是否成功。
     */
    public boolean seekToPrevious() {
        if (lyric == null) {
            return false;
        }
        LyricItem previousItem = lyric.getPreviousItem();
        if (previousItem != null) {
            return seekTo(previousItem.getTimestamp());
        } else {
            return false;
        }
    }

    /**
     * 设置循环模式
     * @param mode 参见 @PLAY_MODE
     */
    public void setPlayMode(PLAY_MODE mode) {
        this.playMode = mode;
    }

    public void setOnCompletionListener(OnCompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    public void setOnLyricUpdateListener(OnLyricUpdateListener lyricUpdateListener) {
        this.lyricUpdateListener = lyricUpdateListener;
    }

    public void setOnPositionChangedListener(OnPositionChangedListener positionChangedListener) {
        this.positionChangedListener = positionChangedListener;
    }

    /*
    class WriteThread extends Thread {
        byte[] data = new byte[bufferSizeInBytes];

        @Override
        public void run() {
            try {
                while (true) {
                    if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PAUSED) {
                        audioInputStream.read(data, 0, data.length);
                    } else {
                        break;
                    }
                    mAudioTrack.write(data, 0, data.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */

    public interface OnCompletionListener {
        void onCompletion(AudioTrack audioTrack);
    }

    public interface OnLyricUpdateListener {
        void onLyricUpdate(Lyric lyric);
    }

    public interface OnPositionChangedListener {
        void onPositionChanged(int msec);
    }

}
