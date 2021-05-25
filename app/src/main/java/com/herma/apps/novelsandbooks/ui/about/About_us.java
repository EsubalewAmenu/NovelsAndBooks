package com.herma.apps.novelsandbooks.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.herma.apps.novelsandbooks.R;

public class About_us extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        ((Button) findViewById(R.id.telegram)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(About_us.this, "Coming soon... :)", Toast.LENGTH_SHORT).show();
//                About_us.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://t.me/ethio_textbooks")));
            }
        });
        ((Button) findViewById(R.id.moreap)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                openUrl("https://play.google.com/store/apps/developer?id=Herma%20plc");
            }
        });
        ((Button) findViewById(R.id.shareap)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction("android.intent.action.SEND");
                sendIntent.putExtra("android.intent.extra.SUBJECT", About_us.this.getText(R.string.app_name));
                sendIntent.putExtra("android.intent.extra.TEXT", "Downloads \nhttps://play.google.com/store/apps/details?id=" + About_us.this.getPackageName());
                sendIntent.setType("text/plain");
                About_us.this.startActivity(Intent.createChooser(sendIntent, About_us.this.getText(R.string.app_name)));
            }
        });
        ((Button) findViewById(R.id.description)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                About_us.this.startActivity(new Intent(About_us.this.getApplicationContext(), Describe.class));
            }
        });
    }

    private void openUrl(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
