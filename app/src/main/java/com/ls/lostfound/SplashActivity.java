package com.ls.lostfound;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // Adjust the duration as needed (in milliseconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Define your animation here (e.g., fade_in and fade_out)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Add any other setup for your splash screen here

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivity(intent);
                finish();

            }
        }, SPLASH_DURATION);
    }
}
