package com.example.aussiegroceryapp;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AiChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        WebView chatWebView = findViewById(R.id.chat_web_view);
        WebSettings webSettings = chatWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        chatWebView.setInitialScale(70);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        chatWebView.setWebChromeClient(new WebChromeClient());
        chatWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        chatWebView.loadUrl("https://mediafiles.botpress.cloud/8107aa6f-91c3-4f42-8c7e-afd425975484/webchat/bot.html");

        //Button to Home
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AiChatActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Button to CreatedList Activity
        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AiChatActivity.this, CreatedListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}