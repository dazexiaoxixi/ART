package com.example.artadmission.UI;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.LinearLayout;
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
public class HistoryAdapter extends BaseAdapter {
	private Context mContext;
	public HistoryAdapter(Context c) {
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
		// TODO Auto-generated method stub    R.id.idText, R.id.studentNumText, R.id.scoreText}
		View view = View.inflate(mContext, R.layout.history_list_item, null);
		LinearLayout rl = (LinearLayout)view.findViewById(R.id.ha);
		TextView idText = (TextView)rl.findViewById(R.id.idText);
		final TextView studentNumText = (TextView)rl.findViewById(R.id.studentNumText);
		TextView scoreText = (TextView)rl.findViewById(R.id.scoreText);
		idText.setText(position+1+"");
		
		return rl;
	}
}
