package com.jve386.kpopchunichi_bcreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewProductName: TextView
    private lateinit var textViewProductPrice: TextView
    private lateinit var buttonLoadTxt: Button
    private lateinit var buttonScanBarcode: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBarcode = findViewById(R.id.editTextBarcode)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewProductName = findViewById(R.id.textViewProductName)
        textViewProductPrice = findViewById(R.id.textViewProductPrice)
        buttonLoadTxt = findViewById(R.id.buttonLoadTxt)
        buttonScanBarcode = findViewById(R.id.buttonScanBarcode)

        buttonScanBarcode.setOnClickListener { startBarcodeScanner() }

        buttonSearch.setOnClickListener {
            val barcode = editTextBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                fetchProductInfo(barcode)
            } else {
                textViewProductName.text = "Introduce un código de barras válido."
            }
        }

        buttonLoadTxt.setOnClickListener { loadTxt() }
    }

    private val barcodeInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcodes = result.data?.getStringExtra("BARCODES")
            barcodes?.split("\n")?.map { it.trim() }?.filter { it.isNotEmpty() }?.forEach { barcode ->
                fetchProductInfo(barcode)
            }
        }
    }

    private fun loadImage(imageUrl: String) {
        val imageView: ImageView = findViewById(R.id.imageViewProduct)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(imageView)
    }

    private fun loadTxt() {
        val intent = Intent(this, BarcodeInputActivity::class.java)
        barcodeInputLauncher.launch(intent)
    }

    private fun fetchProductInfo(barcode: String) {
        lifecycleScope.launch {
            val productInfo = withContext(Dispatchers.IO) {
                try {
                    val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"
                    val document = Jsoup.connect(url).get()

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

            textViewProductName.text = productInfo.name
            textViewProductPrice.text = productInfo.price
            if (productInfo.imageUrl.isNotEmpty()) {
                loadImage(productInfo.imageUrl)
            } else {
                loadImage("R.drawable.placeholder_image")
            }
        }
    }

    data class ProductInfo(val name: String, val price: String, val imageUrl: String)

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            editTextBarcode.setText(it)
            fetchProductInfo(it)
        } ?: run {
            textViewProductName.text = "Escaneo cancelado."
        }
    }

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
