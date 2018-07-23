package com.example.tongxiwen.photopickerfix.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class UsefulTransformation extends BitmapTransformation {

    public static final int OPTION_BLUE = 0x00;
    public static final int OPTION_ROUND = 0x01;

    private static float roundRadius = 0f;
    private static float blueRadius = 0f;
    private Context mContext;
    private int option;

    public UsefulTransformation(Context context, int option) {
        mContext = context;
        roundRadius = Resources.getSystem().getDisplayMetrics().density * 4;
        blueRadius = 15.f;
        this.option = option;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        switch (option) {
            case OPTION_ROUND:
                return roundCrop(pool, toTransform);
            case OPTION_BLUE:
                return blueBitmap(pool, toTransform);
            default:
                return null;
        }
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }

    /**
     * 设置圆角角度(圆角图)
     * @param dp 角度dip
     */
    public void setRoundRadius(int dp){
        if (option == OPTION_ROUND)
            roundRadius = Resources.getSystem().getDisplayMetrics().density * dp;
    }

    /**
     * 设置模糊程度
     * @param radius 模糊
     */
    public void setBlueRadius(float radius){
        if (option == OPTION_BLUE)
            blueRadius = radius;
        if (blueRadius > 25.0f)
            blueRadius = 25.f;
        if (blueRadius < 0)
            blueRadius = 0;
    }

//    ********↓私有方法

    /**
     * 圆角
     */
    private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;
        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        //TODO 搞懂这一句
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rectF, roundRadius, roundRadius, paint);
        return result;
    }

    /**
     * 高斯模糊
     */
    private Bitmap blueBitmap(BitmapPool pool, Bitmap bitmap) {
        if (bitmap == null) return null;
        Bitmap outBitmap = pool.get(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        }
        //用需要创建高斯模糊bitmap创建一个空的bitmap
        // 初始化Renderscript，该类提供了RenderScript context，创建其他RS类之前必须先创建这个类，其控制RenderScript的初始化，资源管理及释放
        RenderScript rs = RenderScript.create(mContext);
        // 创建高斯模糊对象
        //TODO 搞懂这一句
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // 创建Allocations，此类是将数据传递给RenderScript内核的主要方法，并制定一个后备类型存储给定类型
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        //设定模糊度(注：Radius最大只能设置25.f)
        blurScript.setRadius(blueRadius);
        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);
        // recycle the original bitmap
        // bitmap.recycle();
        // After finishing everything, we destroy the Renderscript.
        rs.destroy();
        return outBitmap;
    }

}
