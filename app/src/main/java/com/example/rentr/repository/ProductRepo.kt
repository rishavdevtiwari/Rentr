import com.example.rentr.model.ProductModel

interface ProductRepo {

    fun addProduct(product: ProductModel, callback: (Boolean, String,String?) -> Unit)

    fun updateProduct(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit)

    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit)

    fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit)

    fun getAllProducts(callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun getAllProductsByCategory(category: String, callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun getAvailableProducts(callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun getAllProductsByUser(userId: String, callback: (Boolean, String, List<ProductModel>) -> Unit)

    fun updateAvailability(productId: String, available: Boolean, callback: (Boolean, String) -> Unit)

    fun updateQuantity(productId: String, quantity: Int, callback: (Boolean, String) -> Unit)

}
