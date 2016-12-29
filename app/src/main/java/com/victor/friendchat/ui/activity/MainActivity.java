package com.victor.friendchat.ui.activity;


import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.fragment.FoundFragment;
import com.victor.friendchat.ui.fragment.FriendFragment;
import com.victor.friendchat.ui.fragment.MessageFragment;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.GETPICPOP;
import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.IMAGEPOP;
import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.SETSTATUSPOP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupWindow.OnDismissListener {

    public static final String TAG_MSG_FRAGMENT = "tag_msg_fragment";
    private static final String TAG_FRIEND_FRAGMENT = "tag_friend_fragment";
    private static final String TAG_FOUND_FRAGMENT = "tag_found_fragment";

    @ViewInject(R.id.ll_avatar)
    private LinearLayout mLlAvatar;
    @ViewInject(R.id.civ_avatar)
    private CircleImageView mCivAvatar;
    @ViewInject(R.id.iv_status)
    private ImageView mIvStatus;
    @ViewInject(R.id.tv_title)
    private TextView mTvTitle;
    @ViewInject(R.id.iv_add)
    private ImageView mIvAdd;
    @ViewInject(R.id.tv_message)
    private TextView mTvMsg;
    @ViewInject(R.id.tv_friend)
    private TextView mTvFriend;
    @ViewInject(R.id.tv_found)
    private TextView mTvFound;
    @ViewInject(R.id.nv_profile)
    private NavigationView mNvProfile;
    @ViewInject(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;
    @ViewInject(R.id.iv_Img_null)
    private ImageView mIvNull;

    private User mUser;
    private String url_icon = Constant.URL.ICON + "?name=";
    private PopupWindow mPopImage;
    private RelativeLayout mRlPopNull;
    private TextView mTvPopStatus;
    private TextView mTvPopCancel;
    private TextView mTvPopBigAvatar;
    private TextView mTvPopModify;
    private PopupWindow mPopGetPic;
    private RelativeLayout mRlPopGetPicNull;
    private TextView mTvGetPicPopCancel;
    private TextView mTvGetPicPopPhoto;
    private TextView mTvGetPicPopCamera;
    private PopupWindow mPopSetStatus;
    private RelativeLayout mRlSetStatusNull;
    private TextView mTvSetStatusPopCancel;
    private TextView mTvSetStatusPopOnline;
    private TextView mTvSetStatusPopQme;
    private TextView mTvSetStatusPopBusy;
    private TextView mTvSetStatusPopNoBother;
    private TextView mTvSetStatusPopLeave;
    private TextView mTvSetStatusPopInvisible;
    private ImageView mCivNvAvatar;
    private Intent mIntent;
    private SweetAlertDialog mUpdateAvatarDialog;
    private MessageFragment mMsgFragment;
    private FriendFragment mFrindFragment;
    private FoundFragment mFoundFragment;

    enum POPCODE {
        IMAGEPOP,
        GETPICPOP,
        SETSTATUSPOP
    }

    enum STATUS {
        ONLINE,
        QME,
        BUSY,
        NOBOTHER,
        LEAVE,
        INVISIBLE
    }

    enum FRAGMENT_TYPE {
        MESSAGE,
        FRIEND,
        LUNTAN
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x.view().inject(this);

        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");
        initNvView();
        initListener();
        initImgPop();
        initGetPicPop();
        initSetStatusPop();
        initUpdateAvatarDialog();
        updateFragment(FRAGMENT_TYPE.MESSAGE);
    }

    private void updateFragment(FRAGMENT_TYPE type) {
        initTabContent();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                ft.hide(f);
            }
        }
        Fragment fragment;
        switch (type) {
            case MESSAGE:
                mTvMsg.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_move_pressed_icon, 0, 0);
                mTvMsg.setTextColor(mTvMsg.getResources().getColor(R.color.toolBar));
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_MSG_FRAGMENT);
                if (fragment == null) {
                    mMsgFragment = new MessageFragment();
                    ft.add(R.id.fl_content, mMsgFragment, TAG_MSG_FRAGMENT);
                } else {
                    ft.show(fragment);
                }
                break;
            case FRIEND:
                mTvFriend.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_me_pressed_icon, 0, 0);
                mTvFriend.setTextColor(mTvFriend.getResources().getColor(R.color.toolBar));
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRIEND_FRAGMENT);
                if (fragment == null) {
                    mFrindFragment = new FriendFragment();
                    ft.add(R.id.fl_content, mFrindFragment, TAG_FRIEND_FRAGMENT);
                } else {
                    ft.show(fragment);
                }
                break;
            case LUNTAN:
                mTvFound.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_found_pressed_icon, 0, 0);
                mTvFound.setTextColor(mTvFriend.getResources().getColor(R.color.toolBar));
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FOUND_FRAGMENT);
                if (fragment == null) {
                    mFoundFragment = new FoundFragment();
                    ft.add(R.id.fl_content, mFoundFragment, TAG_FOUND_FRAGMENT);
                } else {
                    ft.show(fragment);
                }
                break;
        }
        ft.commit();
    }

    /**
     * 初始化状态框
     */
    private void initUpdateAvatarDialog() {

        mUpdateAvatarDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mUpdateAvatarDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mUpdateAvatarDialog.setTitleText("正在更换头像......");
        mUpdateAvatarDialog.setCancelable(false);

    }

    private void initListener() {
        //mCivAvatar.setOnClickListener(this);
        mIvAdd.setOnClickListener(this);
        mTvMsg.setOnClickListener(this);
        mTvFriend.setOnClickListener(this);
        mTvFound.setOnClickListener(this);
        mLlAvatar.setOnClickListener(this);
        mCivNvAvatar.setOnClickListener(this);
    }

    private void initNvView() {
        if (mDrawerLayout != null) {
            setupDrawerContent(mNvProfile);
        }
        View NvHeaderView = mNvProfile.inflateHeaderView(R.layout.navigation_header);
        mCivNvAvatar = (ImageView) NvHeaderView.findViewById(R.id.civ_nv_avatar);
        ImageView ivNvImg = (ImageView) NvHeaderView.findViewById(R.id.iv_nv_img);
        TextView tvNvName = (TextView) NvHeaderView.findViewById(R.id.tv_nv_name);
        ImageView ivNvWeather = (ImageView) NvHeaderView.findViewById(R.id.iv_nv_weather);
        TextView tvNvWeather = (TextView) NvHeaderView.findViewById(R.id.tv_nv_weather);
        TextView tvNvDate = (TextView) NvHeaderView.findViewById(R.id.tv_nv_date);

        tvNvName.setText(mUser.nickname);
        if (TextUtils.isEmpty(mUser.icon)) { // 判断如果用户没有设定头像，就设置默认头像
            if (mUser.sex.equals("男")) {
                mCivNvAvatar.setImageResource(R.mipmap.avatar_male);
                mCivAvatar.setImageResource(R.mipmap.avatar_male);
            } else if (mUser.sex.equals("女")) {
                mCivNvAvatar.setImageResource(R.mipmap.avatar_female);
                mCivAvatar.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (mUser.icon.substring(0, 4).equals("http")) {
                Picasso.with(MainActivity.this).load(mUser.icon).resize(200, 200).centerInside().into(mCivNvAvatar);
                Picasso.with(MainActivity.this).load(mUser.icon).resize(200, 200).centerInside().into(mCivAvatar);
            } else {
                Picasso.with(MainActivity.this).load(url_icon + mUser.icon).resize(200, 200).centerInside().into(mCivNvAvatar);
                Picasso.with(MainActivity.this).load(url_icon + mUser.icon).resize(200, 200).centerInside().into(mCivAvatar);
            }
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.item_own_data:
                                UIUtils.showShortToast(MainActivity.this, "个人资料");
                                break;
                            case R.id.item_setting:
                                mIntent = new Intent(MainActivity.this, SystemSettingActivity.class);
                                startActivity(mIntent);
                                break;
                            case R.id.item_about:
                                UIUtils.showShortToast(MainActivity.this, "关于作者");
                                break;

                        }
                        menuItem.setChecked(false);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_found:
                updateFragment(FRAGMENT_TYPE.LUNTAN);
                break;
            case R.id.tv_message:
                updateFragment(FRAGMENT_TYPE.MESSAGE);
                break;
            case R.id.tv_friend:
                updateFragment(FRAGMENT_TYPE.FRIEND);
                break;
            case R.id.iv_add:
                //IUtils.showShortToast(this, "添加好友");
                startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
                break;
            case R.id.ll_avatar:
                mDrawerLayout.openDrawer(Gravity.LEFT);//开启抽屉
                //UIUtils.showShortToast(this, "打开抽屉");
                break;
            case R.id.civ_nv_avatar:
                // 弹出设置头像对话框
                showPopupWindow(IMAGEPOP);
                UIUtils.showShortToast(this, "更改资料");
                // mDrawerLayout.closeDrawers();//关闭抽屉
                break;
            case R.id.third_popupwindow_layout_null:
                if (mPopImage != null) {
                    mPopImage.dismiss();
                }
                break;
            case R.id.third_popupwindow_layout_nulls:
                if (mPopGetPic != null) {
                    mPopGetPic.dismiss();
                }

                break;
            case R.id.third_popupwindow_layout_nullss:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                break;
            case R.id.third_popupwindow_textView_quxiao:
                if (mPopImage != null) {
                    mPopImage.dismiss();
                }

                break;
            case R.id.third_popupwindow_textView_quxiaoo:
                if (mPopGetPic != null) {
                    mPopGetPic.dismiss();

                }

                break;
            case R.id.third_popupwindow_textView_quxiaooo:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();
                }

                break;
            case R.id.third_popupwindow_textView_look:
                UIUtils.showShortToast(this, "查看大头像");

                break;
            case R.id.third_popupwindow_textView_change:
                showPopupWindow(GETPICPOP);
                break;
            case R.id.third_popupwindow_textView_status://设置状态

                showPopupWindow(SETSTATUSPOP);


                break;
            case R.id.third_popupwindow_textView_photo:
                UIUtils.showShortToast(this, "从相册选取");
                break;

            case R.id.third_popupwindow_textView_camera:
                UIUtils.showShortToast(this, "拍照");

                break;
            case R.id.third_popupwindow_textView_status_online:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.ONLINE);
                break;
            case R.id.third_popupwindow_textView_status_qme:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.QME);
                break;
            case R.id.third_popupwindow_textView_status_busy:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.BUSY);
                break;
            case R.id.third_popupwindow_textView_status_wurao:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.NOBOTHER);
                break;
            case R.id.third_popupwindow_textView_status_leave:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.LEAVE);
                break;
            case R.id.third_popupwindow_textView_status_yinshen:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(STATUS.INVISIBLE);
                break;
        }
    }

    /**
     * 初始化设置头像的popupwindow
     */
    private void initImgPop() {

        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View view = inflater.inflate(R.layout.third_image_popupwindow, null);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 创建PopupWindow对象
        mPopImage = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mPopImage.setOnDismissListener(this);
        mRlPopNull = (RelativeLayout) view
                .findViewById(R.id.third_popupwindow_layout_null);
        mTvPopStatus = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_status);
        mTvPopCancel = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_quxiao);
        mTvPopBigAvatar = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_look);
        mTvPopModify = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_change);
        mRlPopNull.setOnClickListener(this);
        mTvPopCancel.setOnClickListener(this);
        mTvPopStatus.setOnClickListener(this);
        mTvPopBigAvatar.setOnClickListener(this);
        mTvPopModify.setOnClickListener(this);
        // 需要设置一下此参数，点击外边可消失
        mPopImage.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        mPopImage.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        mPopImage.setFocusable(true);

    }

    /**
     * 初始化获取照片popupwindow
     */
    private void initGetPicPop() {

        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View view = inflater.inflate(R.layout.third_image_popupwindows, null);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 创建PopupWindow对象
        mPopGetPic = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mPopGetPic.setOnDismissListener(this);
        mRlPopGetPicNull = (RelativeLayout) view
                .findViewById(R.id.third_popupwindow_layout_nulls);
        mTvGetPicPopCancel = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_quxiaoo);
        mTvGetPicPopPhoto = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_photo);
        mTvGetPicPopCamera = (TextView) view
                .findViewById(R.id.third_popupwindow_textView_camera);
        mRlPopGetPicNull.setOnClickListener(this);
        mTvGetPicPopCancel.setOnClickListener(this);
        mTvGetPicPopPhoto.setOnClickListener(this);
        mTvGetPicPopCamera.setOnClickListener(this);
        // 需要设置一下此参数，点击外边可消失
        mPopGetPic.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        mPopGetPic.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        mPopGetPic.setFocusable(true);
    }

    /**
     * 初始化设置在线状态的popupWindow
     */
    private void initSetStatusPop() {

        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View view = inflater.inflate(R.layout.third_image_popupwindowss, null);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 创建PopupWindow对象
        mPopSetStatus = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mPopSetStatus.setOnDismissListener(this);
        mRlSetStatusNull = (RelativeLayout) view.findViewById(R.id.third_popupwindow_layout_nullss);
        mTvSetStatusPopCancel = (TextView) view.findViewById(R.id.third_popupwindow_textView_quxiaooo);
        mTvSetStatusPopOnline = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_online);
        mTvSetStatusPopQme = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_qme);
        mTvSetStatusPopBusy = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_busy);
        mTvSetStatusPopNoBother = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_wurao);
        mTvSetStatusPopLeave = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_leave);
        mTvSetStatusPopInvisible = (TextView) view.findViewById(R.id.third_popupwindow_textView_status_yinshen);

        mRlSetStatusNull.setOnClickListener(this);
        mTvSetStatusPopCancel.setOnClickListener(this);
        mTvSetStatusPopOnline.setOnClickListener(this);
        mTvSetStatusPopQme.setOnClickListener(this);
        mTvSetStatusPopBusy.setOnClickListener(this);
        mTvSetStatusPopNoBother.setOnClickListener(this);
        mTvSetStatusPopLeave.setOnClickListener(this);
        mTvSetStatusPopInvisible.setOnClickListener(this);
        // 需要设置一下此参数，点击外边可消失
        mPopGetPic.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        mPopGetPic.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        mPopGetPic.setFocusable(true);
    }


    /**
     * 显示popupwindow
     */
    private void showPopupWindow(POPCODE code) {
        if (code == IMAGEPOP) {
            if (mPopImage.isShowing()) {
                // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                mPopImage.dismiss();
            } else {
                // 显示窗口
                // pop.showAsDropDown(v);
                // 获取屏幕和PopupWindow的width和height
                mPopImage.setAnimationStyle(R.style.MenuAnimationFade);
                mPopImage.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                mPopImage.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                mPopImage.showAsDropDown(mIvNull, 0, 0);

                WindowManager.LayoutParams lp = getWindow()
                        .getAttributes();
                lp.alpha = 0.7f;
                getWindow().setAttributes(lp);

            }
        } else if (code == GETPICPOP) {
            if (mPopImage != null) {
                mPopImage.dismiss();
                if (mPopGetPic.isShowing()) {
                    // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                    mPopGetPic.dismiss();
                } else {
                    // 显示窗口
                    // pop.showAsDropDown(v);
                    // 获取屏幕和PopupWindow的width和height
                    mPopGetPic.setAnimationStyle(R.style.MenuAnimationFade);
                    mPopGetPic.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    mPopGetPic.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                    mPopGetPic.showAsDropDown(mIvNull, 0, 0);

                    WindowManager.LayoutParams lp = getWindow()
                            .getAttributes();
                    lp.alpha = 0.7f;
                    getWindow().setAttributes(lp);

                }

            }
        } else if (code == SETSTATUSPOP) {
            if (mPopImage != null) {
                mPopSetStatus.dismiss();
                if (mPopSetStatus.isShowing()) {
                    // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                    mPopSetStatus.dismiss();
                } else {
                    // 显示窗口
                    // pop.showAsDropDown(v);
                    // 获取屏幕和PopupWindow的width和height
                    mPopSetStatus.setAnimationStyle(R.style.MenuAnimationFade);
                    mPopSetStatus.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    mPopSetStatus.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                    mPopSetStatus.showAsDropDown(mIvNull, 0, 0);

                    WindowManager.LayoutParams lp = getWindow()
                            .getAttributes();
                    lp.alpha = 0.7f;
                    getWindow().setAttributes(lp);

                }

            }
        }

    }

    /**
     * 初始化图片,文字
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initTabContent() {
        mTvFound.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_found_icon, 0, 0);
        mTvFound.setTextColor(getResources().getColor(R.color.dark_gray));
        mTvFriend.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.tab_me_icon, 0, 0);
        mTvFriend.setTextColor(getResources().getColor(R.color.dark_gray));
        mTvMsg.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.tab_move_icon, 0, 0);
        mTvMsg.setTextColor(getResources().getColor(R.color.dark_gray));


    }

    @Override
    public void onDismiss() {

    }

    /**
     * 设置状态
     * @param status 状态
     */
    private void setPresence(STATUS status) {


    }
}
