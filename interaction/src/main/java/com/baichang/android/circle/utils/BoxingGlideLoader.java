/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.baichang.android.circle.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import com.baichang.android.circle.R;
import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * use https://github.com/bumptech/glide as media loader.
 * can <b>not</b> be used in Production Environment.
 *
 * @author ChenSL
 */
public class BoxingGlideLoader implements IBoxingMediaLoader {

  @Override
  public void displayThumbnail(@NonNull ImageView img, @NonNull String absPath, int width, int height) {
    String path = "file://" + absPath;
    try {
      // https://github.com/bumptech/glide/issues/1531
      Glide.with(img.getContext()).load(path).placeholder(R.mipmap.interaction_icon_default_image).crossFade().centerCrop()
          .into(img);
    } catch (IllegalArgumentException ignore) {
    }

  }

  @Override
  public void displayRaw(@NonNull final ImageView img, @NonNull String absPath, final IBoxingCallback callback) {
    String path = "file://" + absPath;
    Glide.with(img.getContext())
        .load(path)
        .asBitmap()
        .listener(new RequestListener<String, Bitmap>() {
          @Override
          public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            if (callback != null) {
              callback.onFail(e);
              return true;
            }
            return false;
          }

          @Override
          public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
              boolean isFromMemoryCache, boolean isFirstResource) {

            if (resource != null && callback != null) {
              img.setImageBitmap(resource);
              callback.onSuccess();
              return true;
            }
            return false;
          }
        })
        .into(img);

  }

  private Bitmap transform(Bitmap source) {

    final int mMaxWidth = 720;
    final int mMaxHeight = 1080;

    int targetWidth, targetHeight;
    double aspectRatio;

    if (source.getWidth() > source.getHeight()) {
      targetWidth = mMaxWidth;
      aspectRatio = (double) source.getHeight() / (double) source.getWidth();
      targetHeight = (int) (targetWidth * aspectRatio);
    } else {
      targetHeight = mMaxHeight;
      aspectRatio = (double) source.getWidth() / (double) source.getHeight();
      targetWidth = (int) (targetHeight * aspectRatio);
    }

    Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
    if (result != source) {
      source.recycle();
    }
    return result;
  }
}
