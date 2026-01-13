package com.example.rentr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentr.model.ProductModel
import com.example.rentr.repository.ProductRepo

class ProductViewModel(val repo: ProductRepo) : ViewModel() {

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_PAID = "paid"
        const val STATUS_RENTED = "rented"
        const val STATUS_RETURNING = "returning"
        const val STATUS_RETURNED = "returned"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_COMPLETED = "completed"
    }

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

    fun addProduct(product: ProductModel, callback: (Boolean, String, String?) -> Unit) {
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
        repo.getProductById(productId) { success, msg, data ->
            if (success) {
                _product.postValue(data)
            } else {
                _product.postValue(null)
            }
            _loading.postValue(false)
            callback(success, msg, data)
        }
    }

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllProducts { success, msg, data ->
            if (success) {
                _allProducts.postValue(data)
            } else {
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun getAllProductsByCategory(category: String, callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllProductsByCategory(category) { success, msg, data ->
            if (success) {
                _allProducts.postValue(data ?: emptyList())
            } else {
                _allProducts.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit) {
        _loading.postValue(true)
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
        repo.getAllProductsByUser(userId) { success, msg, data ->
            if (success) {
                _allProducts.postValue(data)
            } else {
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

    fun flagProduct(
        productId: String,
        userId: String,
        reason: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.flagProduct(productId, userId, reason) { success, message ->
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun updateProductFlags(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.updateProductFlags(productId, product) { success, message ->
            if (success) {
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
                val updatedList = _flaggedProducts.value?.filter { it.productId != productId } ?: emptyList()
                _flaggedProducts.postValue(updatedList)
            }
            _loading.postValue(false)
            callback(success, message)
        }
    }


    fun endRental(productId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.endRental(productId) { success, msg ->
            _loading.postValue(false)
            if (success) {
                _product.value?.let {
                    _product.postValue(it.copy(outOfStock = false, rentalStatus = ""))
                }
            }
            callback(success, msg)
        }
    }

    fun placeRentalRequest(
        productId: String,
        renterId: String,
        days: Int,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.placeRentalRequest(productId, renterId, days) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun cancelRentalRequest(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.cancelRentalRequest(productId, renterId) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun approveRentalRequest(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.approveRentalRequest(productId) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun rejectRentalRequest(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.rejectRentalRequest(productId) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun handoverProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.handoverProduct(productId) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun requestReturn(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.requestReturn(productId, renterId) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun verifyReturn(
        productId: String,
        callback: (Boolean, String, Long) -> Unit
    ) {
        _loading.postValue(true)
        repo.verifyReturn(productId) { success, message, returnTime ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message, returnTime)
        }
    }

    fun updateRentalStatus(productId: String, status: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.updateRentalStatus(productId, status) { success, message ->
            _loading.postValue(false)
            if (success) {
                getProductById(productId) { _, _, _ -> }
            }
            callback(success, message)
        }
    }

    fun completeCheckout(
        productId: String,
        pickupLocation: String,
        paymentMethod: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        getProductById(productId) { success, message, product ->
            if (success && product != null) {
                val updatedProduct = product.copy(
                    rentalStatus = STATUS_RENTED,
                    pickupLocation = pickupLocation,
                    paymentMethod = paymentMethod,
                    rentalStartDate = System.currentTimeMillis(),
                    rentalEndDate = System.currentTimeMillis() + (product.rentalDays * 24 * 60 * 60 * 1000L),
                    availability = false,
                    outOfStock = true
                )
                updateProduct(productId, updatedProduct, callback)
            } else {
                _loading.postValue(false)
                callback(false, "Product not found: $message")
            }
        }
    }

    fun markProductForReview(productId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.markProductForReview(productId) { success, message ->
            if (success) {
                // Update product in state
                getProductById(productId) { _, _, _ -> }
                // Also update flagged products list
                getFlaggedProducts { _, _, _ -> }
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
                // Clear all flag data but DON'T decrement user flag count
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

    class Factory(private val repo: ProductRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                return ProductViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}