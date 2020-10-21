package com.chenyou.noveleditor.pager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.chenyou.noveleditor.R;

/**
 * 用于设置字体大小
 */
public class SliderFont extends View {
    private final Context context;//上下文
    private SharedPreferences shared = null;
    private Drawable mThumb;//图标
    private Paint progressPaint;
    private Paint mThumbPaint;

    private int mWidth;
    private int mHeight;
    private int mScreenWidth;
    private int mSpec = 0;
    private int mOffsetLeft;//左内边距
    private int mSliderWidth;//图标活动范围=宽度-左和右内边距
    private int mIndex;//图标所在位置1-7
    private int mCenterX;//图标所在X轴的中心位置
    private int mCenterY;//图标所在Y轴的中心位置


    //字体大小
    private float[] fontSize = new float[]{
            12, 14, 16, 19, 22, 24, 26
    };


    public SliderFont(Context context) {
        this(context, null);
    }

    public SliderFont(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderFont(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        shared = context.getSharedPreferences("setdata", Context.MODE_PRIVATE);
        init();
    }

    private void init() {
        mThumb = context.getResources().getDrawable(R.drawable.icon_slip_circle);//移动图标
        mThumbPaint = new Paint();
        progressPaint = new Paint();
        //初始化字体所在的位置
        mIndex = shared.getInt("mFontIndex",0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = 500;
        } else {
            mWidth = width;
        }

        //MeasureSpec.AT_MOST->最大尺寸，控件的宽高为WRAP_CONTENT，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸
        if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = 50;
        } else {
            mHeight = height;
        }

        //重绘视图，自定义视图大小
        setMeasuredDimension(mWidth, mHeight);
    }

    public void setScreenWidth(int screenWidth) {
        //一定要在它画之前调用，width最好是它父类的宽度
        mScreenWidth = screenWidth - 80;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        progressPaint.setColor(context.getResources().getColor(R.color.divide_color));
        progressPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 7; i++) {
            //画竖着的线
            canvas.drawRect(mSpec * i + mOffsetLeft, (mHeight - 30) / 2, mSpec * i + 3f + mOffsetLeft, (mHeight - 30) / 2 + 30, progressPaint);
            if (i != 6) {
                //画横着的线
                canvas.drawRect(mSpec * i + mOffsetLeft, (mHeight - 30) / 2 + 15, mSpec * (i + 1) + mOffsetLeft, (mHeight - 30) / 2 + 18f, progressPaint);
            }
        }
        //画thumb
        mThumb.setBounds(mCenterX - mHeight / 2 + 5, mCenterY - mHeight / 2 + 5, mCenterX + mHeight / 2 - 5, mCenterY + mHeight / 2 - 5);
        mThumb.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //得到Slider的长度
        mSliderWidth = mWidth - getPaddingLeft() - getPaddingRight();
        //得到偏移量
        mOffsetLeft = getPaddingLeft();
        mSpec = mSliderWidth / 6;//将图标活动范围分成6份
        mCenterX = mSpec * mIndex + mOffsetLeft;//指定图标X轴中心位置
        mCenterY = (mHeight - 30) / 2 + 15;//指定图标Y轴中心位置
    }

    /**
     * 设置图标中心
     *
     * @param center
     */
    public void setCenter(float center) {
        //当手指不滑动的时候，设置thumb的中心点的x坐标
        center = center - (mScreenWidth - mWidth) / 2;
        mCenterX = (int) center;
    }

    /**
     * 调整图标中心
     *
     * @param local
     * @return
     */
    public int adJustCenter(float local) {
        //调整thumb所在的位置，
        //让thumb永久在点上
        local = local - (mScreenWidth - mWidth) / 2;
        mIndex = (int) local / mSpec;
        //限制最小字体序号
        if (mIndex <= 0) {
            mIndex = 0;
        }
        //限制最大字体序号
        if (mIndex >= 6) {
            mIndex = 6;
        }
        //图标X轴中心位置
        mCenterX = mSpec * mIndex + mOffsetLeft;
        invalidate();//重绘视图

        return mIndex;
    }

    /**
     * 获取字体大小
     *
     * @param index
     * @return
     */
    public float getFontSize(int index) {
        return fontSize[index];
    }

    /**
     * 移动图标
     *
     * @param spec
     */
    public void move(float spec) {
        //当手势为MOVE时，改变thumb的位置
        mCenterX += spec;
        //限制图标左边活动范围
        if (mCenterX <= mOffsetLeft) {
            mCenterX = mOffsetLeft;
        }

        //限制图标右边活动范围
        if (mCenterX >= mWidth - mOffsetLeft) {
            mCenterX = mWidth - mOffsetLeft;
        }
    }

}
