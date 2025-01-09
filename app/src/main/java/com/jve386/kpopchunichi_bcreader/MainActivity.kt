package com.jve386.kpopchunichi_bcreader

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewProductName: TextView
    private lateinit var textViewProductPrice: TextView
    private lateinit var buttonLoadTxt: Button
    private lateinit var buttonLoadExcel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asocia los elementos de la interfaz de usuario
        editTextBarcode = findViewById(R.id.editTextBarcode)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewProductName = findViewById(R.id.textViewProductName)
        textViewProductPrice = findViewById(R.id.textViewProductPrice)
        buttonLoadTxt = findViewById(R.id.buttonLoadTxt)
        buttonLoadExcel = findViewById(R.id.buttonLoadExcel)

        // Setea el listener para el botón de búsqueda
        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                FetchProductInfoTask().execute(barcode)  // Llamar a la tarea de búsqueda
            } else {
                textViewProductName.text = "Introduce un código de barras válido."
            }
        }

        // Listener para el botón "Cargar txt"
        buttonLoadTxt.setOnClickListener {
            loadTxt() // Llamada a la función vacía
        }

        // Listener para el botón "Cargar Excel"
        buttonLoadExcel.setOnClickListener {
            loadExcel() // Llamada a la función vacía
        }
    }

    // Función vacía para cargar txt
    private fun loadTxt() {
        // Aquí agregarás el código para cargar un archivo txt
    }

    // Función vacía para cargar Excel
    private fun loadExcel() {
        // Aquí agregarás el código para cargar un archivo Excel
    }

    private inner class FetchProductInfoTask : AsyncTask<String, Void, ProductInfo>() {
        override fun doInBackground(vararg barcodes: String): ProductInfo {
            val barcode = barcodes[0]  // Extrae el código de barras del parámetro

            // Asegúrate de que la URL esté correctamente formada
            val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"

            return try {
                // Realiza la conexión y obtiene el documento HTML
                val document = Jsoup.connect(url).get()

                // Extraer el nombre del producto
                val nameElement = document.selectFirst("div.product-description h3.product-title")
                val productName = nameElement?.text() ?: "Producto no encontrado"


                // Extraer el precio
                val priceElement = document.selectFirst("span.product-price")
                val price = priceElement?.text() ?: "Precio no disponible"

                // Retorna la información del producto
                ProductInfo(productName, price)
            } catch (e: Exception) {
                ProductInfo("Error: ${e.message}",  "")
            }
        }

        override fun onPostExecute(result: ProductInfo) {
            // Actualizar la UI con el nombre y precio
            textViewProductName.text = result.name
            textViewProductPrice.text = result.price
        }
    }

    // Data class para almacenar la información del producto
    data class ProductInfo(val name: String, val price: String)
}
