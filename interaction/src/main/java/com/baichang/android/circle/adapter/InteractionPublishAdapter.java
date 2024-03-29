package com.baichang.android.circle.adapter;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.os.Build.VERSION_CODES;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.baichang.android.circle.R;
import com.baichang.android.circle.adapter.InteractionPublishAdapter.Holder;
import com.baichang.android.circle.entity.InteractionPublishImageData;
import com.bilibili.boxing.model.entity.BaseMedia;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iCong on 2017/3/24.
 */

public class InteractionPublishAdapter extends RecyclerView.Adapter<Holder> {

  private static final int NORMAL_VIEW = 0;
  private static final int FOOT_VIEW = 1;
  private List<InteractionPublishImageData> mList;

  public InteractionPublishAdapter(SelectPhotoClickListener listener) {
    mList = new ArrayList<>();
    setSelectPhotoClickListener(listener);
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case NORMAL_VIEW:
        return new Holder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.interaction_item_publish_image_layout, parent, false),
            NORMAL_VIEW);
      case FOOT_VIEW:
        return new Holder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.interaction_item_publish_foot_view, parent, false),
            FOOT_VIEW);
      default:
        return null;
    }
  }

  @Override
  public void onBindViewHolder(Holder holder, int position) {
    if (getItemViewType(position) == FOOT_VIEW) {
      return;
    }
    holder.ivImage.setImageBitmap(mList.get(position).bitmap);
  }

  @Override
  public int getItemViewType(int position) {
    if (position == getItemCount() - 1) {
      return FOOT_VIEW;
    } else {
      return NORMAL_VIEW;
    }
  }

  @Override
  public int getItemCount() {
    return mList.size() + 1;
  }


  public void addData(InteractionPublishImageData data) {
    mList.add(data);
    notifyItemInserted(getItemCount() - 2);
  }

  public void setData(ArrayList<BaseMedia> list) {

  }

  private void removeData(int position) {
    mList.remove(position);
    notifyItemRemoved(position);
  }

  public List<InteractionPublishImageData> getList() {
    return mList;
  }

  public List<String> getPathList() {
    List<String> path = new ArrayList<>();
    for (InteractionPublishImageData imageData : mList) {
      path.add(imageData.path);
    }
    return path;
  }

  public void addData(Bitmap bitmap, String path) {
    InteractionPublishImageData data = new InteractionPublishImageData(path, bitmap);
    mList.add(data);
    notifyItemInserted(getItemCount() - 2);
  }

  private SelectPhotoClickListener selectPhotoClickListener;

  public void setSelectPhotoClickListener(SelectPhotoClickListener listener) {
    selectPhotoClickListener = listener;
  }

  public interface SelectPhotoClickListener {

    void select();
  }

  class Holder extends ViewHolder implements OnClickListener {

    ImageButton btnDelete;
    ImageView ivImage;

    Holder(View itemView, int type) {
      super(itemView);
      if (type == NORMAL_VIEW) {
        ivImage = (ImageView) itemView.findViewById(R.id.item_interaction_publish_iv_image);
        btnDelete = (ImageButton) itemView.findViewById(R.id.item_interaction_publish_btn_delete);
        btnDelete.setOnClickListener(this);
      } else {
        itemView.findViewById(R.id.item_interaction_publish_btn_add).setOnClickListener(this);
      }
    }

    @Override
    public void onClick(View v) {
      int i = v.getId();
      if (i == R.id.item_interaction_publish_btn_add) {// getInstance the center for the clipping circle
        int cx = (v.getLeft() + v.getRight()) / 2;
        int cy = (v.getTop() + v.getBottom()) / 2;
        // getInstance the final radius for the clipping circle
        int finalRadius = Math.max(v.getWidth(), v.getHeight());
        // create the animator for this view (the start radius is zero)
        Animator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
          // make the view visible and start the animation
          v.setVisibility(View.VISIBLE);
          anim.start();
        }
        if (selectPhotoClickListener != null) {
          selectPhotoClickListener.select();
        }
      } else if (i == R.id.item_interaction_publish_btn_delete) {
        removeData(getLayoutPosition());
      }
    }
  }
}
