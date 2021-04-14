package com.mrkiriss.wifilocalpositioning.ui.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.models.MapPoint;

public class LocationDetectoinFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.fragment_location_detectoin, container, false);
        MapWebView webView = r.findViewById(R.id.web);
        webView.initWebView();

        webView.setBackgroundColor(Color.GREEN);
        webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.loadDataWithBaseURL("file:///android_asset/img/", "<img src='all2.png' />",
                "text/html", "utf-8", null);

        Handler handler = new Handler();
        Runnable task = () -> {
            MapPoint mp = new MapPoint();
            mp.setX(10);
            mp.setY(29);
            webView.changeLocation(mp);

            Runnable task1 = () -> {
                MapPoint mp1 = new MapPoint();
                mp1.setX(10);
                mp1.setY(22);
                webView.changeLocation(mp1);

            };
            handler.postDelayed(task1, 2000);
        };
        handler.postDelayed(task, 500);

        return r;
    }
}