package com.example.batterymanager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity
{
    private SharedPreferences SettingData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SettingData = getSharedPreferences("mySetting", MODE_PRIVATE);
        setTheme(SettingData.getInt("isDark", R.style.theme_white));

        setContentView(R.layout.activity_detail);

        ScrollView scroll = (ScrollView)findViewById(R.id.scroll_detail);
        scroll.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        final Handler handler  = new Handler();
        final Runnable  runnable = new Runnable() {
            @Override
            public void run()
            {
                update();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        // 戻るボタンの表示
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void update()
    {
        setContentView(R.layout.activity_detail);

        Intent battery = battery();

        // バッテリーの残量
        TextView attrBatLevel = (TextView)findViewById(R.id.battery_level);
        int         level        = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        attrBatLevel.setText("");
        attrBatLevel.setText(String.valueOf(level) + "%");

        // バッテリーの状態
        TextView    attrBatHealth = (TextView)findViewById(R.id.battery_health);
        int         health        = battery.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        attrBatHealth.setText("");
        attrBatHealth.setText(batteryHealthStr(health));

        // アイコン
        /*
        TextView    attrBatIcon = (TextView)findViewById(R.id.battery_icon);
        int         icon        = battery.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1);
        attrBatIcon.setText("");
        attrBatIcon.setText(String.valueOf(icon));
        */

        // バッテリーの接続元
        TextView    attrBatPlugSource = (TextView)findViewById(R.id.battery_plug_source);
        int         plugSource        = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        attrBatPlugSource.setText("");
        attrBatPlugSource.setText(batteryPlgSrcStr(plugSource));

        // バッテリーの存在有無
        /*
        TextView    attrBatPresent = (TextView)findViewById(R.id.battery_present);
        boolean     present        = battery.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        attrBatPresent.setText("");
        attrBatPresent.setText(batteryIsPresentStr(present));
        */

        // バッテリーの最大値
        TextView    attrBatScale = (TextView)findViewById(R.id.battery_scale);
        int         scale        = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        attrBatScale.setText("");
        attrBatScale.setText(String.valueOf(scale) + "%");

        // バッテリーの接続状態
        TextView    attrBatStatus = (TextView)findViewById(R.id.battery_status);
        int         status        = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        attrBatStatus.setText("");
        attrBatStatus.setText(MainActivity.batteryStatusStr(status));

        // バッテリーの種類
        TextView    attrBatTech = (TextView)findViewById(R.id.battery_technology);
        String      technology  = battery.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        attrBatTech.setText("");
        attrBatTech.setText(technology);

        // バッテリーの温度
        TextView    attrBatTemp = (TextView)findViewById(R.id.battery_temperature);
        int         temperature = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        attrBatTemp.setText("");
        attrBatTemp.setText(String.valueOf((float)temperature / 10) + " °C");

        // バッテリーの電圧
        TextView    attrBatVoltage = (TextView)findViewById(R.id.battery_voltage);
        int         voltage        = battery.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        attrBatVoltage.setText("");
        attrBatVoltage.setText(String.valueOf((float)voltage / 1000) + " V");

        if(Build.VERSION.SDK_INT >= 21)
        {
            BatteryManager manager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);

            if (manager != null)
            {
//                TextView    attrBatCapacity = (TextView)findViewById(R.id.battery_capacity);
//                int         capacity        = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//                attrBatCapacity.setText("");
//                attrBatCapacity.setText(String.valueOf(capacity));
//
//                TextView    attrBatChargeCounter = (TextView)findViewById(R.id.battery_charge_counter);
//                int         chargeCounter        = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
//                attrBatChargeCounter.setText("");
//                attrBatChargeCounter.setText(String.valueOf(chargeCounter));
//
//                TextView    attrBatCurrentAverage = (TextView)findViewById(R.id.battery_current_average);
//                int         currentAverage        = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
//                attrBatCurrentAverage.setText("");
//                attrBatCurrentAverage.setText(String.valueOf(currentAverage));
//
//                TextView    attrBatCurrentNow = (TextView)findViewById(R.id.battery_current_now);
//                int         currentNow        = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
//                attrBatCurrentNow.setText("");
//                attrBatCurrentNow.setText(String.valueOf(currentNow));
//
//                TextView    attrBatEnergyCounter = (TextView)findViewById(R.id.battery_energy_counter);
//                long        energyCounter        = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
//                attrBatEnergyCounter.setText("");
//                attrBatEnergyCounter.setText(String.valueOf(energyCounter));
            }
        }
    }

    private Intent battery()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent          battery = this.registerReceiver(null, ifilter);
        return battery;
    }

    private String batteryHealthStr(int health)
    {
        String ret = "";

        switch (health)
        {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                ret = "良好";
                break;

            case BatteryManager.BATTERY_HEALTH_DEAD:
                ret = "不良";
                break;

            case BatteryManager.BATTERY_HEALTH_COLD:
                ret = "冷たい";
                break;

            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                ret = "オーバーヒート";
                break;

            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                ret = "電圧超過";
                break;

            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                ret = "不明";
                break;

            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                ret = "予期せぬエラー";
                break;

            default:
                break;
        }

        return ret;
    }

    private String batteryPlgSrcStr(int plugged)
    {
        String ret = "なし";

        switch (plugged)
        {
            case BatteryManager.BATTERY_PLUGGED_AC:
                ret = "ACアダプタ";
                break;

            case BatteryManager.BATTERY_PLUGGED_USB:
                ret = "USBアダプタ";
                break;

            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                ret = "ワイヤレス";
                break;

            default:
                break;
        }

        return ret;
    }

    /*
    private String batteryIsPresentStr(boolean isPresent)
    {
        String ret;
        if (isPresent == true)
        {
            ret = "有";
        }
        else
        {
            ret = "無";
        }

        return ret;
    }
    */

    // 戻るメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean ret = true;

        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return false;
    }
}
