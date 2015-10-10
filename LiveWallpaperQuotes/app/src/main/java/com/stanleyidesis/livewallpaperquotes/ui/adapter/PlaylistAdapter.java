package com.stanleyidesis.livewallpaperquotes.ui.adapter;

import android.support.v7.widget.CardView;
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
 * PlaylistAdapter.java
 * @author Stanley Idesis
 *
 * From Live-Wallpaper-Quotes
 * https://github.com/stanidesis/live-wallpaper-quotes
 *
 * Please report any issues
 * https://github.com/stanidesis/live-wallpaper-quotes/issues
 *
 * Date: 10/05/2015
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    List<Object> playlistItems;

    public PlaylistAdapter() {
        final Playlist activePlaylist = Playlist.active();
        playlistItems = new ArrayList<>();
//        playlistItems.addAll(PlaylistCategory.forPlaylist(activePlaylist));
//        playlistItems.addAll(PlaylistAuthor.forPlaylist(activePlaylist));
//        playlistItems.addAll(PlaylistQuote.forPlaylist(activePlaylist));
        // TODO REMOVE
        for (Category category : Category.listAll(Category.class)) {
            playlistItems.add(new PlaylistCategory(activePlaylist, category));
            if (playlistItems.size() == 5) {
                break;
            }
        }
        for (Author author : Author.listAll(Author.class)) {
            playlistItems.add(new PlaylistAuthor(activePlaylist, author));
            if (playlistItems.size() == 10) {
                break;
            }
        }
        for (Quote quote : Quote.listAll(Quote.class)) {
            playlistItems.add(new PlaylistQuote(activePlaylist, quote));
            if (playlistItems.size() == 15) {
                break;
            }
        }
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlaylistViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, null));
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
        final Object o = playlistItems.get(position);
        if (o instanceof PlaylistCategory) {
            PlaylistCategory playlistCategory = (PlaylistCategory) o;
            holder.updateWithCategory(playlistCategory.category);
        } else if (o instanceof PlaylistAuthor) {
            PlaylistAuthor playlistAuthor = (PlaylistAuthor) o;
            holder.updateWithAuthor(playlistAuthor.author);
        } else if (o instanceof PlaylistQuote) {
            PlaylistQuote playlistQuote = (PlaylistQuote) o;
            holder.updateWithQuote(playlistQuote.quote);
        }
    }

    @Override
    public int getItemCount() {
        return playlistItems.size();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Object data;
        ImageView icon;
        TextView title;
        TextView subtitle;
        TextView description;
        View moreButton;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.iv_playlist_item_icon);
            title = (TextView) itemView.findViewById(R.id.tv_playlist_item_title);
            subtitle = (TextView) itemView.findViewById(R.id.tv_playlist_item_subtitle);
            description = (TextView) itemView.findViewById(R.id.tv_playlist_item_description);
            moreButton = itemView.findViewById(R.id.iv_playlist_item_more);
            moreButton.setOnClickListener(this);
        }

        void updateWithCategory(Category category) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_400));
            data = category;
            icon.setImageResource(R.mipmap.ic_style_white);
            title.setText(category.name);
            subtitle.setText("Category");
            description.setText("Recovers quotes related to \"" + category.name + "\"");
        }

        void updateWithAuthor(Author author) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_600));
            data = author;
            icon.setImageResource(R.mipmap.ic_person_white);
            title.setText(author.name);
            subtitle.setText("Author");
            description.setText("Recovers quotes authored by " + author.name);
        }

        void updateWithQuote(Quote quote) {
            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.palette_800));
            data = quote;
            icon.setImageResource(R.mipmap.ic_format_quote_white);
            title.setText(quote.author.name);
            subtitle.setText("Quote");
            description.setText(quote.text);
        }

        @Override
        public void onClick(View view) {
            // TODO
        }
    }
}
