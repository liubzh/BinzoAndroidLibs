package com.binzosoft.audio;

import android.util.Log;

import com.binzosoft.audio.model.WavInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavDecoder {

    private static final String RIFF_HEADER = "RIFF";
    private static final String WAVE_HEADER = "WAVE";
    private static final String FMT_HEADER = "fmt ";
    private static final String DATA_HEADER = "data";

    public static final int HEADER_SIZE = 44;

    private static final String CHARSET = "ASCII";

    public static final String TAG = "WAV";

    private InputStream wavStream;

    public WavDecoder(InputStream wavStream) {
        this.wavStream = wavStream;
    }

    public void checkFormat(boolean condition, String message) throws DecoderException {
        // 不满足条件抛出异常
        if (!condition) {
            throw new DecoderException(message);
        }
    }

    public WavInfo readHeader() throws IOException, DecoderException {

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();
        Log.i(TAG, "format: " + format);
        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM

        int channels = buffer.getShort();
        Log.i(TAG, "channels: " + channels);
        checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);

        int rate = buffer.getInt();
        Log.i(TAG, "rate: " + rate);
        checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);

        buffer.position(buffer.position() + 6);
        int bits = buffer.getShort();
        Log.i(TAG, "bits: " + bits);
        checkFormat(bits == 16, "Unsupported bits: " + bits);

        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            Log.d(TAG, "Skipping non-data chunk");
            int size = buffer.getInt();
            wavStream.skip(size);

            buffer.rewind();
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        Log.i(TAG, "dataSize: " + dataSize);
        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(rate, channels, dataSize);
    }

    public byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {
        byte[] data = new byte[info.getDataSize()];
        stream.read(data, 0, data.length);
        return data;
    }

    public class DecoderException extends Exception {

        public DecoderException(String message) {
            super(message);
        }

    }

}
