package com.qmamu.search;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Qmamu Search - a single-screen search utility, tuned to open as fast as
 * possible.
 *
 * Speed decisions, in the order they matter:
 *  - the page load starts in onCreate without waiting on a connectivity probe
 *  - the first paint comes from the WebView cache when there is one
 *  - the host name is resolved on a background thread while the WebView boots
 *  - the WebView is never recreated on rotation (see the manifest)
 */
public class MainActivity extends AppCompatActivity {

    private static final String HOME = "https://qmamu.com/search";
    private static final String HOST = "qmamu.com";

    private WebView webView;
    private SwipeRefreshLayout refresh;
    private LinearLayout offline;
    private boolean firstPaintDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        refresh = findViewById(R.id.refresh);
        offline = findViewById(R.id.offline);

        // --- 2. WebView settings -------------------------------------------------
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        // Paint the cached copy instantly on later launches. After the first page
        // has rendered we switch to LOAD_DEFAULT so every search is fresh.
        s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        s.setAllowFileAccess(false);
        s.setGeolocationEnabled(false);
        s.setBlockNetworkImage(false);
        s.setLoadsImagesAutomatically(true);

        webView.setBackgroundColor(getResources().getColor(R.color.bg));

        // --- 3. In-app navigation + 5/6. spinner + offline hooks -----------------
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true; // never hand off to an external browser
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                refresh.setRefreshing(false); // 5. stop the spinner
                if (!firstPaintDone) {
                    firstPaintDone = true;
                    // The shell is on screen: from here on always hit the network.
                    view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                refresh.setRefreshing(false);
                if (request.isForMainFrame()) showOffline(); // 6. main frame failed
            }
        });

        // --- 5. Pull-to-refresh --------------------------------------------------
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                webView.reload();
            }
        });

        // --- 6. Retry ------------------------------------------------------------
        Button retry = findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadIfOnline();
            }
        });

        // Resolve the host while the engine boots so the first connection is quick.
        warmUp();

        // Start loading immediately - do not sit on a connectivity probe first.
        offline.setVisibility(View.GONE);
        refresh.setVisibility(View.VISIBLE);
        webView.loadUrl(HOME);
    }

    /** Warm the DNS cache off the main thread. */
    private void warmUp() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.InetAddress.getByName(HOST);
                } catch (Exception ignored) {
                }
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /** 6. Connectivity check - modern path, with a legacy fallback below API 23. */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /** Used by Retry: reload the page, or show the offline screen again. */
    private void loadIfOnline() {
        if (isOnline()) {
            offline.setVisibility(View.GONE);
            refresh.setVisibility(View.VISIBLE);
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.reload();
        } else {
            showOffline();
        }
    }

    private void showOffline() {
        refresh.setVisibility(View.GONE);
        offline.setVisibility(View.VISIBLE);
    }

    /** 4. Back - walk the history first, then leave the app. */
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
