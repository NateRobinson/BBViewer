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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import com.arcblock.btcblockviewer.BlocksByHeightQuery;
import com.arcblock.btcblockviewer.R;
import com.arcblock.btcblockviewer.ui.TxsDetailActivity;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class ListBlocksChildAdapter extends BaseQuickAdapter<BlocksByHeightQuery.Datum1, BaseViewHolder> {
	public ListBlocksChildAdapter(int layoutResId, @Nullable List<BlocksByHeightQuery.Datum1> data) {
		super(layoutResId, data);
	}

	@Override
	protected void convert(BaseViewHolder helper, BlocksByHeightQuery.Datum1 item) {
		View childItem = helper.getView(R.id.child_item);

		if (item.getSize() < 100) {
			childItem.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_one);
		} else if (item.getSize() < 200) {
			childItem.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_two);
		} else if (item.getSize() < 500) {
			childItem.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_three);
		} else if (item.getSize() < 1000) {
			childItem.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_four);
		} else {
			childItem.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_five);
		}

		int width = ScreenUtils.getScreenWidth();
		int itemWidth = (width - 12 * ConvertUtils.dp2px(10)) / 5;
		int itemHeight = itemWidth;

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidth, itemHeight);
		params.setMargins(ConvertUtils.dp2px(10), ConvertUtils.dp2px(10), ConvertUtils.dp2px(10), ConvertUtils.dp2px(10));
		childItem.setLayoutParams(params);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			childItem.setOutlineProvider(new ViewOutlineProvider() {
				@Override
				public void getOutline(View view, Outline outline) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), ConvertUtils.dp2px(5));
					}
				}
			});
			childItem.setTranslationZ(ConvertUtils.dp2px(8));
		}

		childItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, TxsDetailActivity.class);
				intent.putExtra(TxsDetailActivity.TXS_HASH, item.getHash());
				intent.putExtra(TxsDetailActivity.TXS_SIZE, item.getSize());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, childItem, "txs_bg");
					mContext.startActivity(intent, options.toBundle());
				} else {
					mContext.startActivity(intent);
				}
			}
		});
	}
}
