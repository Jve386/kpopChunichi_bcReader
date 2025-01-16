package com.jve386.kpopchunichi_bcreader.data

import org.jsoup.Jsoup
import com.jve386.kpopchunichi_bcreader.model.ProductInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductService {

    suspend fun fetchProductInfo(barcode: String): ProductInfo {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect("https://www.chunichicomics.com/busqueda?controller=search&s=$barcode").get()
                val productName = document.selectFirst("div.product-description h3.product-title")?.text() ?: "Producto no encontrado"
                val price = document.selectFirst("span.product-price")?.text() ?: "Precio no disponible"
                val imageUrl = document.selectFirst("img.js-lazy-product-image")?.attr("data-src") ?: ""
                val baseUrl = "https://www.chunichicomics.com"
                val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl else "$baseUrl$imageUrl"

                ProductInfo(productName, price, fullImageUrl)
            } catch (e: Exception) {
                ProductInfo("Error: ${e.message ?: "Error desconocido"}", "", "")
            }
        }
    }
}