package com.victor.friendchat.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.victor.friendchat.uitl.UIUtils;

/**
 * Created by Victor on 2016/12/28.
 */
public class FriendFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(UIUtils.getContext());
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);
        textView.setText("好友Fragment");
        return textView;
    }
}
