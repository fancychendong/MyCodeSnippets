package com.xiao.dong.mylibrary.view.richtextview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.xiao.dong.mylibrary.R;

import java.lang.ref.WeakReference;

/**
 * Created by chenxiaodong on 16/7/20.
 */
public class ImageSpanTextView {

    //just for test
    private static String[] testStr = {"测试组合课程然后又是企业指派", "测试组合课程然后又是企业指派测试组合课程然后又是企业指派", "测试组合课程然后又", "测试组合课程然后又是企业", "daffad", "测试组合课程然后又是企业测试组", "测试组合课程测试组合课程然后又是企业"};
    private static int count = 0;

    private static SpannableString getSpannableString(TextView textView, String title, int widthOfLine, int maxLines, int imageRes) {
        title = testStr[count%testStr.length];
        count ++;
        final String blank = "  ";
        title += blank;
//        int textWidth = PlatformUtil.getDisplayWidth(widget.getContext()) - Util.dip2px(widget.getContext(), 150);
        int textWidth = widthOfLine;
        int maxTextWidth = textWidth * maxLines;

        SpannableString spannableTitle = new SpannableString(title);
        final String placeIcon = "[icon]";
//        if (count%2 > 0) {
        Drawable drawable = null;
        CustomMyCourseCardImageSpan span = null;
//            if (displayData.compositeType == CourseConst.COMPOSITE_TYPE_MICRO) {
        drawable = textView.getContext().getResources().getDrawable(imageRes);
//            } else if (displayData.compositeType == CourseConst.COMPOSITE_TYPE_SERIES) {
//                drawable = widget.getContext().getResources().getDrawable(R.drawable.ic_mycourse_series);
//            } else {
//                return spannableTitle;
//            }
        maxTextWidth = maxTextWidth - drawable.getIntrinsicWidth();

        Drawable assignDrawable = null;
//            if (count%2 > 0) {
        assignDrawable = textView.getContext().getResources().getDrawable(imageRes);
        int blankWidth = (int) Layout.getDesiredWidth(blank, 0, blank.length(), textView.getPaint());
        maxTextWidth = maxTextWidth - assignDrawable.getIntrinsicWidth() - blankWidth;
        title = getEllipsizeStr(textView, title, maxTextWidth);
        spannableTitle = new SpannableString(title + placeIcon + blank + placeIcon);
//            } else {
//                displayData.title = getEllipsizeStr(widget, displayData.title, maxTextWidth);
//                spannableTitle = new SpannableString(displayData.title + placeIcon);
//            }

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        span = new CustomMyCourseCardImageSpan(drawable, CustomMyCourseCardImageSpan.ALIGN_BASELINE);
        spannableTitle.setSpan(span, title.length(), title.length() + placeIcon.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//            if (count%2 > 0) {
        assignDrawable.setBounds(0, 0, assignDrawable.getIntrinsicWidth(), assignDrawable.getIntrinsicHeight());
        CustomMyCourseCardImageSpan secondSpan = new CustomMyCourseCardImageSpan(assignDrawable, CustomMyCourseCardImageSpan.ALIGN_BASELINE);
        spannableTitle.setSpan(secondSpan, title.length() + placeIcon.length() + blank.length(), title.length() + placeIcon.length() * 2 + blank.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        } else if (displayData.isEnterpriseAssign) {
//            displayData.title = getEllipsizeStr(widget, displayData.title, maxTextWidth);
//
//            Drawable drawable = widget.getContext().getResources().getDrawable(R.drawable.ic_mycourse_enterprise_assign);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), Util.dip2px(widget.getContext(), 14) + 0);
//            CustomMyCourseCardImageSpan span = new CustomMyCourseCardImageSpan(drawable, CustomMyCourseCardImageSpan.ALIGN_BASELINE);
//            spannableTitle = new SpannableString(displayData.title + placeIcon);
//            spannableTitle.setSpan(span, displayData.title.length(), displayData.title.length() + placeIcon.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }

        return spannableTitle;
    }

    private static String getEllipsizeStr(TextView widget, String origin, int maxTextWidth) {
        if (widget.getPaint() != null) {

            // 根据长度截取出剪裁后的文字,如果字数不够,则返回未阶段字数
            final String ellipsizeStr = (String) TextUtils.ellipsize(origin, widget.getPaint(), maxTextWidth, TextUtils.TruncateAt.END);
            return ellipsizeStr;
        }
        return "";
    }

    /**自定义ImageSpan,重写draw方法,实现我的学习卡片上tag标签显示问题*/
    private static class CustomMyCourseCardImageSpan extends ImageSpan {

        public CustomMyCourseCardImageSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getCachedDrawable();
            if (b != null) {
                canvas.save();
                int transY = bottom - b.getBounds().bottom;
                //这种方法计算错了,在网上看到的,被人误导了
//                if (mVerticalAlignment == ALIGN_BASELINE) {
//                    if (x > 0) {    //当tag单行显示时,tag标签太居上问题,原因是textview默认有行间距
//                        transY -= paint.getFontMetricsInt().descent;
//                    } else if (x == 0) {
//                        transY = (int) (Math.ceil(paint.getFontMetrics().descent - paint.getFontMetrics().top) + 4);
//                    }
//                }
                //这种方法可以完美解决居中对齐文字的问题
                transY = ((bottom-top) - b.getBounds().bottom)/2+top;
                //抗锯齿
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                canvas.translate(x, transY);
                b.draw(canvas);
                canvas.restore();
            } else {
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            }
        }

        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = mDrawableRef;
            Drawable d = null;

            if (wr != null)
                d = wr.get();

            if (d == null) {
                d = getDrawable();
                mDrawableRef = new WeakReference<Drawable>(d);
            }

            return d;
        }

        private WeakReference<Drawable> mDrawableRef;
    }
}
