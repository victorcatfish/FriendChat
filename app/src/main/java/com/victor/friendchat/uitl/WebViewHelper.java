package com.victor.friendchat.uitl;

import android.os.Build;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import java.io.File;

/** WebView的工具类，方便管理webView显示
 * Created by Victor on 2016/7/10.
 */
public class WebViewHelper {

    public static final String IMAGE_STYLE = "<head><style>img{max-width:100%;height:auto;}</style></head>";
    public static final String LOCAL_HTML_NAME = "local_html";

    public static void settingWevView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setDefaultFontSize(15);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        int sysVersion = Build.VERSION.SDK_INT;
        if (sysVersion >= 11) {
            settings.setDisplayZoomControls(false);
        } else {
            ZoomButtonsController zbc = new ZoomButtonsController(webView);
            zbc.getZoomControls().setVisibility(View.GONE);
        }
    }

    /**强html类型的数据保存到本地文件，方便返回的时候调用
     * 默认路径如果有SD卡 SD卡下的html文件目录,如果不存在 应用cache目录*/
    public static void saveData2Localhtml(String htmlData, String fileName) {
        String dir = FileUtils.getDir("html");
        File file = new File(dir, fileName);
        FileUtils.writeFile(htmlData, file.getAbsolutePath(), false);
    }

    /**获取本地的html文件(默认路径如果有SD卡 SD卡下的html文件目录,如果不存在 应用cache目录)*/
    public static String getLocalHtmlDataPath(String fileName) {
        return FileUtils.getDir("html") + fileName;
    }

}
