package com.example.rentr.viewmodel

import ProductRepo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.model.ProductModel

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    private val _product = MutableLiveData<ProductModel?>()
    val product: MutableLiveData<ProductModel?>
        get() = _product

    private val _allProducts = MutableLiveData<List<ProductModel>>(emptyList())
    val allProducts: MutableLiveData<List<ProductModel>> = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean>
        get() = _loading

    fun addProduct(product: ProductModel, callback: (Boolean, String,String?) -> Unit) {
        repo.addProduct(product, callback)
    }

    fun updateProduct(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productId, product, callback)
    }

    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productId, callback)
    }

    fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit) {
        _loading.postValue(true)
        repo.getProductById(productId){
            success, msg, data ->
            if(success){
                _product.postValue(data)
            }else{
                _product.postValue(null)
            }
            //****//
            _loading.postValue(false)
            callback(success,msg,data)
        }
    }

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllProducts{
            success, msg, data ->
            if(success){
                _allProducts.postValue(data)
            }else{
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun getAllProductsByCategory(category: String, callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllProductsByCategory(category){
            success, msg, data ->
            if(success){
                _allProducts.postValue(data?: emptyList())
            }else{
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        repo.getAvailableProducts { success, msg, data ->
            if (success) {
                _allProducts.postValue(data)
            } else {
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun getAllProductsByUser(userId: String, callback: (Boolean, String, List<ProductModel>) -> Unit) {
        _loading.postValue(true)
        repo.getAllProductsByUser(userId){
            success, msg, data ->
            if(success){
                _allProducts.postValue(data)
            }else{
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }

    }

    fun updateAvailability(productId: String, available: Boolean, callback: (Boolean, String) -> Unit) {
        repo.updateAvailability(productId, available, callback)
    }

    fun updateQuantity(productId: String, quantity: Int, callback: (Boolean, String) -> Unit) {
        repo.updateQuantity(productId, quantity, callback)
    }

    fun clearProducts() {
        _allProducts.postValue(emptyList())
    }

    class Factory(private val repo: ProductRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(repo) as T
        }
    }

}
