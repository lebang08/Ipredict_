package com.woyuce.activity.Adapter;

import java.util.List;

import com.woyuce.activity.Bean.SpeakingSearch;
import com.woyuce.activity.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SpeakingSearchAdapter extends BaseAdapter{

	private List<SpeakingSearch> mList;
	private LayoutInflater mInflater;
	
	public SpeakingSearchAdapter(Context context, List<SpeakingSearch> data) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listitem_speakingsearch, null);
			viewHolder.txtSearch = (TextView) convertView.findViewById(R.id.text_item_search);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.txtSearch.setText(mList.get(position).subname);
		return convertView;
	}

	class ViewHolder{
		public TextView txtSearch;
	}
}