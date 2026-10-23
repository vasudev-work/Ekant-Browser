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

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Scanner;

/**
 * Qmamu - the meta-search frontend as an app.
 *
 * The page lives in assets/www. Every request to the app's own origin is served
 * straight from the APK (see serveAsset), so the interface opens instantly, works
 * offline and never depends on a site being up. Only the results travel over the
 * network: the page calls the Qmamu backend, whose address is injected into
 * index.html as it is served.
 */
public class MainActivity extends AppCompatActivity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String ORIGIN = "https://" + HOST;
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
        s.setDomStorageEnabled(true);          // the page keeps the backend address here
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setAllowFileAccess(false);
        s.setBlockNetworkLoads(false);
        webView.setBackgroundColor(getResources().getColor(R.color.bg));

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return serveAsset(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (HOST.equals(u.getHost())) {
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

    /**
     * Serve everything under the app origin from the APK.
     *
     * index.html is rewritten on the way out so the page knows the backend
     * address before any of its own scripts run. Anything that is not a bundled
     * file falls back to index.html, which keeps the single page reachable even
     * if something navigates to "/".
     */
    private WebResourceResponse serveAsset(Uri u) {
        if (u == null || !HOST.equals(u.getHost())) return null;

        String path = u.getPath();
        if (path == null || path.equals("/") || path.equals("/index.html")) {
            path = "/assets/www/index.html";
        }
        if (!path.startsWith("/assets/www/")) {
            path = "/assets/www/index.html";
        }

        String rel = path.substring("/assets/".length());   // www/index.html
        try {
            InputStream in = getAssets().open(rel);
            if (rel.endsWith(".html")) {
                String html = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
                in.close();
                String inject = "<script>window.QMAMU_API=" + JSONObject.quote(apiBase()) + ";</script>";
                html = html.replace("</head>", inject + "</head>");
                return new WebResourceResponse("text/html", "utf-8",
                        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
            }
            return new WebResourceResponse(mimeOf(rel), "utf-8", in);
        } catch (Exception e) {
            return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                    Collections.<String, String>emptyMap(), new ByteArrayInputStream(new byte[0]));
        }
    }

    private String mimeOf(String rel) {
        String r = rel.toLowerCase();
        if (r.endsWith(".css")) return "text/css";
        if (r.endsWith(".js")) return "application/javascript";
        if (r.endsWith(".svg")) return "image/svg+xml";
        if (r.endsWith(".png")) return "image/png";
        if (r.endsWith(".json")) return "application/json";
        if (r.endsWith(".html")) return "text/html";
        return "application/octet-stream";
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
