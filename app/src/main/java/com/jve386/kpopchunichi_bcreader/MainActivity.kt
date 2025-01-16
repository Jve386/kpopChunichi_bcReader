package com.jve386.kpopchunichi_bcreader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity : AppCompatActivity() {
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewProductName: TextView
    private lateinit var textViewProductPrice: TextView
    private lateinit var buttonLoadTxt: Button
    private lateinit var buttonLoadExcel: Button
    private lateinit var buttonScanBarcode: Button

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
        buttonScanBarcode = findViewById(R.id.buttonScanBarcode)

        // Setea el listener para el botón de escaneo de códigos de barras
        buttonScanBarcode.setOnClickListener {
            startBarcodeScanner()
        }

        // Setea el listener para el botón de búsqueda
        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                fetchProductInfo(barcode)  // Llamar a la tarea de búsqueda
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

    private fun fetchProductInfo(barcode: String) {
        lifecycleScope.launch {
            val productInfo = withContext(Dispatchers.IO) {
                try {
                    val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"
                    val document = Jsoup.connect(url).get()

                    val nameElement = document.selectFirst("div.product-description h3.product-title")
                    val productName = nameElement?.text() ?: "Producto no encontrado"

                    val priceElement = document.selectFirst("span.product-price")
                    val price = priceElement?.text() ?: "Precio no disponible"

                    ProductInfo(productName, price)
                } catch (e: Exception) {
                    // Provide a more detailed error message
                    ProductInfo("Error: ${e.message ?: "Unknown error"}", "")
                }
            }

            // Update UI with the result
            textViewProductName.text = productInfo.name
            textViewProductPrice.text = productInfo.price
        }
    }


    // Data class para almacenar la información del producto
    data class ProductInfo(val name: String, val price: String)

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            editTextBarcode.setText(result.contents)
            fetchProductInfo(result.contents)  // Buscar el producto escaneado
        } else {
            textViewProductName.text = "Escaneo cancelado."
        }
    }

    private fun startBarcodeScanner() {
        val options = ScanOptions().apply {
            setPrompt("Escanea el código de barras")
            setBeepEnabled(true)
            setOrientationLocked(false)  // Asegura que siga la orientación del dispositivo
            setCaptureActivity(PortraitCaptureActivity::class.java)  // Forzar vertical
            setBarcodeImageEnabled(true)
        }
        barcodeLauncher.launch(options)
    }
}
