package com.stanleyidesis.quotograph.ui.adapter;

import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.orm.SugarRecord;
import com.stanleyidesis.quotograph.BuildConfig;
import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.api.controller.LWQLoggerHelper;
import com.stanleyidesis.quotograph.api.db.Author;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Playlist;
import com.stanleyidesis.quotograph.api.db.PlaylistAuthor;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.PlaylistQuote;
import com.stanleyidesis.quotograph.api.db.Quote;
import com.stanleyidesis.quotograph.api.misc.UserSurveyController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * PlaylistAdapter.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 10/05/2015
 */
public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_SURVEY = 1;
    private final int VIEW_TYPE_AD = 2;

    public interface Delegate {
        void onPlaylistItemRemove(PlaylistAdapter adapter, int position);
        void onQuoteEdit(PlaylistAdapter adapter, int position);
        void onMakeQuotograph(PlaylistAdapter adapter, int position);
    }

    private Delegate delegate;
    private List<Object> playlistItems;
    private List<PlaylistCategory> playlistCategories;
    private List<PlaylistAuthor> playlistAuthors;
    private List<PlaylistQuote> playlistQuotes;
    private List<AdPlaceholder> adPlaceholders;
    private boolean showSurvey;

    public PlaylistAdapter() {
        this(null);
    }

    public PlaylistAdapter(Delegate delegate) {
        this.delegate = delegate;
        final Playlist activePlaylist = Playlist.active();
        adPlaceholders = new ArrayList<>();
        playlistCategories = PlaylistCategory.forPlaylist(activePlaylist);
        playlistAuthors = PlaylistAuthor.forPlaylist(activePlaylist);
        playlistQuotes = PlaylistQuote.forPlaylist(activePlaylist);
        Collections.sort(playlistCategories);
        Collections.sort(playlistAuthors);
        Collections.sort(playlistQuotes);
        playlistItems = new ArrayList<>();
        insertAll();
        // Log user data
        logUserData();
    }

    public Object getItem(int position) {
        return playlistItems.get(position);
    }

    public void insertItem(Object object) {
        int insertPosition;
        List<?> playlistEntryList = null;
        if (object instanceof PlaylistCategory) {
            playlistEntryList = playlistCategories;
            insertPosition = findFirstPosition(playlistCategories);
            playlistCategories.add((PlaylistCategory) object);
            Collections.sort(playlistCategories);
            if (insertPosition != -1) {
                insertPosition += playlistCategories.indexOf(object);
            } else {
                insertPosition = findFirstPosition(playlistAuthors);
                if (insertPosition == -1) insertPosition = findFirstPosition(playlistQuotes);
            }
        } else if (object instanceof PlaylistAuthor) {
            playlistEntryList = playlistAuthors;
            insertPosition = findFirstPosition(playlistAuthors);
            playlistAuthors.add((PlaylistAuthor) object);
            Collections.sort(playlistAuthors);
            if (insertPosition != -1) {
                insertPosition += playlistAuthors.indexOf(object);
            } else {
                insertPosition = findLastPosition(playlistCategories) + 1;
                if (insertPosition == 0) insertPosition = findFirstPosition(playlistQuotes);
            }
        } else {
            playlistEntryList = playlistQuotes;
            insertPosition = findFirstPosition(playlistQuotes);
            playlistQuotes.add((PlaylistQuote) object);
            Collections.sort(playlistQuotes);
            if (insertPosition != -1) {
                insertPosition += playlistQuotes.indexOf(object);
            } else {
                insertPosition = findLastPosition(playlistAuthors) + 1;
                if (insertPosition == 0) insertPosition = findLastPosition(playlistCategories) + 1;
            }
        }
        playlistItems.add(insertPosition, object);
        notifyItemInserted(insertPosition);
        if (playlistEntryList.size() == 5) {
            // Insert an add
            insertAdAt(findFirstPosition(playlistEntryList), true);
        }

        // Update user data
        logUserData();
    }

    private int findFirstPosition(List<?> playlistEntities) {
        if (playlistEntities.size() == 0) {
            return -1;
        }
        return playlistItems.indexOf(playlistEntities.get(0));
    }

    private int findLastPosition(List<?> playlistEntities) {
        if (playlistEntities.size() == 0) {
            return -1;
        }
        return playlistItems.indexOf(playlistEntities.get(playlistEntities.size() - 1));
    }

    public void removeItem(Object item) {
        removeItem(playlistItems.indexOf(item));
    }

    private void removeItem(int position) {
        final Object remove = playlistItems.remove(position);
        if (remove instanceof PlaylistCategory) {
            playlistCategories.remove(remove);
        } else if (remove instanceof PlaylistAuthor) {
            playlistAuthors.remove(remove);
        } else if (remove instanceof PlaylistQuote){
            playlistQuotes.remove(remove);
        }
        notifyItemRemoved(position);

        // Update user data
        logUserData();
    }

    private void insertAll() {
        playlistItems.clear();
        playlistItems.addAll(playlistCategories);
        if (playlistCategories.size() > 4) {
            insertAdAt(findFirstPosition(playlistCategories), false);
        }
        playlistItems.addAll(playlistAuthors);
        if (playlistAuthors.size() > 4) {
            insertAdAt(findFirstPosition(playlistAuthors), false);
        }
        playlistItems.addAll(playlistQuotes);
        if (playlistQuotes.size() > 4) {
            insertAdAt(findFirstPosition(playlistQuotes), false);
        }
        if (showSurvey) {
            playlistItems.add(0, new SurveyPlaceholder());
        }
        if (adPlaceholders.size() == 0) {
            // Append to top
            insertAdAt(showSurvey ? 1 : 0, false);
        }
    }

    public void setShowSurvey(boolean showSurvey) {
        if (this.showSurvey == showSurvey) {
            return;
        }
        this.showSurvey = showSurvey;
        if (showSurvey) {
            playlistItems.add(0, new SurveyPlaceholder());
            notifyItemInserted(0);
        } else {
            playlistItems.remove(0);
            notifyItemRemoved(0);
        }
    }

    public void insertAdAt(int position, boolean notify) {
        if (BuildConfig.DEBUG && !BuildConfig.TEST_ADS) {
            return;
        }
        AdPlaceholder adPlaceholder = new AdPlaceholder();
        adPlaceholders.add(adPlaceholder);
        playlistItems.add(position, adPlaceholder);
        if (notify) notifyItemInserted(position);
    }

    public void removeAllAds() {
        playlistItems.removeAll(adPlaceholders);
        adPlaceholders.clear();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof SugarRecord) {
            return 0;
        } else if (item instanceof SurveyPlaceholder) {
            return VIEW_TYPE_SURVEY;
        } else {
            return VIEW_TYPE_AD;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SURVEY) {
            return new SurveyPlaylistViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.survey_playlist_item, null));
        } else if (viewType == VIEW_TYPE_AD) {
            return new AdViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_playlist_item, null));
        } else {
            return new PlaylistViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == VIEW_TYPE_SURVEY) {
            return;
        } else if (itemViewType == VIEW_TYPE_AD) {
            ((AdViewHolder) holder).update();
            return;
        }
        PlaylistViewHolder playlistViewHolder = (PlaylistViewHolder) holder;
        final Object o = getItem(position);
        if (o instanceof PlaylistCategory) {
            PlaylistCategory playlistCategory = (PlaylistCategory) o;
            playlistViewHolder.updateWithCategory(playlistCategory.category);
        } else if (o instanceof PlaylistAuthor) {
            PlaylistAuthor playlistAuthor = (PlaylistAuthor) o;
            playlistViewHolder.updateWithAuthor(playlistAuthor.author);
        } else if (o instanceof PlaylistQuote) {
            PlaylistQuote playlistQuote = (PlaylistQuote) o;
            playlistViewHolder.updateWithQuote(playlistQuote.quote);
        }
    }

    @Override
    public int getItemCount() {
        return playlistItems.size();
    }

    public int getPlaylistItemCount() {
        return playlistItems.size();
    }

    private int getCategoryCount() {
        return playlistCategories.size();
    }

    private int getAuthorCount() {
        return playlistAuthors.size();
    }

    private int getQuoteCount() {
        return playlistQuotes.size();
    }

    private void logUserData() {
        LWQLoggerHelper.get()
                .logCategoryCount(getCategoryCount());
        LWQLoggerHelper.get()
                .logAuthorCount(getAuthorCount());
        LWQLoggerHelper.get()
                .logQuoteCount(getQuoteCount());
    }

    /**
     * Dummy classes to identify where, in the full list,
     * do we insert Ads and Survey, among other things.
     */
    private abstract class PlaylistPlaceholder {}
    private class SurveyPlaceholder extends PlaylistPlaceholder {}
    private class AdPlaceholder extends PlaylistPlaceholder {}

    class SurveyPlaylistViewHolder
            extends RecyclerView.ViewHolder
            implements AdapterView.OnItemClickListener {

        ListPopupWindow listPopupWindow;

        private SurveyPlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick({R.id.iv_survey_playlist_item_more,
                R.id.cv_survey_playlist_item_card})
        void revealPopup2(View view) {
            int popupWidth = view.getResources().getDimensionPixelSize(R.dimen.playlist_popup_width);
            listPopupWindow = new ListPopupWindow(view.getContext());
            listPopupWindow.setAnchorView(view);
            listPopupWindow.setWidth(popupWidth);
            listPopupWindow.setOnItemClickListener(this);
            listPopupWindow.setModal(true);
            String[] options = new String[]{
                    view.getResources().getString(R.string.survey_okay),
                    view.getResources().getString(R.string.survey_later),
                    view.getResources().getString(R.string.survey_never)
            };
            listPopupWindow.setAdapter(new ArrayAdapter<>(view.getContext(),
                    R.layout.support_simple_spinner_dropdown_item, android.R.id.text1, options));
            listPopupWindow.show();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listPopupWindow.dismiss();
            listPopupWindow = null;
            if (!(view instanceof TextView)) {
                return;
            }
            String selectedOption = ((TextView) view).getText().toString();
            if (selectedOption.equalsIgnoreCase(view.getResources().getString(R.string.survey_never))) {
                UserSurveyController.handleResponse(UserSurveyController.RESPONSE_NEVER);
            } else if (selectedOption.equalsIgnoreCase(view.getResources().getString(R.string.survey_later))) {
                UserSurveyController.handleResponse(UserSurveyController.RESPONSE_LATER);
            } else if (selectedOption.equalsIgnoreCase(view.getResources().getString(R.string.survey_okay))) {
                UserSurveyController.handleResponse(UserSurveyController.RESPONSE_OKAY);
            }
            setShowSurvey(false);
        }
    }

    class PlaylistViewHolder
            extends RecyclerView.ViewHolder
            implements AdapterView.OnItemClickListener {

        Object data;
        @Bind(R.id.iv_playlist_item_icon) ImageView icon;
        @Bind(R.id.tv_playlist_item_title) TextView title;
        @Bind(R.id.tv_playlist_item_subtitle) TextView subtitle;
        @Bind(R.id.tv_playlist_item_description) TextView description;
        ListPopupWindow listPopupWindow;

        private PlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithCategory(Category category) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_400));
            data = category;
            icon.setImageResource(R.mipmap.ic_style_white);
            title.setText(category.name);
            subtitle.setText(R.string.category);
            description.setText(LWQApplication.get().getString(R.string.blank_quotes, category.name));
        }

        void updateWithAuthor(Author author) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_600));
            data = author;
            icon.setImageResource(R.mipmap.ic_person_white);
            title.setText(author.name);
            subtitle.setText(R.string.author);
            description.setText(LWQApplication.get().getString(R.string.quotes_by, author.name));
        }

        void updateWithQuote(Quote quote) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_800));
            data = quote;
            icon.setImageResource(R.mipmap.ic_format_quote_white);
            title.setText(quote.author.name);
            subtitle.setText(R.string.quote);
            description.setText(quote.text);
        }

        @OnClick({R.id.iv_playlist_item_more, R.id.cv_playlist_item_card}) void revealPopup(View view) {
            int popupWidth = view.getResources().getDimensionPixelSize(R.dimen.playlist_popup_width);
            listPopupWindow = new ListPopupWindow(view.getContext());
            listPopupWindow.setAnchorView(view);
            listPopupWindow.setWidth(popupWidth);
            listPopupWindow.setOnItemClickListener(this);
            listPopupWindow.setModal(true);
            String [] options = null;
            if (data instanceof Quote) {
                options = view.getResources().getStringArray(R.array.popup_playlist_quote_options);
            } else {
                options = view.getResources().getStringArray(R.array.popup_playlist_options);
            }
            listPopupWindow.setAdapter(new ArrayAdapter<>(view.getContext(),
                    R.layout.support_simple_spinner_dropdown_item, android.R.id.text1, options));
            listPopupWindow.show();
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            listPopupWindow.dismiss();
            listPopupWindow = null;
            if (!(view instanceof TextView)) {
                return;
            }
            TextView textView = (TextView) view;
            if (textView.getText().toString().equalsIgnoreCase("remove")) {
                if (delegate != null) {
                    delegate.onPlaylistItemRemove(PlaylistAdapter.this, getAdapterPosition());
                }
            } else if (textView.getText().toString().equalsIgnoreCase("edit")) {
                if (delegate != null) {
                    delegate.onQuoteEdit(PlaylistAdapter.this, getAdapterPosition());
                }
            } else if (textView.getText().toString().equalsIgnoreCase("make a quotograph")) {
                if (delegate != null) {
                    delegate.onMakeQuotograph(PlaylistAdapter.this, getAdapterPosition());
                }
            }
        }
    }

    class AdViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.prl_playlist_item_ad)
        PercentRelativeLayout nativeAdHolder;

        NativeExpressAdView nativeExpressAdView;

        AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.v("NEAV", "onAdLoaded");
            }

            @Override
            public void onAdOpened() {
                Log.v("NEAV", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                Log.v("NEAV", "onAdLeftApplication");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.v("NEAV", "onAdFailedToLoad: " + i);
            }

            @Override
            public void onAdClosed() {
                Log.v("NEAV", "onAdClosed");
            }
        };

        AdViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            nativeExpressAdView = new NativeExpressAdView(itemView.getContext());
            nativeExpressAdView.setAdListener(adListener);
            nativeAdHolder.addView(nativeExpressAdView, PercentRelativeLayout.LayoutParams.WRAP_CONTENT,
                    PercentRelativeLayout.LayoutParams.WRAP_CONTENT);
            nativeAdHolder.post(new Runnable() {
                @Override
                public void run() {
                    requestAd();
                }
            });
        }

        void requestAd() {
            AdRequest request = new AdRequest.Builder()
                    .addTestDevice(itemView.getContext().getString(R.string.admob_nexus_5x_device_id))
                    .addTestDevice(itemView.getContext().getString(R.string.admob_moto_g_device_id))
                    .build();
            int maxWidthInt = nativeAdHolder.getWidth();
            int maxHeightInt = nativeAdHolder.getHeight();
            maxWidthInt = (int) (maxWidthInt * 1f / itemView.getResources().getDisplayMetrics().density);
            maxHeightInt = (int) (maxHeightInt * 1f / itemView.getResources().getDisplayMetrics().density);
            nativeExpressAdView.setAdSize(new AdSize(maxWidthInt, maxHeightInt));
            nativeExpressAdView.setAdUnitId(itemView.getContext().getString(R.string.admob_playlist_native_small));
            nativeExpressAdView.loadAd(request);
        }

        boolean runOnce = false;

        void update() {
            itemView.setBackgroundColor(
                    Integer.parseInt(
                            FirebaseRemoteConfig.getInstance().getString(
                                    RemoteConfigConst.ADMOB_PLAYLIST_NATIVE_SMALL_BG_COLOR),
                            16)
            );
            if (runOnce) {
                runOnce = false;
                requestAd();
            }
        }
    }
}
