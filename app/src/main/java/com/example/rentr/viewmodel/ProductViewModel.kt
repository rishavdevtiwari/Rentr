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

    private val _flaggedProducts = MutableLiveData<List<ProductModel>>(emptyList())
    val flaggedProducts: MutableLiveData<List<ProductModel>> = _flaggedProducts

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

    fun updateProductVerification(productId: String, verified: Boolean, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.getProductById(productId) { success, msg, product ->
            if (success && product != null) {
                repo.updateProduct(productId, product.copy(verified = verified)) { updateSuccess, updateMsg ->
                    _loading.postValue(false)
                    if (updateSuccess) {
                        // Update local state if needed
                        product?.let {
                            _product.postValue(it.copy(verified = verified))
                        }
                    }
                    callback(updateSuccess, updateMsg)
                }
            } else {
                _loading.postValue(false)
                callback(false, "Product not found")
            }
        }
    }

    fun updateAvailability(productId: String, available: Boolean, callback: (Boolean, String) -> Unit) {
        repo.updateAvailability(productId, available, callback)
    }

    fun updateRating(productId: String, userId: String, rating: Int, callback: (Boolean, String) -> Unit) {
        repo.updateRating(productId, userId, rating, callback)
    }

    fun clearProducts() {
        _allProducts.postValue(emptyList())
    }

    fun getFlaggedProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        _loading.postValue(true)
        repo.getFlaggedProducts { success, message, products ->
            if (success) {
                _flaggedProducts.postValue(products)
            } else {
                _flaggedProducts.postValue(emptyList())
            }
            _loading.postValue(false)
            callback(success, message, products)
        }
    }

    fun updateProductFlags(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.updateProductFlags(productId, product) { success, message ->
            if (success) {
                // Update the specific product in the list
                val updatedList = _flaggedProducts.value?.toMutableList() ?: mutableListOf()
                val index = updatedList.indexOfFirst { it.productId == productId }
                if (index != -1) {
                    updatedList[index] = product
                    _flaggedProducts.postValue(updatedList)
                }
            }
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun clearProductFlags(productId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.clearFlags(productId) { success, message ->
            if (success) {
                // Remove from flagged products list
                val updatedList = _flaggedProducts.value?.filter { it.productId != productId } ?: emptyList()
                _flaggedProducts.postValue(updatedList)
            }
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun resolveFlaggedProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        getProductById(productId) { success, message, product ->
            if (success && product != null) {
                val updatedProduct = product.copy(
                    flagged = false,
                    flaggedBy = emptyList(),
                    flaggedReason = emptyList(),
                    appealReason = "",
                    availability = true
                )
                updateProductFlags(productId, updatedProduct, callback)
            } else {
                callback(false, "Product not found: $message")
            }
        }
    }

    fun markProductForReview(productId: String, callback: (Boolean, String) -> Unit) {
        getProductById(productId) { success, message, product ->
            if (success && product != null) {
                val updatedProduct = product.copy(
                    availability = false,
                    flagged = true
                )
                updateProductFlags(productId, updatedProduct, callback)
            } else {
                callback(false, "Product not found: $message")
            }
        }
    }


    class Factory(private val repo: ProductRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(repo) as T
        }
    }

}