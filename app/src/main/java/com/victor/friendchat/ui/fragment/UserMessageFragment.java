package com.victor.friendchat.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.victor.friendchat.R;
import com.victor.friendchat.domain.User;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/** 用户信息Fragment
 * Created by Victor on 2016/12/29.
 */
public class UserMessageFragment extends android.support.v4.app.Fragment {

    @ViewInject(R.id.tv_user_id)
    private TextView mTvId;
    @ViewInject(R.id.tv_user_age)
    private TextView mTvAge;
    @ViewInject(R.id.tv_user_gender)
    private TextView mTvGender;
    @ViewInject(R.id.tv_user_qq)
    private TextView mTvQQ;

    private User mUser;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_message, container, false);
        x.view().inject(this, view);
        if (mUser != null) {
            Drawable nav_up = null;
            if (mUser.sex.equals("男")) {
                mTvGender.setText("男");
                nav_up = getResources().getDrawable(R.mipmap.icon_male);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                mTvGender.setCompoundDrawables(null, null, nav_up, null);
            } else {
                mTvGender.setText("女");
                nav_up = getResources().getDrawable(R.mipmap.icon_female);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                mTvGender.setCompoundDrawables(null, null, nav_up, null);
            }
            mTvAge.setText(mUser.years);
            mTvQQ.setText(mUser.qq);
            mTvId.setText(mUser.id + "");
        }
        return view;
    }
}
