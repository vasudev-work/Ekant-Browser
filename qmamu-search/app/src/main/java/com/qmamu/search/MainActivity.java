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
 * Qmamu Search - a single-screen search utility.
 *
 * The numbered comments map each block to the build prompt.
 */
public class MainActivity extends AppCompatActivity {

    private static final String HOME = "https://qmamu.com/search";

    private WebView webView;
    private SwipeRefreshLayout refresh;
    private LinearLayout offline;

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
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

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

        loadIfOnline();
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

    /** Load the search page, or show the offline screen when there is no network. */
    private void loadIfOnline() {
        if (isOnline()) {
            offline.setVisibility(View.GONE);
            refresh.setVisibility(View.VISIBLE);
            if (webView.getUrl() == null) {
                webView.loadUrl(HOME);
            } else {
                webView.reload();
            }
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
}
