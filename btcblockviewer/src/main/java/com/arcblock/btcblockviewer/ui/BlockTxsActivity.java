/*
 * Copyright (c) 2017-present ArcBlock Foundation Ltd <https://www.arcblock.io/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.arcblock.btcblockviewer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.api.Query;
import com.arcblock.btcblockviewer.BtcBlockViewerApp;
import com.arcblock.btcblockviewer.R;
import com.arcblock.btcblockviewer.TransactionsOfBlockQuery;
import com.arcblock.btcblockviewer.adapter.TxsAdapter;
import com.arcblock.btcblockviewer.type.PageInput;
import com.arcblock.btcblockviewer.utils.StatusBarUtils;
import com.arcblock.corekit.CoreKitPagedQuery;
import com.arcblock.corekit.CoreKitPagedQueryResultListener;
import com.arcblock.corekit.PagedQueryHelper;
import com.arcblock.corekit.utils.CoreKitDiffUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class BlockTxsActivity extends AppCompatActivity {

    public static final String BLOCK_HEIGHT_KEY = "block_height_key";

    private RecyclerView txsRv;
    private TextView mBlockHeightTv;
    private TextView titleTv;

    private TxsAdapter mTxsAdapter;
    private List<TransactionsOfBlockQuery.Datum> mTxs = new ArrayList<>();
    private PagedQueryHelper<TransactionsOfBlockQuery.Data, TransactionsOfBlockQuery.Datum> mPagedQueryHelper;
    private CoreKitPagedQuery<TransactionsOfBlockQuery.Data, TransactionsOfBlockQuery.Datum> mCoreKitPagedQuery;
    private int blockHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        this.getWindow().setBackgroundDrawableResource(R.color.transparent);

        StatusBarUtils.MIUISetStatusBarLightMode(this.getWindow(), false);
        StatusBarUtils.FlymeSetStatusBarLightMode(this.getWindow(), false);

        setContentView(R.layout.activity_txs);

        blockHeight = getIntent().getIntExtra(BLOCK_HEIGHT_KEY, 0);

        initView();
        initData();
    }

    private void initView() {

        mBlockHeightTv = findViewById(R.id.block_height_tv);
        titleTv = findViewById(R.id.title_tv);

        txsRv = (RecyclerView) findViewById(R.id.txs_rv);
        txsRv.setLayoutManager(new GridLayoutManager(this, 5));

        mTxsAdapter = new TxsAdapter(R.layout.item_txs, mTxs);
        mTxsAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mCoreKitPagedQuery.startLoadMoreQuery();
            }
        }, txsRv);
        mTxsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
        txsRv.setAdapter(mTxsAdapter);
    }

    private void initData() {

        mBlockHeightTv.setText(blockHeight + "");

        mPagedQueryHelper = new PagedQueryHelper<TransactionsOfBlockQuery.Data, TransactionsOfBlockQuery.Datum>() {
            @Override
            public Query getInitialQuery() {
                return TransactionsOfBlockQuery.builder().blockHeight(blockHeight).build();
            }

            @Override
            public Query getLoadMoreQuery() {
                PageInput pageInput = null;
                if (!TextUtils.isEmpty(getCursor())) {
                    pageInput = PageInput.builder().cursor(getCursor()).build();
                }
                return TransactionsOfBlockQuery.builder().blockHeight(blockHeight).paging(pageInput).build();
            }

            @Override
            public List<TransactionsOfBlockQuery.Datum> map(TransactionsOfBlockQuery.Data data) {
                if (data.getTransactionsByIndex() != null) {
                    // set page info to CoreKitPagedHelper
                    if (data.getTransactionsByIndex().getPage() != null) {
                        // set is have next flag to CoreKitPagedHelper
                        setHasMore(data.getTransactionsByIndex().getPage().isNext());
                        // set new cursor to CoreKitPagedHelper
                        setCursor(data.getTransactionsByIndex().getPage().getCursor());
                    }
                    return data.getTransactionsByIndex().getData();
                }
                return null;
            }
        };

        mCoreKitPagedQuery = new CoreKitPagedQuery<>(this, BtcBlockViewerApp.getInstance().abCoreKitClient(), mPagedQueryHelper);
        mCoreKitPagedQuery.setPagedQueryResultListener(new CoreKitPagedQueryResultListener<TransactionsOfBlockQuery.Datum>() {
            @Override
            public void onSuccess(List<TransactionsOfBlockQuery.Datum> list) {
                //1. handle return data
                // new a old list
                List<com.arcblock.btcblockviewer.TransactionsOfBlockQuery.Datum> oldList = new ArrayList<>();
                oldList.addAll(mTxs);
                // set mBlocks with new data
                mTxs = list;
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CoreKitDiffUtil<>(oldList, mTxs), true);
                // need this line , otherwise the update will have no effect
                mTxsAdapter.setNewListData(mTxs);
                result.dispatchUpdatesTo(mTxsAdapter);

                //2. view status change and loadMore component need
                if (mPagedQueryHelper.isHasMore()) {
                    mTxsAdapter.setEnableLoadMore(true);
                    mTxsAdapter.loadMoreComplete();
                } else {
                    mTxsAdapter.loadMoreEnd();
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        mCoreKitPagedQuery.startInitQuery();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
