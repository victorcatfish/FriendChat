package com.victor.friendchat.ui.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppMessage;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.uitl.SharedPreferencesUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.xmpp.XmppContentProvider;
import com.victor.friendchat.xmpp.XmppService;
import com.victor.friendchat.xmpp.XmppTool;

import org.jivesoftware.smack.packet.Presence;
import org.xutils.view.annotation.ViewInject;

public class TransactionFriendActivity extends BaseActivity {

    private static final String url_icon = Constant.URL.ICON + "?name=";

    @ViewInject(R.id.tv_transaction_name)
    private TextView mTvName;
    @ViewInject(R.id.civ_transaction_avatar)
    private CircleImageView mCirAvatar;
    @ViewInject(R.id.tv_transaction_gender)
    private TextView mTvGender;
    @ViewInject(R.id.tv_apply_date)
    private TextView mTvApplyDate;
    @ViewInject(R.id.tv_agree)
    private TextView mTvAgree;
    @ViewInject(R.id.tv_refuse)
    private TextView mTvRefuse;
    @ViewInject(R.id.ll_response)
    private LinearLayout mLlResponse;
    @ViewInject(R.id.tv_tips)
    private TextView mTvTips;
    @ViewInject(R.id.rl_msg)
    private RelativeLayout mRlMsg;

    XmppMessage mXmppMsg;
    private User mUsers;
    private Presence mPresence;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_transaction_friend;
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        mXmppMsg = (XmppMessage) intent.getSerializableExtra("xmpp_user");
        if (mXmppMsg != null) {
            mUsers = new Gson().fromJson(mXmppMsg.user.name, User.class);
            refreshView();
        }
    }

    private void refreshView() {
        mTvName.setText(mUsers.nickname);
        mTvApplyDate.setText("申请时间: " + mXmppMsg.time);
        if (mXmppMsg.type.equals("add")) {
            if (mXmppMsg.result == 1) {
                mLlResponse.setVisibility(View.VISIBLE);
                mTvTips.setVisibility(View.GONE);

            } else {
                mLlResponse.setVisibility(View.GONE);
                if (mXmppMsg.result == 0) {
                    mTvTips.setText("已同意" + "该申请");
                } else if (mXmppMsg.result == -1) {
                    mTvTips.setText("已拒绝" + "该申请");
                }
                mTvTips.setVisibility(View.VISIBLE);

            }
        } else if (mXmppMsg.type.equals("tongyi") || mXmppMsg.type.equals("jujue")) {
            mLlResponse.setVisibility(View.GONE);
            mTvTips.setVisibility(View.VISIBLE);
            mTvTips.setText(mXmppMsg.content);
            if (mXmppMsg.result == 1) {
                ContentValues values = new ContentValues();
                values.put("result", 0);
                XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?",
                        new String[]{mXmppMsg.id + ""});
                SharedPreferencesUtil.setFriendMessageNumber_subone(this, mXmppMsg.to);
                Intent intents = new Intent("xmpp_receiver");
                intents.putExtra("type", mXmppMsg.type);
                sendBroadcast(intents);
            }


        }

        Drawable nav_up = null;
        if (mUsers.sex.equals("男")) {
            mTvGender.setText("男");
            nav_up = getResources().getDrawable(R.mipmap.icon_male);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
            mTvGender.setCompoundDrawables(null, null, nav_up, null);
        } else {
            mTvGender.setText("女");
            nav_up = getResources().getDrawable(R.mipmap.icon_female);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
            mTvGender.setCompoundDrawables(null, null, nav_up, null);
        }
        if (mUsers.icon.equals("")) {
            if (mUsers.sex.equals("男")) {
                mCirAvatar.setImageResource(R.mipmap.avatar_male);
                mTvGender.setText("男");
            } else {
                mTvGender.setText("女");
                mCirAvatar.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (mUsers.icon.substring(0, 4).equals("http")) {
                Picasso.with(this).load(mUsers.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside().into(mCirAvatar);
            } else {
                Picasso.with(this).load(url_icon + mUsers.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside().into(mCirAvatar);
            }
        }
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.friend_apply);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void initListener() {
        mTvAgree.setOnClickListener(this);
        mTvRefuse.setOnClickListener(this);
        mRlMsg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_msg:
                if (XmppTool.getInstance().isConnection() == false) {
                    UIUtils.showShortToast(this, "已断开,正在重连中....");
                    return;
                }
                Intent in = new Intent(this, ShowMessageActivity.class);
                in.putExtra("user", mXmppMsg.user);
                startActivity(in);
                break;
            case R.id.tv_agree:
                mPresence = new Presence(Presence.Type.subscribed);// 同意是subscribed
                // 拒绝是unsubscribe
                mPresence.setTo(mXmppMsg.user.userName + "@" + XmppTool.getInstance().getConn().getServiceName());// 接收方jid
                mPresence.setFrom(mXmppMsg.to + "@" + XmppTool.getInstance().getConn().getServiceName());// 发送方jid
                XmppTool.getInstance().getConn().sendPacket(mPresence);// connection是你自己的XMPPConnection链接
                if (XmppTool.getInstance().addUser(
                        mXmppMsg.user.userName + "@" + XmppTool.getInstance().getConn().getServiceName(),
                        mXmppMsg.user.name, "我的好友")) {
                    XmppTool.getInstance().addUserToGroup(
                            mXmppMsg.user.userName + "@" + XmppTool.getInstance().getConn().getServiceName(),
                            "我的好友");
                    Log.i("transaction", "添加好友");

                }
                ContentValues values = new ContentValues();
                values.put("result", 0);
                XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?",
                        new String[]{mXmppMsg.id + ""});
                SharedPreferencesUtil.setFriendMessageNumber_subone(this, mXmppMsg.to);
                mLlResponse.setVisibility(View.GONE);
                mTvTips.setVisibility(View.VISIBLE);
                mTvTips.setText("已同意该申请");
                Intent intent = new Intent("xmpp_receiver");
                intent.putExtra("type", "tongyi");
                sendBroadcast(intent);

                break;
            case R.id.tv_refuse:
                mPresence = new Presence(Presence.Type.unsubscribe);// 同意是subscribed
                // 拒绝是unsubscribe
                mPresence.setTo(mXmppMsg.user.userName + "@" + XmppTool.getInstance().getConn().getServiceName());// 接收方jid
                mPresence.setFrom(mXmppMsg.to + "@" + XmppTool.getInstance().getConn().getServiceName());// 发送方jid
                XmppTool.getInstance().getConn().sendPacket(mPresence);// connection是你自己的XMPPConnection链接
                ContentValues valuess = new ContentValues();
                valuess.put("result", -1);
                XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, valuess, "id=?", new String[]{mXmppMsg.id + ""});
                SharedPreferencesUtil.setFriendMessageNumber_subone(this, mXmppMsg.to);
                mLlResponse.setVisibility(View.GONE);
                mTvTips.setVisibility(View.VISIBLE);
                mTvTips.setText("已拒绝该申请");
                Intent intents = new Intent("xmpp_receiver");
                intents.putExtra("type", "jujue");
                sendBroadcast(intents);
                break;
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
