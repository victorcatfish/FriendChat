package com.victor.friendchat.uitl;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/** BitmapUtils的工厂类
 * Created by Victor on 2016/6/22.
 */
public class BitmapHelper {

    /*public static BitmapUtils sBitmapUtils;

    static {
        sBitmapUtils = new BitmapUtils(UIUtils.getContext());
    }*/

    public static void display(ImageView container, String uri) {
//        sBitmapUtils.display(container, uri);
        Picasso.with(UIUtils.getContext()).load(uri).into(container);
    }
}
