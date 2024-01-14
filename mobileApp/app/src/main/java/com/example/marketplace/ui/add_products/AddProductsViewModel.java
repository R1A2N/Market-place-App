package com.example.marketplace.ui.add_products;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddProductsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AddProductsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is add Products fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}