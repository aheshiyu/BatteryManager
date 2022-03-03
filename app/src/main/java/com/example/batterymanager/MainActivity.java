package com.example.batterymanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    private Intent  Battery;        // バッテリー
    private int     BatLimit;       // 通知上限バッテリー残量
    private int     BatRemain;      // バッテリー残量
    private boolean BatIsPlugged;   // 充電中有無
    private boolean BatIsNotified;  // 通知有無

    private SharedPreferences SettingData;

    private Notification Notification;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init();

        final Handler   handler  = new Handler();
        final Runnable  runnable = new Runnable() {
            @Override
            public void run()
            {
                update();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void init() {
        BatIsPlugged = false;
        BatIsNotified = false;

        SettingData = getSharedPreferences("mySetting", MODE_PRIVATE);
        setTheme(SettingData.getInt("isDark", R.style.theme_white));
    }

    private void update()
    {
        Battery = battery();

        if(Battery != null)
        {
            updateBatteryInfo();
            updateProgressBar();

            if (BatIsPlugged == true)
            {
                if (BatRemain >= BatLimit)
                {
                    if (BatIsNotified == false)
                    {
                        System.out.println("---------------------------------------------------");
                        System.out.println("BATTERY CHARGED!!!");
                        notifyReach();
                        BatIsNotified = true;
                    }
                }
            }
            else
            {
                BatIsNotified = false;
            }
        }
    }

    private void updateBatteryInfo()
    {
        setContentView(R.layout.activity_main);

        SettingData = getSharedPreferences("mySetting", MODE_PRIVATE);
        BatLimit    = SettingData.getInt("limit", 100);

        // バッテリーの残量
        TextView    attrBatteryRemain = findViewById(R.id.battery_remain);
        int         batteryRemain     = Battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        attrBatteryRemain.setText("");
        attrBatteryRemain.setText(batteryRemain + "%");

        BatRemain = batteryRemain;

        // バッテリーの接続状態
        TextView    attrBatteryStatus = findViewById(R.id.battery_status);
        int         batteryStatus     = Battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        attrBatteryStatus.setText("");
        attrBatteryStatus.setText(batteryStatusStr(batteryStatus));

        // バッテリー接続有無
        int batteryPlugged = Battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        BatIsPlugged = isPlugged(batteryPlugged);

        // バッテリー満タン時間
        TextView attrBatteryTime      = findViewById(R.id.battery_time);
        TextView attrBatteryTimeSpace = findViewById(R.id.battery_time_space);
        if (Build.VERSION.SDK_INT >= 28) {
            String          timeStr = "";
            BatteryManager  manager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
            long            seconds = manager.computeChargeTimeRemaining() / 1000;  // ミリ秒を秒に変換

            if (seconds > 0)
            {
                long h = TimeUnit.SECONDS.toHours(seconds);
                long m = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);

                timeStr += "充電完了までの時間: 約";

                if (h > 0)
                {
                    timeStr += " " + h + "時間";
                }

                if (m > 0)
                {
                    timeStr += " " + m + "分";
                }

                attrBatteryTime.setText("");
                attrBatteryTime.setText(timeStr);
            }
            else
            {
                attrBatteryTime.setVisibility(View.GONE);
                attrBatteryTimeSpace.setVisibility(View.GONE);
            }
        }
        else
        {
            attrBatteryTime.setVisibility(View.GONE);
            attrBatteryTimeSpace.setVisibility(View.GONE);
        }
    }

    private void updateProgressBar()
    {
        int         batteryScale        = Battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        ProgressBar attrBatteryProgress = findViewById(R.id.battery_progress);
        LayerDrawable progressDrawable  = (LayerDrawable) attrBatteryProgress.getProgressDrawable();
        Drawable primaryColor           = progressDrawable.getDrawable(2);  // circular_progress 内での primary の宣言が2つ目だから

        // 残量の最大値を更新
        attrBatteryProgress.setMax(batteryScale);

        // 通知残量の更新
        attrBatteryProgress.setSecondaryProgress(BatLimit);

        // 残量の更新
        attrBatteryProgress.setProgress(BatRemain);

        // 残量による色の更新
        primaryColor.setColorFilter(progressColor(BatRemain), PorterDuff.Mode.SRC_IN);
    }

    private void notifyReach()
    {
        boolean isSound   = SettingData.getBoolean("isSound", true);
        boolean isVibrate = SettingData.getBoolean("isVibrate", true);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        // アプリ名をチャンネルIDとして利用
        String channelID = getString(R.string.app_name);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |             // 起動中のアプリがあってもこちらを優先する
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |  // 起動中のアプリがあってもこちらを優先する
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);  // 「最近利用したアプリ」に表示させない
        PendingIntent contentIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        // 通知音
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // アンドロイドのバージョンで振り分け
        // APIが26以上の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // 通知チャンネルIDを生成してインスタンス化
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_DEFAULT);

            // 通知音の無効化
            if (isSound == false)
            {
                System.out.println("---------------------------------------");
                System.out.println("Sound false");
                notificationChannel.setSound(null, null);
            }
            else
            {
                System.out.println("---------------------------------------");
                System.out.println("Sound true");
            }
            // 振動の無効化
            if (isVibrate == false)
            {
                System.out.println("---------------------------------------");
                System.out.println("Vibration false");
                notificationChannel.enableVibration(false);
            }
            else
            {
                System.out.println("---------------------------------------");
                System.out.println("Vibration true");
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[] {0, 500, 100, 500});
            }
            // 通知の説明のセット
            notificationChannel.setDescription(channelID);
            // 通知チャンネルの作成
            notificationManager.createNotificationChannel(notificationChannel);
            // 通知の生成と設定とビルド
            Notification.Builder builder = new Notification.Builder(this, channelID)
                    .setContentTitle(BatLimit + "%になりました")
                    .setContentText("充電ケーブルを抜いてください")
                    .setSmallIcon(R.drawable.icon_notification);
            Notification = builder.build();
            Notification.flags |= Notification.FLAG_AUTO_CANCEL;
            builder.setContentIntent(contentIntent);
        }
        // APIが25以下の場合
        else
        {
            int wait    = 0;    // 振動開始待機時間
            int vibrate = 500;  // 振動時間
            int sleep   = 100;  // 振動間隔時間

            //APIが「25」以下の場合
            //通知の生成と設定とビルド
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(getString(R.string.app_name));
            builder.setContentText(BatLimit + "%になりました");
            builder.setSmallIcon(R.drawable.icon_notification);

            // 通知音の無効化
            if (isSound == true)
            {
                builder.setSound(alarmSound);
            }

            // 振動の無効化
            if (isVibrate == true)
            {
                builder.setVibrate(new long[] {wait, vibrate, sleep, vibrate, sleep});
            }

            Notification = builder.build();
            Notification.flags |= Notification.FLAG_AUTO_CANCEL;
            builder.setContentIntent(contentIntent);
        }

        //通知の発行
        notificationManager.notify(1, Notification);
    }

    private Intent battery()
    {
        IntentFilter    filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent          battery = this.registerReceiver(null, filter);
        return battery;
    }

    private int progressColor(int batteryRemain)
    {
        int ret          = getResources().getColor(R.color.color_progress_primary);
        int batteryScale = Battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (batteryRemain <= (int)(batteryScale * 0.15))
        {
            ret = getResources().getColor(R.color.color_progress_under15);
        }
        else if (batteryRemain <= (int)(batteryScale * 0.4))
        {
            ret = getResources().getColor(R.color.color_progress_under40);
        }

        return ret;
    }

    private boolean isPlugged(int plugged)
    {
        boolean ret = false;

        switch (plugged)
        {
            case BatteryManager.BATTERY_PLUGGED_AC:
            case BatteryManager.BATTERY_PLUGGED_USB:
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                ret = true;
                break;
        }

        return ret;
    }

    public static String batteryStatusStr(int status)
    {
        String ret = "";

        switch (status)
        {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                ret = "充電中...";
                break;

            case  BatteryManager.BATTERY_STATUS_DISCHARGING:
            case  BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                ret = "充電していません";
                break;

            case  BatteryManager.BATTERY_STATUS_FULL:
                ret = "満タン";
                break;

            case  BatteryManager.BATTERY_STATUS_UNKNOWN:
                ret = "不明";
                break;

            default:
                break;
        }

        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;

        switch (item.getItemId())
        {
            case R.id.menu_setting:
                Intent intent = new Intent(getApplication(), SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }

        return ret;
    }

    // 標準の戻るボタンでの動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            this.moveTaskToBack(true);
            return true;
        }
        return false;
    }

    public void transitionDetail(View view)
    {
        Intent intent = new Intent(getApplication(), DetailActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}