package com.victor.friendchat.ui.activity;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.global.Location;
import com.victor.friendchat.uitl.DialogViewBuilder;
import com.victor.friendchat.uitl.LogUtils;
import com.victor.friendchat.uitl.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import static com.victor.friendchat.ui.activity.RegisterActivity.GENDER.FEMALE;
import static com.victor.friendchat.ui.activity.RegisterActivity.GENDER.MALE;

public class RegisterActivity extends BaseActivity {

    @ViewInject(R.id.et_register_phone)
    private EditText mEtPhoneNum;
    @ViewInject(R.id.et_register_pwd)
    private EditText mEtPwd;
    @ViewInject(R.id.et_register_pwd_confirm)
    private EditText mEtPwdConfirm;
    @ViewInject(R.id.tv_register_submit)
    private TextView mTvSubmit;
    @ViewInject(R.id.et_register_nick)
    private EditText mEtNick;
    @ViewInject(R.id.ll_phone_register_male)
    private LinearLayout mLLMale;
    @ViewInject(R.id.ll_phone_register_female)
    private LinearLayout mLlFemale;
    @ViewInject(R.id.tv_phone_register_male)
    private TextView mTvMale;
    @ViewInject(R.id.tv_phone_register_female)
    private TextView mTvFemale;


    private boolean isLegalNum = false;
    private boolean isLegalPwd = false;
    private String mPwd;
    private String mPhoneNum;
    private Location mLocation;
    private String mGender = "男";
    private String mNickName;

    enum GENDER {
        MALE,
        FEMALE
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_register;
    }

    @Override
    protected void initView() {
        DialogViewBuilder.init(this, "注册中......");
        mLocation = new Location(this);
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.register);
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
        mTvSubmit.setOnClickListener(this);
        mLLMale.setOnClickListener(this);
        mLlFemale.setOnClickListener(this);

        // 监听输入手机号码的变化
        mEtPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString();
                // 判断手机号码必须大于11位且为正确手机号码
                if (phone.length() == 11 && judgeNum(phone)) {
                    isLegalNum = true;
                } else {
                    isLegalNum = false;
                }

                switchSubmitButtonStatus(isLegalNum, isLegalPwd);
            }
        });

        mEtPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String pwdRegex = "^\\w+$";
                String password = s.toString();
                if (password.length() >= 6 && password.length() <= 16 && password.matches(pwdRegex)) {
                    isLegalPwd = true;
                } else {
                    isLegalPwd = false;
                }
                switchSubmitButtonStatus(isLegalNum, isLegalPwd);
            }
        });
    }

    /**
     * 根据手机号和密码是否符合规定，来改变提交按钮的状态
     * @param legalNum 手机号码是否合法
     * @param legalPwd 密码是否合法
     */
    private void switchSubmitButtonStatus(boolean legalNum, boolean legalPwd) {
        if (legalNum && legalPwd) {
            mTvSubmit.setEnabled(true);
            mTvSubmit.setBackgroundResource(R.drawable.shape_login_textview_focused);
        } else {
            mTvSubmit.setEnabled(false);
            mTvSubmit.setBackgroundResource(R.drawable.shape_register_textview_normal);
        }
    }

    /**
     * 判断手机号码是否正确
     * @param phoneNum
     * @return
     */
    private boolean judgeNum(String phoneNum) {
        // "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        String telRegex = "[1][3578]\\d{9}";
        if (TextUtils.isEmpty(phoneNum)) {
            return false;
        } else {
            return phoneNum.matches(telRegex);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register_submit:
                submitAccount();
                break;
            case R.id.ll_phone_register_male:
                selectGender(MALE);
                break;
            case R.id.ll_phone_register_female:
                selectGender(FEMALE);
                break;
        }
    }

    /**
     * 选择性别
     * @param gender 性别
     */
    private void selectGender(GENDER gender) {
        if (gender == MALE) {
            mLLMale.setBackgroundResource(R.drawable.shape_regster_gender_male_focused);
            mLlFemale.setBackgroundResource(R.drawable.shape_regster_gender_female_nomal);
            mTvMale.setTextColor(Color.WHITE);
            mTvFemale.setTextColor(getResources().getColor(R.color.dark_gray));
            mGender = "男";

        } else if (gender == FEMALE) {
            mLLMale.setBackgroundResource(R.drawable.shape_regster_gender_male_nomal);
            mLlFemale.setBackgroundResource(R.drawable.shape_regster_gender_female_focused);
            mTvFemale.setTextColor(Color.WHITE);
            mTvMale.setTextColor(getResources().getColor(R.color.dark_gray));
            mGender = "女";
        }
    }

    /**
     * 提交注册信息
     */
    private void submitAccount() {
        mPhoneNum = mEtPhoneNum.getText().toString().trim();
        mPwd = mEtPwd.getText().toString().trim();
        mNickName = mEtNick.getText().toString().trim();
        if (TextUtils.isEmpty(mNickName)) {
            mNickName = mPhoneNum;
        }
        String pwdConfirm = mEtPwdConfirm.getText().toString().trim();

        if (!mPwd.equals(pwdConfirm)) {
            UIUtils.showLongToast(this, "两次密码输入不一致");
            return;
        }
        if (mPwd.length() < 6 || mPwd.length() > 16) {
            UIUtils.showLongToast(this, "密码长度为6~16位之间");
            return;
        }

        DialogViewBuilder.show();
        String position = mLocation.position;
        String city = mLocation.city;
        if (position == null) {
            position = "";
        }
        if (city == null) {
            city = "";
        }
        String url = Constant.URL.DO_GET_URSER;
        RequestParams params = new RequestParams(url);
        LogUtils.sf(url);
        params.addBodyParameter(Constant.RequestParamNames.user, mPhoneNum);
        params.addBodyParameter(Constant.RequestParamNames.nickname, mNickName);
        params.addBodyParameter(Constant.RequestParamNames.password, mPwd);
        params.addBodyParameter(Constant.RequestParamNames.sex, mGender);
        params.addBodyParameter(Constant.RequestParamNames.icon, "");
        params.addBodyParameter(Constant.RequestParamNames.city, city);
        params.addBodyParameter(Constant.RequestParamNames.location, position);
        params.addBodyParameter(Constant.RequestParamNames.years, "");
        params.addBodyParameter(Constant.RequestParamNames.qq, "");
        params.addBodyParameter(Constant.RequestParamNames.action, "save");

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getString("code").equals("success")) {
                            UIUtils.showShortToast(RegisterActivity.this, "注册成功");
                            DialogViewBuilder.dismiss();
                            finish();
                        } else {
                            UIUtils.showShortToast(RegisterActivity.this, "注册失败！请重试！");
                            DialogViewBuilder.dismiss();
                            mTvSubmit.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        DialogViewBuilder.dismiss();
                        mTvSubmit.setEnabled(true);
                    }
                } else {
                    DialogViewBuilder.dismiss();
                    mTvSubmit.setEnabled(true);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                UIUtils.showLongToast(RegisterActivity.this, "网络连接失败，请检查网络设置！");
                DialogViewBuilder.dismiss();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
