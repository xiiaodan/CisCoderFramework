package com.baichang.android.circle.present.Impl;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.baichang.android.circle.R;
import com.baichang.android.circle.adapter.InteractionDetailAdapter;
import com.baichang.android.circle.adapter.InteractionDetailAdapter.ParentContentOnClickListener;
import com.baichang.android.circle.common.InteractionConfig;
import com.baichang.android.circle.common.InteractionConfig.InteractionListener;
import com.baichang.android.circle.common.InteractionFlag.Event;
import com.baichang.android.circle.entity.InteractionCommentList;
import com.baichang.android.circle.entity.InteractionCommentReplyList;
import com.baichang.android.circle.entity.InteractionDetailData;
import com.baichang.android.circle.entity.InteractionUserData;
import com.baichang.android.circle.model.Impl.InteractInteractionImpl;
import com.baichang.android.circle.model.InteractInteraction;
import com.baichang.android.circle.present.InteractionDetailPresent;
import com.baichang.android.circle.utils.AnimatorUtil;
import com.baichang.android.circle.view.InteractionDetailView;
import com.baichang.android.circle.widget.CommentTextView.CommentOnClickListener;
import com.baichang.android.circle.widget.ForceClickImageView;
import com.baichang.android.circle.widget.photocontents.PhotoContents;
import com.baichang.android.circle.widget.photocontents.PhotoContents.OnItemClickListener;
import com.baichang.android.circle.widget.photocontents.adapter.PhotoContentsBaseAdapter;
import com.baichang.android.circle.widget.photopreview.ImageInfo;
import com.baichang.android.circle.widget.photopreview.ImagePreviewActivity;
import com.baichang.android.common.BaseEventData;
import com.baichang.android.common.IBaseInteraction.BaseListener;
import com.baichang.android.imageloader.ImageLoader;
import com.baichang.android.utils.BCDialogUtil;
import com.baichang.android.utils.BCToolsUtil;
import com.baichang.android.widget.circleImageView.CircleImageView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by iCong on 2017/3/21.
 */

public class InteractionDetailPresentImpl
    implements InteractionDetailPresent, OnClickListener, CommentOnClickListener,
    ParentContentOnClickListener, OnItemClickListener {

  private static final int REPLY_TYPE = 132;
  private static final int REPLY_TYPE2 = 133;
  private static final int CHILD_COMMENT_TYPE = 641;
  private static final int PARENT_COMMENT_TYPE = 642;
  private static final int THEM_TYPE = 643;
  private InteractionDetailView mView;
  private InteractInteraction mInteraction;

  private TextView tvName;
  private TextView tvTime;
  private TextView tvTitle;
  private TextView tvContent;
  private TextView tvComment;
  private TextView tvCount;
  private CircleImageView ivAvatar;
  private PhotoContents mPhotos;
  private PhotoContentsAdapter mPhotoAdapter;
  private InteractionDetailAdapter mAdapter;
  private InteractionCommentReplyList mCommentListData;
  private int currentType = THEM_TYPE;
  private int trendsId;
  private int mCommentCount;
  private String commentId;

  public InteractionDetailPresentImpl(int id, InteractionDetailView view) {
    this.trendsId = id;
    mView = view;
    mInteraction = new InteractInteractionImpl();
    mPhotoAdapter = new PhotoContentsAdapter(mView.getContext());
    mAdapter = new InteractionDetailAdapter(this, this);
    EventBus.getDefault().register(this);
  }

  @Override public void onDestroy() {
    mView = null;
    EventBus.getDefault().unregister(this);
  }

  @Override public void onStart() {
    mView.showProgressBar();
    mInteraction.getInteractionDetail(trendsId, detailListener);
  }

  @Override public void attachView(RecyclerView recyclerView, View header) {
    mAdapter.addHeaderView(header);
    recyclerView.setAdapter(mAdapter);
    tvName = (TextView) header.findViewById(R.id.interaction_detail_tv_name);
    tvTime = (TextView) header.findViewById(R.id.interaction_detail_tv_time);
    tvTitle = (TextView) header.findViewById(R.id.interaction_detail_tv_title);
    tvComment = (TextView) header.findViewById(R.id.interaction_detail_tv_comment);
    tvContent = (TextView) header.findViewById(R.id.interaction_detail_tv_content);
    tvCount = (TextView) header.findViewById(R.id.interaction_detail_tv_count);
    ivAvatar = (CircleImageView) header.findViewById(R.id.interaction_detail_iv_avatar);

    tvComment.setOnClickListener(this);
    header.findViewById(R.id.interaction_detail_tv_report).setOnClickListener(this);
    initConfig();
    mPhotos = (PhotoContents) header.findViewById(R.id.interaction_detail_photo_content);
    mPhotos.setOnItemClickListener(this);
  }

  private void initConfig() {
    int commentDrawableRes = InteractionConfig.getInstance().getCommentDrawableRes();
    if (commentDrawableRes != -1) {
      Drawable drawable = ContextCompat.getDrawable(tvComment.getContext(), commentDrawableRes);
      drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
      tvComment.setCompoundDrawables(drawable, null, null, null);
    }
  }

  @Override public void refresh() {
    mView.showProgressBar();
    mInteraction.getInteractionDetail(trendsId, detailListener);
  }

  @Override public void send(String content) {
    if (TextUtils.isEmpty(content)) {
      mView.showMsg("请输入回复内容");
      return;
    }
    InteractionUserData user = InteractionConfig.getInstance().getUser();
    if (user == null) {
      return;
    }
    switch (currentType) {
      case REPLY_TYPE:
        if (mCommentListData == null) {
          break;
        }
        InteractionCommentReplyList replyData = new InteractionCommentReplyList();
        replyData.replayName = user.name;
        replyData.replayContent = content;
        replyData.replayUserId = user.id;
        replyData.trendsId = String.valueOf(trendsId);
        replyData.created = BCToolsUtil.getCurrentTime();
        replyData.commentId = mCommentListData.commentId;
        replyData.commentUserId = mCommentListData.commentUserId;
        replyData.commentName = mCommentListData.commentName;
        addCommentChild(replyData);
        break;
      case REPLY_TYPE2:
        if (mCommentListData == null) {
          break;
        }
        InteractionCommentReplyList replyData2 = new InteractionCommentReplyList();
        replyData2.replayName = user.name;
        replyData2.replayContent = content;
        replyData2.replayUserId = user.id;
        replyData2.created = BCToolsUtil.getCurrentTime();
        replyData2.trendsId = String.valueOf(trendsId);
        replyData2.commentId = mCommentListData.commentId;
        replyData2.commentUserId = mCommentListData.replayUserId;
        replyData2.commentName = mCommentListData.replayName;
        addCommentChild(replyData2);
        break;
      case CHILD_COMMENT_TYPE:
        InteractionCommentReplyList ownerData = new InteractionCommentReplyList();
        ownerData.replayContent = content;
        ownerData.commentName = user.name;
        ownerData.commentUserId = user.id;
        ownerData.trendsId = String.valueOf(trendsId);
        ownerData.created = BCToolsUtil.getCurrentTime();
        ownerData.commentId = commentId;
        addCommentChild(ownerData);
        break;
      case PARENT_COMMENT_TYPE:
        InteractionCommentReplyList parentComment = new InteractionCommentReplyList();
        parentComment.replayContent = content;
        parentComment.commentName = user.name;
        parentComment.commentUserId = user.id;
        parentComment.created = BCToolsUtil.getCurrentTime();
        parentComment.trendsId = String.valueOf(trendsId);
        parentComment.commentId = commentId;
        addCommentChild(parentComment);
        break;
      case THEM_TYPE:
        InteractionCommentList data = new InteractionCommentList();
        data.content = content;
        data.avatar = user.avatar;
        data.name = user.name;
        data.time = BCToolsUtil.getCurrentTime();
        data.userId = user.id;
        data.trendsId = trendsId;
        addComment(data);
        break;
    }
  }

  @Override public void share() {
    final InteractionListener listener = InteractionConfig.getInstance().getListener();
    if (listener != null) {
      mInteraction.getShareLink(String.valueOf(trendsId), new BaseListener<String>() {
        @Override public void success(String url) {
          listener.share(mView.getActivity(), tvTitle.getText().toString(),
              tvContent.getText().toString(), url);
        }

        @Override public void error(String error) {
          mView.showMsg(error);
        }
      });
    }
  }

  @Override public void collect() {
    mInteraction.collect(trendsId, collectListener);
  }

  @Override public void praise() {
    mInteraction.praise(trendsId, praiseListener);
  }

  // 回复某人的评论
  private void addCommentChild(InteractionCommentReplyList data) {
    int index = mAdapter.addChildComment(data);
    mView.scrollToPosition(index);
    mView.hideInputKeyBord();
    mView.setReportHint("回复");
    mCommentListData = null;
    mInteraction.reply(data, replyListener);
  }

  // 评论该帖子
  private void addComment(InteractionCommentList data) {
    mAdapter.addComment(data);
    mView.hideInputKeyBord();
    mView.setReportHint("回复");
    mView.scrollToPosition(1);// 滚动到顶部
    mInteraction.comment(trendsId, data, commentListener);
    // 评论数量 +1
    EventBus.getDefault().post(new BaseEventData(Event.INTERACTION_COMMENT_COUNT_ADD));
  }

  private BaseListener<Boolean> replyListener = new BaseListener<Boolean>() {
    @Override public void success(Boolean aBoolean) {
      mView.showMsg("回复成功");
    }

    @Override public void error(String error) {
      mView.showMsg(error);
    }
  };

  private BaseListener<Boolean> commentListener = new BaseListener<Boolean>() {
    @Override public void success(Boolean aBoolean) {
      mView.showMsg("评论成功");
    }

    @Override public void error(String error) {
      mView.showMsg(error);
    }
  };

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.interaction_detail_tv_report) {
      int colorRes = InteractionConfig.getInstance().getTextFontColor();
      BCDialogUtil.showDialog(v.getContext(),
          colorRes == -1 ? R.color.interaction_text_font : colorRes, "举报", "确定举报该条内容吗？",
          new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              mInteraction.report(InteractionDetailPresentImpl.this.trendsId, reportListener);
            }
          }, null);
    } else if (id == tvComment.getId()) {
      AnimatorUtil.scale(v);
      mView.setReportHint(" 评论 ");
      mView.showInputKeyBord();
      currentType = THEM_TYPE;
    }
  }

  @Override public void commentOnClick(String commentId) {
    this.commentId = commentId;
    mView.setReportHint(" 评论 ");
    mView.showInputKeyBord();
    currentType = CHILD_COMMENT_TYPE;
  }

  @Override public void replyOnClick(InteractionCommentReplyList data) {
    mView.setReportHint(" 回复 " + data.replayName);
    mView.showInputKeyBord();
    mCommentListData = data;
    currentType = REPLY_TYPE;
  }

  @Override public void replyOnClick2(InteractionCommentReplyList data) {
    mView.setReportHint(" 回复 " + data.replayName);
    mView.showInputKeyBord();
    mCommentListData = data;
    currentType = REPLY_TYPE2;
  }

  // 父内容 点击
  @Override public void parentContentOnClick(String commentId) {
    this.commentId = commentId;
    mView.setReportHint(" 评论 ");
    mView.showInputKeyBord();
    currentType = PARENT_COMMENT_TYPE;
  }

  // 子内容点击
  @Override public void childContentOnClick(InteractionCommentReplyList data) {
    if (TextUtils.isEmpty(data.replayName)) {
      mView.setReportHint(" 回复 " + data.commentName);
    } else {
      mView.setReportHint(" 回复 " + data.replayName);
    }
    mView.showInputKeyBord();
    mCommentListData = data;
    currentType = REPLY_TYPE;
  }

  private BaseListener<InteractionDetailData> detailListener =
      new BaseListener<InteractionDetailData>() {

        @Override public void success(InteractionDetailData detail) {
          mView.hideProgressBar();
          if (detail.images != null && !detail.images.isEmpty()) {
            mPhotos.setAdapter(mPhotoAdapter);
            mPhotoAdapter.setData(detail.images);
          }
          tvName.setText(detail.name);
          tvTime.setText(detail.time);
          tvTitle.setText(detail.title);
          tvContent.setText(detail.content);
          ImageLoader.loadImageError(mView.getContext(), detail.avatar,
              R.mipmap.interaction_icon_default, ivAvatar);
          mAdapter.setData(detail.commentList);
          mCommentCount = detail.commentList.size();
          tvCount.setText("评论（" + mCommentCount + "）");
          mView.setCollectState(detail.isCollection == 1);
          mView.setPraiseState(detail.isPraise == 1);
        }

        @Override public void error(String error) {
          mView.hideProgressBar();
          mView.showMsg(error);
        }
      };

  private BaseListener<Boolean> reportListener = new BaseListener<Boolean>() {
    @Override public void success(Boolean aBoolean) {
      mView.showMsg("举报成功");
    }

    @Override public void error(String error) {
      mView.showMsg(error);
    }
  };
  private BaseListener<Boolean> praiseListener = new BaseListener<Boolean>() {
    @Override public void success(Boolean aBoolean) {
      mView.showMsg("操作成功");
    }

    @Override public void error(String error) {
      mView.showMsg(error);
    }
  };

  private BaseListener<Boolean> collectListener = new BaseListener<Boolean>() {
    @Override public void success(Boolean aBoolean) {
      mView.showMsg("操作成功");
    }

    @Override public void error(String error) {
      mView.showMsg(error);
    }
  };

  @Override public void onItemClick(PhotoContents photoContents, int position) {
    List<ImageInfo> imageInfoList = new ArrayList<>();
    for (int i = 0; i < mPhotoAdapter.getList().size(); i++) {
      ImageInfo info = new ImageInfo();
      View imageView = photoContents.getChildAt(i);
      info.bigImageUrl = mPhotoAdapter.getList().get(i);
      info.imageViewWidth = imageView.getWidth();
      info.imageViewHeight = imageView.getHeight();
      int[] points = new int[2];
      imageView.getLocationInWindow(points);
      info.imageViewX = points[0];
      info.imageViewY = points[1];
      imageInfoList.add(info);
    }
    Intent intent = new Intent(photoContents.getContext(), ImagePreviewActivity.class);
    Bundle bundle = new Bundle();
    bundle.putSerializable(ImagePreviewActivity.IMAGE_INFO, (Serializable) imageInfoList);
    bundle.putInt(ImagePreviewActivity.CURRENT_ITEM, position);
    intent.putExtras(bundle);
    photoContents.getContext().startActivity(intent);
    ((Activity) photoContents.getContext()).overridePendingTransition(0, 0);
  }

  private class PhotoContentsAdapter extends PhotoContentsBaseAdapter {

    private Context mContext;
    private List<String> mList;

    PhotoContentsAdapter(Context context) {
      mContext = context;
      mList = new ArrayList<>();
    }

    @Override public ImageView onCreateView(ImageView imageView, ViewGroup viewGroup, int i) {
      if (imageView == null) {
        imageView = new ForceClickImageView(viewGroup.getContext());
        imageView.setScaleType(ScaleType.CENTER_CROP);
      }
      return imageView;
    }

    @Override public void onBindData(int i, @NonNull ImageView imageView) {
      ImageLoader.loadImageError(mContext, mList.get(i), R.mipmap.interaction_icon_default,
          imageView);
    }

    @Override public int getCount() {
      return mList.size();
    }

    public void setData(List<String> list) {
      mList = list;
      notifyDataChanged();
    }

    public List<String> getList() {
      return mList;
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void event(BaseEventData data) {
    if (data.key == Event.INTERACTION_COMMENT_COUNT_ADD) {
      mCommentCount++;
      tvCount.setText("评论（" + mCommentCount + "）");
    }
  }
}
