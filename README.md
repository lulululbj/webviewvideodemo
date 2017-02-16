# videodemo
webview实现全屏播放的一种方式

　　用过WebView的开发者们肯定都知道这里面的坑数不胜数，加载缓慢，内存泄露，文件选择......没错，全屏播放视频，这又是一个大坑。一个没有修饰过的原生WebView几乎不可能在某一个主流视频网站实现全屏播放，倘若在客户端自己实现简单的播放器，链接拿过来，摆个VideoView，想怎么全屏怎么全屏，放在WebView上，一切就悲剧了，大多数情况下点击全屏按钮是没有反应的，或者无法实现横屏全屏。今天来介绍一种简单易行粗暴的方式来实现WebView的视频全屏播放。

　　当你无从下手的时候，照例先看一下[官方文档](https://developer.android.com/reference/android/webkit/WebView.html)，很多常见的问题官方文档都给我们提供思路。你会发现下面这样一段话，

![](http://upload-images.jianshu.io/upload_images/2466095-e4f037256adde6ef.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

　　应用如果需要支持HTML5的video标签，必须打开硬件加速。我们只需要在Application标签或者相应Activity标签下添加`android:hardwareAccelerated="true"`即可。接着为了支持全屏，需要重写`WebChromeClient`的`onShowCustomView()`和`onHideCustomView()`方法，这两个方法缺一不可。先来看一下`onShowCustomView（）`:

![](http://upload-images.jianshu.io/upload_images/2466095-1bc33530363160a0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

　　当前页面进入全屏模式的时候会调用这个方法，并且返回了两个参数。第一个是我们要在全屏模式时显示的View，第二个是一个CustomViewCallBack接口，可以调用这个接口请求关闭全屏模式。再看一下`onHideCustomView()`方法：

![](http://upload-images.jianshu.io/upload_images/2466095-773bd024ee72825f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

　　通知应用当前页面已经关闭全屏模式，我们需要做的操作是隐藏之前`onSHowCustomView()`方法中取到的View。了解这两个方法之后，我们就可以进行一些操作来实现简单的全屏播放了。布局文件中我们增加一个和WebView同层级的Framelayout，如下所示：

        <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

        <FrameLayout
        android:id="@+id/videoContainer"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

　　在Activity中实现自定义的WebChromeClient，在onShowCustomView中横屏，隐藏WebView,并将得到的View添加到FrameLayout中显示。在onHideCustomView中隐藏View，显示WebView，并竖屏，代码如下：

    ` private class CustomWebViewChromeClient extends WebChromeClient{

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            fullScreen();
            mWebView.setVisibility(View.GONE);
            mVideoContainer.setVisibility(View.VISIBLE);
            mVideoContainer.addView(view);
            mCallBack=callback;
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            fullScreen();
            if (mCallBack!=null){
                mCallBack.onCustomViewHidden();
            }
            mWebView.setVisibility(View.VISIBLE);
            mVideoContainer.removeAllViews();
            mVideoContainer.setVisibility(View.GONE);
            super.onHideCustomView();
        }
    }

    private void fullScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }`

　　最后别忘记处理屏幕旋转，否则横竖屏切换会重新走一遍生命周期。通过在一些主流视频网站上的测试，在爱奇艺，土豆，芒果TV，PPTV等可正常全屏，在腾讯，乐视，BiliBili,Acfun等网站仍然无法全屏。通过日志我们可以发现，根本没有回调onShowCustomView这个方法，所以没有执行相应代码。原因暂时还不得而知，有小伙伴了解的可以沟通一下。那么如何得到用户点击全屏按钮的事件呢，既然是一个html页面，java语言可以操作的东西就不多了，JavaScript就可以大显身手了。通过向页面注入一些js语句我们可以做很多事情，只需要知道全屏按钮的Class标识，就可以通过js，当用户点击全屏按钮的时候调用我们本地方法，具体代码如下：

    "javascript:document.getElementsByClassName('" + tag + "')[0].addEventListener('click',function(){onClick.fullscreen();return false;});"

　　tag是Class标识，onClick.fullscreen()是我自己本地定义的方法。那么如何注入这段js代码呢？我们只需要重写WebClient的`onPageFinished()`方法，如下所示：

    private class CustomWebClient extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js=TagUtils.getJs(url);
            view.loadUrl(js);
        }
    }

    private class JsObject{

        @JavascriptInterface
        public void fullscreen(){
            //监听到用户点击全屏按钮
           fullScreen();
        }
    }

    mWebView.addJavascriptInterface(new JsObject(),"onClick");

　　这样就可以实现上述几个网站的全屏播放了。
   经测试，腾讯和BiliBili没有问题了，乐视和Acfun仍然不可以全屏，即使已经找到了全屏按钮的Class标识。哪位大仙可以提供一个解释。
   下面给出一些我收集的几个视频网站的全屏按钮Class标识：

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

　　总结一下，正如文章题目所说，实现WebView全屏播放的一种方式，而且肯定不是主流的一种方式，仅仅只是一种比较简单的方式。通过反编译一些浏览器应用的apk，大多数是通过js获取到当前页面视频的链接，用自定义的播放器来播放，这样使得播放界面可以自定义，用户体验更好，当然，我也想过这种方法，可是已经跪在起点，如何获得当前页面视频的播放地址。研究过的同学可以和我交流交流，不胜感激。



> 有任何疑问，欢迎加群讨论：261386924

　　
