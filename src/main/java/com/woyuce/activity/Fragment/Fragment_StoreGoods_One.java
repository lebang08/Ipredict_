package com.woyuce.activity.Fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.woyuce.activity.R;

import java.util.ArrayList;

public class Fragment_StoreGoods_One extends Fragment {

    private ViewFlipper mFlipper;

    private TextView mTxtGoodsTitle, mTxtGoodsPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storegoods_one, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mFlipper = (ViewFlipper) view.findViewById(R.id.viewflip_activity_storegoods);

        mTxtGoodsTitle = (TextView) view.findViewById(R.id.txt_activity_storegoods_goodstitle);
        mTxtGoodsPrice = (TextView) view.findViewById(R.id.txt_activity_storegoods_goodsprice);
        setView();
    }

    /**
     * 将上一级获取到的数据设置在View上
     */
    private void setView() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.img_error)
                .showImageOnFail(R.mipmap.img_error).cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        //将获取到的数据设置到View上
        ArrayList<String> mList = getArguments().getStringArrayList("mList");
        mTxtGoodsTitle.setText(getArguments().getString("goods_title"));
        mTxtGoodsPrice.setText(getArguments().getString("sales_price"));

        //查看是否有轮播图
        if (mList.size() == 0) {
            mFlipper.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < mList.size(); i++) {
                ImageView img = new ImageView(getActivity());
                ImageLoader.getInstance().displayImage(mList.get(i), img, options);
                mFlipper.addView(img);
            }
            mFlipper.setInAnimation(getActivity(), R.anim.left_in);
            mFlipper.setOutAnimation(getActivity(), R.anim.left_out);
            mFlipper.startFlipping();
        }
    }
}