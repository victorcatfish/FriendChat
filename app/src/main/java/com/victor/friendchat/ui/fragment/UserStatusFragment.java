package com.victor.friendchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demievil.library.RefreshLayout;
import com.victor.friendchat.R;
import com.victor.friendchat.adapter.FoundListViewAdapter;
import com.victor.friendchat.domain.NewsFound;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.ui.activity.ShowFoundActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

/** 用户状态栏fragment
 * Created by Victor on 2016/12/29.
 */
public class UserStatusFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {

    @ViewInject(R.id.lv_status_fragment)
    private ListView mListView;
    @ViewInject(R.id.rfl_status_fragment)
    private RefreshLayout mRefreshLayout;
    @ViewInject(R.id.tv_status_fragment)
    private TextView mTextView;

    private User mUser;
    private View mFooterView;

    private int foundSize;
    private int index = 0;
    private ArrayList<NewsFound> mlist;
    private FoundListViewAdapter mAdapter;
    private TextView mTvLoadMore;
    private ProgressBar mPbLoadMore;
    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUser = (User) arguments.getSerializable("user");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_status, container, false);
        x.view().inject(this, mView);
        initFooterView();
        mListView.setOnItemClickListener(this);
        mListView.addFooterView(mFooterView);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadListener(this);
        mRefreshLayout.setChildView(mListView);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_dark);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getUserMessage(0);
    }

    private void initFooterView() {
        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.found_list_item_more, null);
        mTvLoadMore = (TextView) mFooterView.findViewById(R.id.tv_load_more);
        mPbLoadMore = (ProgressBar) mFooterView.findViewById(R.id.pb_load_more);
        mTvLoadMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_load_more:
                loadData();
                break;
        }
    }

    private void loadData() {
        index += 10;
        if (foundSize == mlist.size()) {
            mTvLoadMore.setText("数据已加载完毕");
            mTvLoadMore.setEnabled(false);
            return;
        }
        mTvLoadMore.setVisibility(View.GONE);
        mPbLoadMore.setVisibility(View.VISIBLE);
        getUserMessage(index);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (ShowFoundActivity.instance != null) {
            getActivity().finish();
        }
        Intent intent = new Intent(getActivity(), ShowFoundActivity.class);
        intent.putExtra("news_luntan", mlist.get(position));
        startActivity(intent);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoad() {

    }

    /**
     * 获取用户资料
     */
    void getUserMessage(int limit) {
        if (limit == 0) {
            mlist = new ArrayList<>();
        }
        RequestParams params = new RequestParams(Constant.URL.DO_GET_LUNTAN);
        params.addBodyParameter("user", mUser.user);
        params.addBodyParameter("action", "search_user");
        params.addBodyParameter("limit", limit + "");

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                mRefreshLayout.setLoading(false);
                mRefreshLayout.setRefreshing(false);

                try {
                    if (result != null) {
                        JSONObject object = new JSONObject(result);
                        if (object.getString("code").equals("success")) {
                            mRefreshLayout.setVisibility(View.VISIBLE);
                            mTextView.setVisibility(View.GONE);
                            JSONArray array = object.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                NewsFound found = new NewsFound();
                                User u = new User();
                                u.nickname = object.getString("nickname");
                                u.sex = object.getString("sex");
                                u.icon = object.getString("icon");
                                u.user = object.getString("user");
                                found.lid = object.getInt("lid");
                                found.user = u;
                                found.time = object.getString("time");
                                found.content = object.getString("content");
                                found.image = object.getString("image");
                                found.location = object.getString("location");
                                found.pinglun = object.getString("pinglun_size");
                                foundSize = object.getInt("state_size");
                                mlist.add(found);
                            }

                            if (index == 0) {
                                mAdapter = new FoundListViewAdapter(getActivity(), mlist);
                                mListView.setAdapter(mAdapter);

                            } else {
                                mAdapter.setList(mlist);
                                mAdapter.notifyDataSetChanged();
                                mTvLoadMore.setVisibility(View.VISIBLE);
                                mPbLoadMore.setVisibility(View.GONE);

                            }
                        } else {
                            mRefreshLayout.setVisibility(View.GONE);
                            mTextView.setVisibility(View.VISIBLE);

                        }
                    } else {
                        Toast.makeText(getActivity(), "网络连接失败，请查看网络设置", Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "网络连接失败，请查看网络设置", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mView != null) {
            ((ViewGroup)mView.getParent()).removeView(mView);
        }
    }
}
