package com.victor.friendchat.ui.activity;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppChat;
import com.victor.friendchat.domain.XmppFriend;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.uitl.SaveUserUtil;
import com.victor.friendchat.uitl.TimeUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.widget.MsgListView;
import com.victor.friendchat.xmpp.XmppContentProvider;
import com.victor.friendchat.xmpp.XmppFriendMessageProvider;
import com.victor.friendchat.xmpp.XmppReceiver;
import com.victor.friendchat.xmpp.XmppService;
import com.victor.friendchat.xmpp.XmppTool;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends BaseActivity implements MsgListView.IXListViewListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener, EmojiconGridFragment.OnEmojiconClickedListener {

    public static ChatActivity ca;
    public static XmppFriend sXmppFriend;

    @ViewInject(R.id.mlv_chat)
    private MsgListView mMsgListView;
    @ViewInject(R.id.iv_add_emoji)
    private ImageView mIvAddEmoji;
    @ViewInject(R.id.tv_send)
    private TextView mTvSend;
    @ViewInject(R.id.eet_input)
    private EmojiconEditText mEetInput;
    @ViewInject(R.id.fl_emojicons)
    private FrameLayout mflEmojicons;

    List<XmppChat> mXmppChats;
    private ChatAdapter mAdapter;
    private ChatManager mChatManager;
    private Chat mChat;
    boolean bool = false;

    XmppReceiver.updateActivity uc = new XmppReceiver.updateActivity() {

        @Override
        public void update(String type) {

        }


        @Override
        public void update(XmppChat xc) {
            if (mXmppChats != null) {

                mXmppChats.add(xc);
                mAdapter.setData(mXmppChats);


            }

        }
    };
    private XmppReceiver mXmppReceiver;
    private User mUser;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(sXmppFriend.user.nickname);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
        initialData();
    }

    @Override
    protected void initView() {
        ca = this;
        mXmppReceiver = new XmppReceiver(uc);
        registerReceiver(mXmppReceiver, new IntentFilter("xmpp_receiver"));
        mUser = SaveUserUtil.loadAccount(this);
        mTvSend.setEnabled(false);
        sXmppFriend = (XmppFriend) getIntent().getSerializableExtra("xmpp_friend");

        setEmojiconFragment(false);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setPullRefreshEnable(false);
        mEetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1) {
                    mTvSend.setEnabled(false);
                    mTvSend.setBackgroundResource(R.drawable.chat_text_send_nomal);

                } else {
                    mTvSend.setEnabled(true);
                    mTvSend.setBackgroundResource(R.drawable.chat_text_send_foc);
                }
            }
        });

    }

    /**
     * 初始化数据
     */
    public void initialData() {

        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                mXmppChats = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String main = cursor.getString(cursor.getColumnIndex("main"));
                    String user = cursor.getString(cursor.getColumnIndex("user"));
                    String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                    String icon = cursor.getString(cursor.getColumnIndex("icon"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    String sex = cursor.getString(cursor.getColumnIndex("sex"));
                    String too = cursor.getString(cursor.getColumnIndex("too"));
                    String times = cursor.getString(cursor.getColumnIndex("time"));
                    long time = Long.parseLong(times);
                    int viewType = cursor.getInt(cursor.getColumnIndex("viewtype"));
                    XmppChat xm = new XmppChat(main, user, nickname, icon, type, content, sex, too, viewType, time);
                    Log.i("mChat》》》》》》》》》》》", too + "\n" + user.toLowerCase());
                    mXmppChats.add(xm);
                }
                mAdapter = new ChatAdapter();
                mMsgListView.setAdapter(mAdapter);
                mMsgListView.setSelection(mAdapter.getCount() - 1);
            }

        }.startQuery(0, null, XmppFriendMessageProvider.CONTENT_CHATS_URI, null,
                "main=?", new String[]{mUser.user + sXmppFriend.user.user}, null);

        connectionChat();
    }

    /**
     * 建立聊天
     */
    private void connectionChat() {
        mChatManager = XmppTool.getInstance().getConn().getChatManager();
        mChat = mChatManager.createChat(sXmppFriend.user.user.toLowerCase() + "@" + XmppTool.getInstance().getConn().getServiceName(), null);
        Log.i("mChat》》》》》》》》》》》", sXmppFriend.user.user.toLowerCase() + "@" + XmppTool.getInstance().getConn().getServiceName());

    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    protected void initListener() {
        mMsgListView.setXListViewListener(this);
        mIvAddEmoji.setOnClickListener(this);
        mTvSend.setOnClickListener(this);
        mEetInput.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_emoji:
                if (!bool) {
                    hintKbTwo();
                    mflEmojicons.setVisibility(View.VISIBLE);
                    bool = true;
                } else {
                    mflEmojicons.setVisibility(View.GONE);
                    bool = false;
                }

                break;
            case R.id.tv_send:
                if (XmppTool.getInstance().isConnection() == false) {
                    UIUtils.showShortToast(ChatActivity.this, "已断开,正在重连中....");
                    return;
                }
                try {

                    mChat.sendMessage(mEetInput.getText().toString());
                    ContentValues values = new ContentValues();
                    values.put("content", mEetInput.getText().toString());
                    values.put("time", TimeUtil.getDate());
                    values.put("result", 0);
                    XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "main=? and type=?", new String[]{mUser.user + sXmppFriend.user.user, "chat"});
                    mTvSend.setEnabled(false);
                    XmppChat xc = new XmppChat(mUser.user + sXmppFriend.user.user, mUser.user, mUser.nickname, mUser.icon, 1, mEetInput.getText().toString(), mUser.sex, sXmppFriend.user.user, 1, new Date().getTime());
                    mXmppChats.add(xc);
                    XmppFriendMessageProvider.add_message(xc);
                    mAdapter.setData(mXmppChats);
                    mEetInput.setText("");

                } catch (XMPPException e) {
                    e.printStackTrace();
                    UIUtils.showShortToast(ChatActivity.this, "发送失败,请检查网络是否异常");
                }

                break;
            case R.id.eet_input:
                if (bool) {
                    mflEmojicons.setVisibility(View.GONE);
                    bool = false;
                }
                break;
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ca = null;
                if (MainActivity.main != null) {
                    if (MainActivity.main.mMsgFragment != null) {
                        ContentValues values = new ContentValues();
                        values.put("result", 0);
                        XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "main=? and type=?", new String[]{mUser.user + sXmppFriend.user.user, "chat"});
                        MainActivity.main.mMsgFragment.initData();
                    }
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEetInput, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEetInput);
    }

    class ChatAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public ChatAdapter() {
            this.mInflater = LayoutInflater.from(ChatActivity.this);
        }

        public void setData(List<XmppChat> lists) {
            mXmppChats = lists;
            mAdapter.notifyDataSetChanged();
            mMsgListView.setSelection(lists.size());
        }

        @Override
        public int getCount() {
            return mXmppChats.size();
        }

        @Override
        public Object getItem(int position) {
            return mXmppChats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {

            return mXmppChats.get(position).viewType;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String url_icon = Constant.URL.ICON + "?name=";
            ViewHolder viewHolder;
            int type = mXmppChats.get(position).type;
            if (convertView == null || convertView.getTag(R.mipmap.ic_launcher + type) == null) {

                if (type == 2) {
                    convertView = mInflater.inflate(R.layout.chat_intput, parent, false);
                } else {
                    convertView = mInflater.inflate(R.layout.chat_output, parent, false);
                }
                viewHolder = buildHolder(convertView);
                convertView.setTag(R.mipmap.ic_launcher + type, viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.mipmap.ic_launcher
                        + type);
            }
            viewHolder.tv_content.setText(mXmppChats.get(position).content);
            viewHolder.tv_name.setText(mXmppChats.get(position).nickname);
            if (mXmppChats.get(position).time == 0) {
                viewHolder.tv_time.setText("");
            } else {
                viewHolder.tv_time.setText(TimeUtil.getChatTime(mXmppChats.get(position).time));
            }

            if (mXmppChats.get(position).icon.equals("")) {
                if (mXmppChats.get(position).sex.equals("男")) {
                    viewHolder.iv_icon.setImageResource(R.mipmap.avatar_male);
                } else {
                    viewHolder.iv_icon.setImageResource(R.mipmap.avatar_female);
                }
            } else {
                if (mXmppChats.get(position).icon.substring(0, 4).equals("http")) {
                    Picasso.with(ChatActivity.this).load(mXmppChats.get(position).icon)
                            .resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                            .into(viewHolder.iv_icon);
                } else {
                    Picasso.with(ChatActivity.this).load(url_icon + mXmppChats.get(position).icon)
                            .resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                            .into(viewHolder.iv_icon);
                }
            }


            return convertView;
        }
    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_content = (EmojiconTextView) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.iv_icon = (CircleImageView) convertView.findViewById(R.id.iv_icon);
        return holder;
    }

    private static class ViewHolder {
        EmojiconTextView tv_content;
        TextView tv_name;
        TextView tv_time;
        CircleImageView iv_icon;

    }

    @Override
    public void onBackPressed() {
        if (bool) {
            mflEmojicons.setVisibility(View.GONE);
            bool = false;
        } else {
            ca = null;
            if (MainActivity.main != null) {
                if (MainActivity.main.mMsgFragment != null) {
                    ContentValues values = new ContentValues();
                    values.put("result", 0);
                    XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "main=? and type=?", new String[]{mUser.user + sXmppFriend.user.user, "chat"});
                    MainActivity.main.mMsgFragment.initData();
                }
            }
            finish();

        }
    }

    /**
     * 关闭软键盘
     */
    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mXmppReceiver);
    }
}
