package com.stanleyidesis.quotograph.ui.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanleyidesis.quotograph.LWQApplication;
import com.stanleyidesis.quotograph.R;
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

    final int VIEW_TYPE_PLAYLIST = 0;
    final int VIEW_TYPE_SURVEY = 1;

    public interface Delegate {
        void onPlaylistItemRemove(PlaylistAdapter adapter, int position);
        void onQuoteEdit(PlaylistAdapter adapter, int position);
        void onMakeQuotograph(PlaylistAdapter adapter, int position);
    }

    Delegate delegate;
    List<Object> playlistItems;
    List<PlaylistCategory> playlistCategories;
    List<PlaylistAuthor> playlistAuthors;
    List<PlaylistQuote> playlistQuotes;
    boolean showSurvey;
    int surveyOffset;

    public PlaylistAdapter() {
        this(null);
    }

    public PlaylistAdapter(Delegate delegate) {
        this.delegate = delegate;
        final Playlist activePlaylist = Playlist.active();
        playlistCategories = PlaylistCategory.forPlaylist(activePlaylist);
        playlistAuthors = PlaylistAuthor.forPlaylist(activePlaylist);
        playlistQuotes = PlaylistQuote.forPlaylist(activePlaylist);
        Collections.sort(playlistCategories);
        Collections.sort(playlistAuthors);
        Collections.sort(playlistQuotes);
        playlistItems = new ArrayList<>();
        playlistItems.addAll(playlistCategories);
        playlistItems.addAll(playlistAuthors);
        playlistItems.addAll(playlistQuotes);

        // Log user data
        logUserData();
    }

    public Object getItem(int position) {
        if (getItemViewType(position) == VIEW_TYPE_SURVEY) {
            return null;
        }
        return playlistItems.get(position - surveyOffset);
    }

    public void insertItem(Object object) {
        if (object instanceof PlaylistCategory) {
            playlistCategories.add((PlaylistCategory) object);
            Collections.sort(playlistCategories);
        } else if (object instanceof PlaylistAuthor) {
            playlistAuthors.add((PlaylistAuthor) object);
            Collections.sort(playlistAuthors);
        } else {
            playlistQuotes.add((PlaylistQuote) object);
            Collections.sort(playlistQuotes);
        }
        playlistItems.clear();
        playlistItems.addAll(playlistCategories);
        playlistItems.addAll(playlistAuthors);
        playlistItems.addAll(playlistQuotes);
        notifyItemInserted(playlistItems.indexOf(object));

        // Update user data
        logUserData();
    }

    public void removeItem(Object item) {
        final int position = playlistItems.indexOf(item);
        if (position > -1) {
            removeItem(position + surveyOffset);
        }
    }

    public void removeItem(int position) {
        final Object remove = playlistItems.remove(position - surveyOffset);
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

    public void setShowSurvey(boolean showSurvey) {
        if (this.showSurvey == showSurvey) {
            return;
        }
        this.showSurvey = showSurvey;
        surveyOffset = showSurvey ? 1 : 0;
        if (showSurvey) {
            notifyItemInserted(0);
        } else {
            notifyItemRemoved(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return showSurvey && position == 0 ?
                VIEW_TYPE_SURVEY : VIEW_TYPE_PLAYLIST;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SURVEY) {
            return new SurveyPlaylistViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.survey_playlist_item, null));
        } else {
            return new PlaylistViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SURVEY) {
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
        return playlistItems.size() + surveyOffset;
    }

    public int getPlaylistItemCount() {
        return playlistItems.size();
    }

    public int getCategoryCount() {
        return playlistCategories.size();
    }

    public int getAuthorCount() {
        return playlistAuthors.size();
    }

    public int getQuoteCount() {
        return playlistQuotes.size();
    }

    public void logUserData() {
        LWQLoggerHelper.get()
                .logCategoryCount(getCategoryCount());
        LWQLoggerHelper.get()
                .logAuthorCount(getAuthorCount());
        LWQLoggerHelper.get()
                .logQuoteCount(getQuoteCount());
    }

    class SurveyPlaylistViewHolder
            extends RecyclerView.ViewHolder
            implements AdapterView.OnItemClickListener {

        ListPopupWindow listPopupWindow;

        public SurveyPlaylistViewHolder(View itemView) {
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

        public PlaylistViewHolder(View itemView) {
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
}
