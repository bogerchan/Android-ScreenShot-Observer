package me.bogerchan.screenshotdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_SCREENSHOT = 1;

    private TextView tvInfo;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = (TextView) findViewById(R.id.tv_info);

        mHandler = new MyHandler(this);

        registerScreenShotContentObserver();
    }

    private void registerScreenShotContentObserver() {
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ScreenShotObserver(this, mHandler));
    }

    private void println(String msg) {
        tvInfo.setText(String.format("%s\n%s\n", tvInfo.getText(), msg));
    }

    public static class MyHandler extends Handler {

        private WeakReference<MainActivity> relevant;

        public MyHandler(MainActivity activity) {
            relevant = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCREENSHOT:
                    if (relevant!= null && relevant.get() != null) {
                        relevant.get().println(msg.obj.toString());
                    }
                    break;
            }
        }
    }
}
