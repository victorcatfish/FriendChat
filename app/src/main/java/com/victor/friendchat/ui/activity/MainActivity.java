package com.victor.friendchat.ui.activity;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.LocationManagerProxy;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppChat;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.global.MyApplication;
import com.victor.friendchat.ui.fragment.FoundFragment;
import com.victor.friendchat.ui.fragment.FriendFragment;
import com.victor.friendchat.ui.fragment.MessageFragment;
import com.victor.friendchat.uitl.Base64Coder;
import com.victor.friendchat.uitl.ImageCompressUtils;
import com.victor.friendchat.uitl.LogUtils;
import com.victor.friendchat.uitl.PhotoSelectedHelper;
import com.victor.friendchat.uitl.SetImageUtil;
import com.victor.friendchat.uitl.SharedPreferencesUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.xmpp.XmppReceiver;
import com.victor.friendchat.xmpp.XmppTool;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.GETPICPOP;
import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.IMAGEPOP;
import static com.victor.friendchat.ui.activity.MainActivity.POPCODE.SETSTATUSPOP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupWindow.OnDismissListener, AMapLocalWeatherListener {

    public static final String TAG_MSG_FRAGMENT = "tag_msg_fragment";
    private static final String TAG_FRIEND_FRAGMENT = "tag_friend_fragment";
    private static final String TAG_FOUND_FRAGMENT = "tag_found_fragment";
    public static MainActivity main;

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
    public MessageFragment mMsgFragment;
    public FriendFragment mFrindFragment;
    public FoundFragment mFoundFragment;
    private LocationManagerProxy mLocationManagerProxy;
    public XmppReceiver mXmppReceiver;
    PhotoSelectedHelper mPhotoSelectedHelper;
    String path;

    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mUpdateAvatarDialog.dismiss();
                if (mFoundFragment != null) {
                    mFoundFragment.updateData(mUser.icon);
                }
                if (mFoundFragment != null) {
                    mFoundFragment.update();
                }

                String filename = (String) msg.obj;
                Toast.makeText(MainActivity.this, "头像修改成功", Toast.LENGTH_SHORT).show();
                Picasso.with(MainActivity.this).load(new File(filename)).resize(200, 200).centerCrop().into(mCivNvAvatar);
                Picasso.with(MainActivity.this).load(new File(filename)).resize(200, 200).centerCrop().into(mCivAvatar);
            } else if (msg.what == 1) {
                mUpdateAvatarDialog.dismiss();
                Toast.makeText(MainActivity.this, "头像修改失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    XmppReceiver.updateActivity ua = new XmppReceiver.updateActivity() {
        @Override
        public void update(String type) {
            switch (type) {
                case "status":
                    if (mFrindFragment != null) {
                        mFrindFragment.getData();
                    }
                    break;
                case "tongyi":
                    if (mFrindFragment != null) {
                        mFrindFragment.initialData();
                    }
                    if (mFrindFragment != null) {
                        mFrindFragment.getData();
                    }
                    break;
                case "add":
                    LogUtils.sf("有添加好友申请进入-----------------");
                    mMsgFragment.initData();
                    break;
                case "jujue":
                case "chat":
                    if (mFrindFragment != null) {
                        mFrindFragment.initialData();
                    }
                    break;
            }
        }

        @Override
        public void update(XmppChat xc) {

        }
    };
    private ImageView mIvNvWeather;
    private ImageView mIvNvImg;
    private TextView mTvNvName;
    private TextView mTvNvWeather;
    private TextView mTvNvDate;
    private ImageView mIvNvStatus;


    enum POPCODE {
        IMAGEPOP,
        GETPICPOP,
        SETSTATUSPOP
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
        MyApplication.getInstance().addActivity(this);
        x.view().inject(this);
        main = this;
        mPhotoSelectedHelper = new PhotoSelectedHelper(MainActivity.this);
        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");
        initView();
        initListener();
        initImgPop();
        initGetPicPop();
        initSetStatusPop();
        initUpdateAvatarDialog();
        updateFragment(FRAGMENT_TYPE.MESSAGE);
    }

    private void initView() {
        mXmppReceiver = new XmppReceiver(ua);
        registerReceiver(mXmppReceiver, new IntentFilter("xmpp_receiver"));
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestWeatherUpdates(
                LocationManagerProxy.WEATHER_TYPE_LIVE, this);
        initNvView();
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
                mTvTitle.setText("消息");
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
                mTvTitle.setText("好友");
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
                mTvTitle.setText("动态");
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
        mIvNvImg = (ImageView) NvHeaderView.findViewById(R.id.iv_nv_img);
        mTvNvName = (TextView) NvHeaderView.findViewById(R.id.tv_nv_name);
        mIvNvStatus = (ImageView) NvHeaderView.findViewById(R.id.iv_nv_status);
        mIvNvWeather = (ImageView) NvHeaderView.findViewById(R.id.iv_nv_weather);
        mTvNvWeather = (TextView) NvHeaderView.findViewById(R.id.tv_nv_weather);
        mTvNvDate = (TextView) NvHeaderView.findViewById(R.id.tv_nv_date);

        mTvNvName.setText(mUser.nickname);
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
                //UIUtils.showShortToast(this, "更改资料");
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
                // UIUtils.showShortToast(this, "查看大头像");
                if (mPopImage != null) {
                    mPopImage.dismiss();

                }
                Intent intent = new Intent(this, ShowImageActivity.class);
                if (mUser.icon.equals("")) {
                    path = mUser.sex;
                } else {
                    path = mUser.icon;
                }
                intent.putExtra("path", path);
                intent.putExtra("type", "icon");
                startActivity(intent);
                break;
            case R.id.third_popupwindow_textView_change:
                showPopupWindow(GETPICPOP);
                break;
            case R.id.third_popupwindow_textView_status://设置状态

                showPopupWindow(SETSTATUSPOP);


                break;
            case R.id.third_popupwindow_textView_photo:
                //UIUtils.showShortToast(this, "从相册选取");
                if (mPopGetPic != null) {
                    mPopGetPic.dismiss();

                }
                if (mUser != null) {
                    mPhotoSelectedHelper.imageSelection(mUser.user, "pic");
                }

                break;

            case R.id.third_popupwindow_textView_camera:
                //UIUtils.showShortToast(this, "拍照");
                if (mPopGetPic != null) {
                    mPopGetPic.dismiss();

                }
                if (mUser != null) {
                    mPhotoSelectedHelper.imageSelection(mUser.user, "take");
                }

                break;
            case R.id.third_popupwindow_textView_status_online:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.ONLINE);
                break;
            case R.id.third_popupwindow_textView_status_qme:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.QME);
                break;
            case R.id.third_popupwindow_textView_status_busy:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.BUSY);
                break;
            case R.id.third_popupwindow_textView_status_wurao:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.NOBOTHER);
                break;
            case R.id.third_popupwindow_textView_status_leave:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.LEAVE);
                break;
            case R.id.third_popupwindow_textView_status_yinshen:
                if (mPopSetStatus != null) {
                    mPopSetStatus.dismiss();

                }
                setPresence(XmppTool.STATUS.INVISIBLE);
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
    public void onWeatherLiveSearched(AMapLocalWeatherLive aMapLocalWeatherLive) {
        if (aMapLocalWeatherLive != null && aMapLocalWeatherLive.getAMapException().getErrorCode() == 0) {
            String city = aMapLocalWeatherLive.getCity();//城市
            String weather = aMapLocalWeatherLive.getWeather();//天气情况
            String windDir = aMapLocalWeatherLive.getWindDir();//风向
            String windPower = aMapLocalWeatherLive.getWindPower();//风力
            String humidity = aMapLocalWeatherLive.getHumidity();//空气湿度
            String reportTime = aMapLocalWeatherLive.getReportTime();//数据发布时间
            String wendu = aMapLocalWeatherLive.getTemperature();//温度
            mTvNvWeather.setText(city + ":\t" + wendu + "℃");
            mTvNvDate.setText(weather + "\n" + reportTime);

            if (weather.contains("雨")) {
                mIvNvWeather.setImageResource(R.mipmap.yu_60);
            } else if (weather.equals("阴")) {
                mIvNvWeather.setImageResource(R.mipmap.yun_26);
            } else if (weather.contains("云")) {
                mIvNvWeather.setImageResource(R.mipmap.c_28);
            } else if (weather.contains("雪")) {
                mIvNvWeather.setImageResource(R.mipmap.c_14);
            } else if (weather.equals("晴")) {
                mIvNvWeather.setImageResource(R.mipmap.c_32);
            } else if (weather.contains("沙")) {
                mIvNvWeather.setImageResource(R.mipmap.c_62);
            } else if (weather.contains("霾") || weather.contains("雾")) {
                mIvNvWeather.setImageResource(R.mipmap.c_63);
            }
            //  tv_me_name.setText(wendu+"\n"+city+"\n"+weather+"\n"+windDir+"\n"+windPower+"\n"+humidity+"\n"+reportTime);

        } else {
            // 获取天气预报失败

        }
    }

    @Override
    public void onWeatherForecaseSearched(AMapLocalWeatherForecast aMapLocalWeatherForecast) {
        UIUtils.showShortToast(this, "获取天气预报失败:" + aMapLocalWeatherForecast.getAMapException().getErrorMessage());
    }

    @Override
    public void onDismiss() {

    }

    /**
     * 设置状态
     * @param status 状态
     */
    private void setPresence(XmppTool.STATUS status) {
        XmppTool.getInstance().setPresence(status);
        SharedPreferencesUtil.setInt(MainActivity.this, "status", mUser.user + "status", status.ordinal());
        XmppTool.getInstance().setPresence(mIvStatus, mIvNvStatus, this, mUser.user);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XmppTool.disConnectServer();
        unregisterReceiver(mXmppReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == mPhotoSelectedHelper.TAKE_PHOTO) {
            if (!(resultCode == RESULT_OK)) {
                return;
            }

            if (data != null) {
                mPhotoSelectedHelper.cropImageUri(data.getData(), 200, 200, mUser.user);
                mPhotoSelectedHelper.cropImageUri(mPhotoSelectedHelper.getCaptureUri(), 200, 200, mUser.user);
            }


        } else if (requestCode == mPhotoSelectedHelper.PHOTO_CROP) {
            if (!(resultCode == RESULT_OK)) {
                return;
            }
            final String cropPath = mPhotoSelectedHelper.getCropPath();
            if (cropPath != null) {
                mUpdateAvatarDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        upload(cropPath, "tack");
                    }
                }.start();


            }

        } else if (requestCode == mPhotoSelectedHelper.PIC_PHOTO) {
            if (data == null) {
                return;
            } else {
                Uri uri = data.getData();
                if (uri != null) {
                    path = SetImageUtil.getPath(this, uri);
                    if (path != null) {
                        mUpdateAvatarDialog.show();
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                upload(path, "pic");
                            }
                        }.start();


                    }

                }
            }
        } else if (requestCode == 199) {
            if (data == null) {
                return;
            }
            String nickname = data.getStringExtra("nickname");
            String sex = data.getStringExtra("sex");
            String qq = data.getStringExtra("qq");
            String years = data.getStringExtra("years");

            mUser.nickname = nickname;
            mUser.sex = sex;
            mUser.qq = qq;
            mUser.years = years;
            mTvNvName.setText(nickname);
        } else if (resultCode == 200) {
            if (mFoundFragment != null) {
                mFoundFragment.update();
            }

        }

    }

    // 上传
    public void upload(final String paths, String type) {

        String filename = paths.substring(paths.lastIndexOf("/") + 1);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ImageCompressUtils.getimage(paths).compress(Bitmap.CompressFormat.JPEG,
                50, stream);
        byte[] b = stream.toByteArray();
        // 将图片流以字符串形式存储下来
        String file = new String(Base64Coder.encodeLines(b));


        RequestParams params = new RequestParams(Constant.URL.DO_GET_URSER);
        params.addHeader("Accept",
                "text/javascript, text/html, application/xml, text/xml");
        params.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
        params.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        params.addHeader("Connection", "Keep-Alive");
        params.addHeader("Cache-Control", "no-cache");
        params.addHeader("Content-Type", "application/x-www-form-urlencoded");

        params.addBodyParameter(Constant.RequestParamNames.file, file);
        if (type.equals("pic")) {
            filename = mUser.user + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        }

        params.addBodyParameter(Constant.RequestParamNames.filename, filename);
        params.addBodyParameter(Constant.RequestParamNames.user, mUser.user);
        params.addBodyParameter(Constant.RequestParamNames.action, "update_icon");


        final String finalFilename = filename;
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                path = paths;
                mUser.icon = finalFilename;
                System.out.println("上传完成");
                Message m = new Message();
                m.what = 0;
                m.obj = path;

                h.sendMessage(m);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                System.out.println("上传失败");
                mUpdateAvatarDialog.dismiss();
                Message m = new Message();
                m.what = 1;

                h.sendMessage(m);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                System.out.println("上传失败");
                mUpdateAvatarDialog.dismiss();
                Message m = new Message();
                m.what = 1;
                h.sendMessage(m);
            }

            @Override
            public void onFinished() {

            }
        });
    }
}
