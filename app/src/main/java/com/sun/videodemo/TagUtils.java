package com.sun.videodemo;

import android.text.TextUtils;

/**
 * TODO
 * Created by luyao
 * on 2016/10/21 10:27
 */
public class TagUtils {

    public static String getTagByUrl(String url) {
        if (url.contains("qq")) {
            return "tvp_fullscreen_button"; // http://m.v.qq.com
        } else if (url.contains("youku")) {
            return "x-zoomin";              // http://www.youku.com
        } else if (url.contains("bilibili")) {
            return "icon-widescreen";       // http://www.bilibili.com/mobile/index.html
        } else if (url.contains("acfun")) {
            return "controller-btn-fullscreen"; //http://m.acfun.tv   无效
        } else if (url.contains("le")) {
            return "hv_ico_screen";         // http://m.le.com  无效
        }
        return "";
    }

    //  "javascript:document.getElementsByClassName('" + referParser(url) + "')[0].addEventListener('click',function(){local_obj.playing();return false;});"
    public static String getJs(String url) {
        String tag = getTagByUrl(url);
        if (TextUtils.isEmpty(tag)) {
            return "javascript:";
        } else {
            return "javascript:document.getElementsByClassName('" + tag + "')[0].addEventListener('click',function(){onClick.fullscreen();return false;});";
        }
    }
}
