package com.jve386.kpopchunichi_bcreader

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class MainActivity : AppCompatActivity() {
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewProductName: TextView
    private lateinit var imageViewProduct: ImageView
    private lateinit var textViewProductPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asocia los elementos de la interfaz de usuario
        editTextBarcode = findViewById(R.id.editTextBarcode)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewProductName = findViewById(R.id.textViewProductName)
        imageViewProduct = findViewById(R.id.imageViewProduct)
        textViewProductPrice = findViewById(R.id.textViewProductPrice)

        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                FetchProductInfoTask().execute(barcode)  // Llamar a la tarea de búsqueda
            } else {
                textViewProductName.text = "Introduce un código de barras válido."
            }
        }
    }

    private inner class FetchProductInfoTask : AsyncTask<String, Void, ProductInfo>() {
        override fun doInBackground(vararg barcodes: String): ProductInfo {
            val barcode = barcodes[0]  // Extrae el código de barras del parámetro

            // Asegúrate de que la URL esté correctamente formada
            val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"

            return try {
                // Realiza la conexión y obtiene el documento HTML
                val document: Document = Jsoup.connect(url).get()

                // Extraer el nombre del producto
                val nameElement: Element? = document.selectFirst("div.product-description h3.product-title")
                val productName = nameElement?.text() ?: "Producto no encontrado"  // Si no hay nombre, mostrar mensaje predeterminado

                // Extraer la imagen del producto
                val imageElement: Element? = document.selectFirst("#product-images-large img")
                Log.d("ProductInfo", "Elementos encontrados: ${document.select("div#product-images-large img").size}")
                // Verifica que el 'src' es relativo o absoluto
                val imageUrl = imageElement?.attr("src")?.let {
                    if (it.startsWith("/")) "https://www.chunichicomics.com$it" else it
                } ?: ""
                Log.d("ProductInfo", "Imagen URL: $imageUrl")  // Aquí puedes comprobar el valor de la URL

                // Extraer el precio
                val priceElement: Element? = document.selectFirst("span.product-price")
                val price = priceElement?.text() ?: "Precio no disponible"  // Si no hay precio, mostrar mensaje predeterminado

                // Retorna la información del producto
                ProductInfo(productName, imageUrl, price)
            } catch (e: Exception) {
                Log.e("ProductInfo", "Error al obtener datos: ${e.message}")
                // En caso de error, devuelve un objeto vacío o con un mensaje de error
                ProductInfo("Error: ${e.message}", "", "")
            }
        }

        override fun onPostExecute(result: ProductInfo) {
            // Depuración: Mostrar la URL de la imagen obtenida
            Log.d("ProductInfo", "Imagen URL: ${result.imageUrl}")

            // Actualiza la interfaz de usuario con la información obtenida
            textViewProductName.text = result.name
            textViewProductPrice.text = result.price

            // Si la URL de la imagen no está vacía, carga la imagen con Glide
            if (result.imageUrl.isNotEmpty()) {
                Glide.with(this@MainActivity)
                    .load(result.imageUrl)
                    .centerCrop()  // Ajuste de la imagen para que ocupe toda la vista de manera proporcional
                    .into(imageViewProduct)
            } else {
                // Si no hay URL de imagen, elimina la imagen en la vista
                imageViewProduct.setImageDrawable(null)
            }
        }
    }

    // Data class para almacenar la información del producto
    data class ProductInfo(val name: String, val imageUrl: String, val price: String)
}
