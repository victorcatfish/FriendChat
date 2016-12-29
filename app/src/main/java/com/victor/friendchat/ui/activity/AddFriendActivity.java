package com.victor.friendchat.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppUser;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.xmpp.XmppTool;

import org.xutils.view.annotation.ViewInject;

import java.lang.ref.WeakReference;
import java.util.List;

/***
 * 添加好友的activity
 */
public class AddFriendActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @ViewInject(R.id.et_search_user)
    private EditText mEtSearch;
    @ViewInject(R.id.btn_search_user)
    private Button mBtnSearch;
    @ViewInject(R.id.lv_search_user_result)
    private ListView mLvResult;
    private List<XmppUser> mXmppUsers;

    private AddFriendHandler mHandler = new AddFriendHandler(this);


    static class AddFriendHandler extends Handler {
        WeakReference<AddFriendActivity> mActivityReference;

        public AddFriendHandler(AddFriendActivity activity) {
            mActivityReference = new WeakReference(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AddFriendActivity activity = mActivityReference.get();
            if (activity != null) {
                handleMessage(msg, activity);
            }
        }
        private void handleMessage(Message msg, AddFriendActivity activity) {
            if (msg.what == 1) {
                if (activity.mXmppUsers != null) {
                    activity.mLvResult.setAdapter(activity.new AddFriendAdapter());
                }

            }
        }

    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_add_friend;
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.add_friend);
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
        mBtnSearch.setOnClickListener(this);
        mLvResult.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search_user:
                if (XmppTool.getInstance().isConnection() == false) {
                    UIUtils.showShortToast(this, "已断开,正在重连中....");
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        mXmppUsers = XmppTool.getInstance().searchUsers(
                                mEtSearch.getText().toString().trim());
                        Message m = new Message();
                        m.what = 1;
                        mHandler.sendMessage(m);
                    }
                }.start();
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


    class AddFriendAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mXmppUsers != null && mXmppUsers.size() > 0) {
                return mXmppUsers.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mXmppUsers != null && mXmppUsers.size() > 0) {
                return mXmppUsers.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(UIUtils.getContext(), R.layout.item_add_friend, null);
                holder.mTvName = (TextView) convertView.findViewById(R.id.tv_item_name);
                holder.mTvCity = (TextView) convertView.findViewById(R.id.tv_item_city);
                holder.mCivAvatar = (CircleImageView) convertView.findViewById(R.id.civ_item_avatar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            User user = new Gson().fromJson(mXmppUsers.get(position).name, User.class);
            holder.mTvName.setText(user.nickname);
            if (user.city == null || user.city.equals("")) {
                user.city = "未知星球";
            }
            holder.mTvCity.setText(user.city);
            if (user.icon.equals("")) {
                if (user.sex.equals("男")) {
                    holder.mCivAvatar.setImageResource(R.mipmap.avatar_male);
                } else {
                    holder.mCivAvatar.setImageResource(R.mipmap.avatar_female);
                }
            } else {
                if (user.icon.substring(0, 4).equals("http")) {
                    Picasso.with(AddFriendActivity.this).load(user.icon).resize(200, 200)
                            .placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend)
                            .centerInside().into(holder.mCivAvatar);
                } else {
                    Picasso.with(AddFriendActivity.this).
                            load(Constant.URL.ICON + "?name=" + user.icon)
                            .resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend).
                            centerInside().into(holder.mCivAvatar);
                }
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView mTvName;
        TextView mTvCity;
        CircleImageView mCivAvatar;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (XmppTool.getInstance().isConnection() == false) {
            UIUtils.showShortToast(AddFriendActivity.this, "已断开,正在重连中....");
            return;
        }
        Intent intent = new Intent(AddFriendActivity.this, ShowMessageActivity.class);
        User user = new Gson().fromJson(mXmppUsers.get(position).name, User.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}
