import com.example.rentr.model.ProductModel

interface ProductRepo {

    fun addProduct(product: ProductModel, callback: (Boolean, String,String?) -> Unit)

    fun updateProduct(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit)

    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit)

    fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit)

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun getAllProductsByCategory(category: String, callback: (Boolean, String, List<ProductModel>?) -> Unit)

    fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun getAllProductsByUser(userId: String, callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun updateAvailability(productId: String, available: Boolean, callback: (Boolean, String) -> Unit)

    fun updateRating(productId: String, userId: String, rating: Int, callback: (Boolean, String) -> Unit)

    fun getFlaggedProducts(callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun updateProductFlags(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit)

    fun flagProduct(
        productId: String,
        userId: String,
        reason: String,
        callback: (Boolean, String) -> Unit
    )

    fun endRental(productId: String, callback: (Boolean, String) -> Unit)
    fun placeRentalRequest(
        productId: String,
        renterId: String,
        days: Int,
        callback: (Boolean, String) -> Unit
    )

    fun cancelRentalRequest(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    )
    fun approveRentalRequest(productId: String, callback: (Boolean, String) -> Unit)
    fun rejectRentalRequest(productId: String, callback: (Boolean, String) -> Unit)
    fun handoverProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    )

    fun requestReturn(
        productId: String,
        renterId: String,
        callback: (Boolean, String) -> Unit
    )

    fun verifyReturn(
        productId: String,
        callback: (Boolean, String, Long) -> Unit
    )
    fun updateRentalStatus(productId: String, status: String, callback: (Boolean, String) -> Unit)
    fun clearFlags(productId: String, callback: (Boolean, String) -> Unit)
}

