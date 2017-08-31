package com.epiano.slidepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.epiano.commutil.R;
import com.epiano.view.LSettingItem;


public class MeFragment extends Fragment {

	private LSettingItem mSettingItemOne;
	private LSettingItem mSettingItemFour;

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_me, null);
		mSettingItemOne = (LSettingItem) view.findViewById(R.id.item_one);
		mSettingItemFour = (LSettingItem) view.findViewById(R.id.item_four);

		mSettingItemOne.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
			@Override
			public void click(boolean isChecked) {
				Toast.makeText(getActivity().getApplicationContext(), "我的消息", Toast.LENGTH_SHORT).show();
			}
		});
		mSettingItemFour.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
			@Override
			public void click(boolean isChecked) {
				Toast.makeText(getActivity().getApplicationContext(), "选中开关：" + isChecked, Toast.LENGTH_SHORT).show();
			}
		});
		mSettingItemOne.setRightText("我是右侧改变的文字");
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}
