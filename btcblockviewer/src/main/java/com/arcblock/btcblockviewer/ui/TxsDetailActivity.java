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

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arcblock.btcblockviewer.BtcBlockViewerApp;
import com.arcblock.btcblockviewer.R;
import com.arcblock.btcblockviewer.TransactionByHashQuery;
import com.arcblock.btcblockviewer.utils.BtcValueUtils;
import com.arcblock.btcblockviewer.utils.StatusBarUtils;
import com.arcblock.corekit.CoreKitQuery;
import com.arcblock.corekit.CoreKitResultListener;
import com.blankj.utilcode.util.ConvertUtils;

public class TxsDetailActivity extends AppCompatActivity {

    public static final String TXS_HASH = "txs_hash";
    public static final String TXS_SIZE = "txs_size";

    private LinearLayout cardLl;
    private TextView hashTv;
    private TextView block_hash_tv;
    private TextView block_height_tv;
    private TextView size_tv;
    private TextView virtual_size_tv;
    private TextView weight_tv;
    private TextView input_tv;
    private TextView output_tv;
    private TextView input_num_tv;
    private TextView output_num_tv;
    private TextView fees_tv;

    private String txsHash;
    private int txsSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        this.getWindow().setBackgroundDrawableResource(R.color.transparent);

        StatusBarUtils.MIUISetStatusBarLightMode(this.getWindow(), false);
        StatusBarUtils.FlymeSetStatusBarLightMode(this.getWindow(), false);

        setContentView(R.layout.activity_txs_detail);

        txsHash = getIntent().getStringExtra(TXS_HASH);
        txsSize = getIntent().getIntExtra(TXS_SIZE, 0);

        initView();
        initData();
    }

    private void initView() {
        cardLl = findViewById(R.id.card_ll);
        hashTv = findViewById(R.id.hash_tv);

        block_hash_tv = findViewById(R.id.block_hash_tv);
        block_height_tv = findViewById(R.id.block_height_tv);
        size_tv = findViewById(R.id.size_tv);
        virtual_size_tv = findViewById(R.id.virtual_size_tv);
        weight_tv = findViewById(R.id.weight_tv);
        input_tv = findViewById(R.id.input_tv);
        output_tv = findViewById(R.id.output_tv);
        input_num_tv = findViewById(R.id.input_num_tv);
        output_num_tv = findViewById(R.id.output_num_tv);
        fees_tv = findViewById(R.id.fees_tv);

        if (txsSize < 100) {
            cardLl.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_one);
        } else if (txsSize < 200) {
            cardLl.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_two);
        } else if (txsSize < 500) {
            cardLl.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_three);
        } else if (txsSize < 1000) {
            cardLl.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_four);
        } else {
            cardLl.setBackgroundResource(R.drawable.item_list_blocks_child_view_bg_five);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardLl.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), ConvertUtils.dp2px(5));
                    }
                }
            });
            cardLl.setTranslationZ(ConvertUtils.dp2px(8));
        }
    }

    private void initData() {
        hashTv.setText("Txs-" + txsHash);
        CoreKitQuery coreKitQuery = new CoreKitQuery(this, BtcBlockViewerApp.getInstance().abCoreKitClient());
        coreKitQuery.query(TransactionByHashQuery.builder().hash(txsHash).build(), new CoreKitResultListener<TransactionByHashQuery.Data>() {
            @Override
            public void onSuccess(TransactionByHashQuery.Data data) {
                com.arcblock.btcblockviewer.TransactionByHashQuery.TransactionByHash transactionByHash = data.getTransactionByHash();
                if (transactionByHash != null) {
                    block_hash_tv.setText(transactionByHash.getBlockHash());
                    block_height_tv.setText(transactionByHash.getBlockHeight() + "");
                    size_tv.setText(transactionByHash.getSize() + " Bytes");
                    virtual_size_tv.setText(transactionByHash.getVirtualSize() + " Bytes");
                    weight_tv.setText(transactionByHash.getWeight() + "");
                    input_tv.setText(BtcValueUtils.formatBtcValue(transactionByHash.getTotal()));
                    output_tv.setText(BtcValueUtils.formatBtcValue(transactionByHash.getTotal()));
                    input_num_tv.setText(transactionByHash.getNumberInputs() + "");
                    output_num_tv.setText(transactionByHash.getNumberOutputs() + "");
                    fees_tv.setText(BtcValueUtils.formatBtcValue(transactionByHash.getFees()));
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
