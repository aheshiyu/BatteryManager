package com.example.batterymanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity
{
    private AlertDialog         Alert;
    private SharedPreferences   SettingData;
    private EditText            EditText;
    private boolean             IsNormalCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SettingData = getSharedPreferences("mySetting", MODE_PRIVATE);
        setTheme(SettingData.getInt("isDark", R.style.theme_white));

        setContentView(R.layout.activity_setting);

        createAlert();
        initListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            findViewById(R.id.item_isSound).setVisibility(View.GONE);
            findViewById(R.id.item_isVibrate).setVisibility(View.GONE);
        }

        int batLimit = getSharedPreferences("mySetting", MODE_PRIVATE).getInt("limit", 100);
        EditText editText=(EditText)findViewById(R.id.setting_limit);
        editText.setText(String.valueOf(batLimit));

        IsNormalCheck   = false;
        boolean isDark  = false;
        int     styleID = getSharedPreferences("mySetting", MODE_PRIVATE).getInt("isDark", R.style.theme_white);
        switch (styleID)
        {
            case R.style.theme_white:
                isDark = false;
                break;

            case R.style.theme_dark:
                isDark = true;
                break;

            default:
                break;
        }
        Switch darkSwitch = (Switch)findViewById(R.id.setting_isDark);
        darkSwitch.setChecked(isDark);
        IsNormalCheck = true;
        System.out.println("------------------------------------------------------");
        System.out.println(isDark);

        boolean isSound = getSharedPreferences("mySetting", MODE_PRIVATE).getBoolean("isSound", true);
        Switch soundSwitch = (Switch)findViewById(R.id.setting_isSound);
        soundSwitch.setChecked(isSound);
        System.out.println(isSound);
        System.out.println("------------------------------------------------------");

        boolean isVibrate = getSharedPreferences("mySetting", MODE_PRIVATE).getBoolean("isVibrate", true);
        Switch vibrateSwitch = (Switch)findViewById(R.id.setting_isVibrate);
        vibrateSwitch.setChecked(isVibrate);
        System.out.println(isVibrate);
        System.out.println("------------------------------------------------------");

        // 戻るボタンの表示
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initListener()
    {
        setContentView(R.layout.activity_setting);

        // 通知残量テキストボックス
        EditText = (EditText)findViewById(R.id.setting_limit);
        EditText.setFocusable(true);
        EditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean isEditing)
            {
                if (isEditing == false)
                {
                    updateBatteryLimit(EditText.getText().toString());
                }
            }
        });

        // ダークモードスイッチ
        Switch darkSwitch = findViewById(R.id.setting_isDark);
        darkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                System.out.println("------------------------------------------------------");
                System.out.println(isChecked);
                Editor editor = SettingData.edit();

                if (isChecked == true)
                {
                    editor.putInt("isDark", R.style.theme_dark);
                }
                else
                {
                    editor.putInt("isDark", R.style.theme_white);
                }
                editor.apply();

                if (IsNormalCheck == true)
                {
                    finish();

                    Intent intent = new Intent(getApplication(), SettingActivity.class);
                    startActivity(intent);
                }
            }
        });

        // 通知音スイッチ
        Switch soundSwitch = findViewById(R.id.setting_isSound);
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
//                System.out.println("------------------------------------------------------");
//                System.out.println(isChecked);
                Editor editor = SettingData.edit();
                editor.putBoolean("isSound", isChecked);
                editor.apply();
            }
        });

        // 振動スイッチ
        Switch vibrateSwitch = findViewById(R.id.setting_isVibrate);
        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
//                System.out.println("------------------------------------------------------");
//                System.out.println(isChecked);
                Editor editor = SettingData.edit();
                editor.putBoolean("isVibrate", isChecked);
                editor.apply();
            }
        });
    }

    private void updateBatteryLimit(String editable)
    {
        try
        {
            int batLimit = Integer.parseInt(editable);

            if ((batLimit >= 0) && (batLimit <= 100))
            {
                Editor editor = SettingData.edit();
                editor.putInt("limit", batLimit);
                editor.apply();
            }
            else
            {
                Alert.show();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    // アラートを作成するメソッド
    private void createAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("入力エラー");
        builder.setMessage("0～100までの値を入れてください");
        builder.setPositiveButton("OK", null);
        Alert = builder.create();
    }

    // 戻るメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {        boolean ret = true;

        switch (item.getItemId())
        {
            case android.R.id.home:
//                finish();
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
            // 標準の戻るボタンでもEditTextの内容を更新する
            updateBatteryLimit(EditText.getText().toString());
//            finish();
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }
        return false;
    }

    // EditText 外をタップするとフォーカスを外すメソッド
    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            View v = getCurrentFocus();
            if ( v instanceof EditText)
            {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                {
//                    System.out.println("------------------------------------------------");
//                    System.out.println("focus, touchEvent!!!!!!");
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}