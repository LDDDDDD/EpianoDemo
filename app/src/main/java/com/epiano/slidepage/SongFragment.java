package com.epiano.slidepage;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.epiano.adapter.BasicAdapter;
import com.epiano.bean.JsonUtils;
import com.epiano.bean.Song;
import com.epiano.commutil.R;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xdandroid.simplerecyclerview.Divider;
import com.xdandroid.simplerecyclerview.OnItemClickListener;
import com.xdandroid.simplerecyclerview.SimpleRecyclerView;
import com.xdandroid.simplerecyclerview.SimpleSwipeRefreshLayout;
import com.xdandroid.simplerecyclerview.UIUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongFragment extends Fragment {

    SimpleSwipeRefreshLayout mSwipeContainer;
    SimpleRecyclerView mRecyclerView;
    BasicAdapter mAdapter;
    List<Song> mSampleList;
    List<Song> moreSampleList;
    JsonUtils jsonUtils;
    int startnum = 0;
    int endnum = 10;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                setupRecyclerView();
                initData();
            }
            if (msg.what == 2) {
                mSampleList.addAll(moreSampleList);
                mAdapter.onAddedAll(mSampleList.size());
                System.out.println("我是xia吗？" + startnum+"-->"+ endnum+"-->"+mSampleList.size());
            }
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSwipeContainer = (SimpleSwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mRecyclerView = (SimpleRecyclerView) view.findViewById(R.id.recycler_view);
        setupSwipeContainer(view);
        jsonUtils = new JsonUtils();
        Thread thread = new Thread(new SongHandler());
        thread.start();
    }

    class SongHandler implements Runnable {
        @Override
        public void run() {
            try{
                mSampleList = jsonUtils.parseSongFromJson( getSongList("http://121.42.153.237/liudian/song.php"));
            }catch (Exception e){
                e.getStackTrace();
            }
        }
    }

    class Song1Handler implements Runnable {
        @Override
        public void run() {
            try{
                moreSampleList = jsonUtils.parseSongFromJson( getSongList1("http://121.42.153.237/liudian/song.php"));
            }catch (Exception e){
                e.getStackTrace();
            }
        }
    }

    void setupSwipeContainer(View fragmentView) {
        mSwipeContainer.setColorSchemeResources(R.color.colorAccent);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {initData();}
        });

        //启动SwipeRefreshLayout样式下拉刷新转圈。
        //mSwipeContainer.setRefreshing(true);
        //启动自定义LoadingView布局。
        mRecyclerView.setLoadingView(fragmentView.findViewById(R.id.loading_view));
    }

    void setupRecyclerView() {
        //添加Divider
        mRecyclerView.addItemDecoration(new Divider(
                //分割线宽1dp
                UIUtils.dp2px(getActivity(), 1),
                //分割线颜色#DDDDDD
                Color.parseColor("#DDDDDD"),
                false,
                //分割线左侧留出20dp的空白，不绘制
                UIUtils.dp2px(getActivity(), 20), 0, 0, 0));

        mAdapter = new BasicAdapter() {

            protected void onLoadMore(Void v) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        moreSampleList = new ArrayList<>();
                        Thread thread = new Thread(new Song1Handler());
                        thread.start();
                        startnum = endnum;
                        endnum +=10;
                    }
                }, 1777);
            }

            protected boolean hasMoreElements(Void v) {
                return mSampleList != null && mSampleList.size() <= 666;
            }

            @Override
            protected int getViewType(int i) {
                return 0;
            }
        };

        //设置加载更多的Threshold, 即离最后一个还有多少项时就开始提前加载
        mAdapter.setThreshold(7);

        //设置点击事件的监听器
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position, int viewType) {
                Toast.makeText(getActivity(), "Clicked " + position, Toast.LENGTH_SHORT).show();
                String url = "http://121.42.153.237/sdir/"+position+".sco";

            }
        });
        /**
         * true为使用 SwipeRefreshLayout 样式的加载更多转圈，以及设置转圈的颜色。false为使用 ProgressBar样式的加载更多转圈。
         * SwipeRefreshLayout 样式与系统版本无关。
         * ProgressBar的外观因系统版本而异，仅在 API 21 以上的 Android 系统中具有 Material Design 风格。
         */
        mAdapter.setUseMaterialProgress(true, new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});

        //也可单独调用API设置转圈的颜色变化序列.
        //mAdapter.setColorSchemeColors(new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});

        //可设置转圈所在圆形突起的背景色.
        //mAdapter.setProgressBackgroundColor(0xFFFAFAFA);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        //设置EmptyView
        //mRecyclerView.setEmptyView(getActivity().findViewById(R.id.empty_view));

        //显示ErrorView
        //mRecyclerView.showErrorView(getActivity().findViewById(R.id.error_view));

        //隐藏ErrorView
        //mRecyclerView.hideErrorView();
    }

    void initData() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mAdapter.setList(mSampleList);
                mSwipeContainer.setRefreshing(false);
            }
        }, 1777);
    }


    private String getSongList(String url) {
        String data = "";
        // 创建okHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("snum", startnum+"");
        builder.add("enum", endnum+"");
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        //发送请求获取响应
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            data = response.body().string();
            mHandler.sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private String getSongList1(String url) {
        String data = "";
        // 创建okHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("snum", startnum+"");
        builder.add("enum", endnum+"");
        //创建一个请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        //发送请求获取响应
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            data = response.body().string();
            mHandler.sendEmptyMessage(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
