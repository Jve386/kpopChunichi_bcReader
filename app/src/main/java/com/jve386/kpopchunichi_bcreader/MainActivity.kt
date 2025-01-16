package com.jve386.kpopchunichi_bcreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
        buttonScanBarcode = findViewById(R.id.buttonScanBarcode)

        // Listener para escanear código de barras
        buttonScanBarcode.setOnClickListener {
            startBarcodeScanner()
        }

        // Listener para buscar producto manualmente
        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                fetchProductInfo(barcode)
            } else {
                textViewProductName.text = "Introduce un código de barras válido."
            }
        }

        // Listener para cargar códigos manualmente
        buttonLoadTxt.setOnClickListener {
            loadTxt()
        }
    }

    /**
     * Lanzador para la actividad de ingreso de códigos de barras manualmente
     */
    private val barcodeInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val barcodes = data?.getStringExtra("BARCODES")
            if (!barcodes.isNullOrEmpty()) {
                val barcodeList = barcodes.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                barcodeList.forEach { barcode ->
                    fetchProductInfo(barcode)
                }
            }
        }
    }

    /**
     * Abre la pantalla para ingresar códigos manualmente
     */
    private fun loadTxt() {
        val intent = Intent(this, BarcodeInputActivity::class.java)
        barcodeInputLauncher.launch(intent)
    }

    /**
     * Consulta la información de un producto a partir de un código de barras
     */
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
                    ProductInfo("Error: ${e.message ?: "Error desconocido"}", "")
                }
            }

            // Actualiza la interfaz con la información del producto
            textViewProductName.text = productInfo.name
            textViewProductPrice.text = productInfo.price
        }
    }

    /**
     * Data class para almacenar la información del producto
     */
    data class ProductInfo(val name: String, val price: String)

    /**
     * Lanzador del escáner de código de barras
     */
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            editTextBarcode.setText(result.contents)
            fetchProductInfo(result.contents)
        } else {
            textViewProductName.text = "Escaneo cancelado."
        }
    }

    /**
     * Inicia el escáner de código de barras
     */
    private fun startBarcodeScanner() {
        val options = ScanOptions().apply {
            setPrompt("Escanea el código de barras")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setCaptureActivity(PortraitCaptureActivity::class.java)
            setBarcodeImageEnabled(true)
        }
        barcodeLauncher.launch(options)
    }
}
