package com.example.rentr.viewmodel

import ProductRepo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rentr.model.ProductModel

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    private val _product = MutableLiveData<ProductModel>()
    val product: MutableLiveData<ProductModel>
        get() = _product

    private val _allProducts = MutableLiveData<List<ProductModel>?>()
    val allProducts: MutableLiveData<List<ProductModel>?>
        get() = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    fun addProduct(product: ProductModel, callback: (Boolean, String, String) -> Unit) {
        repo.addProduct(product, callback)
    }

    fun addProductToDatabase(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProductToDatabase(productId, product, callback)
    }

    fun updateProduct(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productId, product, callback)
    }

    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productId, callback)
    }

    fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit) {
        repo.getProductById(productId, callback)
    }

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        repo.getAllProducts(callback)
    }

    fun getAllProductsByCategory(category: String, callback: (Boolean, String, List<ProductModel>) -> Unit) {
        repo.getAllProductsByCategory(category, callback)
    }

    fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        repo.getAvailableProducts(callback)
    }

    fun getAllProductsByUser(userId: String, callback: (Boolean, String, List<ProductModel>) -> Unit) {
        repo.getAllProductsByUser(userId, callback)
    }

    fun updateAvailability(productId: String, available: Boolean, callback: (Boolean, String) -> Unit) {
        repo.updateAvailability(productId, available, callback)
    }

    fun updateQuantity(productId: String, quantity: Int, callback: (Boolean, String) -> Unit) {
        repo.updateQuantity(productId, quantity, callback)
    }

}
