package com.victor.friendchat.ui.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demievil.library.RefreshLayout;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.adapter.FoundCommentListViewAdapter;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.NewsComment;
import com.victor.friendchat.domain.NewsFound;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.global.Location;
import com.victor.friendchat.uitl.DialogViewBuilder;
import com.victor.friendchat.uitl.SaveUserUtil;
import com.victor.friendchat.uitl.UIUtils;
import com.victor.friendchat.widget.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Date;

public class ShowFoundActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {

    public static ShowFoundActivity instance;


    @ViewInject(R.id.show_luntan_gridview)
    private GridView mGridView;
    @ViewInject(R.id.show_luntan_imageview_icon)
    private CircleImageView mCivAvatar;
    @ViewInject(R.id.show_luntan_textView_name)
    private TextView mTvName;
    @ViewInject(R.id.show_luntan_pinglun_total)
    private TextView mTvCommentTotal;
    @ViewInject(R.id.show_luntan_text_enterpinglun)
    private TextView mTvPubConmment;
    @ViewInject(R.id.show_luntan_editText_pinglun)
    private EditText mEdComment;
    @ViewInject(R.id.show_luntan_textView_location)
    private TextView mTvLocation;
    @ViewInject(R.id.show_luntan_textView_time)
    private TextView mTvTime;
    @ViewInject(R.id.show_luntan_textView_content)
    private TextView mTvContent;
    @ViewInject(R.id.show_luntan_listview)
    private ListView mlistview;
    @ViewInject(R.id.show_luntan_freshLayout)
    private RefreshLayout mRefreshLayout;

    private User mUser;
    private Intent mIntent;
    private NewsFound mNewsFound;
    private InputMethodManager mImm;
    private Location mLocation;
    ArrayList<NewsComment> mNewsComments;
    private View footerLayout;

    private TextView mTvLoadMore;
    private ProgressBar mPbLoadMore;

    String url_icon = Constant.URL.ICON + "?name=";
    String url = Constant.URL.DO_GET_LUNTAN;
    private int mCommentNum;
    private int index = 0;
    private FoundCommentListViewAdapter mAdapter;
    private boolean xg_bool = false;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_show_found;
    }

    @Override
    protected void initView() {
        instance = this;
        mUser = SaveUserUtil.loadAccount(this);
        /*result = XGPushManager.onActivityStarted(ShowLuntanActivity.this);
        if (result != null) {
            xg_bool = true;
            // 获取自定义key-value
            String customContent = result.getCustomContent();
            if (customContent != null && customContent.length() != 0) {
                try {
                    JSONObject obj = new JSONObject(customContent);
                    // key1为前台配置的key
                    if (!obj.isNull("data")) {
                        news_luntan = new News_luntan();
                        String data = obj.getString("data");
                        JSONObject object = new JSONObject(data);
                        User userr = new User();
                        userr.setUser(object.getString("user"));
                        userr.setNickname(object.getString("nickname"));
                        userr.setIcon(object.getString("icon"));
                        userr.setSex(object.getString("sex"));
                        news_luntan.setUser(userr);
                        news_luntan.setLid(object.getInt("lid"));
                        news_luntan.setContent(object.getString("content"));
                        news_luntan.setImage(object.getString("image"));
                        news_luntan.setTime(object.getString("time"));
                        news_luntan.setLocation(object.getString("location"));


                    }
                    // ...
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mIntent = getIntent();
            news_luntan = (News_luntan) mIntent.getSerializableExtra("news_luntan");

        }*/

        mIntent = getIntent();
        mNewsFound = (NewsFound) mIntent.getSerializableExtra("news_luntan");
        mImm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        DialogViewBuilder.init(this, "正在评论......");
        mLocation = new Location(this);
        mNewsComments = new ArrayList<>();
        final String[] grid_img = mNewsFound.image.split(";");

        footerLayout = getLayoutInflater().inflate(R.layout.found_list_item_more, null);
        if (mNewsFound.image.equals("")) {

        } else {
            mGridView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return grid_img.length;
                }

                @Override
                public Object getItem(int position) {
                    return grid_img[position];
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
                        convertView = LayoutInflater.from(ShowFoundActivity.this).inflate(R.layout.layout_imagview, null);
                        holder.iv = (ImageView) convertView.findViewById(R.id.imageView);
                        holder.iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowFoundActivity.this, ShowImageActivity.class);
                                intent.putExtra("str[]", grid_img);
                                intent.putExtra("type", "luntan");
                                intent.putExtra("number", position);
                                startActivity(intent);
                            }
                        });
                        convertView.setTag(holder);
                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    final String urlpath = Constant.URL.DO_GET_LUNTAN + "?action=search_image&name=" + grid_img[position];
                    Picasso.with(ShowFoundActivity.this)
                            .load(urlpath)
                            .resize(200, 200).centerCrop()
                            .placeholder(R.mipmap.image_null_default)
                            .error(R.mipmap.image_null_default)
                            .into(holder.iv);
                    return convertView;
                }
            });
        }


        mTvLoadMore = (TextView) footerLayout.findViewById(R.id.tv_load_more);
        mTvLoadMore.setOnClickListener(this);
        mPbLoadMore = (ProgressBar) footerLayout.findViewById(R.id.pb_load_more);


        mTvName.setText(mNewsFound.user.nickname);

        if (mNewsFound.location.equals("")) {
            mTvLocation.setVisibility(View.GONE);
        } else {
            mTvLocation.setVisibility(View.VISIBLE);

            mTvLocation.setText(" " + mNewsFound.location);
        }
        if (mNewsFound.user.icon.equals("")) {
            if (mNewsFound.user.sex.equals("男")) {
                mCivAvatar.setImageResource(R.mipmap.avatar_male);
            } else {
                mCivAvatar.setImageResource(R.mipmap.avatar_female);
            }
        } else {
            if (mNewsFound.user.icon.substring(0, 4).equals("http")) {
                Picasso.with(this).load(mNewsFound.user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                        .into(mCivAvatar);
            } else {
                Picasso.with(this).load(url_icon + mNewsFound.user.icon).resize(200, 200)
                        .placeholder(R.mipmap.qq_addfriend_search_friend)
                        .error(R.mipmap.qq_addfriend_search_friend).centerInside()
                        .into(mCivAvatar);
            }
        }
        mTvTime.setText(mNewsFound.time);
        mTvContent.setText(mNewsFound.content);

        mlistview.addFooterView(footerLayout);


        mRefreshLayout.setChildView(mlistview);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_dark);

    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText("动态");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    protected void initListener() {
        mTvPubConmment.setOnClickListener(this);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadListener(this);
        getData(mNewsFound.lid, 0);
    }

    /**
     * 根据文章id获取评论
     *
     * @param plid
     */
    public void getData(int plid, int limit) {

        RequestParams params = new RequestParams(url);

        params.addBodyParameter(Constant.RequestParamNames.plid, plid + "");
        params.addBodyParameter(Constant.RequestParamNames.user, mUser.user);
        params.addBodyParameter(Constant.RequestParamNames.limit, limit + "");
        params.addBodyParameter(Constant.RequestParamNames.action, "search_pinglun");


        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getString("code").equals("success")) {

                            JSONArray array = object.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                object = array.getJSONObject(i);
                                NewsComment comment = new NewsComment();
                                User users = new User();
                                users.nickname = object.getString("nickname");
                                mCommentNum = object.getInt("size");
                                users.sex = object.getString("sex");
                                users.icon = object.getString("icon");
                                comment.setIspzan(object.getString("ispzan"));

                                JSONObject ob = object.getJSONObject("pdata");
                                comment.setPcid(ob.getInt("pcid"));
                                comment.setPid(ob.getInt("pid"));
                                comment.setPcontent(ob.getString("pcontent"));
                                comment.setPlocation(ob.getString("plocation"));
                                comment.setPtime(ob.getString("ptime"));
                                comment.setPzan(ob.getString("pzan"));
                                users.user = ob.getString("user");
                                comment.setUser(users);
                                mNewsComments.add(comment);
                            }

                            if (mNewsComments.size() < 0) {
                                return;
                            } else {

                            }
                            mTvCommentTotal.setText("热门评论(" + mCommentNum + ")");
                            // ((NewsContentActivity)getActivity()).pl_size=mCommentNum;
                            if (index == 0) {
                                mAdapter = new FoundCommentListViewAdapter(ShowFoundActivity.this, mNewsComments, mNewsFound.user.user);
                                mlistview.setAdapter(mAdapter);
                                mlistview.setVisibility(View.VISIBLE);
                                if (xg_bool) {
                                    xg_bool = false;
                                    mlistview.setSelection(mNewsComments.size() - 1);
                                }

                            } else {

                                mAdapter.setList(mNewsComments);
                                mAdapter.notifyDataSetChanged();
                                mTvLoadMore.setVisibility(View.VISIBLE);
                                mPbLoadMore.setVisibility(View.GONE);
                            }


                        } else {
                            //  tv_total.setText("暂无评论");

                        }
                        mRefreshLayout.setRefreshing(false);
                        mRefreshLayout.setLoading(false);
                    } catch (JSONException e) {
                        mRefreshLayout.setRefreshing(false);
                        mRefreshLayout.setLoading(false);
                        e.printStackTrace();
                    }
                } else {
                    mRefreshLayout.setLoading(false);
                    mRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            mRefreshLayout.setRefreshing(false);
            mRefreshLayout.setLoading(false);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 提交评论
     */
    private void pubComment() {

        String pinglun = mEdComment.getText().toString();
        if (pinglun.equals("")) {
            UIUtils.showShortToast(ShowFoundActivity.this, "请先评论");
            return;
        }
        DialogViewBuilder.show();
        mTvPubConmment.setEnabled(false);
        Date date = new Date();
        long ptime = date.getTime();
        RequestParams params = new RequestParams(url);
        params.addBodyParameter(Constant.RequestParamNames.plid, mNewsFound.lid + "");
        params.addBodyParameter(Constant.RequestParamNames.author, mNewsFound.user.user);
        params.addBodyParameter(Constant.RequestParamNames.action, "save_pinglun");
        params.addBodyParameter(Constant.RequestParamNames.user, mUser.user);
        params.addBodyParameter(Constant.RequestParamNames.plocation, mLocation.city);
        params.addBodyParameter(Constant.RequestParamNames.ptime, ptime + "");
        params.addBodyParameter(Constant.RequestParamNames.pcontent, pinglun);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                mTvPubConmment.setEnabled(true);
                DialogViewBuilder.dismiss();
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if (object.getString("code").equals("success")) {

                            Toast.makeText(ShowFoundActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                            mEdComment.setText("");
                            if (mImm.isActive()) {//关闭键盘
                                mImm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                            refreshData();

                            //更新评论内容

                        } else {
                            Toast.makeText(ShowFoundActivity.this, "评论失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        mTvPubConmment.setEnabled(true);
                        DialogViewBuilder.dismiss();
                        e.printStackTrace();
                        UIUtils.showShortToast(UIUtils.getContext(), "Json解析失败，请检查获取数据");
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                mTvPubConmment.setEnabled(true);
                DialogViewBuilder.dismiss();
                UIUtils.showShortToast(UIUtils.getContext(), "网络连接失败，请查看网络设置");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 加载更多数据
     */
    private void loadData() {
        index += 10;
        if (mCommentNum == mNewsComments.size()) {
            mTvLoadMore.setText("数据已加载完毕");
            mTvLoadMore.setEnabled(false);

            return;
        }
        mTvLoadMore.setVisibility(View.GONE);
        mPbLoadMore.setVisibility(View.VISIBLE);
        getData(mNewsFound.lid, index);
    }

    /**
     * 刷新数据
     */
    public void refreshData() {
        index = 0;
        mTvLoadMore.setEnabled(true);
        mTvLoadMore.setText("加载更多");
        mNewsComments = new ArrayList<>();
        getData(mNewsFound.lid, 0);
        mTvLoadMore.setVisibility(View.VISIBLE);
        mPbLoadMore.setVisibility(View.GONE);
        mRefreshLayout.setLoading(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_luntan_text_enterpinglun:
                pubComment();
                break;
            case R.id.tv_load_more:
                loadData();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocation.stopLocation();
        instance = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        instance = null;
        /*if (result != null) {
            if (MainActivity.main == null) {
                new Thread() {

                    @Override
                    public void run() {
                        boolean result = XmppTool.getInstance().login(users.getUser(), users.getPassword(), ShowLuntanActivity.this);
                        if (result) {

                            Intent intent = new Intent(ShowFoundActivity.this, MainActivity.class);
                            intent.putExtra("user", users);
                            startActivity(intent);
                            finish();
                        } else {

                            finish();
                        }
                    }

                }.start();


            } else {
                finish();
            }
        } else {
            finish();
        }*/
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void onLoad() {
        loadData();
    }

    class ViewHolder {
        ImageView iv;
    }
}
