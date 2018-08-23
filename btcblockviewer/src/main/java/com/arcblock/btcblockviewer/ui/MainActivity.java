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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.arcblock.btcblockviewer.BlocksByHeightQuery;
import com.arcblock.btcblockviewer.BtcBlockViewerApp;
import com.arcblock.btcblockviewer.R;
import com.arcblock.btcblockviewer.adapter.ListBlocksAdapter;
import com.arcblock.btcblockviewer.type.PageInput;
import com.arcblock.btcblockviewer.utils.StatusBarUtils;
import com.arcblock.corekit.bean.CoreKitBean;
import com.arcblock.corekit.bean.CoreKitPagedBean;
import com.arcblock.corekit.utils.CoreKitBeanMapper;
import com.arcblock.corekit.utils.CoreKitDiffUtil;
import com.arcblock.corekit.utils.CoreKitPagedHelper;
import com.arcblock.corekit.viewmodel.CoreKitPagedViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListBlocksAdapter.OnViewMoreClickListener {

	private RecyclerView mBlocksRv;
	private TextView mBlockHeightTv;
	private TextView titleTv;
	private ImageView nextIv;
	private ImageView preIv;
	private ImageView aboutIcon;
	private TextView aboutTv;

	private ListBlocksAdapter mListBlocksAdapter;

	private List<BlocksByHeightQuery.Datum> mBlocks = new ArrayList<>();
	private CoreKitPagedViewModel<BlocksByHeightQuery.Data, BlocksByHeightQuery.Datum> mBlocksByHeightQueryViewModel;
	private int startIndex = 448244;
	private int endIndex = 448344;

	private int currentPosi = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		this.getWindow().setBackgroundDrawableResource(R.color.transparent);

		StatusBarUtils.MIUISetStatusBarLightMode(this.getWindow(), false);
		StatusBarUtils.FlymeSetStatusBarLightMode(this.getWindow(), false);

		setContentView(R.layout.activity_main);

		initView();
		initData();
	}

	private void initView() {

		RelativeLayout headRl = findViewById(R.id.head_rl);
		mBlockHeightTv = findViewById(R.id.block_height_tv);
		titleTv = findViewById(R.id.title_tv);
		nextIv = findViewById(R.id.next_iv);
		preIv = findViewById(R.id.pre_iv);
		aboutIcon = findViewById(R.id.about_icon);
		aboutTv = findViewById(R.id.about_tv);

		mBlocksRv = (RecyclerView) findViewById(R.id.blocks_rv);
		mBlocksRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

		LinearSnapHelper linearSnapHelper = new LinearSnapHelper();

		mListBlocksAdapter = new ListBlocksAdapter(R.layout.item_list_blocks, mBlocks, this);
		mListBlocksAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				mBlocksByHeightQueryViewModel.loadMore();
			}
		}, mBlocksRv);
		mListBlocksAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

			}
		});
		mBlocksRv.setAdapter(mListBlocksAdapter);

		mBlocksRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					if (getCurrentItem() >= 0 && getCurrentItem() < mBlocks.size()) {
						mBlockHeightTv.setText(mBlocks.get(getCurrentItem()).getHeight() + "");
						currentPosi = getCurrentItem();
						refreshNextAndPre();
					}
				}
			}
		});

		linearSnapHelper.attachToRecyclerView(mBlocksRv);

		nextIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBlocksRv.smoothScrollToPosition(currentPosi + 1);
			}
		});

		preIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBlocksRv.smoothScrollToPosition(currentPosi - 1);
			}
		});

		refreshNextAndPre();

		findViewById(R.id.about_us_ll).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					Pair<View, String> shareViewOne = new Pair<>((View)aboutIcon, "about_icon");
					Pair<View, String> shareViewTwo = new Pair<>((View)aboutTv, "about_tv");
					ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, shareViewOne, shareViewTwo);
					startActivity(intent, options.toBundle());
				} else {
					startActivity(intent);
				}
			}
		});

		findViewById(R.id.help_iv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(MainActivity.this)
						.title("Description")
						.customView(R.layout.dialog_desc, true)
						.positiveText("Ok")
						.show();
			}
		});
	}

	private void refreshNextAndPre() {
		preIv.setVisibility(View.VISIBLE);
		nextIv.setVisibility(View.VISIBLE);
		if (currentPosi == 0) {
			preIv.setVisibility(View.INVISIBLE);
		}
		if (currentPosi == mBlocks.size() - 1) {
			nextIv.setVisibility(View.INVISIBLE);
		}
	}

	private int getCurrentItem() {
		return ((LinearLayoutManager) mBlocksRv.getLayoutManager())
				.findFirstVisibleItemPosition();
	}

	private void initData() {

		mBlockHeightTv.setText(startIndex + "");
		//1.init a corekitpagedhelper
		//	1.1 set initial query
		//  1.2 set loadmore query
		//  1.3 set refresh query
		final CoreKitPagedHelper coreKitPagedHelper = new CoreKitPagedHelper() {

			@Override
			public Query getInitialQuery() {
				return BlocksByHeightQuery.builder().fromHeight(startIndex).toHeight(endIndex).build();
			}

			@Override
			public Query getLoadMoreQuery() {
				PageInput pageInput = null;
				if (!TextUtils.isEmpty(getCursor())) {
					pageInput = PageInput.builder().cursor(getCursor()).build();
				}
				return BlocksByHeightQuery.builder().fromHeight(startIndex).toHeight(endIndex).paging(pageInput).build();
			}

			@Override
			public Query getRefreshQuery() {
				return BlocksByHeightQuery.builder().fromHeight(startIndex).toHeight(endIndex).build();
			}
		};

		//2. init data mapper
		CoreKitBeanMapper<Response<BlocksByHeightQuery.Data>, List<BlocksByHeightQuery.Datum>> blocksMapper = new CoreKitBeanMapper<Response<BlocksByHeightQuery.Data>, List<BlocksByHeightQuery.Datum>>() {

			@Override
			public List<BlocksByHeightQuery.Datum> map(Response<BlocksByHeightQuery.Data> dataResponse) {
				if (dataResponse != null && dataResponse.data().getBlocksByHeight() != null) {
					// set page info to CoreKitPagedHelper
					if (dataResponse.data().getBlocksByHeight().getPage() != null) {
						// set is have next flag to CoreKitPagedHelper
						coreKitPagedHelper.setHasMore(dataResponse.data().getBlocksByHeight().getPage().isNext());
						// set new cursor to CoreKitPagedHelper
						coreKitPagedHelper.setCursor(dataResponse.data().getBlocksByHeight().getPage().getCursor());
					}
					return dataResponse.data().getBlocksByHeight().getData();
				}
				return null;
			}
		};

		//3. init the ViewModel with CustomClientFactory
		CoreKitPagedViewModel.CustomClientFactory factory = new CoreKitPagedViewModel.CustomClientFactory(blocksMapper, coreKitPagedHelper, BtcBlockViewerApp.getInstance().abCoreKitClient());
		mBlocksByHeightQueryViewModel = ViewModelProviders.of(this, factory).get(CoreKitPagedViewModel.class);
		mBlocksByHeightQueryViewModel.getCleanQueryData().observe(this, new Observer<CoreKitPagedBean<List<BlocksByHeightQuery.Datum>>>() {
			@Override
			public void onChanged(@Nullable CoreKitPagedBean<List<BlocksByHeightQuery.Datum>> coreKitPagedBean) {
				//1. handle return data
				if (coreKitPagedBean.getStatus() == CoreKitBean.SUCCESS_CODE) {
					if (coreKitPagedBean.getData() != null) {
						// new a old list
						List<BlocksByHeightQuery.Datum> oldList = new ArrayList<>();
						oldList.addAll(mBlocks);

						// set mBlocks with new data
						mBlocks = coreKitPagedBean.getData();
						DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CoreKitDiffUtil<>(oldList, mBlocks), true);
						// need this line , otherwise the update will have no effect
						mListBlocksAdapter.setNewListData(mBlocks);
						result.dispatchUpdatesTo(mListBlocksAdapter);

						refreshNextAndPre();
					}
				}

				//2. view status change and loadMore component need
				if (coreKitPagedHelper.isHasMore()) {
					mListBlocksAdapter.setEnableLoadMore(true);
					mListBlocksAdapter.loadMoreComplete();
				} else {
					mListBlocksAdapter.loadMoreEnd();
				}
			}
		});
	}

	@Override
	public void onViewMoreClick(int blockHeight) {
		Intent intent = new Intent(this, BlockTxsActivity.class);
		intent.putExtra(BlockTxsActivity.BLOCK_HEIGHT_KEY, blockHeight);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			Pair<View, String> shareViewOne = new Pair<>((View)mBlockHeightTv, "block_height_tv");
			Pair<View, String> shareViewTwo = new Pair<>((View)titleTv, "title_tv");
			ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, shareViewOne, shareViewTwo);
			startActivity(intent, options.toBundle());
		} else {
			startActivity(intent);
		}
	}
}
