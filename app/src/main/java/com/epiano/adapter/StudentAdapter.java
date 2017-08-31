package com.epiano.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.epiano.bean.Student;
import com.epiano.commutil.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/8/10.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> actors;
    private Context mContext;
    private int[] imageIds = new int[]
            {R.drawable.dingdangmao, R.drawable.haizeiwang,
                    R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao, R.drawable.dingdangmao};

    public StudentAdapter(Context context, List<Student> actors) {
        this.mContext = context;
        this.actors = actors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //给ViewHolder设置布局文件
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.studentlist_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        // 给ViewHolder设置元素
        Student p = actors.get(i);
        viewHolder.Name.setText(p.getUserName());
        int j = Integer.parseInt(p.getPortraitId().toString());

        viewHolder.icon.setBackgroundResource(imageIds[j]);
        viewHolder.phone.setText(p.getPhoneNum());
    }

    @Override
    public int getItemCount() {
        // 返回数据总数
        return actors == null ? 0 : actors.size();
    }


    // 重写的自定义ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.Name)
        TextView Name;
        @Bind(R.id.phone)
        TextView phone;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
