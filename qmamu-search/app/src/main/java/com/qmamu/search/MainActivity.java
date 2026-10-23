package com.qmamu.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Qmamu Search - a single-screen search utility.
 *
 * The app is the front end for the Qmamu engine: you pick a search engine in the
 * chip row, type a query, and the results always open on the Qmamu search page
 * (the chosen engine is passed along as a parameter so the page can use it).
 */
public class MainActivity extends AppCompatActivity {

    private static final String QMAMU = "https://qmamu.com/search";
    private static final String HOST = "qmamu.com";

    /** id, label - the id is sent to Qmamu as ?engine= */
    private static final String[][] ENGINES = {
            {"qmamu", "Qmamu"},
            {"google", "Google"},
            {"bing", "Bing"},
            {"ddg", "DuckDuckGo"},
            {"brave", "Brave"},
            {"yandex", "Yandex"},
    };

    private WebView webView;
    private SwipeRefreshLayout refresh;
    private LinearLayout offline;
    private LinearLayout enginesRow;
    private EditText input;

    private SharedPreferences prefs;
    private String engine = "qmamu";
    private boolean firstPaintDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("qmamu", MODE_PRIVATE);
        engine = prefs.getString("engine", "qmamu");

        webView = findViewById(R.id.webview);
        refresh = findViewById(R.id.refresh);
        offline = findViewById(R.id.offline);
        enginesRow = findViewById(R.id.engines);
        input = findViewById(R.id.input);

        // --- 2. WebView settings, tuned for a fast first paint --------------------
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        s.setAllowFileAccess(false);
        s.setGeolocationEnabled(false);
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

        // --- search bar + engine chips -------------------------------------------
        buildEngineChips();
        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    return true;
                }
                return false;
            }
        });

        warmUp();

        // Start loading immediately - do not sit on a connectivity probe first.
        offline.setVisibility(View.GONE);
        refresh.setVisibility(View.VISIBLE);
        webView.loadUrl(QMAMU);
    }

    /** The chip row: tap an engine to make it the one sent to Qmamu. */
    private void buildEngineChips() {
        enginesRow.removeAllViews();
        for (String[] e : ENGINES) {
            final String id = e[0];
            final boolean on = id.equals(engine);
            TextView chip = new TextView(this);
            chip.setText(e[1]);
            chip.setTextSize(13f);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(16), dp(9), dp(16), dp(9));
            chip.setTextColor(on ? getResources().getColor(R.color.white)
                    : getResources().getColor(R.color.text_secondary));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(18));
            bg.setColor(on ? getResources().getColor(R.color.brand)
                    : getResources().getColor(R.color.surface));
            chip.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    engine = id;
                    prefs.edit().putString("engine", id).apply();
                    buildEngineChips();
                }
            });
            enginesRow.addView(chip);
        }
    }

    /** Search on the chosen engine, but always land on the Qmamu results page. */
    private void submit() {
        String q = input.getText().toString().trim();
        if (q.isEmpty()) return;
        hideKeyboard();
        input.clearFocus();

        if (looksLikeUrl(q)) {
            webView.loadUrl(q.startsWith("http") ? q : "https://" + q);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.loadUrl(QMAMU + "?q=" + Uri.encode(q) + "&engine=" + Uri.encode(engine));
        }
    }

    private boolean looksLikeUrl(String q) {
        if (q.contains(" ")) return false;
        String h = q;
        int slash = h.indexOf('/');
        if (slash > 0) h = h.substring(0, slash);
        return h.startsWith("http") || h.contains(".");
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
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
