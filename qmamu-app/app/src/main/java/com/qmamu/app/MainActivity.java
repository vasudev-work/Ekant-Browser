package com.qmamu.app;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Qmamu - the meta-search frontend as an app.
 *
 * The whole page is bundled in assets/www, so the interface opens instantly and
 * does not depend on any site being up. Only the results come from the network:
 * the page calls the Qmamu backend, whose address is injected into index.html
 * when it is served (and can be changed from the page's gear button).
 */
public class MainActivity extends AppCompatActivity {

    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final String START = ORIGIN + "/assets/www/index.html";
    private static final String DEFAULT_API = "https://qmamu.com/api/search";

    private WebView webView;
    private LinearLayout setup;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setup = findViewById(R.id.setup);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // the page stores the backend address here
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setAllowFileAccess(false);
        webView.setBackgroundColor(getResources().getColor(R.color.bg));

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String path = request.getUrl().getPath();
                if (path != null && path.endsWith("/www/index.html")) {
                    WebResourceResponse injected = indexWithApi();
                    if (injected != null) return injected;
                }
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (u.toString().startsWith(ORIGIN)) {
                    view.loadUrl(u.toString());
                    return true;
                }
                return false;   // anything else goes to the system browser
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && firstLoad) showSetup();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                firstLoad = false;
            }
        });

        Button retry = findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load(null);
            }
        });

        load(queryFrom(getIntent()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String q = queryFrom(intent);
        if (q != null) load(q);
    }

    /** A query handed over by another app (ACTION_WEB_SEARCH). */
    private String queryFrom(Intent intent) {
        if (intent == null) return null;
        if (!Intent.ACTION_WEB_SEARCH.equals(intent.getAction())) return null;
        String q = intent.getStringExtra(SearchManager.QUERY);
        if (q == null) q = intent.getStringExtra(Intent.EXTRA_TEXT);
        return (q == null || q.trim().isEmpty()) ? null : q.trim();
    }

    private void load(String query) {
        setup.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        if (query != null) {
            webView.loadUrl(START + "?q=" + Uri.encode(query));
        } else if (webView.getUrl() == null) {
            webView.loadUrl(START);
        } else {
            webView.reload();
        }
    }

    /**
     * index.html is served from memory with the backend address baked in, so the
     * page knows where to look before any of its own scripts run.
     */
    private WebResourceResponse indexWithApi() {
        try {
            InputStream in = getAssets().open("www/index.html");
            String html = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
            in.close();
            String inject = "<script>window.QMAMU_API=" + JSONObject.quote(apiBase()) + ";</script>";
            html = html.replace("</head>", inject + "</head>");
            return new WebResourceResponse("text/html", "utf-8",
                    new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

    /** Saved backend address, or the built-in default. */
    private String apiBase() {
        SharedPreferences p = getSharedPreferences("qmamu", MODE_PRIVATE);
        return p.getString("api", DEFAULT_API);
    }

    private void showSetup() {
        webView.setVisibility(View.GONE);
        setup.setVisibility(View.VISIBLE);
    }

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
