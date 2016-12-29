package com.victor.friendchat.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.uitl.DataCleanUtil;
import com.victor.friendchat.uitl.SharedPreferencesUtil;
import com.victor.friendchat.uitl.UIUtils;

import org.xutils.view.annotation.ViewInject;

public class SystemSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String GESTURE_ON_OFF = "gestrue_on_off";
    private static final String SOUND_ON_OFF = "sound_on_off";
    private static final String VIBRATION_ON_OFF = "vibration_on_off";

    @ViewInject(R.id.rl_setting_clear)
    private RelativeLayout mRlClear;
    @ViewInject(R.id.rl_setting_update)
    private RelativeLayout mRlUpdate;
    @ViewInject(R.id.cb_setting_gesture)
    private CheckBox mCbGesture;
    @ViewInject(R.id.cb_setting_sound)
    private CheckBox mCbSound;
    @ViewInject(R.id.cb_setting_vibration)
    private CheckBox mCbVibrarion;
    @ViewInject(R.id.tv_setting_version)
    private TextView mTvVersion;
    @ViewInject(R.id.tv_setting_update)
    private TextView mTvUpdate;
    @ViewInject(R.id.tv_setting_logout)
    private TextView mTvLogout;
    @ViewInject(R.id.tv_setting_cache)
    private TextView mTvCache;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void initView() {
        try {
            // 设置版本号和缓存大小
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            mTvVersion.setText("V" + packageInfo.versionName);
            mTvCache.setText(DataCleanUtil.getTotalCacheSize(this));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.system_setting);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    protected void initListener() {
        mRlClear.setOnClickListener(this);
        mRlUpdate.setOnClickListener(this);
        mTvLogout.setOnClickListener(this);
        mCbGesture.setOnClickListener(this);

        mCbSound.setOnCheckedChangeListener(this);
        mCbVibrarion.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(Constant.SP.SYSTEM_SETTING, MODE_PRIVATE);
        boolean isGestrueOn = preferences.getBoolean(GESTURE_ON_OFF, false);
        boolean isSoundOn = preferences.getBoolean(SOUND_ON_OFF, true);
        boolean isVibrationOn = preferences.getBoolean(VIBRATION_ON_OFF, false);
        mCbGesture.setChecked(isGestrueOn);
        mCbSound.setChecked(isSoundOn);
        mCbVibrarion.setChecked(isVibrationOn);

        try {
            mTvCache.setText(DataCleanUtil.getTotalCacheSize(this));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_setting_logout:
                UIUtils.showShortToast(this, "注销");
                break;
            case R.id.rl_setting_clear:
                new AlertDialog.Builder(this).setTitle("清除缓存")
                        .setMessage("清除缓存后使用的流量可能会额外增加，确定清除？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clear();
                            }
                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.rl_setting_update:
                UIUtils.showShortToast(this, "检查更新");
                break;
            case R.id.cb_setting_gesture:
                UIUtils.showShortToast(this, "手势设置");
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_setting_sound:
                SharedPreferencesUtil.setBoolean(this, Constant.SP.SYSTEM_SETTING, SOUND_ON_OFF, isChecked);
                break;
            case R.id.cb_setting_vibration:
                SharedPreferencesUtil.setBoolean(this, Constant.SP.SYSTEM_SETTING, VIBRATION_ON_OFF, isChecked);
                break;
        }
    }

    /**
     * 清理缓存
     */
    private void clear() {
        DataCleanUtil.clearAllCache(this);
        try {
            mTvCache.setText(DataCleanUtil.getTotalCacheSize(this));
            UIUtils.showShortToast(this, "清除缓存成功...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
