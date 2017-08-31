package com.epiano.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epiano.bean.Song;
import com.epiano.commutil.R;
import com.xdandroid.simplerecyclerview.Adapter;

import java.util.List;

public abstract class BasicAdapter extends Adapter {

    List<Song> mSampleList;

    public void setList(List<Song> sampleList) {
        mSampleList = sampleList;
        notifyDataSetChanged();
    }

    @Override
    protected RecyclerView.ViewHolder onViewHolderCreate(ViewGroup parent, int viewType) {
        return new TextVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false));
    }

    @Override
    protected void onViewHolderBind(RecyclerView.ViewHolder holder, int position, int viewType) {
        TextVH textVH = (TextVH) holder;
        textVH.title.setText(mSampleList.get(position).getSongname());
        textVH.content.setText(mSampleList.get(position).getWriter());
    }

    @Override
    protected int getCount() {
        return mSampleList != null ? mSampleList.size() : 0;
    }

    static final class TextVH extends RecyclerView.ViewHolder {
        TextVH(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.tv_title);
            content = (TextView) v.findViewById(R.id.tv_content);
        }

        TextView title, content;
    }

    @Override
    protected int getItemSpanSizeForGrid(int position, int viewType, int spanSize) {
        return 1;
    }
}
