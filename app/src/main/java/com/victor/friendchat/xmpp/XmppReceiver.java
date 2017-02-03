package com.victor.friendchat.xmpp;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.victor.friendchat.R;
import com.victor.friendchat.domain.User;
import com.victor.friendchat.domain.XmppChat;
import com.victor.friendchat.domain.XmppFriend;
import com.victor.friendchat.domain.XmppMessage;
import com.victor.friendchat.domain.XmppUser;
import com.victor.friendchat.ui.activity.ChatActivity;
import com.victor.friendchat.ui.activity.MainActivity;
import com.victor.friendchat.uitl.SharedPreferencesUtil;
import com.victor.friendchat.uitl.TimeUtil;

import java.util.List;


/**
 * Created by jzh on 2016/1/8.
 */
public class XmppReceiver extends BroadcastReceiver {

    updateActivity ua = null;
    public NotificationManager manager = null;


    public XmppReceiver(updateActivity ua) {
        this.ua = ua;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {

        String type = intent.getStringExtra("type");
        if (type.equals("chat")) {

            XmppChat xc = (XmppChat) intent.getSerializableExtra("chat");
            if (ChatActivity.ca != null) {
                //在chat界面更新信息
                Log.i("xmpppppp", ChatActivity.sXmppFriend.user.user + "\t" + xc.nickname);
                if (ChatActivity.sXmppFriend.user.user.equals(xc.user)) {
                    ua.update(xc);
                }
                chatDatas(xc.main, xc.user, xc.too, xc.content);
            } else {
                int num = chatData(xc.main, xc.user, xc.too, xc.content);
                if (XmppService.vibrator != null && SharedPreferencesUtil.getBoolean(context, "tishi", "zhendong", true)) {
                    XmppService.vibrator.vibrate(500);
                }
                // 判断应用是否运行在前台
                if (!isAppOnForeground(context)) {

                    //在message界面更新信息
                    if (manager == null) {
                        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    }
                    Intent intent1 = new Intent(context, ChatActivity.class);
                    User users = new User();
                    users.user = xc.user;
                    users.nickname = xc.nickname;
                    intent1.putExtra("xmpp_friend", new XmppFriend(users));
                    PendingIntent pi = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notify = new Notification.Builder(context)
                            .setAutoCancel(true)
                            .setTicker("有新消息")
                            .setSmallIcon(R.mipmap.icon_logo)
                            .setContentTitle("来自" + xc.nickname + "的消息")
                            .setContentText(xc.content)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setWhen(System.currentTimeMillis())
                            .setNumber(num)
                            .setContentIntent(pi).build();
                    manager.notify(0, notify);
                } else {
                    if (XmppService.pool != null && SharedPreferencesUtil.getBoolean(context, "tishi", "music", true)) {
                        XmppService.pool.play(1, 1, 1, 0, 0, 1);
                    }
                    // 更新消息列表界面
                    if (MainActivity.main != null) {
                        MainActivity.main.mMsgFragment.initData();
                    }
                }


            }


        }
        ua.update(type);
    }


    public interface updateActivity {
        public void update(String type);


        public void update(XmppChat xc);
    }

    public int chatData(final String main, final String users, final String to, final String content) {

        Cursor cursor = XmppService.resolver.query(XmppContentProvider.CONTENT_MESSAGES_URI, null,
                "main=? and type=?", new String[]{main, "chat"}, null);

        if (!cursor.moveToFirst()) {
            //插入
            List<XmppUser> list1 = XmppTool.getInstance().searchUsers(users);
            Log.i("XmppService_add", list1.get(0).userName + "\n" + list1.get(0).name);
            XmppMessage xm = new XmppMessage(to,
                    "chat",
                    new XmppUser(list1.get(0).userName, list1.get(0).name),
                    TimeUtil.getDate(),
                    content,
                    1,
                    main
            );
            XmppContentProvider.add_message(xm);

            return 1;
        } else {
            //更新
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int result = cursor.getInt(cursor.getColumnIndex("result"));
            ContentValues values = new ContentValues();
            values.put("content", content);
            values.put("time", TimeUtil.getDate());
            values.put("result", (result + 1));
            XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?", new String[]{id + ""});
            return (result + 1);
        }

    }


    public void chatDatas(final String main, final String users, final String to, final String content) {

        Cursor cursor = XmppService.resolver.query(XmppContentProvider.CONTENT_MESSAGES_URI, null,
                "main=? and type=?", new String[]{main, "chat"}, null);

        if (!cursor.moveToFirst()) {
            //插入
            List<XmppUser> list1 = XmppTool.getInstance().searchUsers(users);
            Log.i("XmppService_add", list1.get(0).userName + "\n" + list1.get(0).name);
            XmppMessage xm = new XmppMessage(to,
                    "chat",
                    new XmppUser(list1.get(0).userName, list1.get(0).name),
                    TimeUtil.getDate(),
                    content,
                    0,
                    main
            );
            XmppContentProvider.add_message(xm);
        } else {
            //更新
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            ContentValues values = new ContentValues();
            values.put("content", content);
            values.put("time", TimeUtil.getDate());
            values.put("result", 0);
            XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?", new String[]{id + ""});

        }

    }

    /**
     * 判断应用是否运行在前台
     * @param context 上下文
     * @return 如果运行在前台 true， 否则 false
     */
    public boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }


}
