package io.palaima.debugdrawer.okhttp;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import io.palaima.debugdrawer.base.DebugModule;

public class OkHttpModule implements DebugModule {

    private static final boolean HAS_OKHTTP;

    static {
        boolean hasDependency;

        try {
            Class.forName("com.squareup.okhttp.OkHttpClient");
            hasDependency = true;
        } catch (ClassNotFoundException e) {
            hasDependency = false;
        }

        HAS_OKHTTP = hasDependency;
    }

    private final OkHttpClient client;

    private TextView okHttpCacheMaxSizeView;

    private TextView okHttpCacheWriteErrorView;

    private TextView okHttpCacheRequestCountView;

    private TextView okHttpCacheNetworkCountView;

    private TextView okHttpCacheHitCountView;

    public OkHttpModule(@NonNull OkHttpClient client) {
        if (!HAS_OKHTTP) {
            throw new RuntimeException("OkHttp dependency is not found");
        }
        this.client = client;
    }

    @NonNull @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.dd_debug_drawer_module_okhttp, parent, false);

        okHttpCacheMaxSizeView = (TextView) view.findViewById(R.id.dd_debug_okhttp_cache_max_size);
        okHttpCacheWriteErrorView = (TextView) view.findViewById(R.id.dd_debug_okhttp_cache_write_error);
        okHttpCacheRequestCountView = (TextView) view.findViewById(R.id.dd_debug_okhttp_cache_request_count);
        okHttpCacheNetworkCountView = (TextView) view.findViewById(R.id.dd_debug_okhttp_cache_network_count);
        okHttpCacheHitCountView = (TextView) view.findViewById(R.id.dd_debug_okhttp_cache_hit_count);

        okHttpCacheMaxSizeView.setText(sizeString(maxSize()));

        refresh();
        return view;
    }



    private void refresh() {
        int writeTotal = writeSuccessCount() + writeAbortCount();
        int percentage = (int) ((1f * writeAbortCount() / writeTotal) * 100);
        okHttpCacheWriteErrorView.setText(
            new StringBuilder().append(writeAbortCount())
                .append(" / ")
                .append(writeTotal)
                .append(" (")
                .append(percentage)
                .append("%)")
                .toString()
        );
        okHttpCacheRequestCountView.setText(String.valueOf(requestCount()));
        okHttpCacheNetworkCountView.setText(String.valueOf(networkCount()));
        okHttpCacheHitCountView.setText(String.valueOf(hitCount()));
    }

    @Override
    public void onOpened() {
        refresh();
    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    private static String sizeString(long bytes) {
        String[] units = new String[] { "B", "KB", "MB", "GB" };
        int unit = 0;
        while (bytes >= 1024) {
            bytes /= 1024;
            unit += 1;
        }
        return bytes + units[unit];
    }

    private long maxSize() {
        return client.getCache().getMaxSize();
    }

    private int writeSuccessCount() {
        return client.getCache().getWriteSuccessCount();
    }

    private int writeAbortCount() {
        return client.getCache().getWriteAbortCount();
    }

    private int requestCount() {
        return client.getCache().getRequestCount();
    }

    private int networkCount() {
        return client.getCache().getNetworkCount();
    }

    private int hitCount() {
        return client.getCache().getHitCount();
    }
}
