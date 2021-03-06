package com.victor.friendchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.domain.NewsComment;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.activity.ShowMessageActivity;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by victor on 2015/9/28.
 */
public class FoundCommentListViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<NewsComment> news;
    String user;
    String url_icon = Constant.URL.ICON + "?name=";


    public FoundCommentListViewAdapter(Context context, ArrayList<NewsComment> news, String user) {
        this.context = context;
        this.news = news;
        this.user = user;


    }


    public void setList(ArrayList<NewsComment> list) {
        news = list;
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public Object getItem(int position) {
        return news.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;


        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.comment_list_item, null);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.listview_pinglun_item_imageview_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_name);
            holder.tv_location = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_location);
            holder.tv_time = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_time);
            holder.tv_lou = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_lou);
            holder.tv_content = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_content);
            holder.tv_zan = (TextView) convertView.findViewById(R.id.listview_pinglun_item_textView_zan);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.tv_lou.setText("第" + (position + 1) + "楼");

        String stime = "";

        if (news.get(position).getUser().user.contains("http")) {
            stime = news.get(position).getPtime();
        } else {
            Date date = new Date();
            long time = date.getTime();
            long sytime = time - Long.parseLong(news.get(position).getPtime());
            long ltime = sytime / 1000;

            if (ltime >= 0 && ltime < 60) {
                if (ltime == 0) {
                    stime = "刚刚";
                } else {
                    stime = ltime + "秒前";
                }

            } else if (ltime >= 60 && ltime < 3600) {
                stime = ltime / 60 + "分钟前";
            } else if (ltime >= 3600 && ltime < 3600 * 24) {
                stime = ltime / 3600 + "小时前";
            } else if (ltime >= 3600 * 24 && ltime < 3600 * 48) {
                stime = "昨天";
            } else if (ltime >= 3600 * 48 && ltime < 3600 * 72) {
                stime = "前天";
            } else if (ltime >= 3600 * 72) {
                stime = ltime / 86400 + "天前";
            } else {
                stime = "1212122";
            }
        }
        holder.tv_time.setText(stime);

        holder.tv_content.setText(news.get(position).getPcontent());
        holder.tv_zan.setText(news.get(position).getPzan() + " ");
        if (news.get(position).getPlocation().equals("")) {
            holder.tv_location.setVisibility(View.GONE);
        } else {
            holder.tv_location.setVisibility(View.VISIBLE);
            if (news.get(position).getPlocation().contains("null")) {
                holder.tv_location.setText("未知星球");
            } else {
                holder.tv_location.setText(news.get(position).getPlocation());
            }

        }
        Drawable nav_upp = null;
        if (user.equals(news.get(position).getUser().user)) {
            nav_upp = context.getResources().getDrawable(R.mipmap.louzhu);
            nav_upp.setBounds(0, 0, nav_upp.getMinimumWidth(), nav_upp.getMinimumHeight());

            holder.tv_name.setCompoundDrawables(null, null, nav_upp, null);

        } else {
            holder.tv_name.setCompoundDrawables(null, null, null, null);
        }
        holder.tv_name.setText(news.get(position).getUser().nickname);
        Drawable nav_up = null;
        if (news.get(position).getIspzan().equals("1")) {
            nav_up = context.getResources().getDrawable(R.mipmap.pic_btn_liked);
            holder.tv_zan.setEnabled(false);

        } else {
            nav_up = context.getResources().getDrawable(R.mipmap.pic_btn_like);
            holder.tv_zan.setEnabled(true);
        }
        nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
        holder.tv_zan.setCompoundDrawables(null, null, nav_up, null);
        holder.tv_zan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (news.get(position).getIspzan().equals("1")) {

                } else {
                    holder.tv_zan.setEnabled(false);
                    Drawable nav_upp = context.getResources().getDrawable(R.mipmap.pic_btn_liked);
                    nav_upp.setBounds(0, 0, nav_upp.getMinimumWidth(), nav_upp.getMinimumHeight());
                    holder.tv_zan.setCompoundDrawables(null, null, nav_upp, null);
                    holder.tv_zan.setText(Integer.parseInt((news.get(position).getPzan())) + 1 + " ");
                    update_zan(news.get(position).getPid(), position, holder.tv_zan);

                }

            }
        });
        if (news.get(position).getUser().icon.equals("")) {
            if (news.get(position).getUser().sex.equals("男")) {
                holder.iv_icon.setImageResource(R.mipmap.avatar_male);
            } else {
                holder.iv_icon.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (news.get(position).getUser().icon.substring(0, 4).equals("http")) {
                Picasso.with(context).load(news.get(position).getUser().icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                        .into(holder.iv_icon);
            } else {
                Picasso.with(context).load(url_icon + news.get(position).getUser().icon)
                        .resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                        .into(holder.iv_icon);
            }
        }
        holder.tv_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowMessageActivity.class);
                intent.putExtra("user", news.get(position).getUser().user);
                context.startActivity(intent);
            }
        });
        holder.iv_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowMessageActivity.class);
                intent.putExtra("user", news.get(position).getUser().user);
                context.startActivity(intent);
            }
        });


        return convertView;
    }


    private class ViewHolder {
        private TextView tv_name;
        private TextView tv_location;
        private TextView tv_time;
        private TextView tv_lou;
        private TextView tv_content;
        private TextView tv_zan;
        private ImageView iv_icon;
    }


//    /**
//     * 状态holder类
//     */
//    public class State_ViewHolder {
//        private CircleImageView iv_icon;
//        private GridView gv;
//        private TextView tv_location;
//        private TextView tv_name;
//        private TextView tv_time;
//        private TextView tv_content;
//
//
//    }


    private void update_zan(int pid, final int position, final TextView tv) {
        RequestParams params = new RequestParams(Constant.URL.DO_GET_LUNTAN);
        params.addBodyParameter(Constant.RequestParamNames.action, "update_zan");
        params.addBodyParameter(Constant.RequestParamNames.pid, pid + "");
        params.addBodyParameter(Constant.RequestParamNames.user, user);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                news.get(position).setIspzan("1");
                news.get(position).setPzan(Integer.parseInt((news.get(position).getPzan())) + 1 + " ");
                tv.setEnabled(true);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                tv.setEnabled(true);
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
