package com.epiano.slidepage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epiano.commutil.MusicScore.AppsAdapter;
import com.epiano.commutil.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyStudentsFragment extends Fragment {

	Context mCtx;

	private static String TAG = "Video";

	public MyStudentsFragment() {
		// Required empty public constructor
	}

	public void setctx(Context ctx) {
		// Required empty public constructor
		mCtx = ctx;
	}

	final int ITEMNUM = 24;
	final int GrpSize = 3;

	private String[] names = new String[]
			{ "叮当猫", "海贼王", "樱桃小丸子", "熊猫",
					"叮当猫", "海贼王", "樱桃小丸子", "熊猫",
					"叮当猫", "海贼王", "樱桃小丸子", "熊猫",
					"叮当猫", "海贼王", "樱桃小丸子", "熊猫",
					"叮当猫", "海贼王", "樱桃小丸子", "熊猫",
					"叮当猫", "海贼王", "樱桃小丸子", "熊猫",
			};
	private String[] descs = new String[]
			{ "可爱的小孩", "One pease", "一个Q女性", "国宝动物",
				"可爱的小孩", "One pease", "一个Q女性", "国宝动物",
				"可爱的小孩", "One pease", "一个Q女性", "国宝动物",
				"可爱的小孩", "One pease", "一个Q女性", "国宝动物",
				"可爱的小孩", "One pease", "一个Q女性", "国宝动物",
				"可爱的小孩", "One pease", "一个Q女性", "国宝动物",
			};
	//这是三张图片的id的集合
	private int[] imageIds = new int[]
			{ R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
					R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
					R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
					R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
					R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
					R.drawable.dingdangmao , R.drawable.haizeiwang, R.drawable.yingtaoxiaowanzi, R.drawable.xiongmao,
			};

	//private int[] GridviewIds = new int[ITEMNUM];

	ImageView imgV[] = new ImageView[ITEMNUM];
	TextView t1[]  = new TextView[ITEMNUM];
	TextView t2[]  = new TextView[ITEMNUM];
	//GridView gV[]  = new GridView[ITEMNUM];
	Button bgoleft[] = new Button[ITEMNUM];
	Button bgoright[] = new Button[ITEMNUM];
	Button blastlession[] = new Button[ITEMNUM];
	Button bnewlession[] = new Button[ITEMNUM];

	// wg add
	private GridView[] GridviewIds = new GridView[ITEMNUM];
	private AppsAdapter[] GridAdapters = new AppsAdapter[ITEMNUM];
	private View[] GridButtons = new View[ITEMNUM * GrpSize]; // friendgridviewbutton


	private ListView messagelistview;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
		for(int i = 0; i < ITEMNUM; i++)
		{
//	        	GridviewIds[i] = (GridView)layoutInflater.inflate(R.id.gridView3, null); //new GridView(mCtx);
//	        	//GridviewIds[i] = R.id.gridView1;
//
//	        	GridAdapters[i] = new AppsAdapter();
//	        	GridAdapters[i].SetGridView(GridviewIds[i]);
//	        	GridAdapters[i].SetGrpId(i);
//
//	        	GridviewIds[i].setAdapter(GridAdapters[i]);
//
//	        	GridviewIds[i].setColumnWidth(50);
//
//	        	GridviewIds[i].setOnItemClickListener(new OnItemClickListener() {
//	    			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//	    				int z = 0;
//	    			}
//	        	});
//
//
//	        	for(int j = 0; j < GrpSize; j++)
//	        	{
//	        		int p = i * GrpSize + j;
//		        	GridButtons[p] = layoutInflater.inflate(R.layout.friendgridviewbutton, null);
//		        	//GridviewIds[i].addView(GridButtons[p]);
//	        	}
		}

		View view = inflater.inflate(R.layout.content_message, container, false);

		ArrayList<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < names.length; i++) {
			Map<String, Object> listem = new HashMap<String, Object>();
			listem.put("head", imageIds[i]);
			listem.put("name", names[i]);
			listem.put("desc", descs[i]);
			//listem.put("gridview", GridviewIds[i]);
			listems.add(listem);
		}

	        /*SimpleAdapter的参数说明
	         * 第一个参数 表示访问整个android应用程序接口，基本上所有的组件都需要
	         * 第二个参数表示生成一个Map(String ,Object)列表选项
	         * 第三个参数表示界面布局的id  表示该文件作为列表项的组件
	         * 第四个参数表示该Map对象的哪些key对应value来生成列表项
	         * 第五个参数表示来填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
	         * 注意的是map对象可以key可以找不到 但组件的必须要有资源填充  因为 找不到key也会返回null 其实就相当于给了一个null资源
	         * 下面的程序中如果 new String[] { "name", "head", "desc","name" } new int[] {R.id.name,R.id.head,R.id.desc,R.id.head}
	         * 这个head的组件会被name资源覆盖
	         * */
//	        SimpleAdapter simplead = new SimpleAdapter(mCtx, listems, //getContext()
//	                R.layout.messagelistview, new String[] { "name", "head", "desc", "gridview" },
//	                new int[] {R.id.name,R.id.head,R.id.desc}); //, R.id.gridView2});
//
//	        messagelistview = (ListView)view.findViewById(R.id.messageListView); // messagelistview
//	        messagelistview.setAdapter(simplead);
//	        messagelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//	                                    long arg3) {
//	                //通过单击事件，获得单击选项的内容
//	                String text = messagelistview.getItemAtPosition(arg2) + "";
//	                //通过Toast对象显示出来。
//	                Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
//	            }
//	        });

		ListAdapter la = new ListAdapter();
		messagelistview = (ListView)view.findViewById(R.id.mystudentlistview); //messageListView); // new ListView(getActivity()); //
		messagelistview.setAdapter(la);

		return view;
	}

	//int pos = -1;
	public class ListAdapter extends BaseAdapter {

		int selectItem = -1;

		int mGrpId = -1;

		GridView mGv;

		private LayoutInflater inflater;

		View vholder[] = new View[names.length];

		public ListAdapter() {

			mCtx = getActivity();

			inflater = LayoutInflater.from(mCtx);
		}

//	        public void SetGridView(GridView gv)
//	        {
//	        	mGv = gv;
//	        }
//	        public void SetGrpId(int GrpId)
//	        {
//	        	mGrpId = GrpId;
//	        }


		public View getView(final int position, View convertView, ViewGroup parent) {
			//PicView i;

			//pos = position;

			ViewHolder holder = null;

			//ViewHolder hoder = null;
			//if (true) //convertView == null)
			if (convertView == null)
			{

				convertView = inflater.inflate(R.layout.messagelistview, null);

				//Log.e(TAG, "GetView null, position: " + position + ", convertView: " + convertView);


				imgV[position] = (ImageView)convertView.findViewById(R.id.head);
				t1[position] = (TextView)convertView.findViewById(R.id.name);
				t2[position] = (TextView)convertView.findViewById(R.id.desc);
				//gV[position] = (GridView)convertView.findViewById(R.id.gridView2);
				imgV[position].setImageDrawable(getResources().getDrawable(imageIds[position])); //R.drawable.right)

				holder = new ViewHolder();
				holder.view = (ImageView)convertView.findViewById(R.id.head);
				holder.t1 = (TextView)convertView.findViewById(R.id.name);
				holder.t2 = (TextView)convertView.findViewById(R.id.desc);
				holder.b1 = (Button)convertView.findViewById(R.id.buttongoleft);
				holder.b2 = (Button)convertView.findViewById(R.id.buttonlastlession);
				holder.b3 = (Button)convertView.findViewById(R.id.buttonnewlession);
				holder.b4 = (Button)convertView.findViewById(R.id.buttongoright);
				convertView.setTag(holder);


//	        		LessionAdapter leA = new LessionAdapter();
//	    	        gV[position].setAdapter(leA);

				bgoleft[position] = (Button)convertView.findViewById(R.id.buttongoleft);
				bgoright[position] = (Button)convertView.findViewById(R.id.buttongoright);
				blastlession[position] = (Button)convertView.findViewById(R.id.buttonlastlession);
				bnewlession[position] = (Button)convertView.findViewById(R.id.buttonnewlession);
				bgoleft[position].setOnClickListener(new Button.OnClickListener(){
					@Override
					public void onClick(View arg0) {

						String text = "L " + arg0.toString(); //this.toString();
						//String text = String.valueOf(pos);
						//通过Toast对象显示出来。
						Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
					}
				});
				bgoright[position].setOnClickListener(new Button.OnClickListener(){
					@Override
					public void onClick(View arg0) {

						String text = "R " + arg0.toString(); //this.toString();
						//String text = String.valueOf(pos);
						//通过Toast对象显示出来。
						Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
					}
				});

				//t1[position].setText(getResources().getText(names[position]));
				t1[position].setText(names[position]);
				t2[position].setText(descs[position]);

//	        		t1[position].setOnClickListener(new Button.OnClickListener(){
//	                    @Override
//	                    public void onClick(View arg0) {
//
//	                    	String text = arg0.toString(); //this.toString();
//	    	                //通过Toast对象显示出来。
//	    	                Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
//	                    }
//	                });
//	        		messagelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//	     	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//	     	                                    long arg3) {
//	     	                //通过单击事件，获得单击选项的内容
//	     	                //String text = messagelistview.getItemAtPosition(arg2) + "";
//
////	     	            	Toast.makeText(mCtx, String.valueOf(arg2), Toast.LENGTH_SHORT).show();
//
//	     	            	if (t1[arg2] != null)
//	     	            	{
//		     	            	String text = t1[arg2].getText().toString();
//
//		     	                //通过Toast对象显示出来。
//		     	                Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
//	     	            	}
//	     	            	else
//	     	            	{
//	     	            		Toast.makeText(mCtx, String.valueOf(arg2), Toast.LENGTH_SHORT).show();
//	     	            	}
//	     	            }
//	     	        });

//	        		convertView.setOnClickListener(new Button.OnClickListener(){
//	                    @Override
//	                    public void onClick(View arg0) {
//
//	                    	String text = arg0.toString(); //this.toString();
//	                    	//String text = String.valueOf(pos);
//	    	                //通过Toast对象显示出来。
//	    	                Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
//	                    }
//	                });
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();

//	        		holder.view.setImageDrawable(getResources().getDrawable(imageIds[position]));
//		            holder.t1.setText(t1[position].getText()); //(String)data.get(position).get("title"));
//		            holder.t2.setText(t2[position].getText()); //(String)data.get(position).get("info"));
				holder.view.setImageDrawable(getResources().getDrawable(imageIds[position]));
				holder.t1.setText(names[position]);
				holder.t2.setText(descs[position]);

				//Log.e(TAG, "GetView OK  , position: " + position + ", convertView: " + convertView);
			}

			holder.view.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					//String text = arg0.toString(); //this.toString();
					String text = "person " + String.valueOf(position);
					//通过Toast对象显示出来。
					Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
				}
			});

			holder.b1.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					//String text = arg0.toString(); //this.toString();
					String text = "b1 " + String.valueOf(position);
					//通过Toast对象显示出来。
					Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
				}
			});
			holder.b2.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					//String text = arg0.toString(); //this.toString();
					String text = "b2 " + String.valueOf(position);
					//通过Toast对象显示出来。
					Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
				}
			});
			holder.b3.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					//String text = arg0.toString(); //this.toString();
					String text = "b3 " + String.valueOf(position);
					//通过Toast对象显示出来。
					Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
				}
			});
			holder.b4.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View arg0) {

					//String text = arg0.toString(); //this.toString();
					String text = "b4 " + String.valueOf(position);
					//通过Toast对象显示出来。
					Toast.makeText(mCtx, text, Toast.LENGTH_SHORT).show(); // getContext()
				}
			});

			//return convertView;
			//if (vholder[position] != null)
			//{
			//	return vholder[position];
			//}

			return convertView;
		}

		public void setSelection(int position)
		{
			selectItem = position;
		}

		public final int getCount() {
			int c = names.length;
			return c; // PicViewCount; // mApps.size();
		}

		public final Object getItem(int position) {
			return position; //mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}


	}

	public static class ViewHolder
	{
		public ImageView view;
		public TextView t1;
		public TextView t2;
		public Button b1;
		public Button b2;
		public Button b3;
		public Button b4;
	}

	// gridview adapter
	public class LessionAdapter extends BaseAdapter {

		int selectItem = -1;


		private LayoutInflater inflater;

		public LessionAdapter() {

			mCtx = getActivity();

			inflater = LayoutInflater.from(mCtx);
		}

		//	        public void SetGridView(GridView gv)
//	        {
//	        	mGv = gv;
//	        }
//	        public void SetGrpId(int GrpId)
//	        {
//	        	mGrpId = GrpId;
//	        }
		public View getView(int position, View convertView, ViewGroup parent) {
			//PicView i;

			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.friendgridviewbutton, null);
			}

			return convertView;

//	            return  GridButtons[mGrpId * GrpSize + position];
		}

		public void setSelection(int position)
		{
			selectItem = position;
		}

		public final int getCount() {
			return 3; // PicViewCount; // mApps.size();
		}

		public final Object getItem(int position) {
			return position; //mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

}
