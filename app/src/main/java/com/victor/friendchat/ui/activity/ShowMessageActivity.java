package com.victor.friendchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppFriend;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.fragment.UserMessageFragment;
import com.victor.friendchat.ui.fragment.UserStatusFragment;
import com.victor.friendchat.uitl.LogUtils;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.xmpp.XmppService;
import com.victor.friendchat.xmpp.XmppTool;

import org.xutils.view.annotation.ViewInject;

public class ShowMessageActivity extends BaseActivity {

    @ViewInject(R.id.civ_show_message_avatar)
    private CircleImageView mCivAvatar;
    @ViewInject(R.id.tv_show_message_name)
    private TextView mTvName;
    @ViewInject(R.id.tv_show_message_city)
    private TextView mTvCity;
    @ViewInject(R.id.tl_show_message)
    private TabLayout mTabLayout;
    @ViewInject(R.id.vp_show_message)
    private ViewPager mViewPager;
    @ViewInject(R.id.btn_show_message_add)
    private Button mBtnAdd;

    private User mUsrs;
    private UserMessageFragment mUserMsgFragment;
    private UserStatusFragment mUserStatusFragment;
    private String mName;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_show_message;
    }

    @Override
    protected void initView() {
        mUsrs = (User) getIntent().getSerializableExtra("user");
        if (mUsrs != null) {
            Gson gson = new Gson();
            mName = gson.toJson(mUsrs);
        }
        LogUtils.sf (mUsrs.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
        initViewPager();
    }

    @Override
    protected void initToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void initListener() {
        mBtnAdd.setOnClickListener(this);
        mCivAvatar.setOnClickListener(this);
    }

    private void refreshView() {
        mTvName.setText(mUsrs.nickname);
        if (TextUtils.isEmpty(mUsrs.city)) {
            mTvCity.setText("未知星球");
        } else {
            mTvCity.setText(mUsrs.city);
        }

        if (TextUtils.isEmpty(mUsrs.icon)) {
            if (mUsrs.sex.equals("男")) {
                mCivAvatar.setImageResource(R.mipmap.avatar_male);
            } else {
                mCivAvatar.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (mUsrs.icon.substring(0, 4).equals("http")) {
                Picasso.with(ShowMessageActivity.this)
                        .load(mUsrs.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend)
                        .centerInside().into(mCivAvatar);
            } else {
                Picasso.with(ShowMessageActivity.this)
                        .load(Constant.URL.ICON + "?name=" + mUsrs.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside().into(mCivAvatar);
            }
        }
        mCivAvatar.setOnClickListener(ShowMessageActivity.this);
        if (XmppService.user.equals(mUsrs.user)) {
            mBtnAdd.setVisibility(View.GONE);
        } else {
            mBtnAdd.setVisibility(View.VISIBLE);
            if (XmppTool.getInstance().isFriendly(mUsrs.user)) {
                mBtnAdd.setText("发信息");
            } else {
                mBtnAdd.setText("加好友");
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.civ_show_message_avatar:
                if (mUsrs != null) {
                    Intent in = new Intent(this, ShowImageActivity.class);
                    String path = "";
                    if (mUsrs.icon.equals("")) {
                        path = mUsrs.sex;
                    } else {
                        path = mUsrs.icon;
                    }
                    in.putExtra("path", path);
                    in.putExtra("type", "icon");
                    startActivity(in);
                }
                break;
            case R.id.btn_show_message_add:
                if (mBtnAdd.getText().equals("发信息")) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("xmpp_friend", new XmppFriend(mUsrs));
                    startActivity(intent);
                    finish();

                } else if (mBtnAdd.getText().equals("加好友")) {
                    //15612610827;江城北望丶;15612610827_20151202_232508.jpg;男

                    Log.i("search>>>>", mUsrs.nickname + "\t" + mUsrs.user);
                    if (XmppTool.getInstance().addUser(
                            mUsrs.user + "@" + XmppTool.getInstance().getConn().getServiceName(),
                            mUsrs.nickname, null)) {
                        XmppTool.getInstance().addUserToGroup(
                                mUsrs.user + "@" + XmppTool.getInstance().getConn().getServiceName(),
                                "我的好友");
                        Log.i("search",
                                "申请添加" + mUsrs.user + "@"
                                        + XmppTool.getInstance().getConn().getServiceName() + "为好友");
                        UIUtils.showShortToast(ShowMessageActivity.this, "请求已发送");
                    } else {
                        UIUtils.showShortToast(ShowMessageActivity.this, "请求发送失败，请稍后再试");
                        Log.i("search", "添加失败");
                    }
                }
                break;
        }
    }

    private void initViewPager() {
        FragmentManager manager = this.getSupportFragmentManager();
        if (manager != null) {
            MessageFragmentPagerAdapter adapter = new MessageFragmentPagerAdapter(manager);
            mViewPager.setAdapter(adapter);
            mTabLayout.setupWithViewPager(mViewPager);
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        }
    }

    class MessageFragmentPagerAdapter extends FragmentPagerAdapter {

        String str[] = {"资料", "动态"};

        public MessageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return getFragment(position);
        }

        @Override
        public int getCount() {
            return str.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return str[position];
        }
    }


    private Fragment getFragment(int position) {

        switch (position) {
            case 0:
                if (mUserMsgFragment == null) {
                    mUserMsgFragment = new UserMessageFragment();
                    Bundle bd = new Bundle();
                    bd.putSerializable("user", mUsrs);
                    mUserMsgFragment.setArguments(bd);
                }
                return mUserMsgFragment;

            case 1:

                if (mUserStatusFragment == null) {
                    mUserStatusFragment = new UserStatusFragment();
                    Bundle bd = new Bundle();
                    bd.putSerializable("user", mUsrs);
                    mUserStatusFragment.setArguments(bd);
                }
                return mUserStatusFragment;
        }

        return null;
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
