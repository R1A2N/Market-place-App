package com.example.marketplace.ui.products_list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marketplace.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class ProductsListFragment extends Fragment {

    private static final String TAG = ProductsListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<Product> productList;
    private ProductAdapter productAdapter;
    private Spinner categorySpinner;
    private boolean isBuyer;

    private static final int ALL_CATEGORIES = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_products_list, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        productAdapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(productAdapter);

        categorySpinner = root.findViewById(R.id.categorySpinner);
        SharedPreferences preferences = requireActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        isBuyer = preferences.getBoolean("IS_BUYER", false);

        // Fetch categories dynamically
        ProductApi productApi = getProductApi();
        Call<List<Category>> getCategoriesCall = productApi.getAllCategories();

        getCategoriesCall.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    List<Category> categories = response.body();
                    categories.add(new Category(-1, "All"));

                    // Populate the spinner with dynamic categories
                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categories
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);

                    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            Category selectedCategory = (Category) parentView.getItemAtPosition(position);
                            int selectedCategoryId = selectedCategory.getID();

                            Log.d(TAG, "Selected category ID: " + selectedCategoryId);

                            // Load products based on user type (buyer or seller)
                            new FetchProductsTask(productApi, selectedCategoryId).execute();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            Log.d(TAG, "No category selected");
                            // Do nothing here
                        }
                    });

                } else {
                    Log.e(TAG, "Failed to fetch categories. HTTP Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Error fetching categories: " + t.getMessage());
            }
        });

        // Fetch products initially with all categories
        new FetchProductsTask(productApi, ALL_CATEGORIES).execute();

        return root;
    }

    private ProductApi getProductApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.11.128:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ProductApi.class);
    }

    private class FetchProductsTask extends AsyncTask<Void, Void, List<Product>> {

        private final ProductApi productApi;
        private final int categoryId;

        FetchProductsTask(ProductApi productApi, int categoryId) {
            this.productApi = productApi;
            this.categoryId = categoryId;
        }

        @Override
        protected List<Product> doInBackground(Void... voids) {
            Log.d(TAG, "Fetching products in background...");
            return fetchDataFromApi(categoryId);
        }

        @Override
        protected void onPostExecute(List<Product> products) {
            Log.d(TAG, "Products fetched successfully. Updating UI...");

            productList = products;
            productAdapter.setProducts(productList);
        }

        private List<Product> fetchDataFromApi(int categoryId) {
            List<Product> productList = new ArrayList<>();

            Log.d(TAG, "Fetching products for category ID: " + categoryId);

            // Grab user ID from shared preferences
            int userId = getUserId();

            Call<List<Product>> getProductsCall;
            if (isBuyer) {
                // Load products for the buyer
                if (categoryId == ALL_CATEGORIES) {
                    getProductsCall = productApi.getAllProducts();
                } else {
                    getProductsCall = productApi.getProductsByCategory(categoryId);
                }
            } else {
                // Load products for the seller
                if (categoryId == ALL_CATEGORIES) {
                    getProductsCall = productApi.getProductsForSeller(userId);
                } else {
                    getProductsCall = productApi.getProductsByCategoryAndUser(categoryId, userId);
                }
            }

            try {
                Response<List<Product>> response = getProductsCall.execute();
                Log.d("Response: ",response.toString());
                if (response.isSuccessful()) {
                    productList = response.body();
                } else {
                    Log.e(TAG, "Failed to fetch products. HTTP Code: " + response.code());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching products: " + e.getMessage());
                e.printStackTrace();
            }

            return productList;
        }

        private int getUserId() {
            SharedPreferences preferences = requireActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            return preferences.getInt("ID", -1);
        }
    }

    public interface ProductApi {
        @GET("product/list")
        Call<List<Product>> getAllProducts();

        @GET("product/category/{id}")
        Call<List<Product>> getProductsByCategory(@Path("id") int categoryId);

        @GET("product/list/{categoryId}/{userId}")
        Call<List<Product>> getProductsByCategoryAndUser(
                @Path("categoryId") int categoryId,
                @Path("userId") int userId
        );

        @GET("product/user/{userId}")
        Call<List<Product>> getProductsForSeller(@Path("userId") int userId);

        @GET("category/list")
        Call<List<Category>> getAllCategories();
    }
}
