package com.epiano.slidepage;

//import com.example.android.apis.R;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.epiano.commutil.MusicScoreBook3D;
import com.epiano.commutil.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import static android.R.attr.targetSdkVersion;


public class SongsFragment extends Fragment {

    private WebView webview;

    static Context mCtx;

    public void setctx(Context ctx) {
        // Required empty public constructor
        mCtx = ctx;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        // FrameLayout fl = new FrameLayout(getActivity());
        // fl.setLayoutParams(params);
        // DisplayMetrics dm = getResources().getDisplayMetrics();
        // final int margin = (int)
        // TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
        // TextView v = new TextView(getActivity());
        // params.setMargins(margin, margin, margin, margin);
        // v.setLayoutParams(params);
        // v.setLayoutParams(params);
        // v.setGravity(Gravity.CENTER);
        // v.setText("聊天界面");
        // v.setTextSize((int)
        // TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, dm));
        // fl.addView(v);
        // return fl;

        View view = inflater.inflate(R.layout.songslistview, container, false);
        webview = (WebView) view.findViewById(R.id.webView1); // messagelistview
        // webview.setAdapter(simplead);
        // messagelistview.setOnItemClickListener(new
        // AdapterView.OnItemClickListener() {
        // public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
        // long arg3) {
        // //通过单击事件，获得单击选项的内容
        // String text = messagelistview.getItemAtPosition(arg2) + "";
        // //通过Toast对象显示出来。
        // Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); //
        // getContext()
        // }
        // });

        final String mimeType = "text/html";

        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);// 设置js可以直接打开窗口，如window.open()，默认为false
        webview.getSettings().setJavaScriptEnabled(true);// 是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        // webview.getSettings().setSupportZoom(true);//是否可以缩放，默认true
        // webview.getSettings().setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        // webview.getSettings().setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        // webview.getSettings().setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        webview.getSettings().setAppCacheEnabled(true);// 是否使用缓存
        webview.getSettings().setDomStorageEnabled(true);// DOM Storage
        // //
        // displayWebview.getSettings().setUserAgentString("User-Agent:Android");//设置用户代理，一般不用

//		webview.setWebChromeClient(new MyWebChromeClient());
        webview.requestFocus();

        // webview.loadData("<a href='http://121.42.153.237/SongLst.php?page=1'>Hello World! - 1</a>", mimeType, null);
        //webView.loadUrl(“file:///android_asset/XX.html“); // 可打开本地URL
        webview.loadUrl("http://121.42.153.237/SongLst.php?page=1");

        // 设置web视图客户端
        webview.setWebViewClient(new MyWebViewClient());
        webview.setDownloadListener(new MyWebViewDownLoadListener());

        mHandler = new Handler();

        return view;
    }


    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
             Log.i("tag", "url="+url);
            // Log.i("tag", "userAgent="+userAgent);
            // Log.i("tag", "contentDisposition="+contentDisposition);
            // Log.i("tag", "mimetype="+mimetype);
            // Log.i("tag", "contentLength="+contentLength);

            Uri uri = Uri.parse(url);

//			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//			//if(intent.resolveActivity(getPackageManager()) != null)
//			{
//				startActivity(intent);
//			}

            // 必须用线程下载，否则崩溃
            DownLoadThread th = new DownLoadThread();
            th.setUrl(url.toString());
            th.start();
        }
    }

    // 内部类
    public class MyWebViewClient extends WebViewClient {
        boolean loadError = false;

        // 如果页面中链接，如果希望点击链接继续在当前browser中响应，
        // 而不是新开Android的系统browser中响应该链接，必须覆盖 webview的WebViewClient对象。
        public boolean shouldOverviewUrlLoading(WebView view, String url) {
            Log.i("tag", "shouldOverviewUrlLoading");
            view.loadUrl(url);
            return true;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i("tag", "onPageStarted");
            //showProgress();
        }

        public void onPageFinished(WebView view, String url) {
            Log.i("tag", "onPageFinished");
            //closeProgress();

            if (!loadError) {//当网页加载成功的时候判断是否加载成功
//                rl_detail.setVisibility(View.GONE);//加载成功的话，则隐藏掉显示正在加载的视图，显示加载了网页内容的WebView
//                webView.setEnabled(true);
//                ll_container_btn.setVisibility(View.VISIBLE);
//                btn_collect.setVisibility(View.VISIBLE);

//				view.loadUrl("javascript:window.local_obj.showSource('<head>'+"
//                        + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
//				view.loadUrl("javascript:window.local_obj.showSource('');");

                loadError = false;
            } else { //加载失败的话，初始化页面加载失败的图，然后替换正在加载的视图页面
//                rl_detail.removeAllViews();
//                emptyView.setEmptyView(EmptyView.EMPTY_EMPTY, "您找的页面暂时走丢了...");
//                rl_detail.addView(emptyView);

                loadError = false;
            }
        }

        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.i("tag", "onReceivedError");

            loadError = true;
            //closeProgress();
        }

        final class InJavaScriptLocalObj {
            @JavascriptInterface
            public void showSource(String html) {
                int z = 0;
                Log.i("tag", html);
            }
        }

    }

    // 如果不做任何处理，浏览网页，点击系统“Back”键，整个Browser会调用finish()而结束自身，
    // 如果希望浏览的网 页回退而不是推出浏览器，需要在当前Activity中处理并消费掉该Back事件。
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // if((keyCode==KeyEvent.KEYCODE_BACK)&&webview.canGoBack()){
        // webview.goBack();
        // return true;
        // }
        return false;
    }

    private Handler mHandler;
    final Runnable mUpdateUI = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // mImag.setImageBitmap(bitmap); // draw

            Intent intent = new Intent();
            intent.setClass(getActivity(), MusicScoreBook3D.class);

            //EditText FileNmaeEditText = (EditText) findViewById(R.id.editText1);
            //String openfilename = FileNmaeEditText.getText().toString();

            Bundle bundle = new Bundle();

            bundle.putString("openfilename", DlFileName);
            intent.putExtras(bundle);

            startActivityForResult(intent, 0);
        }
    };

    String DlFileName;

    class DownLoadThread extends Thread {

        int counter = 0;

        boolean mRuning = true;

        String mdownloadUrl;

        void setUrl(String downloadUrl) {
            mdownloadUrl = downloadUrl;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            //super.run();

            //InitAVCom();

            while (mRuning) {

                //long tick = System.currentTimeMillis();
                download(mdownloadUrl);

                break;
            }

            return;
        }
    }

    String GetScoName(String url) {
        String a[] = url.split("/");

        if (a.length > 0) {
            return a[a.length - 1];
        }

        return null;
    }

    //下载具体操作
    private void download(String downloadUrl) {
        try {

            String scoName = GetScoName(downloadUrl);
            if (scoName == null) {
                return;
            }

            URL url = new URL(downloadUrl);
            //打开连接
            URLConnection conn = url.openConnection();
            //打开输入流
            InputStream is = conn.getInputStream();
            //获得长度
            int contentLength = conn.getContentLength();
            Log.e("tag", "contentLength = " + contentLength);


            File f = getActivity().getFilesDir();
//            File imagePath = new File(f, "images");
//            File newFile = new File(imagePath, "default_image.jpg");
//            Uri contentUri = getUriForFile(mCtx, "com.mydomain.fileprovider", newFile);
            //创建文件夹 MyDownLoad，在存储卡下
            //String dirName = Environment.getExternalStorageDirectory() + "/"; //  + "/MyDownLoad/";
            String dirName = f + "/";


            File file = new File(dirName);
            //不存在创建
            if (!file.exists()) {
                file.mkdir();
            }

            //下载后的文件名
            DlFileName = dirName + scoName;
            File file1 = new File(DlFileName);
            if (file1.exists()) {
                file1.delete();
            }
            //创建字节流
            byte[] bs = new byte[1024];
            int len;
            OutputStream os = new FileOutputStream(DlFileName);
            //写数据
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            //完成后关闭流
            Log.e("tag", "download-finish");
            os.close();
            is.close();

            mHandler.post(mUpdateUI);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 内部类
    private class DownloaderTask extends AsyncTask<String, Void, String> {

        public DownloaderTask() {
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            String url = params[0];
            // Log.i("tag", "url="+url);
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            fileName = URLDecoder.decode(fileName);
            Log.i("tag", "fileName=" + fileName);

            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory, fileName);
            if (file.exists()) {
                Log.i("tag", "The file has already exists.");
                return fileName;
            }
            try {
                HttpClient client = new DefaultHttpClient();
                // client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
                HttpGet get = new HttpGet(url);
                HttpResponse response = client.execute(get);
                if (HttpStatus.SC_OK == response.getStatusLine()
                        .getStatusCode()) {
                    HttpEntity entity = response.getEntity();
                    InputStream input = entity.getContent();

                    writeToSDCard(fileName, input);

                    input.close();
                    // entity.consumeContent();
                    return fileName;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            closeProgressDialog();
            if (result == null) {
                Toast t = Toast.makeText(mCtx, "连接错误！请稍后再试！",
                        Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }
            Log.i("tag", "已保存到SD卡。");
            Toast t = Toast.makeText(getActivity(), "已保存到SD卡。", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory, result);
            Log.i("tag", "Path=" + file.getAbsolutePath());

            Intent intent = getFileIntent(file);

            startActivity(intent);

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }

    private ProgressDialog mDialog;

    private void showProgressDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(getActivity());
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
            mDialog.setMessage("正在加载 ，请等待...");
            mDialog.setIndeterminate(false);// 设置进度条是否为不明确
            mDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    mDialog = null;
                }
            });
            mDialog.show();

        }
    }

    private void closeProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public Intent getFileIntent(File file) {
        // Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type=" + type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    public void writeToSDCard(String fileName, InputStream input) {

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory, fileName);
            // if(file.exists()){
            // Log.i("tag", "The file has already exists.");
            // return;
            // }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = new byte[2048];
                int j = 0;
                while ((j = input.read(b)) != -1) {
                    fos.write(b, 0, j);
                }
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Log.i("tag", "NO SDCard.");
        }
    }

    private String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
		/* 取得扩展名 */
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();

		/* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
                || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
			/* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        }
        // else if(end.equals("pptx")||end.equals("ppt")){
        // type = "application/vnd.ms-powerpoint";
        // }else if(end.equals("docx")||end.equals("doc")){
        // type = "application/vnd.ms-word";
        // }else if(end.equals("xlsx")||end.equals("xls")){
        // type = "application/vnd.ms-excel";
        // }
        else {
            // /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }


    public boolean selfPermissionGranted(String permission) {
        //对于Android <Android M，始终授予自我权限。
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion> = Android M，我们可以
                // use Context＃checkSelfPermission
                result = mCtx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion <Android M，我们必须使用PermissionChecker
//                result = PermissionChecker.checkSelfPermission（上下文，权限）
//                == PermissionChecker.PERMISSION_GRANTED;
                result = PermissionChecker.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PermissionChecker.PERMISSION_DENIED;
            }
            return result;
        }
        return result;
    }
}
