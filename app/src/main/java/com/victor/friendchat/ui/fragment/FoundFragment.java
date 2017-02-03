package com.victor.friendchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demievil.library.RefreshLayout;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.adapter.FoundListViewAdapter;
import com.victor.friendchat.domain.NewsFound;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.activity.FoundToStateActivity;
import com.victor.friendchat.ui.activity.ShowFoundActivity;
import com.victor.friendchat.uitl.DialogViewBuilder;
import com.victor.friendchat.widget.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by Victor on 2016/12/28.
 */
public class FoundFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {


    private View view;
    RefreshLayout mRefreshLayout;
    ListView mlistview;
    private TextView tv_more;
    private ProgressBar pb;
    int index = 0;
    int news_size;
    View footerLayout;
    View headLayout;

    private ArrayList<NewsFound> mlist;
    FoundListViewAdapter adapter;

    FloatingActionButton fab;
    Intent intent;
    public User user;

    CircleImageView iv_icon;
    String url_icon = Constant.URL.ICON + "?name=";
    String url = Constant.URL.DO_GET_LUNTAN;
    String filePath = "G:\\ChatFriend_Img";


    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int limit = msg.arg1;
                RequestParams params = new RequestParams(url);

                params.addBodyParameter(Constant.RequestParamNames.action, "search");
                params.addBodyParameter(Constant.RequestParamNames.limit, limit + "");
                if (limit == 0) {
                    mlist = new ArrayList<>();
                }

                x.http().post(params, new org.xutils.common.Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            DialogViewBuilder.dismiss();
                            if (result != null) {
                                JSONObject object = new JSONObject(result);
                                if (object.getString("code").equals("success")) {
                                    JSONArray array = object.getJSONArray("data");
                                    for (int i = 0; i < array.length(); i++) {
                                        object = array.getJSONObject(i);
                                        NewsFound news = new NewsFound();
                                        User u = new User();
                                        u.nickname = object.getString("nickname");
                                        u.sex = object.getString("sex");
                                        u.icon = object.getString("icon");
                                        u.user = object.getString("user");
                                        news.lid = object.getInt("lid");
                                        news.user = u;
                                        news.time = object.getString("time");
                                        news.content = object.getString("content");
                                        news.image = object.getString("image");
                                        news.location = object.getString("location");
                                        news.pinglun = object.getString("pinglun_size");
                                        news_size = object.getInt("state_size");

                                        mlist.add(news);
                                    }

                                    if (index == 0) {
                                        adapter = new FoundListViewAdapter(getActivity(), mlist);
                                        mlistview.setAdapter(adapter);
                                        mRefreshLayout.setRefreshing(false);
                                    } else {
                                        adapter.setList(mlist);
                                        adapter.notifyDataSetChanged();
                                        tv_more.setVisibility(View.VISIBLE);
                                        pb.setVisibility(View.GONE);
                                        mRefreshLayout.setLoading(false);
                                    }


                                } else {
                                    Toast.makeText(getActivity(), "暂无数据", Toast.LENGTH_SHORT).show();
                                    mRefreshLayout.setLoading(false);
                                    mRefreshLayout.setRefreshing(false);

                                }
                            } else {
                                Toast.makeText(getActivity(), "暂无数据", Toast.LENGTH_SHORT).show();
                                mRefreshLayout.setLoading(false);
                                mRefreshLayout.setRefreshing(false);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Json解析异常，请查看网络设置", Toast.LENGTH_SHORT).show();
                            mRefreshLayout.setLoading(false);
                            mRefreshLayout.setRefreshing(false);
                            DialogViewBuilder.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        DialogViewBuilder.dismiss();
                        mRefreshLayout.setLoading(false);
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "网络连接失败，请查看网络设置", Toast.LENGTH_SHORT).show();
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
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            DialogViewBuilder.init(getActivity(), "正在加载动态......");
            intent = getActivity().getIntent();
            user = (User) intent.getSerializableExtra("user");
            view = inflater.inflate(R.layout.fragment_found, container, false);
            footerLayout = getActivity().getLayoutInflater().inflate(R.layout.found_list_item_more, null);
            headLayout = getActivity().getLayoutInflater().inflate(R.layout.found_list_item_view, null);
            iv_icon = (CircleImageView) headLayout.findViewById(R.id.luntan_imageview_icon);
            if (user.icon.equals("")) {
                if (user.sex.equals("男")) {
                    iv_icon.setImageResource(R.mipmap.avatar_male);
                } else {
                    iv_icon.setImageResource(R.mipmap.avatar_female);
                }
            } else {
                if (user.icon.substring(0, 4).equals("http")) {
                    Picasso.with(getActivity()).load(user.icon).resize(200, 200).centerInside()
                            .placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend).into(iv_icon);
                } else {
                    Picasso.with(getActivity()).load(url_icon + user.icon).resize(200, 200)
                            .centerInside().placeholder(R.mipmap.qq_addfriend_search_friend)
                            .error(R.mipmap.qq_addfriend_search_friend).into(iv_icon);
                }
            }
            mlistview = (ListView) view.findViewById(R.id.fragment_luntan_listview);
            fab = (FloatingActionButton) view.findViewById(R.id.fab);
            fab.attachToListView(mlistview);
            fab.setOnClickListener(this);
            fab.setColorNormal(getResources().getColor(R.color.toolBar));
            fab.setColorPressed(getResources().getColor(R.color.toolBar));
            fab.setColorRipple(getResources().getColor(R.color.toolBar));
            fab.setShadow(true);
            mRefreshLayout = (RefreshLayout) view.findViewById(R.id.fragment_luntan_freshLayout);

            tv_more = (TextView) footerLayout.findViewById(R.id.tv_load_more);
            pb = (ProgressBar) footerLayout.findViewById(R.id.pb_load_more);
            tv_more.setOnClickListener(this);
            mlistview.setOnItemClickListener(this);
            mlistview.addFooterView(footerLayout);
            mlistview.addHeaderView(headLayout);

            mRefreshLayout.setOnRefreshListener(this);
            mRefreshLayout.setOnLoadListener(this);

            mRefreshLayout.setChildView(mlistview);
            mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_dark,
                    android.R.color.holo_red_light,
                    android.R.color.holo_orange_dark);


            mlistview.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                    // Toast.makeText(getActivity(),i+"",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem == 0) {
                        fab.show();
                    } else {
                        fab.hide();
                    }
                }
            });
            DialogViewBuilder.show();
            Message m = h.obtainMessage(1);
            m.arg1 = 0;
            h.sendMessage(m);

        }


        return view;
    }

    public void update() {

        Message m = h.obtainMessage(1);
        m.arg1 = 0;
        h.sendMessage(m);
        if (user.icon.equals("")) {
            if (user.sex.equals("男")) {
                iv_icon.setImageResource(R.mipmap.avatar_male);
            } else {
                iv_icon.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (user.icon.substring(0, 4).equals("http")) {
                Picasso.with(getActivity()).load(user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerCrop().into(iv_icon);
            } else {
                Picasso.with(getActivity()).load(url_icon + user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerCrop().into(iv_icon);
            }
        }
    }


    public void updateData(String icon) {
        Picasso.with(getActivity()).load(url_icon + icon).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerCrop().into(iv_icon);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Intent i = new Intent(getActivity(), FoundToStateActivity.class);
                i.putExtra("user", user);
                getActivity().startActivityForResult(i, 200);
                break;
            case R.id.tv_load_more:
                loadData();
                break;

        }
    }

    private void loadData() {
        index += 10;
        if (news_size == mlist.size()) {
            tv_more.setText("数据已加载完毕");
            tv_more.setEnabled(false);
            return;
        }
        tv_more.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        Message m = h.obtainMessage(1);
        m.arg1 = index;
        h.sendMessage(m);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) {
            ViewGroup vg = (ViewGroup) view.getParent();
            if (vg != null) {
                vg.removeView(view);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position <= mlist.size()) {
            if (position == 0) {

            } else {
                Intent intent = new Intent(getActivity(), ShowFoundActivity.class);
                intent.putExtra("news_luntan", mlist.get(position - 1));
                User user = (User) getActivity().getIntent().getSerializableExtra("user");
                intent.putExtra("user", user.user);
                startActivity(intent);
            }

        }
    }

    @Override
    public void onRefresh() {
        index = 0;
        tv_more.setEnabled(true);
        tv_more.setText("加载更多");
        tv_more.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        mRefreshLayout.setLoading(false);
        Message m = h.obtainMessage(1);
        m.arg1 = index;
        h.sendMessage(m);
    }

    @Override
    public void onLoad() {
        loadData();
    }
}
