package com.example.tongxiwen.photopickerfix;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tongxiwen.photopickerfix.base.FoxActivity;
import com.example.tongxiwen.photopickerfix.view.ImagePickerBottomSheetDialog;
import com.example.tongxiwen.photopickerfix.view.UsefulTransformation;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FoxActivity {


    @BindView(R.id.button_test)
    Button buttonTest;
    @BindView(R.id.switch_free)
    Switch switchFree;
    private ImagePickerBottomSheetDialog imageDialog;

    @BindView(R.id.image_field)
    ImageView imageField;

    @Override
    protected int onInflateLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void create(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkNrequestPermission(Manifest.permission.CAMERA
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        imageDialog = ImagePickerBottomSheetDialog.get(this);
        switchFree.setChecked(false);
        switchFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                imageDialog.setCrop(b);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        File file = imageDialog.onResult(requestCode, resultCode, data);

        UsefulTransformation transformation = new UsefulTransformation(this, UsefulTransformation.OPTION_ROUND);
        transformation.setRoundRadius(20);
        RequestOptions options = new RequestOptions()
                .transforms(transformation);

        Glide
                .with(this)
                .load(file)
                .apply(options)
                .into(imageField);
    }

    @Override
    protected void onPermissionResult(List<String> failedList) {
        if (failedList != null)
            new AlertDialog
                    .Builder(this)
                    .setTitle("提示")
                    .setMessage("有至少一个权限尚未被允许，我就不判断是哪个了，总之即将退出")
                    .setCancelable(false)
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    });
    }

    @OnClick(R.id.button_test)
    public void onViewClicked() {
        imageDialog.show();
    }
}
