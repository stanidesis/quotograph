package com.stanleyidesis.livewallpaperquotes.api.network;

/**
 * Created by stanleyidesis on 7/27/15.
 */
public interface Callback<Return> {
    public void onSuccess(Return returnValue);
    public void onError(String errorMessage);
}
