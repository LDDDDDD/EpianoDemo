package com.epiano.slidepage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.view.Window;

import com.epiano.commutil.R;
import com.epiano.commutil.epdeamon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

//import android.epiano.com.commutil.PagerSlidingTabStrip;

public class SlidePages extends FragmentActivity {

	/**
	 * 聊天界面的Fragment
	 */
	private MeFragment meFragment;

	/**
	 * 发现界面的Fragment
	 */
	private FoundFragment foundFragment;

	/**
	 * 通讯录界面的Fragment
	 */
	private Lession1Fragment contactsFragment;

	private SongsFragment songFragment;

	private ToolsFragment toolsFragment;

	private MyStudent1Fragment myStudentsFragment;


	/**
	 * PagerSlidingTabStrip的实例
	 */
	private PagerSlidingTabStrip tabs;

	/**
	 * 获取当前屏幕的密度
	 */
	private DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activityslide);
		setOverflowShowingAlways();
		dm = getResources().getDisplayMetrics();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));//getSupportFragmentManager
		tabs.setViewPager(pager);
		setTabsValue();
	}

	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		tabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		tabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		// 设置Tab Indicator的高度
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		// 设置Tab标题文字的大小
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, dm));
		// 设置Tab Indicator的颜色
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		// 设置选中Tab文字的颜色 (这是我自定义的一个方法)
		tabs.setSelectedTextColor(getResources().getColor(R.color.white));
		// 取消点击Tab时的背景色
//		tabs.setTabBackground(0);
		tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		//private final String[] titles = { "聊天", "发现", "通讯录" };
		private final String[] titles = {"曲库", "我的学生", "我的课排", "我的信息", "工具"};

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					if (songFragment == null) {
						songFragment = new SongsFragment();
//						songFragment.setctx(getBaseContext());
					}
					return songFragment;
				case 1:
//				if (foundFragment == null) {
//					foundFragment = new FoundFragment();
//				}
//				return foundFragment;
					if (myStudentsFragment == null) {
						myStudentsFragment = new MyStudent1Fragment();
						//myStudentsFragment.setctx(getBaseContext());
					}
					return myStudentsFragment;
				case 2:
					if (contactsFragment == null) {
						contactsFragment = new Lession1Fragment();
					}
					return contactsFragment;
				case 3:
					if (meFragment == null) {
						meFragment = new MeFragment();
					}
					return meFragment;
				case 4:
					if (toolsFragment == null) {
						toolsFragment = new ToolsFragment();
					}
					return toolsFragment;
				default:
					return null;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		stopservice();
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Intent intent;
	private int stopservice()
	{
		{
			intent = new Intent(this, epdeamon.class);
			Bundle bundle=new Bundle();

//			EditText Epassword = (EditText)findViewById(R.id.password);
//	        password = Epassword.getText().toString();
//			bundle.putString("password", password);
//
//			EditText Eusername = (EditText)findViewById(R.id.username);
//	        username = Eusername.getText().toString();
//			bundle.putString("username", username);
//
//			intent.putExtras(bundle);

			stopService(intent);
		}

//		intent = new Intent(Login.this, MainActivity.class);
//		startActivity(intent);
//		Login.this.finish();

		return 1;
	}
}