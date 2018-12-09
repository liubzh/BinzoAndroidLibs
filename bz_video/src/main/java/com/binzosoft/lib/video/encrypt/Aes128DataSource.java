package com.binzosoft.lib.video.encrypt;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.util.Assertions;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Aes128DataSource implements DataSource {
    private final DataSource upstream;
    private final byte[] encryptionKey;
    private final byte[] encryptionIv;
    @Nullable
    private CipherInputStream cipherInputStream;

    public Aes128DataSource(DataSource upstream, byte[] encryptionKey, byte[] encryptionIv) {
        this.upstream = upstream;
        this.encryptionKey = encryptionKey;
        this.encryptionIv = encryptionIv;
    }

    public final long open(DataSpec dataSpec) throws IOException {
        Cipher cipher;
        try {
            cipher = this.getCipherInstance();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new RuntimeException(var7);
        }

        Key cipherKey = new SecretKeySpec(this.encryptionKey, "AES");
        IvParameterSpec cipherIV = new IvParameterSpec(this.encryptionIv);

        try {
            cipher.init(2, cipherKey, cipherIV);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var6) {
            throw new RuntimeException(var6);
        }

        this.cipherInputStream = new CipherInputStream(
                new DataSourceInputStream(this.upstream, dataSpec),
                cipher);

        return -1L;
    }

    public final int read(byte[] buffer, int offset, int readLength) throws IOException {
        Assertions.checkNotNull(this.cipherInputStream);
        int bytesRead = this.cipherInputStream.read(buffer, offset, readLength);
        return bytesRead < 0 ? -1 : bytesRead;
    }

    @Nullable
    public final Uri getUri() {
        return this.upstream.getUri();
    }

    public void close() throws IOException {
        if (this.cipherInputStream != null) {
            this.cipherInputStream = null;
            this.upstream.close();
        }

    }

    protected Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }
}
