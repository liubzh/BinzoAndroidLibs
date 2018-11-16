/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.binzosoft.lib.exoplayer;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * copied from ZipDataSource.java of ExoPlayer library.
 */
public class ZipDataSource implements DataSource {

    private final String TAG = "ZipDataSource";

//    private final DataSource upstream;
//    private final byte[] encryptionKey;
//    private final byte[] encryptionIv;

    // Added. begin
    private Uri uri;
    private ZipFile zipFile;
    private String entryName;
    private InputStream entryInputStream;
    // Added. end

//    private @Nullable CipherInputStream cipherInputStream;

    /**
     * @param upstream The upstream {@link DataSource}.
     * @param encryptionKey The encryption key.
     * @param encryptionIv The encryption initialization vector.
     */
    public ZipDataSource(/*DataSource upstream, byte[] encryptionKey, byte[] encryptionIv,*/
                         String entryName) {
//        this.upstream = upstream;
//        this.encryptionKey = encryptionKey;
//        this.encryptionIv = encryptionIv;
        this.entryName = entryName;
    }

    @Override
    public final long open(DataSpec dataSpec) throws IOException {
//        Cipher cipher;
//        try {
//            cipher = getCipherInstance();
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            throw new RuntimeException(e);
//        }
//
//        Key cipherKey = new SecretKeySpec(encryptionKey, "AES");
//        AlgorithmParameterSpec cipherIV = new IvParameterSpec(encryptionIv);
//
//        try {
//            cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherIV);
//        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//            throw new RuntimeException(e);
//        }
        uri = dataSpec.uri;
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }
        String scheme = dataSpec.uri.getScheme();
        String path = getUri().getPath();
        if ("file".equals(scheme)) {
            path = path.replace("file://", "");
            Log.i(TAG, "path=" + path);
            //DataSourceInputStream inputStream = new DataSourceInputStream(upstream, dataSpec);
            //zipInputStream = new ZipInputStream(inputStream);
            zipFile = new ZipFile(path);
            entryInputStream = zipFile.getInputStream(zipFile.getEntry(entryName));
        } else {
            throw new IllegalArgumentException("uri is not supported");
        }

//        cipherInputStream = new CipherInputStream(inputStream, cipher);
//        inputStream.open();

        return C.LENGTH_UNSET;
    }

    @Override
    public final int read(byte[] buffer, int offset, int readLength) throws IOException {
        Assertions.checkNotNull(entryInputStream);
        int bytesRead = entryInputStream.read(buffer, offset, readLength);
        if (bytesRead < 0) {
            return C.RESULT_END_OF_INPUT;
        }
        return bytesRead;
    }

    @Override
    public final @Nullable Uri getUri() {
//        return upstream.getUri();
        return uri;
    }

    @Override
    public void close() throws IOException {
//        if (cipherInputStream != null) {
//            cipherInputStream = null;
//            upstream.close();
//        }
        if (entryInputStream != null) {
            entryInputStream.close();
        }
        if (zipFile != null) {
            zipFile.close();
        }
    }

//    protected Cipher getCipherInstance() throws NoSuchPaddingException, NoSuchAlgorithmException {
//        return Cipher.getInstance("AES/CBC/PKCS7Padding");
//    }
}
