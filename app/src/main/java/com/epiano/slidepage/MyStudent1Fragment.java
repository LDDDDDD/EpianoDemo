package com.epiano.slidepage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epiano.adapter.StudentAdapter;
import com.epiano.bean.JsonUtils;
import com.epiano.bean.Student;
import com.epiano.commutil.R;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MyStudent1Fragment extends Fragment {


    @Bind(R.id.studentList)
    RecyclerView studentList;

    StudentAdapter adapter;
    List<Student> students;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // 初始化自定义的适配器
                adapter = new StudentAdapter(getActivity(), students);
                // 为mRecyclerView设置适配器
                studentList.setAdapter(adapter);
            }
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread thread = new Thread(new StudnetHandler());
        thread.start();
    }

    class StudnetHandler implements Runnable {
        @Override
        public void run() {
            try{
                JsonUtils jsonUtils = new JsonUtils();
                students = jsonUtils.parseMovieTimeFromJson(htmldata("http://121.42.153.237/liudian/student.php"));
            }catch (Exception e){
                e.getStackTrace();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init(){
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 设置LinearLayoutManager
        studentList.setLayoutManager(layoutManager);
        // 设置ItemAnimator
        studentList.setItemAnimator(new DefaultItemAnimator());
        // 设置固定大小
        studentList.setHasFixedSize(true);
    }

    private String htmldata(String url) {
        String data = "";
        // 创建okHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("userid", 10+"");
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

    @Override
    public void onResume() {
        super.onResume();
        Thread thread = new Thread(new StudnetHandler());
        thread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
