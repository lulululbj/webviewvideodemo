package com.sun.videodemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private FrameLayout mVideoContainer;
    private WebChromeClient.CustomViewCallback mCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView= (WebView) findViewById(R.id.webView);
        mVideoContainer= (FrameLayout) findViewById(R.id.videoContainer);

        initWebView();

        mWebView.loadUrl("http://www.baidu.com");
        mWebView.addJavascriptInterface(new JsObject(),"onClick");

    }

    private void initWebView(){
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new CustomWebViewChromeClient());
        mWebView.setWebViewClient(new CustomWebClient());

        mWebView.addJavascriptInterface(new JsObject(),"onClick");
    }

    private class JsObject{

        @JavascriptInterface
        public void fullscreen(){
            //监听到用户点击全屏按钮
           fullScreen();
        }
    }

    private class CustomWebViewChromeClient extends WebChromeClient{

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            fullScreen();
            mWebView.setVisibility(View.GONE);
            mVideoContainer.setVisibility(View.VISIBLE);
            mVideoContainer.addView(view);
            mCallBack=callback;
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            fullScreen();
            if (mCallBack!=null){
                mCallBack.onCustomViewHidden();
            }
            mWebView.setVisibility(View.VISIBLE);
            mVideoContainer.removeAllViews();
            mVideoContainer.setVisibility(View.GONE);
            super.onHideCustomView();
        }
    }

    private void fullScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private class CustomWebClient extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js=TagUtils.getJs(url);
            view.loadUrl(js);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()){
            mWebView.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
