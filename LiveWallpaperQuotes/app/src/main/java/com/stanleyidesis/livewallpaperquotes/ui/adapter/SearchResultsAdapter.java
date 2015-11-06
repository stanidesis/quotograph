package com.stanleyidesis.livewallpaperquotes.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanleyidesis.livewallpaperquotes.R;
import com.stanleyidesis.livewallpaperquotes.api.db.Author;
import com.stanleyidesis.livewallpaperquotes.api.db.Category;
import com.stanleyidesis.livewallpaperquotes.api.db.Playlist;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistAuthor;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistCategory;
import com.stanleyidesis.livewallpaperquotes.api.db.PlaylistQuote;
import com.stanleyidesis.livewallpaperquotes.api.db.Quote;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Copyright (c) 2015 Stanley Idesis
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
 * SearchResultsAdapter.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 11/03/2015
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.BaseResultHolder> {

    public static interface Delegate {
        /**
         * User removes this from their playlist
         * @param adapter
         * @param playlistItem
         */
        void onRemove(SearchResultsAdapter adapter, Object playlistItem);

        /**
         * User adds this to their playlist
         * @param adapter
         * @param model
         * @return
         */
        Object onAdd(SearchResultsAdapter adapter, Object model);
    }

    static final int TYPE_CATEGORY = 0;
    static final int TYPE_AUTHOR = 1;
    static final int TYPE_QUOTE = 2;

    private class AuthorCouplet {
        Author firstAuthor;
        Author secondAuthor;
    }

    List<Object> searchResults = new ArrayList<>();
    Delegate delegate;


    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public List<Object> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Object> originalSearchResults) {
        this.searchResults.clear();
        for (int i = 0; i < originalSearchResults.size(); i++) {
            final Object o = originalSearchResults.get(i);
            if (o instanceof Category || o instanceof Quote) {
                searchResults.add(o);
            } else {
                AuthorCouplet authorCouplet = new AuthorCouplet();
                authorCouplet.firstAuthor = (Author) o;
                if (i + 1 < originalSearchResults.size() && originalSearchResults.get(i + 1) instanceof Author) {
                    authorCouplet.secondAuthor = (Author) originalSearchResults.get(i + 1);
                    i++;
                }
                searchResults.add(authorCouplet);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (searchResults.get(position) instanceof Quote) {
            return TYPE_QUOTE;
        } else if (searchResults.get(position) instanceof AuthorCouplet) {
            return TYPE_AUTHOR;
        }
        return TYPE_CATEGORY;
    }

    @Override
    public BaseResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_CATEGORY:
                return new CategoryResultHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.category_result_item, null));
            case TYPE_AUTHOR:
                return new AuthorResultHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.author_result_item, null));
            case TYPE_QUOTE:
            default:
                return new QuoteResultHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.quote_result_item, null));
        }
    }

    @Override
    public void onBindViewHolder(BaseResultHolder holder, int position) {
        if (holder instanceof CategoryResultHolder) {
            ((CategoryResultHolder) holder).updateWithCategory((Category) searchResults.get(position));
        } else if (holder instanceof AuthorResultHolder) {
            AuthorCouplet authorCouplet = (AuthorCouplet) searchResults.get(position);
            ((AuthorResultHolder) holder).updateWithAuthor(authorCouplet.firstAuthor, authorCouplet.secondAuthor);
        } else if (holder instanceof QuoteResultHolder) {
            ((QuoteResultHolder) holder).updateWithQuote((Quote) searchResults.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    abstract class BaseResultHolder extends RecyclerView.ViewHolder {

        public BaseResultHolder(View itemView) {
            super(itemView);
        }
    }

    class CategoryResultHolder extends BaseResultHolder {

        Category category;
        PlaylistCategory playlistCategory;

        @Bind(R.id.tv_category_result_item_category)
        TextView categoryTextView;
        @Bind(R.id.iv_category_result_item_add)
        ImageView categoryAddRemove;

        public CategoryResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithCategory(Category category) {
            this.category = category;
            this.playlistCategory = PlaylistCategory.find(Playlist.active(), category);
            categoryTextView.setText(category.name);
            categoryAddRemove.setImageResource(playlistCategory == null ? R.mipmap.ic_add_white : R.mipmap.ic_remove_white);
        }

        @OnClick({R.id.fl_category_result_item_add_wrapper, R.id.iv_category_result_item_add})
        void addOrRemoveCategory() {
            if (playlistCategory == null) {
                playlistCategory = (PlaylistCategory) delegate.onAdd(SearchResultsAdapter.this, category);
                categoryAddRemove.setImageResource(R.mipmap.ic_remove_white);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistCategory);
                playlistCategory = null;
                categoryAddRemove.setImageResource(R.mipmap.ic_add_white);
            }
        }
    }

    class AuthorResultHolder extends BaseResultHolder {

        Author firstAuthor;
        PlaylistAuthor firstPlaylistAuthor;
        Author secondAuthor;
        PlaylistAuthor secondPlaylistAuthor;

        @Bind(R.id.cardview_author_result_second)
        View secondAuthorCard;

        @Bind(R.id.tv_author_result_item_author_first)
        TextView firstAuthorTextView;
        @Bind(R.id.tv_author_result_item_author_second)
        TextView secondAuthorTextView;
        @Bind(R.id.iv_author_result_item_add_first)
        ImageView firstAuthorAddRemove;
        @Bind(R.id.iv_author_result_item_add_second)
        ImageView secondAuthorAddRemove;

        public AuthorResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithAuthor(final Author firstAuthor, Author secondAuthor) {
            this.firstAuthor = firstAuthor;
            this.firstPlaylistAuthor = PlaylistAuthor.find(Playlist.active(), firstAuthor);
            firstAuthorTextView.setText(firstAuthor.name);
            firstAuthorAddRemove.setImageResource(firstPlaylistAuthor == null ? R.mipmap.ic_add_white : R.mipmap.ic_remove_white);

            if (secondAuthor == null) {
                this.secondAuthor = null;
                secondPlaylistAuthor = null;
                secondAuthorCard.setVisibility(View.GONE);
                return;
            }
            this.secondAuthor = secondAuthor;
            secondPlaylistAuthor = PlaylistAuthor.find(Playlist.active(), secondAuthor);
            secondAuthorCard.setVisibility(View.VISIBLE);
            secondAuthorTextView.setText(secondAuthor.name);
            secondAuthorAddRemove.setImageResource(secondPlaylistAuthor == null ? R.mipmap.ic_add_white : R.mipmap.ic_remove_white);
        }

        @OnClick({R.id.fl_author_result_item_add_wrapper_first, R.id.iv_author_result_item_add_first})
        void addOrRemoveFirstAuthor() {
            firstPlaylistAuthor = addOrRemove(firstAuthor, firstPlaylistAuthor, firstAuthorAddRemove);
        }

        @OnClick({R.id.fl_author_result_item_add_wrapper_second, R.id.iv_author_result_item_add_second})
        void addOrRemoveSecondAuthor() {
            secondPlaylistAuthor = addOrRemove(secondAuthor, secondPlaylistAuthor, secondAuthorAddRemove);
        }

        PlaylistAuthor addOrRemove(Author author, PlaylistAuthor playlistAuthor, ImageView addOrRemove) {
            PlaylistAuthor returned = null;
            if (playlistAuthor == null) {
                returned = (PlaylistAuthor) delegate.onAdd(SearchResultsAdapter.this, author);
                addOrRemove.setImageResource(R.mipmap.ic_remove_white);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistAuthor);
                addOrRemove.setImageResource(R.mipmap.ic_add_white);
            }
            return returned;
        }
    }

    class QuoteResultHolder extends BaseResultHolder {

        Quote quote;
        PlaylistQuote playlistQuote;

        @Bind(R.id.tv_quote_result_item_quote)
        TextView quoteTextView;
        @Bind(R.id.tv_quote_result_item_author)
        TextView authorTextView;
        @Bind(R.id.iv_quote_result_item_add)
        ImageView addOrRemoveQuote;

        public QuoteResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithQuote(Quote quote) {
            this.quote = quote;
            playlistQuote = PlaylistQuote.find(Playlist.active(), quote);
            quoteTextView.setText(quote.text);
            authorTextView.setText(quote.author.name);
            addOrRemoveQuote.setImageResource(playlistQuote == null ? R.mipmap.ic_add_white : R.mipmap.ic_remove_white);
        }

        @OnClick({R.id.fl_quote_result_item_add_wrapper, R.id.iv_quote_result_item_add})
        void addOrRemoveQuote() {
            if (playlistQuote == null) {
                playlistQuote = (PlaylistQuote) delegate.onAdd(SearchResultsAdapter.this, quote);
                addOrRemoveQuote.setImageResource(R.mipmap.ic_remove_white);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistQuote);
                playlistQuote = null;
                addOrRemoveQuote.setImageResource(R.mipmap.ic_add_white);
            }
        }
    }
}
