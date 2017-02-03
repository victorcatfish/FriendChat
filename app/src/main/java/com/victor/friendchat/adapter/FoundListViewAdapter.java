package com.victor.friendchat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.domain.NewsFound;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.activity.MainActivity;
import com.victor.friendchat.ui.activity.ShowImageActivity;
import com.victor.friendchat.ui.activity.ShowMessageActivity;

import java.util.ArrayList;

/**
 * Created by victor on 2015/12/28.
 */
public class FoundListViewAdapter extends BaseAdapter {
    Context context;
    ArrayList<NewsFound> news;
    String url_icon = Constant.URL.ICON + "?name=";
    Activity activity;

    public FoundListViewAdapter(Context context, ArrayList<NewsFound> news) {
        this.context = context;
        this.news = news;
        activity = (Activity) context;


    }

    public void setList(ArrayList<NewsFound> list) {
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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.found_listview_item, null);
            holder.mIvIcon = (ImageView) convertView.findViewById(R.id.found_listitem_imageview_icon);
            holder.mTvName = (TextView) convertView.findViewById(R.id.found_listitem_textView_name);
            holder.mTvLocation = (TextView) convertView.findViewById(R.id.found_listitem_textView_location);
            holder.mTvTime = (TextView) convertView.findViewById(R.id.found_listitem_textView_time);
            holder.mTvConten = (TextView) convertView.findViewById(R.id.found_listitem_textView_content);
            holder.mTvComment = (TextView) convertView.findViewById(R.id.found_listitem_textView_pinglun);
            //holder.tv_zan = (TextView) convertView.findViewById(R.id.luntan_listitem_textView_zan);
            holder.mLinearLayout = (LinearLayout) convertView.findViewById(R.id.found_listitem_photo_list);
            // holder.ll = (LinearLayout) convertView.findViewById(R.id.luntan_listitem_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder.ll.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "123", Toast.LENGTH_SHORT).show();
//                Intent intent=new Intent(context, ShowFoundActivity.class);
//                intent.putExtra("NewsFound",news.get(position));
//                context.startActivity(intent);
//            }
//        });
        holder.mTvName.setText(news.get(position).user.nickname);
        holder.mTvComment.setText(news.get(position).pinglun + "评 ");
        holder.mTvTime.setText(news.get(position).time);
        holder.mTvConten.setText(news.get(position).content);
        if (news.get(position).location.equals("")) {
            holder.mTvLocation.setVisibility(View.GONE);
        } else {
            holder.mTvLocation.setVisibility(View.VISIBLE);
            if (news.get(position).location.contains("null")) {
                holder.mTvLocation.setText(" 未知星球");
            } else {

                holder.mTvLocation.setText(" " + news.get(position).location);

            }
        }


        // holder.tv_zan.setText(news.get(position).getZan());

        if (news.get(position).user.icon.equals("")) {
            if (news.get(position).user.sex.equals("男")) {
                holder.mIvIcon.setImageResource(R.mipmap.avatar_male);
            } else {
                holder.mIvIcon.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (news.get(position).user.icon.substring(0, 4).equals("http")) {
                Picasso.with(context).load(news.get(position).user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside().into(holder.mIvIcon);
            } else {
                Picasso.with(context).load(url_icon + news.get(position).user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside().into(holder.mIvIcon);
            }
        }
        if (activity instanceof MainActivity) {
            holder.mTvName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowMessageActivity.class);
                    intent.putExtra("user", news.get(position).user);
                    context.startActivity(intent);
                }
            });
            holder.mIvIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowMessageActivity.class);
                    intent.putExtra("user", news.get(position).user);
                    context.startActivity(intent);
                }
            });
        }

        holder.mLinearLayout.removeAllViews();
        if (news.get(position).image.equals("")) {
        } else {
            ImageView imageView;
            final String[] grid_img = news.get(position).image.split(";");
            for (int i = 0; i < grid_img.length; i++) {
                imageView = new ImageView(context);
                final String urlpath = Constant.URL.BASE_URL + "/DoGetLunTan?action=search_image&name=" + grid_img[i];
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 100),
                        (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 100)));
                int padding = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 3);
                imageView.setPadding(padding, padding, padding, padding);
                final int finalI = i;
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowImageActivity.class);
                        intent.putExtra("str[]", grid_img);
                        intent.putExtra("type", "luntan");
                        intent.putExtra("number", finalI);
                        context.startActivity(intent);
                    }
                });

                Picasso.with(context)
                        .load(urlpath)
                        .resize(200, 200).centerInside()
                        .placeholder(R.mipmap.image_null_default)
                        .error(R.mipmap.image_null_default)
                        .into(imageView);
                holder.mLinearLayout.addView(imageView);
            }
        }
        return convertView;
    }

    public float getRawSize(int unit, float value) {
        Resources res = context.getResources();
        return TypedValue.applyDimension(unit, value, res.getDisplayMetrics());
    }


    static class ViewHolder {

        TextView mTvLocation;
        ImageView mIvIcon;
        TextView mTvName;
        TextView mTvTime;
        TextView mTvConten;
        TextView mTvComment;
        // TextView tv_zan;
        LinearLayout mLinearLayout;
    }


}
