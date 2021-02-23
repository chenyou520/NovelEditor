package com.chenyou.noveleditor.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * 图片处理
 */
public class BitmapUtils {
    public static Bitmap circleBitmap(Bitmap source){
        //获取Bitmap的宽度
        int witdth = source.getWidth();
        //以Bitmap的宽度值作为新的bitmap的宽高值。
        Bitmap bitmap = Bitmap.createBitmap(witdth, witdth, Bitmap.Config.ARGB_8888);
        //以此bitmap为基准，创建一个画布
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //在画布上画一个长方形
        canvas.drawRect(0,0,80,90,paint);

        //设置图片相交情况下的处理方式
        //setXfermode：设置当绘制的图像出现相交情况时候的处理方式的,它包含的常用模式有：
        //PorterDuff.Mode.SRC_IN 取两层图像交集部分,只显示上层图像
        //PorterDuff.Mode.DST_IN 取两层图像交集部分,只显示下层图像
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //在画布上绘制bitmap
        canvas.drawBitmap(source,0,0,paint);
        return bitmap;
    }
    //实现图片的压缩处理
    //设置宽高必须使用浮点型，否则导致压缩的比例：0
    public static Bitmap zoom(Bitmap soucre,float width,float height){
        Matrix matrix = new Matrix();
        matrix.postScale(width/soucre.getWidth(),height/soucre.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(soucre, 0, 0, soucre.getWidth(), soucre.getHeight(), matrix, false);
        return bitmap;
    }
}
