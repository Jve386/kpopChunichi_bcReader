package com.jve386.kpopchunichi_bcreader

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class MainActivity : AppCompatActivity() {
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) { // Agregado el modificador 'override'
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBarcode = findViewById(R.id.editTextBarcode)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewResult = findViewById(R.id.textViewResult)

        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                FetchProductInfoTask().execute(barcode)
            } else {
                textViewResult.text = "Introduce un código de barras válido."
            }
        }
    }

    private inner class FetchProductInfoTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg barcodes: String): String {
            val barcode = barcodes[0]
            val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"
            return try {
                // Conectarse a la URL y obtener el documento HTML
                val document: Document = Jsoup.connect(url).get()

                // Extraer el nombre del producto desde la clase correcta
                val nameElement: Element? =
                    document.selectFirst("div.product-description h3.product-title")
                nameElement?.text() ?: "Producto no encontrado"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            textViewResult.text = result
        }
    }
}
