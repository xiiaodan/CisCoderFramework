/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.baichang.android.circle.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.baichang.android.utils.BCDensityUtil;

public class VerticalDividerDecoration extends RecyclerView.ItemDecoration {

  private Drawable mDivider;

  public VerticalDividerDecoration() {
    mDivider = new ColorDrawable(Color.GRAY);
  }

  public VerticalDividerDecoration(int color) {
    mDivider = new ColorDrawable(color);
  }

  @Override
  public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    final int left = parent.getPaddingLeft();
    final int right = parent.getWidth() - parent.getPaddingRight();

    final int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      final RecyclerView.LayoutParams params =
          (RecyclerView.LayoutParams) child.getLayoutParams();
      final int top = child.getBottom() + params.bottomMargin;
      final int bottom = top + 25;
      mDivider.setBounds(left, top, right, bottom);
      mDivider.draw(c);
    }
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    int itemPosition =
        ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
    int totalItemCount = parent.getAdapter().getItemCount();
    if (itemPosition == totalItemCount - 1) {
      outRect.set(0, 0, 0, 0);
    } else {
      outRect.set(0, 0, 0, BCDensityUtil.dip2px(parent.getContext(), 16));
    }
  }
}

