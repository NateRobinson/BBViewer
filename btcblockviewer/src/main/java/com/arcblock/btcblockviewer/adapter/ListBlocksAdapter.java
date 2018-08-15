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
package com.arcblock.btcblockviewer.adapter;

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arcblock.btcblockviewer.BlocksByHeightQuery;
import com.arcblock.btcblockviewer.R;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class ListBlocksAdapter extends BaseQuickAdapter<BlocksByHeightQuery.Datum, BaseViewHolder> {

	private OnViewMoreClickListener mOnViewMoreClickListener;

	public ListBlocksAdapter(int layoutResId, @Nullable List<BlocksByHeightQuery.Datum> data, OnViewMoreClickListener onViewMoreClickListener) {
		super(layoutResId, data);
		this.mOnViewMoreClickListener = onViewMoreClickListener;
	}

	public void setNewListData(List<BlocksByHeightQuery.Datum> newList) {
		this.mData = newList;
	}

	@Override
	protected void convert(BaseViewHolder helper, BlocksByHeightQuery.Datum item) {
		TextView emptyTv = helper.getView(R.id.empty_tv);
		RecyclerView childRcv = helper.getView(R.id.child_rcv);
		LinearLayout contentLl = helper.getView(R.id.content_ll);

		if (item.getTransactions() == null || item.getTransactions().getData() == null || item.getTransactions().getData().isEmpty()) {
			emptyTv.setVisibility(View.VISIBLE);
			contentLl.setVisibility(View.GONE);
		} else {
			emptyTv.setVisibility(View.GONE);
			contentLl.setVisibility(View.VISIBLE);

			ListBlocksChildAdapter listBlocksChildAdapter = new ListBlocksChildAdapter(R.layout.item_list_blocks_child, item.getTransactions().getData());
			childRcv.setLayoutManager(new GridLayoutManager(mContext, 5));
			childRcv.setAdapter(listBlocksChildAdapter);

			helper.getView(R.id.view_more_tv).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnViewMoreClickListener!=null) {
						mOnViewMoreClickListener.onViewMoreClick(item.getHeight());
					}
//					Intent intent = new Intent(mContext, BlockTxsActivity.class);
//					intent.putExtra(BlockTxsActivity.BLOCK_HEIGHT_KEY, item.getHeight());
//					mContext.startActivity(intent);
				}
			});

			LinearLayout isMoreView = helper.getView(R.id.have_more_status_view);
			if (item.getTransactions().getPage() != null && item.getTransactions().getPage().isNext()) {

				TextView lastNumTv = helper.getView(R.id.last_num_tv);
				int lastNum = item.getTransactions().getPage().getTotal() - 10;
				lastNumTv.setText(lastNum > 99 ? "99+" : lastNum + "");

				isMoreView.setVisibility(View.VISIBLE);
				int width = ScreenUtils.getScreenWidth();
				int itemWidth = (width - 12 * ConvertUtils.dp2px(10)) / 5;
				int itemHeight = itemWidth;

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
				params.setMargins(ConvertUtils.dp2px(10), ConvertUtils.dp2px(10), ConvertUtils.dp2px(10), ConvertUtils.dp2px(10));
				isMoreView.setLayoutParams(params);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					isMoreView.setOutlineProvider(new ViewOutlineProvider() {
						@Override
						public void getOutline(View view, Outline outline) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), ConvertUtils.dp2px(5));
							}
						}
					});
					isMoreView.setTranslationZ(ConvertUtils.dp2px(8));
				}
			} else {
				isMoreView.setVisibility(View.GONE);
			}
		}
	}

	public interface OnViewMoreClickListener {
		void onViewMoreClick(int blockHeight);
	}
}
