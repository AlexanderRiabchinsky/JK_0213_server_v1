package com.template;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static com.template.LoadingActivity.domen;

public class WebActivity extends Activity {
    private WebView browser=null;

 //   @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_web);

        browser = findViewById(R.id.webBrowser);
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookie();
        cookieManager.setAcceptCookie(true);

        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setDefaultTextEncodingName("utf-8");
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);
        browser.setWebViewClient(new MyWebViewClient(this));
        browser.loadUrl(domen);
        browser.clearView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("HTTP-GET","Hi from webActivity: "+domen+browser.getWebViewClient().toString());
        }
    }


    private class MyWebViewClient extends WebViewClient {
        Activity activity;
        private MyWebViewClient(Activity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            Log.d("HTTP-GET","Hi from urlLoading: "+domen+request.getUrl().toString());
            return false;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);
            return false;
        }
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d("HTTP-GET","Check: "+browser.canGoBack()+" and "+browser.copyBackForwardList().getCurrentIndex());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if((browser.copyBackForwardList().getCurrentIndex() >2)&& browser.canGoBack())
            {browser.goBack();}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//@Override
//public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//    // Back?
//    if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack() && event.getRepeatCount() == 0) {
//        // Back
//        moveTaskToBack(true);
//        finish();
//        System.runFinalizersOnExit(false);
//        return true;
//    }
//    else {
//        // Return
//        return super.onKeyDown(keyCode, event);
//    }
//}

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }
//    // получение ранее сохраненного состояния
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }
}