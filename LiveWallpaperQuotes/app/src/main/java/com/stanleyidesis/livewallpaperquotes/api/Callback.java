package com.stanleyidesis.livewallpaperquotes.api;

/**
 * Created by stanleyidesis on 7/28/15.
 */
public interface Callback <Result> {
    public void onSuccess(Result result);
    public void onError(String errorMessage);
}
