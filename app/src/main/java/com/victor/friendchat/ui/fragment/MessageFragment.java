package com.victor.friendchat.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppFriend;
import com.victor.friendchat.domain.XmppMessage;
import com.victor.friendchat.domain.XmppUser;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.activity.ChatActivity;
import com.victor.friendchat.ui.activity.MainActivity;
import com.victor.friendchat.ui.activity.TransactionFriendActivity;
import com.victor.friendchat.uitl.RecyclerViewDividerItemDecoration;
import com.victor.friendchat.uitl.SaveUserUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.BadgeView;
import com.victor.friendchat.widget.CircleImageView;
import com.victor.friendchat.xmpp.XmppContentProvider;
import com.victor.friendchat.xmpp.XmppService;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 2016/12/28.
 */
public class MessageFragment extends Fragment{

    private static final String url_icon = Constant.URL.ICON + "?name=";

    @ViewInject(R.id.rv_fragment_messgae)
    private RecyclerView mRecyclerView;
    @ViewInject(R.id.iv_fragment_messgae)
    private ImageView mImageView;

    private String mUser;
    List<XmppMessage> mXmppMsgs;
    List<XmppMessage> mXmppAddMsg;
    private MessageAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        x.view().inject(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));//设置排列方式
        mRecyclerView.addItemDecoration(new RecyclerViewDividerItemDecoration(getActivity(), RecyclerViewDividerItemDecoration.VERTICAL_LIST));
        initData();
        return view;
    }

    public void initData() {
        mUser = SaveUserUtil.loadAccount(getActivity()).user;
        mXmppMsgs = new ArrayList<>();
        mXmppAddMsg = new ArrayList<>();
        //Cursor cursor = XmppService.resolver.query(XmppContentProvider.CONTENT_MESSAGES_URI, null, "select * from message where too=?", new String[]{SaveUserUtil.loadAccount(getActivity()).getUser()}, null);
        Cursor cursor = XmppService.resolver.query(XmppContentProvider.CONTENT_MESSAGES_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndex("type"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String to = cursor.getString(cursor.getColumnIndex("too"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            XmppUser user = new XmppUser(username, name);
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            int result = cursor.getInt(cursor.getColumnIndex("result"));
            String main = cursor.getString(cursor.getColumnIndex("main"));
            XmppMessage xm = new XmppMessage(id, to, type, user, time, content, result, main);

            if (to.equals(this.mUser)) {
                Log.i("message》》》》》》》》》》》", xm.toString() + "\n" + this.mUser);
                mXmppMsgs.add(xm);
            }

            //            }

        }

        if (mXmppMsgs.size() < 1) {
            mRecyclerView.setVisibility(View.GONE);
        } else {

            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new MessageAdapter();
            initEvent();
            mRecyclerView.setAdapter(mAdapter);

        }
    }

    private void initEvent() {
        mAdapter.setOnItemClickLitener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mXmppMsgs.get(position).type.equals("chat")) {
                    UIUtils.showShortToast(UIUtils.getContext(), "进入聊天");
                    if (MainActivity.main != null) {
                        if (MainActivity.main.mXmppReceiver.manager != null) {
                            MainActivity.main.mXmppReceiver.manager.cancel(0);
                        }
                    }
                    ContentValues values = new ContentValues();
                    values.put("result", 0);
                    XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?", new String[]{mXmppMsgs.get(position).id + ""});
                    mAdapter.setData(position);
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    User users = new Gson().fromJson(mXmppMsgs.get(position).user.name, User.class);
                    intent.putExtra("xmpp_friend", new XmppFriend(users));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), TransactionFriendActivity.class);
                    intent.putExtra("xmpp_user", mXmppMsgs.get(position));
                    startActivity(intent);
                }
            }
        });


        mAdapter.setOnLongItemClickLitener(new OnLongItemClickListener() {
            @Override
            public void onLongItemClick(View view, final int position) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("删除信息");
                dialog.setMessage("是否删除信息？");
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        XmppService.resolver.delete(XmppContentProvider.CONTENT_MESSAGES_URI, "id=?", new String[]{mXmppMsgs.get(position).id + ""});
                        initData();
                    }
                });
                dialog.setNegativeButton("否", null);
                dialog.show();
            }
        });
    }


    class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private OnItemClickListener mOnItemClickLitener;
        private OnLongItemClickListener mOnLongItemClickLitener;
        LayoutInflater inflater;

        void setData(int position) {
            mXmppMsgs.get(position).result = 0;
            mAdapter.notifyDataSetChanged();
        }

        public void setOnItemClickLitener(OnItemClickListener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }

        public void setOnLongItemClickLitener(OnLongItemClickListener mOnLongItemClickLitener) {
            this.mOnLongItemClickLitener = mOnLongItemClickLitener;
        }

        public MessageAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0 || viewType == 1) {
                return new FriendHolder(inflater.inflate(R.layout.fragment_message_recyclerview_item, parent, false));
            }

            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (mOnItemClickLitener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getPosition();
                        mOnItemClickLitener.onItemClick(holder.itemView, pos);
                    }
                });
            }

            if (mOnLongItemClickLitener != null) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = holder.getPosition();
                        mOnLongItemClickLitener.onLongItemClick(holder.itemView, pos);
                        return false;
                    }
                });
            }

            if (holder instanceof FriendHolder) {
                FriendHolder friendHolder = (FriendHolder) holder;
                // String str[] = list.get(position).getUser().getName().split(";");
                User users = new Gson().fromJson(mXmppMsgs.get(position).user.name, User.class);
                friendHolder.tv_title.setText(users.nickname);
                friendHolder.tv_content.setText(mXmppMsgs.get(position).content);
                friendHolder.tv_time.setText(mXmppMsgs.get(position).time);

                switch (mXmppMsgs.get(position).result) {
                    case -1:
                    case 0:
                        friendHolder.tv_ts.setVisibility(View.GONE);
                        break;
                    default:
                        friendHolder.tv_ts.setVisibility(View.VISIBLE);
                        friendHolder.tv_ts.setText(mXmppMsgs.get(position).result + "");
                        break;
                }
                if (users.icon.equals("")) {
                    if (users.sex.equals("男")) {
                        friendHolder.iv.setImageResource(R.mipmap.avatar_male);
                    } else {
                        friendHolder.iv.setImageResource(R.mipmap.avatar_female);
                    }
                } else {
                    if (users.icon.substring(0, 4).equals("http")) {
                        Picasso.with(getActivity()).load(users.icon).resize(200, 200)
                                .placeholder(R.mipmap.qq_addfriend_search_friend)
                                .error(R.mipmap.qq_addfriend_search_friend)
                                .centerInside().into(friendHolder.iv);
                    } else {
                        Picasso.with(getActivity()).load(url_icon + users.icon).resize(200, 200)
                                .placeholder(R.mipmap.qq_addfriend_search_friend)
                                .error(R.mipmap.qq_addfriend_search_friend)
                                .centerInside().into(friendHolder.iv);
                    }
                }
            }
        }


        @Override
        public int getItemViewType(int position) {
            if (mXmppMsgs.get(position).type.equals("add") || mXmppMsgs.get(position).type.equals("agreed")) {
                return 0;
            } else if (mXmppMsgs.get(position).type.equals("chat")) {
                return 1;
            } else {
                return 0;
            }


        }

        @Override
        public int getItemCount() {
            return mXmppMsgs.size();
        }


    }

    class FriendHolder extends RecyclerView.ViewHolder {

        CircleImageView iv;
        TextView tv_title;
        TextView tv_content;
        TextView tv_time;
        BadgeView tv_ts;

        public FriendHolder(View itemView) {
            super(itemView);
            iv = (CircleImageView) itemView.findViewById(R.id.fragment_message_imageview_icon);
            tv_title = (TextView) itemView.findViewById(R.id.fragment_message_textView_title);
            tv_content = (TextView) itemView.findViewById(R.id.fragment_message_textView_content);
            tv_time = (TextView) itemView.findViewById(R.id.fragment_message_textView_time);
            tv_ts = (BadgeView) itemView.findViewById(R.id.tv_tishi);

        }
    }

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private interface OnLongItemClickListener {
        void onLongItemClick(View view, int position);
    }
}
