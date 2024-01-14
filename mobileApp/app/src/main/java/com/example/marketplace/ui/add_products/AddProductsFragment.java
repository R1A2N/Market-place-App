package com.example.marketplace.ui.add_products;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.marketplace.R;
import com.example.marketplace.databinding.FragmentAddProductsBinding;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class AddProductsFragment extends Fragment {

    private FragmentAddProductsBinding binding;
    private EditText editTextProductName;
    private EditText editTextProductDescription;
    private EditText editTextProductPrice;
    private EditText editTextProductPhone;
    private Button buttonChooseImage;
    private ImageView imageViewSelected;
    private Button buttonSubmitProduct;
    private Spinner spinnerProductCategory;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private ProductApi productApi;

    private List<Category> categoriesList;
    private ArrayAdapter<Category> categoryAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AddProductsViewModel addProductsViewModel =
                new ViewModelProvider(this).get(AddProductsViewModel.class);

        binding = FragmentAddProductsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextProductName = root.findViewById(R.id.editTextProductName);
        editTextProductDescription = root.findViewById(R.id.editTextProductDescription);
        editTextProductPrice = root.findViewById(R.id.editTextProductPrice);
        editTextProductPhone = root.findViewById(R.id.editTextProductPhone);
        buttonChooseImage = root.findViewById(R.id.buttonChooseImage);
        imageViewSelected = root.findViewById(R.id.imageViewSelected);
        buttonSubmitProduct = root.findViewById(R.id.buttonSubmitProduct);
        spinnerProductCategory = root.findViewById(R.id.spinnerProductCategory);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.11.128:8080/")
                .client(new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        productApi = retrofit.create(ProductApi.class);

        setupCategorySpinner();

        buttonChooseImage.setOnClickListener(view -> openImagePicker());

        buttonSubmitProduct.setOnClickListener(view -> {
            String productName = editTextProductName.getText().toString();
            String productDescription = editTextProductDescription.getText().toString();
            String phone = editTextProductPhone.getText().toString();
            double productPrice = Double.parseDouble(editTextProductPrice.getText().toString());
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            int sellerId = preferences.getInt("ID", 0);

            Category selectedCategory = (Category) spinnerProductCategory.getSelectedItem();
            int categoryId = selectedCategory.getId();

            ProductRequestBody requestBody = new ProductRequestBody(productName, productDescription,
                    phone, productPrice, sellerId, categoryId);
            Log.d("json:", new Gson().toJson(requestBody));


            createProduct(requestBody);
        });

        return root;
    }

    private void setupCategorySpinner() {
        fetchCategories();

        if (categoriesList != null) {
            categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoriesList);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProductCategory.setAdapter(categoryAdapter);
        } else {
             Toast.makeText(requireContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCategories() {
        Call<List<Category>> getCategoriesCall = productApi.getCategories();

        getCategoriesCall.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                Log.e("FetchCategories", " Response: " + response.toString());
                if (response.isSuccessful()) {
                    categoriesList = response.body();

                    for (Category category : categoriesList) {
                        Log.d("CategoryResponse", "ID: " + category.getId() + ", Name: " + category.getName());
                    }

                    if (categoryAdapter == null) {
                           categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoriesList);
                        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProductCategory.setAdapter(categoryAdapter);
                    } else {
                           categoryAdapter.clear();
                        categoryAdapter.addAll(categoriesList);
                        categoryAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("FetchCategories", "Failed to fetch categories. Response: " + response.toString());
                    Toast.makeText(requireContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d("image:", String.valueOf(imageUri));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imageViewSelected.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createProduct(ProductRequestBody requestBody) {
        Call<JsonObject> createProductCall = productApi.createProduct(RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(requestBody)));
        createProductCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Product created successfully", Toast.LENGTH_SHORT).show();
            if (response.body() != null && response.body().has("id")) {
                        int productId = response.body().get("id").getAsInt();

                        // Upload the image

                            uploadProductImage(productId, imageUri);

                    }

                       } else {
                    Toast.makeText(requireContext(), "Product creation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Product creation failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProductImage(int productId, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "product_image.jpg", requestFile);

            Call<JsonObject> uploadImageCall = productApi.uploadProductImage(productId, imagePart);
            uploadImageCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        // Image uploaded successfully
                        Log.d("UploadImage", "Image uploaded successfully");
                    } else {
                        Log.e("UploadImage", "Failed to upload image. Response: " + response.toString());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    t.printStackTrace();
                    Log.e("UploadImage", "Failed to upload image");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("UploadImage", "IOException: " + e.getMessage());
        }
    }

    interface ProductApi {
        @POST("product/create")
        Call<JsonObject> createProduct(@Body RequestBody body);

        @Multipart
        @POST("product/upload-image/{id}")
        Call<JsonObject> uploadProductImage(@Path("id") int productId, @Part MultipartBody.Part image);

        @GET("category/list")
        Call<List<Category>> getCategories();
    }

    static class ProductRequestBody {
        private final String name;
        private final String desc;
        private final String phone;
        private String image;
        private final double price;
        private final int seller_id;
        private final int category_id;
        private boolean is_available;

        ProductRequestBody(String name, String description, String phone, double price, int seller_id, int category_id) {
            this.name = name;
            this.desc = description;
            this.phone = phone;
            this.price = price;
            this.seller_id = seller_id;
            this.category_id = category_id;

            this.is_available = true;
        }
    }

    static class Category {
        private int ID;
        private String name;

        public int getId() {
            return ID;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
