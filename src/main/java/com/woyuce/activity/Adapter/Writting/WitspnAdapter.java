package com.woyuce.activity.Adapter.Writting;

import java.util.List;

import com.woyuce.activity.Model.Writting.WitCategory;
import com.woyuce.activity.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WitspnAdapter extends BaseAdapter {

	private List<WitCategory> mList;
	private LayoutInflater mInflater;

	public WitspnAdapter(Context context, List<WitCategory> data) {
		mList = data;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder;
		if(convertView == null){
			viewholder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.gvitem_writting_spinner, null);
			viewholder.categoryname = (TextView) convertView.findViewById(R.id.txt_item_witspinner);
			convertView.setTag(viewholder);
		}else{
			viewholder = (ViewHolder) convertView.getTag();
		}
		viewholder.categoryname.setText(mList.get(position).name);	
		return convertView;
	}

	class ViewHolder {
		public TextView categoryname;
	}
}