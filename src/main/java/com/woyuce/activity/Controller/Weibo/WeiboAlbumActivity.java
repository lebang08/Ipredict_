//package com.woyuce.activity.Controller.Weibo;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.graphics.Bitmap;
//import android.media.ExifInterface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.provider.MediaStore;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.assist.ImageSize;
//import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
//import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
//import com.woyuce.activity.AppContext;
//import com.woyuce.activity.R;
//import com.woyuce.activity.Utils.ImageUtils;
//import com.woyuce.activity.Utils.LocalImageHelper;
//import com.woyuce.activity.Utils.StringUtils;
//import com.woyuce.activity.Common.Constants;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author LeBang
// * @Decription:本地相册
// * @date 2016-9-30
// */
//public class WeiboAlbumActivity extends WeiboBaseActivity {
//    ListView listView;
//    ImageView progress;
//    LocalImageHelper helper;
//    View camera;
//    List<String> folderNames;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_weibo_album);
////        //TODO 清除已有照片，会有冲突
////        LocalImageHelper.getInstance().clear();
//
//        listView = (ListView) findViewById(R.id.local_album_list);
//        camera = findViewById(R.id.loacal_album_camera);
//        camera.setOnClickListener(onClickListener);
//        camera.setVisibility(View.GONE);
//        progress = (ImageView) findViewById(R.id.progress_bar);
//        helper = LocalImageHelper.getInstance();
//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_loading);
//        progress.startAnimation(animation);
//        findViewById(R.id.album_back).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //开启线程初始化本地图片列表，该方法是synchronized的，因此当AppContent在初始化时，此处阻塞
//                LocalImageHelper.getInstance().initImage();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //初始化完毕后，显示文件夹列表
//                        if (!isDestroy) {
//                            initAdapter();
//                            progress.clearAnimation();
//                            ((View) progress.getParent()).setVisibility(View.GONE);
//                            listView.setVisibility(View.VISIBLE);
//                            camera.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//            }
//        }).start();
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(WeiboAlbumActivity.this, WeiboAlbumDetailActivity.class);
//                intent.putExtra("local_folder_name", folderNames.get(i));
//                intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
//                startActivity(intent);
//            }
//        });
//    }
//
//    public void initAdapter() {
//        listView.setAdapter(new FolderAdapter(this, helper.getFolderMap()));
//    }
//
//    View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (LocalImageHelper.getInstance().getCurrentSize() + LocalImageHelper.getInstance().getCheckedItems().size() >= 9) {
//                Toast.makeText(WeiboAlbumActivity.this, "最多选择9张图片", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            //  调用相机也是需要6.0权限的
//            if (hasPermission(Manifest.permission.CAMERA)) {
//                doCamera();
//            } else {
//                requestPermission(Constants.CODE_CAMERE, Manifest.permission.CAMERA);
//            }
//        }
//    };
//
//    //重写调用相机的逻辑
//    @Override
//    public void doCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        //  拍照后保存图片的绝对路径
//        String cameraPath = LocalImageHelper.getInstance().setCameraImgPath();
//        File file = new File(cameraPath);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//        startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
//                    String cameraPath = LocalImageHelper.getInstance().getCameraImgPath();
//                    if (StringUtils.isEmpty(cameraPath)) {
//                        Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    File file = new File(cameraPath);
//                    if (file.exists()) {
//                        Uri uri = Uri.fromFile(file);
//                        LocalImageHelper.LocalFile localFile = new LocalImageHelper.LocalFile();
//                        localFile.setThumbnailUri(uri.toString());
//                        localFile.setOriginalUri(uri.toString());
//                        localFile.setOrientation(getBitmapDegree(cameraPath));
//                        LocalImageHelper.getInstance().getCheckedItems().add(localFile);
//                        LocalImageHelper.getInstance().setResultOk(true);
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
//                        //这里本来有个弹出progressDialog的，在拍照结束后关闭，但是要延迟1秒，原因是由于三星手机的相机会强制切换到横屏，
//                        //此处必须等它切回竖屏了才能结束，否则会有异常
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                finish();
//                            }
//                        }, 1000);
//                    } else {
//                        Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
//
//    /**
//     * 读取图片的旋转的角度，还是三星的问题，需要根据图片的旋转角度正确显示
//     *
//     * @param path 图片绝对路径
//     * @return 图片的旋转角度
//     */
//    private int getBitmapDegree(String path) {
//        int degree = 0;
//        try {
//            // 从指定路径下读取图片，并获取其EXIF信息
//            ExifInterface exifInterface = new ExifInterface(path);
//            // 获取图片的旋转信息
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return degree;
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
//    public class FolderAdapter extends BaseAdapter {
//        Map<String, List<LocalImageHelper.LocalFile>> folders;
//        Context context;
//        DisplayImageOptions options;
//
//        FolderAdapter(Context context, Map<String, List<LocalImageHelper.LocalFile>> folders) {
//            this.folders = folders;
//            this.context = context;
//            folderNames = new ArrayList<>();
//
//            options = new DisplayImageOptions.Builder()
//                    .cacheInMemory(true)
//                    .cacheOnDisk(false)
//                    .showImageForEmptyUri(R.mipmap.img_error)
//                    .showImageOnFail(R.mipmap.img_error)
//                    .showImageOnLoading(R.mipmap.img_error)
//                    .bitmapConfig(Bitmap.Config.RGB_565)
//                    .setImageSize(new ImageSize(((AppContext) context.getApplicationContext()).getQuarterWidth(), 0))
//                    .displayer(new SimpleBitmapDisplayer()).build();
//
//            Iterator iter = folders.entrySet().iterator();
//            while (iter.hasNext()) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                String key = (String) entry.getKey();
//                folderNames.add(key);
//            }
//            //根据文件夹内的图片数量降序显示
//            Collections.sort(folderNames, new Comparator<String>() {
//                public int compare(String arg0, String arg1) {
//                    Integer num1 = helper.getFolder(arg0).size();
//                    Integer num2 = helper.getFolder(arg1).size();
//                    return num2.compareTo(num1);
//                }
//            });
//        }
//
//        @Override
//        public int getCount() {
//            return folders.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int i, View convertView, ViewGroup viewGroup) {
//            ViewHolder viewHolder;
//            if (convertView == null || convertView.getTag() == null) {
//                viewHolder = new ViewHolder();
//                convertView = LayoutInflater.from(context).inflate(R.layout.listitem_weibo_album_foler, null);
//                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
//                viewHolder.textView = (TextView) convertView.findViewById(R.id.textview);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            String name = folderNames.get(i);
//            List<LocalImageHelper.LocalFile> files = folders.get(name);
//            viewHolder.textView.setText(name + "(" + files.size() + ")");
//            if (files.size() > 0) {
//                ImageLoader.getInstance().displayImage(files.get(0).getThumbnailUri(), new ImageViewAware(viewHolder.imageView), options,
//                        null, null, files.get(0).getOrientation());
//            }
//            return convertView;
//        }
//
//        private class ViewHolder {
//            ImageView imageView;
//            TextView textView;
//        }
//    }
//}
