package com.xiao.dong.mylibrary.view.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.xiao.dong.mylibrary.R;

/**
 * Created by chenxiaodong on 16/8/26.
 * 继承自FlowLayout, 提供最大行数限制,可展开折叠功能
 */
public class ExpandableFlowLayout extends FlowLayout {

    public static final int STYLE_NONE = 0;     //无样式,即只提供行数截断,无展开折叠功能
    public static final int STYLE_EXPANDABLE = 1;   //最大行数,可提供展开折叠功能

    private int maxLines = 0;   //最大行数
    private int expandStyle = 0;    //折叠展开样式,可扩展

    private int lastChildIndex = -1;    //最后显示的子view,不包括展开的自定义view
    private View expandChild;   //展开的自定义view
    private int lineCount = 1;  //当前行数
    private int oldMaxLines = 0;    //保存最大行数变量

    public ExpandableFlowLayout(Context context) {
        super(context);
        resolveAttributeSet(context, null);
    }

    public ExpandableFlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        resolveAttributeSet(context, attributeSet);
    }

    public ExpandableFlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        resolveAttributeSet(context, attributeSet);
    }

    public void reset(boolean isExpanded) {
        lastChildIndex = -1;
        if (expandChild != null) {
            expandChild.setVisibility(VISIBLE);
        }
        if (isExpanded) {
            maxLines = Integer.MAX_VALUE;
        } else {
            maxLines = oldMaxLines;
        }
    }

    public void setExpandView(View view) {
        if (view == null) {
            return;
        }
        expandChild = view;
        addView(expandChild);
    }

    public boolean isExpanded() {
        if (expandStyle == STYLE_EXPANDABLE) {
            if (maxLines == Integer.MAX_VALUE) {
                return true;
            }
            return false;
        }
        return false;
    }

    public void collapse() {
        if (expandStyle == STYLE_EXPANDABLE) {
            maxLines = oldMaxLines;
            requestLayout();
        }
    }

    public void expand() {
        if (expandStyle == STYLE_EXPANDABLE) {
            oldMaxLines = maxLines;
            maxLines = Integer.MAX_VALUE;
            requestLayout();
        }
    }

    private boolean isLimitExpandStyle() {
        return haveLimitLines() && expandStyle == STYLE_EXPANDABLE;
    }

    private boolean isLimitNoneStyle() {
        return haveLimitLines() && expandStyle == STYLE_NONE;
    }

    private boolean haveLimitLines() {
        return maxLines != Integer.MAX_VALUE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
                - this.getPaddingRight() - this.getPaddingLeft();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
                - this.getPaddingTop() - this.getPaddingBottom();

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int size;
        int mode;

        if (orientation == HORIZONTAL) {
            size = sizeWidth;
            mode = modeWidth;
        } else {
            size = sizeHeight;
            mode = modeHeight;
        }

        int lineThicknessWithSpacing = 0;
        int lineThickness = 0;
        int lineLengthWithSpacing = 0;
        int lineLength;

        int prevLinePosition = 0;

        int controlMaxLength = 0;
        int controlMaxThickness = 0;

        int count = getChildCount();
        lineCount = 1;

        if (isLimitExpandStyle() && expandChild != null) {
            count--;    //如果添加了折叠展开的控件,最后一个子view不在measure范围
            LayoutParams lastChildlp = (LayoutParams) expandChild.getLayoutParams();
            expandChild.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft()
                            + this.getPaddingRight(), lastChildlp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop()
                            + this.getPaddingBottom(), lastChildlp.height));
        }

        for (int i = 0; i < count; i++) {

            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft()
                            + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop()
                            + this.getPaddingBottom(), lp.height));

            int hSpacing = this.getHorizontalSpacing(lp);
            int vSpacing = this.getVerticalSpacing(lp);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            int childLength;
            int childThickness;
            int spacingLength;
            int spacingThickness;

            if (orientation == HORIZONTAL) {
                childLength = childWidth;
                childThickness = childHeight;
                spacingLength = hSpacing;
                spacingThickness = vSpacing;
            } else {
                childLength = childHeight;
                childThickness = childWidth;
                spacingLength = vSpacing;
                spacingThickness = hSpacing;
            }

            lineLength = lineLengthWithSpacing + childLength;
            lineLengthWithSpacing = lineLength + spacingLength;

            boolean newLine = lp.newLine
                    || (mode != MeasureSpec.UNSPECIFIED && lineLength > size);

            boolean breakLine = figureLastChild(size, lineLengthWithSpacing, i);

            if (newLine && !breakLine) {

                lineCount++;

                prevLinePosition = prevLinePosition + lineThicknessWithSpacing;

                lineThickness = childThickness;
                lineLength = childLength;
                lineThicknessWithSpacing = childThickness + spacingThickness;
                lineLengthWithSpacing = lineLength + spacingLength;

                breakLine = figureLastChild(size, lineLengthWithSpacing, i);
            }

            lineThicknessWithSpacing = Math.max(lineThicknessWithSpacing,
                    childThickness + spacingThickness);
            lineThickness = Math.max(lineThickness, childThickness);

            int posX;
            int posY;
            if (orientation == HORIZONTAL) {
                posX = getPaddingLeft() + lineLength - childLength;
                posY = getPaddingTop() + prevLinePosition;
            } else {
                posX = getPaddingLeft() + prevLinePosition;
                posY = getPaddingTop() + lineLength - childHeight;
            }
            lp.setPosition(posX, posY);

            if (breakLine) {
                int breakLineLength = lineLengthWithSpacing + expandChild.getMeasuredWidth();
                int breakLineThickness = prevLinePosition + lineThickness;
                controlMaxLength = Math.max(controlMaxLength, breakLineLength);
                controlMaxThickness = breakLineThickness;

                //将之后的view都移动到下一行
                if (lastChildIndex > 0 && lastChildIndex + 1 < getChildCount()) {
                    for (int j = lastChildIndex + 1; j < getChildCount(); j++) {
                        View nextChild = getChildAt(j);
                        if (nextChild != expandChild) {
                            LayoutParams params = (LayoutParams) nextChild.getLayoutParams();
                            params.setPosition(getPaddingLeft(), breakLineThickness + getPaddingTop());
                        }
                    }
                }

                break;
            }
            controlMaxLength = Math.max(controlMaxLength, lineLength);
            controlMaxThickness = prevLinePosition + lineThickness;
        }

		/* need to take paddings into account */
        if (orientation == HORIZONTAL) {
            controlMaxLength += getPaddingLeft() + getPaddingRight();
            controlMaxThickness += getPaddingBottom() + getPaddingTop();
        } else {
            controlMaxLength += getPaddingBottom() + getPaddingTop();
            controlMaxThickness += getPaddingLeft() + getPaddingRight();
        }

        if (orientation == HORIZONTAL) {
            this.setMeasuredDimension(
                    resolveSize(controlMaxLength, widthMeasureSpec),
                    resolveSize(controlMaxThickness, heightMeasureSpec));
        } else {
            this.setMeasuredDimension(
                    resolveSize(controlMaxThickness, widthMeasureSpec),
                    resolveSize(controlMaxLength, heightMeasureSpec));
        }
    }

    private boolean figureLastChild(int widthSize, int lineLengthWithSpacing, int childIndex) {
        if (isLimitExpandStyle() && expandChild != null) {
            if (haveLimitLines() && lineCount == maxLines && lineLengthWithSpacing + expandChild.getMeasuredWidth() > widthSize) {
                if (lineLengthWithSpacing > widthSize) {
                    lastChildIndex = childIndex - 1;
                } else if (lineLengthWithSpacing + expandChild.getMeasuredWidth() > widthSize) {
                    if (childIndex == getChildCount() - 2) {
                        lastChildIndex = -1;
                    } else {
                        lastChildIndex = childIndex - 1;
                    }
                }
                return true;
            }
        } else if (isLimitNoneStyle()) {
            if (haveLimitLines() && lineCount == maxLines && lineLengthWithSpacing > widthSize) {
                lastChildIndex = childIndex - 1;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int lastLpX = 0, lastLpY = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (isLimitExpandStyle() && i == lastChildIndex && expandChild != null) {
                lastLpX = lp.x + child.getMeasuredWidth() + getHorizontalSpacing(lp);
                lastLpY = lp.y;
                int width = getMeasuredWidth() - getPaddingRight();
                if (lastLpX + expandChild.getMeasuredWidth() > width) {
                    lastLpX = getPaddingLeft();
                    lastLpY = lp.y + child.getMeasuredHeight() + getVerticalSpacing(lp);
                }
            }
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y
                    + child.getMeasuredHeight());
        }
        if (maxLines != Integer.MAX_VALUE && lineCount < maxLines && expandChild != null) {
            expandChild.setVisibility(GONE);
        } else if (maxLines != Integer.MAX_VALUE && lineCount == maxLines && expandChild != null) {
            if (lastChildIndex > 0 && isLimitExpandStyle()) {
                expandChild.layout(lastLpX, lastLpY, lastLpX + expandChild.getMeasuredWidth(), lastLpY + expandChild.getMeasuredHeight());
            } else {
                expandChild.setVisibility(GONE);
            }
        }
    }

    private void resolveAttributeSet(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet,
                R.styleable.FlowLayout);
        try {
            maxLines = a.getInteger(R.styleable.FlowLayout_maxLines, Integer.MAX_VALUE);
            expandStyle = a.getInteger(R.styleable.FlowLayout_expandStyle, STYLE_NONE);
            oldMaxLines = maxLines;
        } finally {
            a.recycle();
        }
    }
}
