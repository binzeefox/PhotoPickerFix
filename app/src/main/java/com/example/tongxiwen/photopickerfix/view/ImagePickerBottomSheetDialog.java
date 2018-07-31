package com.example.tongxiwen.photopickerfix.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.tongxiwen.photopickerfix.R;
import com.example.tongxiwen.photopickerfix.util.FileUtil;

import java.io.File;

public class ImagePickerBottomSheetDialog extends BottomSheetDialog implements View.OnClickListener {

    public static final int REQUEST_CAMERA = 0x00;
    public static final int REQUEST_ALBUM = 0x01;
    public static final int REQUEST_CROP = 0x02;

    /**
     * 需要在manifest内配置fileProvider
     */
    private static final String AUTHORITY
            = "com.example.tongxiwen.photopickerfix.PROVIDER";

    private Activity mContext;
    private String tempPath;
    private File tempFile;
    private Button buttonCamera;
    private Button buttonAlbum;

    private boolean isNougat;   // 是否为7.0

    private boolean isCrop; // 是否进行剪裁

    public static ImagePickerBottomSheetDialog get(Activity activity) {
        return new ImagePickerBottomSheetDialog(activity);
    }

    public static ImagePickerBottomSheetDialog get(Activity activity, boolean isCrop) {
        return new ImagePickerBottomSheetDialog(activity, isCrop);
    }

    private ImagePickerBottomSheetDialog(@NonNull Activity context) {
        super(context);
        mContext = context;
        isNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private ImagePickerBottomSheetDialog(@NonNull Activity context, boolean isCrop) {
        super(context);
        mContext = context;
        this.isCrop = isCrop;
        isNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

//                      ↑构造方法
//                      ↓初始化方法


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
    }

//                       ↓公共方法

    /**
     * 设置是否剪裁
     * @param crop 是否剪裁
     */
    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    /**
     * 结果处理
     * @param requestCode   请求码
     * @param resultCode    结果码
     * @param data  返回数据
     * @return  返回获取的文件
     */
    public File onResult(int requestCode, int resultCode, Intent data) {
        File file;
        if (resultCode != Activity.RESULT_OK)
            return null;
        switch (requestCode) {
            case REQUEST_CAMERA:
                //相机结果
                file = new File(tempPath);
                if (isCrop) {
                    //如果剪裁
                    tempFile = file;
                    openCrop(file);
                    return null;
                } else
                    return file;
            case REQUEST_ALBUM:
                //相册结果，放弃4.4以下情况
                if (data == null)
                    return null;
                Uri rawUri = data.getData();
                file = FileUtil.getImageFileFromUri(mContext, rawUri);
                if (isCrop) {
                    //如果剪裁
                    tempFile = null;
                    openCrop(file);
                    return null;
                } else
                    return file;
            case REQUEST_CROP:
                //剪裁结果
                if (tempFile != null && tempFile.exists())
                    tempFile.delete();
                return new File(tempPath);
            default:
                return null;
        }
    }

//                         ↓内部方法

    /**
     * 开启相机
     */
    private void openCamera() {
        tempPath = null;
        tempPath = FileUtil.getImageTempPath(getContext());
        if (TextUtils.isEmpty(tempPath))
            return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!isNougat) { //非7.0
            intent.putExtra(MediaStore.EXTRA_OUTPUT
                    , Uri.fromFile(new File(tempPath)));
        } else {    //7.0环境
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT
                    , FileProvider.getUriForFile(mContext, AUTHORITY
                            , new File(tempPath)));
        }
        mContext.startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * 开启相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK
                , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(mContext.getPackageManager()) != null)
            mContext.startActivityForResult(intent, REQUEST_ALBUM);
    }

    /**
     * 开启剪裁
     *
     * @param file 原始图片
     */
    private void openCrop(File file) {
        tempPath = FileUtil.getCropTempPath(mContext);
        Uri outUri; //输出路径
        Uri inUri;  //输入路径
        Intent intent = new Intent("com.android.camera.action.CROP");
        //通用设置
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);//自由比例
        intent.putExtra("aspectY", 0);//自由比例
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);  //不要返回Bitmap
        intent.putExtra("noFaceDetection", true);   //取消面部识别
        if (isNougat) {
            //7.0配置
            outUri = FileUtil.getContentUri(mContext, tempPath);
            inUri = FileProvider.getUriForFile(mContext, AUTHORITY, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            outUri = Uri.parse(tempPath);
            inUri = Uri.fromFile(file);
        }
        intent.setDataAndType(inUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        mContext.startActivityForResult(intent, REQUEST_CROP);
    }

    /**
     * 获取视图（节省布局文件，方便移植）
     */
    private View getContentView() {
        float density = Resources.getSystem().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(params);
        container.setPadding(0, (int) density * 16
                , 0, (int) density * 16);
        container.setOrientation(LinearLayout.VERTICAL);

        buttonCamera = new Button(getContext());
        buttonAlbum = new Button(getContext());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonCamera.setText("相机");
        buttonCamera.setBackgroundColor(0xfff);
        buttonCamera.setLayoutParams(buttonParams);
        buttonAlbum.setText("相册");
        buttonAlbum.setBackgroundColor(0xfff);
        buttonAlbum.setLayoutParams(buttonParams);

        buttonCamera.setOnClickListener(this);
        buttonAlbum.setOnClickListener(this);

        container.addView(buttonCamera);
        container.addView(buttonAlbum);
        return container;
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View view) {
        if (view == buttonCamera)
            openCamera();
        else if (view == buttonAlbum)
            openAlbum();
        dismiss();
    }
}
