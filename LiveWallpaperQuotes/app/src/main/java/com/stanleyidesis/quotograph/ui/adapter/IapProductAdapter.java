package com.stanleyidesis.quotograph.ui.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.stanleyidesis.quotograph.IabConst;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.billing.util.SkuDetails;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Copyright (c) 2016 Stanley Idesis
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

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * IapProductAdapter.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 05/01/2016
 */
public class IapProductAdapter extends BaseAdapter {

    public interface Delegate {
        void purchaseProduct(IabConst.Product product);
    }

    Delegate delegate;
    int color1, color2;

    public IapProductAdapter(Delegate delegate) {
        this.delegate = delegate;
        Resources resources = LWQApplication.get().getResources();
        color1 = resources.getColor(R.color.palette_500);
        color2 = resources.getColor(R.color.palette_700);
    }

    @Override
    public int getCount() {
        return IabConst.Product.values().length;
    }

    @Override
    public Object getItem(int position) {
        return IabConst.Product.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.iap_product_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.bind(IabConst.Product.values()[position]);
        convertView.setBackgroundColor(position % 2 == 0 ? color1 : color2);
        return convertView;
    }

    class ViewHolder {

        View root;
        @Bind(R.id.iv_product_item)
        ImageView productImage;
        @Bind(R.id.tv_product_item_price)
        TextView productPrice;
        @Bind(R.id.tv_product_item_title)
        TextView productTitle;
        @Bind(R.id.tv_product_item_description)
        TextView productDescription;
        @Bind(R.id.ll_product_item_screen)
        View purchasedScreen;
        @Bind(R.id.btn_product_item)
        View btnBuyProduct;

        IabConst.Product product;
        SkuDetails skuDetails;

        ViewHolder(View view) {
            this.root = view;
            ButterKnife.bind(this, view);
        }

        void bind(IabConst.Product product) {
            this.product = product;
            this.skuDetails = LWQApplication.getProductDetails(product);
            ImageLoader.getInstance().displayImage(product.imgSource, productImage);
            if (skuDetails == null) {
                productTitle.setText(product.titleRes);
                productDescription.setText(product.descriptionRes);
                productPrice.setVisibility(View.GONE);
            } else {
                productTitle.setText(skuDetails.getTitle().replaceFirst("\\(.+\\)", ""));
                productDescription.setText(skuDetails.getDescription());
                productPrice.setVisibility(View.VISIBLE);
                productPrice.setText(skuDetails.getPrice());
            }
            btnBuyProduct.setContentDescription("Purchase " + productTitle.getText());
            boolean ownsProduct = LWQApplication.ownsProduct(product);
            purchasedScreen.setVisibility(ownsProduct ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.btn_product_item)
        void buttonClick() {
            delegate.purchaseProduct(product);
        }
    }
}
