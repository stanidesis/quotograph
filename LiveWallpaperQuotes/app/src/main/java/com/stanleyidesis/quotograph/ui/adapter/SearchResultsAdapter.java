package com.stanleyidesis.quotograph.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stanleyidesis.quotograph.R;
import com.stanleyidesis.quotograph.RemoteConfigConst;
import com.stanleyidesis.quotograph.api.db.Author;
import com.stanleyidesis.quotograph.api.db.Category;
import com.stanleyidesis.quotograph.api.db.Playlist;
import com.stanleyidesis.quotograph.api.db.PlaylistAuthor;
import com.stanleyidesis.quotograph.api.db.PlaylistCategory;
import com.stanleyidesis.quotograph.api.db.PlaylistQuote;
import com.stanleyidesis.quotograph.api.db.Quote;
import com.stanleyidesis.quotograph.ui.UIUtils;
import com.stanleyidesis.quotograph.ui.view.AddRemoveButton;

import java.util.ArrayList;
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
 * SearchResultsAdapter.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 11/03/2015
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Delegate {
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

    static final private int TYPE_CATEGORY = 0;
    static final private int TYPE_AUTHOR = 1;
    static final private int TYPE_QUOTE = 2;
    static final private int TYPE_AD = 3;

    private class AuthorCouplet {
        Author firstAuthor;
        Author secondAuthor;
    }

    private class AdPlaceholder {}

    private List<Object> searchResults = new ArrayList<>();
    private Delegate delegate;
    private Playlist activePlaylist = Playlist.active();

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
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
        // This area is pretty small, so there's a high likelihood that
        // ads will not render on small devices.
        searchResults.add(0, new AdPlaceholder());
    }

    @Override
    public int getItemViewType(int position) {
        Object item = searchResults.get(position);
        if (item instanceof AdPlaceholder) {
            return TYPE_AD;
        } else if (item instanceof Quote) {
            return TYPE_QUOTE;
        } else if (item instanceof AuthorCouplet) {
            return TYPE_AUTHOR;
        }
        return TYPE_CATEGORY;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_AD:
                return new AdViewHolder(parent.getContext(),
                        RemoteConfigConst.ADMOB_SEARCH_NATIVE_SMALL_BG_COLOR,
                        R.string.admob_search_native_small);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UIUtils.setViewAndChildrenEnabled(holder.itemView, true);
        if (holder instanceof AdViewHolder) {
            ((AdViewHolder) holder).update();
        } else if (holder instanceof CategoryResultHolder) {
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
        @Bind(R.id.add_remove_category_result_item)
        AddRemoveButton categoryAddRemove;

        CategoryResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithCategory(Category category) {
            this.category = category;
            this.playlistCategory = PlaylistCategory.find(activePlaylist, category);
            categoryTextView.setText(category.name);
            categoryAddRemove.setMode(playlistCategory != null);
        }

        @OnClick({R.id.add_remove_category_result_item})
        void addOrRemoveCategory() {
            if (playlistCategory == null) {
                playlistCategory = (PlaylistCategory) delegate.onAdd(SearchResultsAdapter.this, category);
                categoryAddRemove.setMode(true);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistCategory);
                playlistCategory = null;
                categoryAddRemove.setMode(false);
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
        @Bind(R.id.add_remove_author_result_item_first)
        AddRemoveButton firstAuthorAddRemove;
        @Bind(R.id.add_remove_author_result_item_second)
        AddRemoveButton secondAuthorAddRemove;

        AuthorResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithAuthor(final Author firstAuthor, Author secondAuthor) {
            this.firstAuthor = firstAuthor;
            this.firstPlaylistAuthor = PlaylistAuthor.find(activePlaylist, firstAuthor);
            firstAuthorTextView.setText(firstAuthor.name);
            firstAuthorAddRemove.setMode(firstPlaylistAuthor != null);

            if (secondAuthor == null) {
                this.secondAuthor = null;
                secondPlaylistAuthor = null;
                secondAuthorCard.setVisibility(View.GONE);
                return;
            }
            this.secondAuthor = secondAuthor;
            secondPlaylistAuthor = PlaylistAuthor.find(activePlaylist, secondAuthor);
            secondAuthorCard.setVisibility(View.VISIBLE);
            secondAuthorTextView.setText(secondAuthor.name);
            secondAuthorAddRemove.setMode(secondPlaylistAuthor != null);
        }

        @OnClick(R.id.add_remove_author_result_item_first)
        void addOrRemoveFirstAuthor() {
            firstPlaylistAuthor = addOrRemove(firstAuthor, firstPlaylistAuthor, firstAuthorAddRemove);
        }

        @OnClick(R.id.add_remove_author_result_item_second)
        void addOrRemoveSecondAuthor() {
            secondPlaylistAuthor = addOrRemove(secondAuthor, secondPlaylistAuthor, secondAuthorAddRemove);
        }

        PlaylistAuthor addOrRemove(Author author, PlaylistAuthor playlistAuthor, AddRemoveButton addOrRemove) {
            PlaylistAuthor returned = null;
            if (playlistAuthor == null) {
                returned = (PlaylistAuthor) delegate.onAdd(SearchResultsAdapter.this, author);
                addOrRemove.setMode(true);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistAuthor);
                addOrRemove.setMode(false);
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
        @Bind(R.id.add_remove_quote_result_item)
        AddRemoveButton addOrRemoveQuote;

        QuoteResultHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void updateWithQuote(Quote quote) {
            this.quote = quote;
            playlistQuote = PlaylistQuote.find(activePlaylist, quote);
            quoteTextView.setText(quote.text);
            authorTextView.setText(quote.author.name);
            addOrRemoveQuote.setMode(playlistQuote != null);
        }

        @OnClick(R.id.add_remove_quote_result_item)
        void addOrRemoveQuote() {
            if (playlistQuote == null) {
                playlistQuote = (PlaylistQuote) delegate.onAdd(SearchResultsAdapter.this, quote);
                addOrRemoveQuote.setMode(true);
            } else {
                delegate.onRemove(SearchResultsAdapter.this, playlistQuote);
                playlistQuote = null;
                addOrRemoveQuote.setMode(false);
            }
        }
    }

}
