package com.victor.friendchat.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.global.MyApplication;
import com.victor.friendchat.uitl.DialogViewBuilder;
import com.victor.friendchat.uitl.SaveUserUtil;
import com.victor.friendchat.uitl.SharedPreferencesUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.xmpp.XmppService;
import com.victor.friendchat.xmpp.XmppTool;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.ref.WeakReference;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.victor.friendchat.R.id.tv_login;

@RuntimePermissions
public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.et_login_user)
    private EditText mEtUser;
    @ViewInject(R.id.et_login_pwd)
    private EditText mEtPwd;
    @ViewInject(tv_login)
    private TextView mTvLogin;
    @ViewInject(R.id.tv_register)
    private TextView mTvRegister;
    @ViewInject(R.id.tv_forgot_pwd)
    private TextView mTvForgotPwd;
    @ViewInject(R.id.rb_login_qq)
    private RadioButton mRbLoginQQ;
    @ViewInject(R.id.rb_login_weibo)
    private RadioButton mRbLoginWeibo;
    @ViewInject(R.id.rb_login_wechat)
    private RadioButton mRbLoginWechat;

    private LoginHandler mHandler = new LoginHandler(this);

    static class LoginHandler extends Handler {
        WeakReference<Activity> mActivityReference;

        LoginHandler(Activity activity) {
            mActivityReference= new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = mActivityReference.get();
            if (activity != null) {
                handleMessage(msg, activity);
            }
        }

        private void handleMessage(Message msg, final Activity activity) {
            final User user = (User) msg.obj;
            new Thread() {
                @Override
                public void run() {
                    boolean result = XmppTool.getInstance().login(user.user, user.password, UIUtils.getContext());
                    if (result) {

                        UIUtils.runningOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SaveUserUtil.saveAccount(UIUtils.getContext(), user);
                                activity.startService(new Intent(UIUtils.getContext(), XmppService.class));
                                Intent intent = new Intent(UIUtils.getContext(), MainActivity.class);
                                intent.putExtra("user", user);
                                //regster_push(mUser.user);
                                SharedPreferencesUtil.setBoolean(UIUtils.getContext(), "user_message", "login", true);
                                activity.startActivity(intent);
                                activity.finish();
                                DialogViewBuilder.dismiss();
                                UIUtils.showShortToast(UIUtils.getContext(), "登录成功");
                            }
                        });

                    } else {
                        UIUtils.runningOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogViewBuilder.dismiss();
                                UIUtils.showShortToast(UIUtils.getContext(), "登录失败，请重试！");
                            }
                        });

                    }
                }

            }.start();
        }
    }

    @Override
    protected void initView() {
        DialogViewBuilder.init(LoginActivity.this, "正在登录......");
        mEtUser.setText("13888888888");
        mEtPwd.setText("123456");
        LoginActivityPermissionsDispatcher.enterAppWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE})
    void enterApp() {

    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE})
    void rejectPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限提醒")
                .setMessage("是否要拒绝权限，如果拒绝，应用将无法正常运行")
                .setPositiveButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().exit();
                    }
                })
                .setNegativeButton("重设", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.System.canWrite(LoginActivity.this)) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                })
                .show();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogViewBuilder.dismiss();
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.login);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void initListener() {
        mTvLogin.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
        mTvForgotPwd.setOnClickListener(this);
        mRbLoginQQ.setOnClickListener(this);
        mRbLoginWeibo.setOnClickListener(this);
        mRbLoginWechat.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case tv_login:

                String userName =  mEtUser.getText().toString().trim();
                String pwd = mEtPwd.getText().toString().trim();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
                    UIUtils.showShortToast(this, "用户名和密码不能为空");
                    return;
                }
                loginByUserPwd(userName, pwd);
                break;
            case R.id.tv_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_forgot_pwd:
                UIUtils.showShortToast(this, "忘记密码");
                break;
            case R.id.rb_login_qq:
                UIUtils.showShortToast(this, "QQ登录");
                break;
            case R.id.rb_login_weibo:
                UIUtils.showShortToast(this, "微博登录");
                break;
            case R.id.rb_login_wechat:
                UIUtils.showShortToast(this, "微信登录");
                break;
        }

    }

    private void loginByUserPwd(String userName, String pwd) {
        DialogViewBuilder.show();
        RequestParams params = new RequestParams(Constant.URL.DO_GET_URSER);
        params.addBodyParameter(Constant.RequestParamNames.user, userName);
        params.addBodyParameter(Constant.RequestParamNames.password, pwd);
        params.addBodyParameter(Constant.RequestParamNames.action, "login");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getString("code").equals("success")) {
                            object = object.getJSONObject("data");

                            Gson gson = new Gson();
                            User user = gson.fromJson(object.toString(), User.class);

                            Message m = mHandler.obtainMessage();
                            m.obj = user;
                            mHandler.sendMessage(m);

                        } else {
                            DialogViewBuilder.dismiss();
                            UIUtils.showShortToast(LoginActivity.this, "账号或密码有误，请重新输入");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        DialogViewBuilder.dismiss();
                    }
                } else {
                    DialogViewBuilder.dismiss();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        if (DialogViewBuilder.sDialog.isShowing()) {
            DialogViewBuilder.dismiss();
        } else {
            finish();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoginActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
