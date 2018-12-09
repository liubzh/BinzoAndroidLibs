package com.binzosoft.lib.video.encrypt;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSchemeDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

/**
 * originally copied from DefaultDataSource.
 */
public final class MyDataSource implements DataSource {

    private static final String TAG = "MyDataSource";

    private static final String SCHEME_ASSET = "asset";
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_RTMP = "rtmp";
    private static final String SCHEME_RAW = RawResourceDataSource.RAW_RESOURCE_SCHEME;

    private final Context context;
    private final TransferListener<? super DataSource> listener;

    private final DataSource baseDataSource;

    // Lazily initialized.
    private DataSource fileDataSource;
    private DataSource assetDataSource;
    private DataSource contentDataSource;
    private DataSource rtmpDataSource;
    private DataSource dataSchemeDataSource;
    private DataSource rawResourceDataSource;

    private DataSource dataSource;

    private String encryptKey = "0123456789ABCDEF";
    private String encryptIv = "ABCDEF0123456789";

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param userAgent The User-Agent string that should be used when requesting remote data.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public MyDataSource(Context context, TransferListener<? super DataSource> listener,
                        String userAgent, boolean allowCrossProtocolRedirects) {
        this(context, listener, userAgent, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, allowCrossProtocolRedirects);
    }

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param userAgent The User-Agent string that should be used when requesting remote data.
     * @param connectTimeoutMillis The connection timeout that should be used when requesting remote
     *     data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis The read timeout that should be used when requesting remote data,
     *     in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public MyDataSource(Context context, TransferListener<? super DataSource> listener,
                        String userAgent, int connectTimeoutMillis, int readTimeoutMillis,
                        boolean allowCrossProtocolRedirects) {
        this(context, listener,
                new DefaultHttpDataSource(userAgent, null, listener, connectTimeoutMillis,
                        readTimeoutMillis, allowCrossProtocolRedirects, null));
    }

    /**
     * Constructs a new instance that delegates to a provided {@link DataSource} for URI schemes other
     * than file, asset and content.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param baseDataSource A {@link DataSource} to use for URI schemes other than file, asset and
     *     content. This {@link DataSource} should normally support at least http(s).
     */
    public MyDataSource(Context context, TransferListener<? super DataSource> listener,
                        DataSource baseDataSource) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.baseDataSource = Assertions.checkNotNull(baseDataSource);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Assertions.checkState(dataSource == null);
        // Choose the correct source for the scheme.
        String scheme = dataSpec.uri.getScheme();
        Log.i(TAG, "scheme:" + scheme);
        Log.i(TAG, "uri:" + dataSpec.uri.getPath());
        if (Util.isLocalFileUri(dataSpec.uri)) {
            if (dataSpec.uri.getPath().endsWith(".kmp4")) {
                Log.i(TAG, ".......kmp4");
                dataSource = new Aes128DataSource(getFileDataSource(),
                        encryptKey.getBytes("UTF-8"), // 确保加解密编码一致
                        encryptIv.getBytes("UTF-8"));
            } else {
                if (dataSpec.uri.getPath().startsWith("/android_asset/")) {
                    dataSource = getAssetDataSource();
                } else {
                    dataSource = getFileDataSource();
                }
            }
        } else if (SCHEME_ASSET.equals(scheme)) {
            dataSource = getAssetDataSource();
        } else if (SCHEME_CONTENT.equals(scheme)) {
            dataSource = getContentDataSource();
        } else if (SCHEME_RTMP.equals(scheme)) {
            dataSource = getRtmpDataSource();
        } else if (DataSchemeDataSource.SCHEME_DATA.equals(scheme)) {
            dataSource = getDataSchemeDataSource();
        } else if (SCHEME_RAW.equals(scheme)) {
            dataSource = getRawResourceDataSource();
        } else {
            dataSource = baseDataSource;
        }
        // Open the source and return.
        return dataSource.open(dataSpec);
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return dataSource.read(buffer, offset, readLength);
    }

    @Override
    public Uri getUri() {
        return dataSource == null ? null : dataSource.getUri();
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            try {
                dataSource.close();
            } finally {
                dataSource = null;
            }
        }
    }

    private DataSource getFileDataSource() {
        if (fileDataSource == null) {
            fileDataSource = new FileDataSource(listener);
        }
        return fileDataSource;
    }

    private DataSource getAssetDataSource() {
        if (assetDataSource == null) {
            assetDataSource = new AssetDataSource(context, listener);
        }
        return assetDataSource;
    }

    private DataSource getContentDataSource() {
        if (contentDataSource == null) {
            contentDataSource = new ContentDataSource(context, listener);
        }
        return contentDataSource;
    }

    private DataSource getRtmpDataSource() {
        if (rtmpDataSource == null) {
            try {
                // LINT.IfChange
                Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.rtmp.RtmpDataSource");
                rtmpDataSource = (DataSource) clazz.getConstructor().newInstance();
                // LINT.ThenChange(../../../../../../../../proguard-rules.txt)
            } catch (ClassNotFoundException e) {
                // Expected if the app was built without the RTMP extension.
                Log.w(TAG, "Attempting to play RTMP stream without depending on the RTMP extension");
            } catch (Exception e) {
                // The RTMP extension is present, but instantiation failed.
                throw new RuntimeException("Error instantiating RTMP extension", e);
            }
            if (rtmpDataSource == null) {
                rtmpDataSource = baseDataSource;
            }
        }
        return rtmpDataSource;
    }

    private DataSource getDataSchemeDataSource() {
        if (dataSchemeDataSource == null) {
            dataSchemeDataSource = new DataSchemeDataSource();
        }
        return dataSchemeDataSource;
    }

    private DataSource getRawResourceDataSource() {
        if (rawResourceDataSource == null) {
            rawResourceDataSource = new RawResourceDataSource(context, listener);
        }
        return rawResourceDataSource;
    }
}