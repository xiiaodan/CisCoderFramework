package com.baichang.library.test;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.baichang.android.library.BaseActivity;
import com.baichang.android.library.recycleView.RecyclerViewAdapter;
import com.baichang.android.library.recycleView.ViewHolder;
import com.baichang.android.library.retrofit.HttpResultSubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView rvList;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        adapter = new RecyclerViewAdapter<InformationData>(getAty(), R.layout.item_information_list) {
            @Override
            protected void setItemData(ViewHolder holder, InformationData itemData, int position) {
                holder.setTextView(R.id.item_information_tv_title, itemData.titile);
                holder.setTextView(R.id.item_information_tv_content, itemData.abstractTest);
                holder.setTextView(R.id.item_information_tv_date, itemData.created);
                holder.setImageView(R.id.item_information_iv_image, getString(R.string.API_LOAD_IMAGE) + itemData.picture);
            }
        };
        rvList.setLayoutManager(new LinearLayoutManager(getAty()));
        rvList.setAdapter(adapter);

        Map<String, String> map = new HashMap<>();
        map.put("cityId", "1");
        App.request().getInformationList(map).compose(applySchedulers())
                .subscribe(new HttpResultSubscriber<List<InformationData>>().get(list -> {
                    adapter.setData(list);
                }));
    }
}
