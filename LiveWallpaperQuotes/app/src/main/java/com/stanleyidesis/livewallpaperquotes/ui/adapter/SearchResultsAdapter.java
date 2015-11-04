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

    static final int TYPE_CATEGORY = 0;
    static final int TYPE_AUTHOR = 1;
    static final int TYPE_QUOTE = 2;

    List<Object> searchResults = new ArrayList<>();
    int authorIndex = -1;
    int authorCount = 0;
    int quoteIndex = -1;
    int quoteCount = 0;

    public List<Object> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Object> searchResults) {
        this.authorCount = 0;
        this.authorIndex = -1;
        this.quoteCount = 0;
        this.quoteIndex = -1;
        this.searchResults = searchResults;
        for (Object object : searchResults) {
            if (object instanceof Author) {
                authorCount++;
                if (authorIndex == -1) {
                    authorIndex = searchResults.indexOf(object);
                }
            } else if (object instanceof Quote) {
                quoteCount++;
                if (quoteIndex == -1) {
                    quoteIndex = searchResults.indexOf(object);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= quoteIndex && quoteIndex > -1) {
            return TYPE_QUOTE;
        } else if (position >= authorIndex && authorIndex > -1) {
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
            int authorPosition = (position - authorIndex) * 2;
            final Object o = searchResults.get(authorIndex + authorPosition);
            Author firstAuthor = null;
            Author secondAuthor = null;
            if (o instanceof Author) {
                firstAuthor = (Author) o;
            }
            int secondAuthorPosition = authorIndex + authorPosition + 1;
            if (searchResults.size() > secondAuthorPosition && searchResults.get(secondAuthorPosition) instanceof Author) {
                secondAuthor = (Author) searchResults.get(secondAuthorPosition);
            }
            ((AuthorResultHolder) holder).updateWithAuthor(firstAuthor, secondAuthor);
        } else if (holder instanceof QuoteResultHolder) {
            int quotePosition = position;
            if (authorIndex > -1) {
                quotePosition += (int) Math.floor(authorCount / 2);
            }
            ((QuoteResultHolder) holder).updateWithQuote((Quote) searchResults.get(quotePosition));
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size() - (int) Math.floor(authorCount / 2);
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
                playlistCategory = new PlaylistCategory(Playlist.active(), category);
                playlistCategory.save();
                categoryAddRemove.setImageResource(R.mipmap.ic_remove_white);
            } else {
                playlistCategory.delete();
                playlistCategory = null;
            }
            // TODO broadcast this info?
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

        void updateWithAuthor(Author firstAuthor, Author secondAuthor) {
            this.firstAuthor = firstAuthor;
            this.firstPlaylistAuthor = PlaylistAuthor.find(Playlist.active(), firstAuthor);
            firstAuthorTextView.setText(firstAuthor.name);
            firstAuthorAddRemove.setImageResource(firstPlaylistAuthor == null ? R.mipmap.ic_add_white : R.mipmap.ic_remove_white);

            if (secondAuthor == null) {
                secondAuthor = null;
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
            if (author == null) {
                returned = new PlaylistAuthor(Playlist.active(), firstAuthor);
                returned.save();
                addOrRemove.setImageResource(R.mipmap.ic_remove_white);
            } else {
                playlistAuthor.delete();
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
                playlistQuote = new PlaylistQuote(Playlist.active(), quote);
                playlistQuote.save();
                addOrRemoveQuote.setImageResource(R.mipmap.ic_remove_white);
            } else {
                playlistQuote.delete();
                playlistQuote = null;
                addOrRemoveQuote.setImageResource(R.mipmap.ic_add_white);
            }
        }
    }
}
