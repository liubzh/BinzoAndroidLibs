package com.binzosoft.lib.video.encrypt;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

public class MyDataSourceFactory implements DataSource.Factory {

    private final Context context;
    private final TransferListener<? super DataSource> listener;
    private final DataSource.Factory baseDataSourceFactory;

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     */
    public MyDataSourceFactory(Context context, String userAgent) {
        this(context, userAgent, null);
    }

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     * @param listener An optional listener.
     */
    public MyDataSourceFactory(Context context, String userAgent,
                                    TransferListener<? super DataSource> listener) {
        this(context, listener, new DefaultHttpDataSourceFactory(userAgent, listener));
    }

    /**
     * @param context A context.
     * @param listener An optional listener.
     * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
     *     for {@link MyDataSource}.
     * @see MyDataSource#MyDataSource(Context, TransferListener, DataSource)
     */
    public MyDataSourceFactory(Context context, TransferListener<? super DataSource> listener,
                                    DataSource.Factory baseDataSourceFactory) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.baseDataSourceFactory = baseDataSourceFactory;
    }

    @Override
    public MyDataSource createDataSource() {
        return new MyDataSource(context, listener, baseDataSourceFactory.createDataSource());
    }

}
