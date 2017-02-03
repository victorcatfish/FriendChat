package com.victor.friendchat.uitl;

import android.content.Context;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**SweetAlertDialog的构造类
 * Created by Victor on 2016/12/23.
 */

public class DialogViewBuilder {

    public static SweetAlertDialog sDialog;

    public static void init(Context context, String message) {
        sDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sDialog.getProgressHelper().setBarColor(Color.parseColor("#22AF5C"));
        sDialog.setTitleText(message);
        sDialog.setCancelable(true);

    }

    public static void show() {
        if (!sDialog.isShowing()) {
            sDialog.show();
        }

    }

    public static void dismiss() {
        if (sDialog.isShowing()) {
            sDialog.dismiss();
        }

    }
}
