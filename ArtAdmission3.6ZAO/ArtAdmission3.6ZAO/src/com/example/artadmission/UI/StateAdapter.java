package com.example.artadmission.UI;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.artadmission.R;
import com.example.artadmission.define_var;
import com.example.artadmission.DB.DBHelper;
/***************
 *ImageAdapter为显示联系人信息的控件gridview，配置的适配器
 *ImageAdapter mUserIconAdapter = new ImageAdapter(this);
 *mGridView.setAdapter(mUserIconAdapter);
 **************/
public class StateAdapter extends BaseAdapter {
	private Context mContext;
	private Dialog dialog;
	private EditText scoreText;
	private TextView tipsText;
	//	private Dialog myDialog;
	public StateAdapter(Context c) {
		mContext = c;
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
		View view = View.inflate(mContext, R.layout.gridview_item_state, null);
		RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.relaGrid);
		TextView IdText = (TextView)rl.findViewById(R.id.IdText);
		final TextView scoreText = (TextView)rl.findViewById(R.id.scoreText);
		ImageView itemImage = (ImageView)rl.findViewById(R.id.itemImage);
		IdText.setText(position+1+"");
		if(define_var.std!=null && (Integer.valueOf(define_var.std)==(position+1)))
		{
			rl.setBackgroundResource(R.drawable.testing_shape);
		}
		if(define_var.checked[position]==1&&Math.abs(define_var.score[position]+1)>1e-6)		//打分页面
		{
			if(Math.abs(define_var.score[position]+2)<1e-6)
				scoreText.setText("请打分");			//打的分数
			else
			{
				if((define_var.score[position]*10)%10!=0)	//为了打分的时候，98.0显示为98写的
					scoreText.setText(define_var.score[position]+"");
				else scoreText.setText((int)(define_var.score[position])+"");
				/****把打的分数存到数据库****/
				upDateScore(scoreText.getText().toString(), position+1);
			}	
			itemImage.setVisibility(View.GONE);
		}

		else if(define_var.checked[position]!=0 && define_var.checked[position]!=1)
//			if(define_var.checked[position] == 2 || Math.abs(define_var.score[position]+1)<1e-6)		//缺考页面
		{
//			upDateScore("-1", position+1);
			itemImage.setBackgroundResource(R.drawable.kong3);
		}
//		if(define_var.checked[position] == 3)		//有此人，但还没考
//		{
//			itemImage.setBackgroundResource(R.drawable.default_picture_s);
//		}
		return rl;
	}
	//打分大框页面
	public void showScoring(final int position,final String scoreStr,final GridView mStudentGridView){
		LayoutInflater inflater = LayoutInflater.from(mContext);  
		final View textEntryView = inflater.inflate(R.layout.gridview_item, null);  
		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext); 
		scoreText = (EditText)textEntryView.findViewById(R.id.scoreText);
		tipsText= (TextView)textEntryView.findViewById(R.id.tipsText);
		final TextView textView2= (TextView)textEntryView.findViewById(R.id.textView2);
		textView2.setText((position+1)+"号考生");
		Button cancelBtn = (Button)textEntryView.findViewById(R.id.cancelBtn);
		Button sureBtn = (Button)textEntryView.findViewById(R.id.sureBtn);
		if(scoreStr.equals("请打分"))	scoreText.setText("");
		else scoreText.setText(scoreStr);
		scoreText.setSelection(scoreText.getText().toString().length());//光标位置
		OperatingEditText(mContext);
		builder.setCancelable(false); 
		builder.setView(textEntryView);
		if(dialog!=null)
			dialog.dismiss();
		dialog = new Dialog(mContext, R.style.MyDialog);
		//设置它的ContentView
		dialog.setContentView(textEntryView);
		dialog.show();
		//确定打分
		sureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				scorePage(scoreText,tipsText,mStudentGridView,position);
			}});
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}});
		scoreText.setOnKeyListener(new EditText.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
					scorePage(scoreText,tipsText,mStudentGridView,position);
					return true;
				}
				return false;
			}
		});
		dialog.setCanceledOnTouchOutside(false);//点击页面的其他地方dialog不会消失
	}
	//修改形体条件
	@SuppressWarnings("null")
	public void showShape(final int position,String scoreStr,final GridView mStudentGridView){
		DBHelper helpter = new DBHelper(mContext);
		SQLiteDatabase db= helpter.getWritableDatabase();
		Cursor cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","height","standHeight","armLength","legLength"}, 
				"subject=? and roomNum=? and groupNum=? and stdNum=?", new String[]{define_var.subject,define_var.room,define_var.group,(position+1)+""}, null, null, null); 
		if(cursor.moveToFirst())
		{
			String height = cursor.getString(cursor.getColumnIndex("record.height"));
			String standHeight = cursor.getString(cursor.getColumnIndex("record.standHeight"));
			String armLength = cursor.getString(cursor.getColumnIndex("record.armLength"));
			String legLength = cursor.getString(cursor.getColumnIndex("record.legLength"));
			LayoutInflater inflater = LayoutInflater.from(mContext);  
			final View textEntryView = inflater.inflate(R.layout.gridview_item_dance, null);  
			TextView heightText = (TextView)textEntryView.findViewById(R.id.heightText);
			TextView standHeightText = (TextView)textEntryView.findViewById(R.id.standHeightText);
			TextView armLengthText = (TextView)textEntryView.findViewById(R.id.armLengthText);
			TextView legLengthText = (TextView)textEntryView.findViewById(R.id.legLengthText);
			TextView textView2 = (TextView)textEntryView.findViewById(R.id.textView2);
			scoreText = (EditText)textEntryView.findViewById(R.id.scoreText);
			tipsText= (TextView)textEntryView.findViewById(R.id.tipsText);	
			Button cancelBtn = (Button)textEntryView.findViewById(R.id.cancelBtn);
			Button sureBtn = (Button)textEntryView.findViewById(R.id.sureBtn);
			if(scoreStr.equals("请打分"))	scoreText.setText("");
			else scoreText.setText(scoreStr);
			scoreText.setSelection(scoreText.getText().toString().length());//光标位置
			OperatingEditText(mContext);
			if(height!=null)
				heightText.setText(height+"cm");
			else
				heightText.setText("0cm");
			if(standHeightText!=null)
				standHeightText.setText(standHeight+"cm");
			else
				standHeightText.setText("0cm");
			if(armLengthText!=null)
				armLengthText.setText(armLength+"cm");
			else
				armLengthText.setText("0cm");
			if(legLengthText!=null)
				legLengthText.setText(legLength+"cm");
			else
				legLengthText.setText("0cm");
			if(dialog!=null)
				dialog.dismiss();
			dialog = new Dialog(mContext, R.style.MyDialog);
			dialog.setContentView(textEntryView);
			dialog.show();
			textView2.setText((position+1)+"号考生");
			//确定打分 
			sureBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					scorePage(scoreText,tipsText,mStudentGridView,position);
				}
			});
			//取消打分
			cancelBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dialog.dismiss();
				}
			});
			scoreText.setOnKeyListener(new EditText.OnKeyListener()
			{
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
						scorePage(scoreText,tipsText,mStudentGridView,position);
						return true;
					}
					return false;
				}
			});
			dialog.setCanceledOnTouchOutside(false);//点击页面的其他地方dialog不会消失
		}
	}
	private void scorePage(EditText scoreText,TextView tipsText,GridView mStudentGridView,int position)
	{
		System.out.println("----scorePage");
		String score = scoreText.getText().toString();
		if(score!=null && !(score.equals(""))){
			if(score.startsWith("."))
			{
				score = "0"+score;
			}
			float f = Float.valueOf(score);
			if(f<0 || f>100) 
			{
				tipsText.setText("分数不在范围(0-100)内");
			}
			else if(score.indexOf(".") >-1 && (score.length()-score.indexOf("."))>2)
			{
				tipsText.setText("不能超过一位小数");
			}
			else 
			{
				define_var.score[position] = (Float.valueOf(scoreText.getText().toString()));
				scoreText.setText(scoreText.getText().toString());
				dialog.dismiss();
			}
		}
		else if(score!=null && (score.equals("")))
			tipsText.setText("分数不能为空，返回请按取消");
		((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
	}
	private void OperatingEditText(final Context context) {
		scoreText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s != null && s.length() > 0) {
					String score = s.toString();
					if(score.startsWith("."))
					{
						score = "0"+score;
					}
					if(score!=null && !(score.equals(""))){
						float f = Float.valueOf(score);
						if(f<0 || f>100) 
						{
							tipsText.setText("分数不在范围(0-100)内");
						}
						else if(score.indexOf(".") >-1 && (score.length()-score.indexOf("."))>2)
						{
							tipsText.setText("不能超过一位小数");
							//							scoreText.setText(scoreText.getText().toString().substring(0, scoreText.getText().toString().length()-1));
							//							scoreText.setSelection(scoreText.getText().toString().length());
						}
						else 
							tipsText.setText("");
					}
				}		
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}
		});
	}
	private void upDateScore(String str,int i)
	{
		/****把打的分数存到数据库****/
		DBHelper helpter = new DBHelper(mContext);
		ContentValues values = new ContentValues(); 
		SQLiteDatabase db= helpter.getWritableDatabase();
		values = new ContentValues();		
		try {			
			values.put("score", str);
			db.update("record", values,"subject=? and roomNum=? and groupNum=? and stdNum=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,i+""});					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
