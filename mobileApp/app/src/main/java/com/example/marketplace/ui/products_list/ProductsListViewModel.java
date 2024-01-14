package com.example.marketplace.ui.products_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductsListViewModel extends ViewModel {

    private MutableLiveData<String> productsList;

    public ProductsListViewModel() {
        productsList = new MutableLiveData<>();
        // Initialize or fetch your products list data here
        // For now, let's provide a sample string
        productsList.setValue("Product 1\nProduct 2\nProduct 3");
    }

    public LiveData<String> getProductsList() {
        return productsList;
    }
}
