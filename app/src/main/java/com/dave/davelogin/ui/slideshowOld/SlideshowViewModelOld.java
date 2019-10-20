package com.dave.davelogin.ui.slideshowOld;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModelOld extends ViewModel {

    private MutableLiveData<String> mText;

    public SlideshowViewModelOld() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}