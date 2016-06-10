package com.example.imagehandle;

import java.util.List;

import com.example.artcheckin.FrameFormat;
import com.example.artcheckin.R;
import com.example.imagehandle.AsyncImageLoader.ImageCallback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
	private Handler handler = null;
	private Context mContext =null;
	public GridViewAdapter(Context mContext,Handler handler) {
		this.mContext = mContext;
		this.handler = handler;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 10;
	}
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = View.inflate(mContext, R.layout.gridview_item, null);
		LinearLayout rl = (LinearLayout)view.findViewById(R.id.relaGrid);
//		view.setClickable(false);
//		System.out.println("------getView 不可点击"+(position+1)+" gridview item");
		TextView IdText = (TextView)rl.findViewById(R.id.stdNum);
		ImageView itemImage = (ImageView)rl.findViewById(R.id.itemImage);
//		CheckBox isAbsent = (CheckBox)rl.findViewById(R.id.absentCheckBox);
//		isAbsent.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				Message msg = new Message();
//				msg.obj = ""+(position+1);
//				if (((CheckBox) v).isChecked()) {
//					msg.what = FrameFormat.studentAbsentHandle;
//					System.out.println("---选中缺考");
//				} else {
//					msg.what = FrameFormat.studentNormalHandle;
//					System.out.println("---取消选中缺考");
//				}
//				handler.sendMessage(msg);
//			}
//		});
//		isAbsent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//			@Override
//			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//				// TODO Auto-generated method stub
//				Message msg = new Message();
//				msg.obj = ""+(position+1);
//				if(isChecked){
//					msg.what = FrameFormat.studentAbsentHandle;
//					System.out.println("---选中缺考");
//				}else{
//					msg.what = FrameFormat.studentNormalHandle;
//					System.out.println("---取消选中缺考");
//				}
//				handler.sendMessage(msg);
//			}
//        });
		return rl;
	}
	
}
