package com.epiano.slidepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.epiano.bean.CustomDate;
import com.epiano.commutil.R;
import com.epiano.utils.DateUtil;

import butterknife.Bind;
import butterknife.ButterKnife;


public class Lession1Fragment extends Fragment {

	protected static final String TAG = "MainActivity";

	/**月视图 or 周视图*/
	@Bind(R.id.fl_view)
	FrameLayout mFrameLayout;
	
	private CustomDate mClickDate;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lession, container, false);
		ButterKnife.bind(this,view);

		CourseTable table = new CourseTable();
		table.setmContext(getActivity());

		//设置选中日期
		if(mClickDate!=null){
			CustomDate mShowDate = mClickDate;
			int curMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
			//获取周日
			if (mShowDate.day - mShowDate.week+7 > curMonthDays){
				if (mShowDate.month == 12) {
					mShowDate.month = 1;
					mShowDate.year += 1;
				} else {
					mShowDate.month += 1;
				}
				mShowDate.day = (mShowDate.day- mShowDate.week-1)+7-curMonthDays;
			}else{
				mShowDate.day = mShowDate.day - mShowDate.week+7;
			}
			table.setShowDate(mShowDate);
		}

		getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fl_view, table).commit();

		return view;
	}

}
