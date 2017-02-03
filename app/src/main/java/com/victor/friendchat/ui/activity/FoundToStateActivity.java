package com.victor.friendchat.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.global.Constant;
import com.victor.friendchat.uitl.Base64Coder;
import com.victor.friendchat.uitl.DialogViewBuilder;
import com.victor.friendchat.uitl.ImageCompressUtils;
import com.victor.friendchat.uitl.LogUtils;
import com.victor.friendchat.uitl.PhotoSelectedHelper;
import com.victor.friendchat.uitl.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import me.nereo.multi_image_selector.adapter.MainGridAdapter;
import me.nereo.multi_image_selector.bean.Image;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FoundToStateActivity extends BaseActivity implements MainGridAdapter.Callback, PopupWindow.OnDismissListener {

    private static final int REQUEST_IMAGE = 2;
    public static final int RESULT_OK = -1;


    public User mUser;
    @ViewInject(R.id.luntan_state_layout_location)
    private RelativeLayout rl_location;
    @ViewInject(R.id.luntan_state_textview_location)
    private TextView tv_location;
    @ViewInject(R.id.luntan_state_edittext_content)
    private EditText et_content;
    @ViewInject(R.id.luntan_state_gridview)
    private GridView mGridView;
    private MainGridAdapter mainGridAdapter;

    public ArrayList<String> mSelectPath = new ArrayList<>();
    public boolean isYuantu;
    PopupWindow popupWindow;
    ImageView popImageView;
    PhotoSelectedHelper mPhotoSelectedHelper;
    Intent intent;
    private PhotoViewAttacher mAttacher;
    String str_content;
    boolean cb_bool = false;
    String location;
    String url = Constant.URL.DO_GET_LUNTAN;


    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                DialogViewBuilder.show();
                mPublishMenu.setEnabled(false);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = sdf.format(new Date());

                RequestParams params = new RequestParams(url);
                params.addBodyParameter(Constant.RequestParamNames.action, "save");
                params.addBodyParameter(Constant.RequestParamNames.user, mUser.user);
                params.addBodyParameter(Constant.RequestParamNames.time, time);
                params.addBodyParameter(Constant.RequestParamNames.content, str_content);

                if (cb_bool) {
                    params.addBodyParameter(Constant.RequestParamNames.location, location);
                } else {
                    params.addBodyParameter(Constant.RequestParamNames.location, "");
                }
                if (mSelectPath != null && mSelectPath.size() > 0) {
                    params.addBodyParameter(Constant.RequestParamNames.image_size, mSelectPath.size() + "");
                    for (int i = 0; i < mSelectPath.size(); i++) {

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        ImageCompressUtils.getimage(mSelectPath.get(i)).compress(Bitmap.CompressFormat.JPEG,
                                80, stream);
                        byte[] b = stream.toByteArray();
                        // 将图片流以字符串形式存储下来
                        String file = new String(Base64Coder.encodeLines(b));
                        params.addBodyParameter(Constant.RequestParamNames.file + i, file);
                        String filename = mUser.user + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + i + ".jpg";
                        params.addBodyParameter(Constant.RequestParamNames.filename + i, filename);
                        LogUtils.sf(filename + i);
                        LogUtils.sf(Constant.RequestParamNames.filename + i);
                    }
                } else {
                    params.addBodyParameter(Constant.RequestParamNames.image_size, "0");
                }


                // Toast.makeText(LuntanToStateActivity.this, params.toString(), Toast.LENGTH_SHORT).show();

                x.http().post(params, new org.xutils.common.Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        mPublishMenu.setEnabled(true);
                        DialogViewBuilder.dismiss();
                        if (result != null) {
                            try {
                                JSONObject object = new JSONObject(result);
                                if (object.getString("code").equals("success")) {

                                    UIUtils.showShortToast(UIUtils.getContext(), "发表成功");
                                    Intent intents = new Intent(FoundToStateActivity.this, MainActivity.class);
                                    setResult(200, intents);
                                    finish();
                                    //更新评论内容
                                } else {
                                    UIUtils.showShortToast(UIUtils.getContext(), "发表失败，请重试");
                                }
                            } catch (JSONException e) {
                                mPublishMenu.setEnabled(true);
                                DialogViewBuilder.dismiss();
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        mPublishMenu.setEnabled(true);
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
        }
    };
    private MenuItem mPublishMenu;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_found_to_state;
    }

    @Override
    protected void initView() {
        DialogViewBuilder.init(this, "正在发表......");
        mPhotoSelectedHelper = new PhotoSelectedHelper(this);
        intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");

        mainGridAdapter = new MainGridAdapter(FoundToStateActivity.this, this, 9);
        mGridView.setAdapter(mainGridAdapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int width = mGridView.getWidth();
                final int height = mGridView.getHeight();

                final int desireSize = getResources().getDimensionPixelOffset(me.nereo.multi_image_selector.R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(me.nereo.multi_image_selector.R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                mainGridAdapter.setItemSize(columnWidth);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == mSelectPath.size()) {
                    Intent intent = new Intent(FoundToStateActivity.this, MultiImageSelectorActivity.class);
                    // 是否显示拍摄图片
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
                    // 最大可选择图片数量
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
                    // 选择模式
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, 1);
                    // 默认选择
                    if (mSelectPath != null && mSelectPath.size() > 0) {
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
                    }
                    startActivityForResult(intent, REQUEST_IMAGE);
                } else {
                    Picasso.with(FoundToStateActivity.this).load(new File(mSelectPath.get(position))).into(popImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacher = new PhotoViewAttacher(popImageView);
                            popImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (popupWindow != null && popupWindow.isShowing()) {
                                        popupWindow.dismiss();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError() {

                        }
                    });
                    popupWindow.showAtLocation(LayoutInflater.from(FoundToStateActivity.this).inflate(R.layout.activity_found_to_state, null)
                            , Gravity.CENTER, 0, 0);
                }
            }
        });
        initialPopups();
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText("发动态");
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_publish:
                        upData();
                        //UIUtils.showShortToast(UIUtils.getContext(), "发表文章");
                        break;
                }
                return true;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void initListener() {
        rl_location.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_found_to_state, menu);
        mPublishMenu = menu.findItem(R.id.action_publish);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 初始化popupwindow
     */
    private void initialPopups() {
        popImageView = new ImageView(this);
        // popImageView.setPadding(50, 50, 50, 50);
        popupWindow = new PopupWindow(popImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));

    }

    private void upData() {

        str_content = et_content.getText().toString();
        if (str_content.length() < 5) {
           UIUtils.showShortToast(FoundToStateActivity.this, "内容长度必须大于5");
            return;

        }
        Message m = h.obtainMessage(1);
        h.sendMessage(m);

    }

    @Override
    public void callbackDelete(String str) {
        dataDelete(str);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 99) {
            if (data == null) {
                return;
            } else {
                location = data.getStringExtra("location");
                if (location.equals("地点")) {
                    cb_bool = false;
                    tv_location.setText(location);
                } else {
                    cb_bool = true;
                    tv_location.setText(location);
                }

            }
        }

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                isYuantu = data.getBooleanExtra("YUANTU", false);
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                mainGridAdapter.setData(toImages(mSelectPath));
            }
        }


    }

    @Override
    public void onDismiss() {
        WindowManager.LayoutParams lp = getWindow()
                .getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
    }

    private List<Image> toImages(ArrayList<String> mmSelectPath) {
        List<Image> images = new ArrayList<>();
        for (int i = 0; i < mmSelectPath.size(); i++) {
            Image image = new Image();
            image.path = mmSelectPath.get(i);
            images.add(image);
        }
        return images;
    }

    public void dataDelete(String str) {
        if (str == null) {
            return;
        } else {
            if (mSelectPath.contains(str)) {
                mSelectPath.remove(str);
                mainGridAdapter.setData(toImages(mSelectPath));
            }
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
    public void onBackPressed() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            finish();
        }
    }
}
