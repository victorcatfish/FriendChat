package com.victor.friendchat.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.victor.friendchat.R;
import com.victor.friendchat.global.MyApplication;
import com.victor.friendchat.uitl.PermissionUtil;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    protected PermissionUtil mPermissionUtil;
    private static final int PERMISSION_CODE = 999;

    @ViewInject(R.id.tool_bar)
    protected Toolbar mToolbar;
    protected TextView mTvToolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getInstance().addActivity(this);
        setContentView(getLayoutResource());
        x.view().inject(this);
        mTvToolBarTitle = (TextView) mToolbar.findViewById(R.id.tv_toolbar_title);
        initView();
        initToolBar();
        initListener();
        //BaseActivityPermissionsDispatcher.enterAppWithCheck(this);

        /*mPermissionUtil = PermissionUtil.getInstance();
        mPermissionUtil.requestPermissions(this, PERMISSION_CODE, this);*/
    }

    /**
     * 初始化布局
     */
    protected void initView() {

    }

    /**
     * 初始化监听器
     */
    protected void initListener() {

    }

    /**
     * 初始化toolBar
     */
    protected void initToolBar() {

    }

    /**
     * 获取布局资源id
     * @return 布局资源id
     */
    protected abstract int getLayoutResource();

    @Override
    public void onClick(View v) {

    }


    /*@Override
    public void onPermissionSuccess() {
        setContentView(getLayoutResource());
        x.view().inject(this);
        mTvToolBarTitle = (TextView) mToolbar.findViewById(R.id.tv_toolbar_title);
        initView();
        initToolBar();
        initListener();
    }

    @Override
    public void onPermissionReject(String strMessage) {

        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        builder.setTitle("权限提醒")
                .setMessage("是否要拒绝权限，如果拒绝，应用将无法正常运行")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().exit();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.System.canWrite(BaseActivity.this)) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                })
                .show();

    }

    @Override
    public void onPermissionFail() {
        mPermissionUtil.requestPermissions(this, PERMISSION_CODE,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this);
    }*/
}
