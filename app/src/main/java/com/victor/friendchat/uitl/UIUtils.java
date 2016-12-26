package com.victor.friendchat.uitl;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.victor.friendchat.global.MyApplication;


/**Toast工具类
 * Created by Victor on 2016/7/1.
 */
public class UIUtils {

    /**展示短Toast
     * @param context 上下文环境
     * @param text 需要显示的内容
     */
    public static void showShortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /** 展示长Toast
     * @param context 上下文环境
     * @param text 需要显示的内容
     */
    public static void showLongToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**获取上下文对象
     * @return
     */
    public static Context getContext() {
        return MyApplication.getContext();
    }

    public static Handler getHandler() {
        return MyApplication.getHandler();
    }

    /*========================  加载资源文件  ========================**/

    /**获取字符串
     * @param id
     * @return
     */
    public static String getString (int id) {
        return getContext().getResources().getString(id);
    }


    public static long getMainThreadId() {
        return MyApplication.getMainThreadId();
    }

    public static Handler getMainThreadHandler() {
        return MyApplication.getHandler();
    }

    public static void postTaskOnUIThread(Runnable task) {
        int curThreadId = Process.myTid();
        if (curThreadId == getMainThreadId()) {
            task.run();
        } else {
            getMainThreadHandler().post(task);
        }
    }

    /**获取字符串数组
     * @param id
     * @return
     */
    public static String[] getStringArray(int id) {
        Context context = getContext();
        Resources resources = context.getResources();
        return resources.getStringArray(id);
    }

    /**获取资源图片
     * @param id
     * @return
     */
    public static Drawable getDrawable(int id) {
        return getContext().getResources().getDrawable(id);
    }

    /**获取颜色
     * @param id
     * @return
     */
    public static int getColor(int id) {
        return getContext().getResources().getColor(id);
    }

    /**根据ID获取颜色状态选择器
     * @param tabTextColorResId
     * @return
     */
    public static ColorStateList getColorStateList(int tabTextColorResId) {
        return getContext().getResources().getColorStateList(tabTextColorResId);
    }

    /**获取尺寸(像素值)
     * @param id
     * @return
     */
    public static int getDimen(int id) {
        return getContext().getResources().getDimensionPixelSize(id);
    }

    /*========================  dip和px的相互转换  ========================**/

    /** dip转为px
     * @param dip
     * @return
     */
    public static int dip2px(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    /** px转dip
     * @param px
     * @return
     */
    public static float px2dip(float px) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return px / density;
    }

    /*========================  加载布局文件  ========================**/

    /** 将布局文件填充为View对象
     * @param id
     * @return
     */
    public static View inflate(int id) {
        return View.inflate(getContext(),id,null);
    }

    /*========================  线程相关  ========================**/

    /**判断当前线程是否运行在主线程
     * @return
     */
    public static boolean isRunningUIThread() {
        // 获取当前线程ID
        int myTid = Process.myTid();
        // 判断是否和主线程id相等
        if (myTid == getMainThreadId()) {
            return true;
        }
        return false;
    }

    /**让当前run方法里面的代码运行在主线程
     * @param r
     */
    public static void runningOnUIThread(Runnable r) {
        if (isRunningUIThread()) {
            // 如果本身就是运行在主线程，则直接运行
            r.run();
        } else {
            // 如果运行在子线程，借助Hnadler让其运行在主线程
            getHandler().post(r);
        }
    }

    public static int getWindowWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**得到应用程序的包名*/
    public static String getPackageName() {
        return getContext().getPackageName();
    }

    /**post一个延迟任务*/
    public static void postTaskDelay(Runnable task, long delayMills) {
        getMainThreadHandler().postDelayed(task, delayMills);
    }

    /**移除任务*/
    public static void removeTask(Runnable task) {
        getMainThreadHandler().removeCallbacks(task);
    }




}
