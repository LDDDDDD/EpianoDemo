package com.epiano.slidepage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LessionFragment extends Fragment {


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout fl = new FrameLayout(getActivity());
		fl.setLayoutParams(params);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);

//		TextView v = new TextView(getActivity());
//		params.setMargins(margin, margin, margin, margin);
//		v.setLayoutParams(params);
//		v.setLayoutParams(params);
//		v.setGravity(Gravity.CENTER);
//		v.setText("排课");
//		v.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, dm));
//		fl.addView(v);

//		ListView lv = new ListView(getActivity());
//		//lv.setGravity(Gravity.CENTER);
//		fl.addView(lv);

		Context mCtx = getActivity();

		ListView listView = new ListView(mCtx);
		final List<HashMap<String, String>> list = initlist();
		//Log.v("ss", list.size() + "");
		ExtAdapter ext = new ExtAdapter(mCtx, list);
		listView.setAdapter(ext);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				String[] name = { "用户", "名称", "年龄" };
				String[] value = new String[name.length];
				//Log.v("ss", arg2 + "");
				HashMap<String, String> tmp = list.get(arg2);
				int i = 0;
				for (String key : tmp.keySet()) {
					//Log.v("sws", i + "");
					//Log.v("key", key + tmp.get(key));
					value[i] = tmp.get(key);
					i++;
				}
//				Intent intent = new Intent(Test.this, TT.class);
//				Bundle bundle = new Bundle();
//				bundle.putStringArray("name", name);
//				bundle.putStringArray("value", value);
//				intent.putExtras(bundle);
//				startActivity(intent);

				Toast.makeText(getActivity(), value[0], Toast.LENGTH_SHORT).show();
			}

		});

		fl.addView(listView);

		return fl;
	}

	public List<HashMap<String, String>> initlist() {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = null;
		for (int i = 0; i < 10; i++) {
			map = new HashMap<String, String>();
			map.put("id", i + "");
			map.put("name", i + "ww");
			map.put("age", i + "aa");
			list.add(map);
		}
		map = new HashMap<String, String>();
		map.put("id", "用户");
		map.put("name", "姓名");
		map.put("age", "年龄");
		list.add(0, map);
		return list;
	}

	public class ExtAdapter extends BaseAdapter {

		// 数据源
		private List<HashMap<String, String>> list;
		private Context context;

		public ExtAdapter(Context context, List<HashMap<String, String>> list) {
			this.context = context;
			this.list = list;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TableLayout tab = new TableLayout(context);
			tab.setStretchAllColumns(true);
			TableRow row = new TableRow(context);
			TextView tv = null;
			HashMap<String, String> map = list.get(position);
			for (String key : map.keySet()) {
				tv = new TextView(context);
				tv.setText(map.get(key));
				tv.setHeight(30);
				row.addView(tv);
			}
			tab.addView(row);
			return tab;
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
