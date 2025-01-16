package com.jve386.kpopchunichi_bcreader.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.jve386.kpopchunichi_bcreader.R
import com.jve386.kpopchunichi_bcreader.model.ProductInfo
import com.jve386.kpopchunichi_bcreader.data.ProductService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewProductName: TextView
    private lateinit var textViewProductPrice: TextView
    private lateinit var buttonLoadTxt: Button
    private lateinit var buttonScanBarcode: Button

    private val productService = ProductService()

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

    // Lanza el escáner de código de barras
    private fun startBarcodeScanner() {
        val options = ScanOptions()
        options.setPrompt("Escanea el código de barras") // Personalizar texto del escáner si es necesario
        barcodeScanner.launch(options)
    }

    // Registro de la actividad del escáner
    private val barcodeScanner =
        registerForActivityResult(ScanContract()) { result ->
            if (result?.contents != null) {
                val barcode = result.contents
                editTextBarcode.setText(barcode)
                fetchProductInfo(barcode)
            }
        }

    private val barcodeInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
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
                productService.fetchProductInfo(barcode)
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
}
