package com.jve386.kpopchunichi_bcreader.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import com.jve386.kpopchunichi_bcreader.R
import com.jve386.kpopchunichi_bcreader.model.ProductInfo

class BarcodeInputActivity : AppCompatActivity() {

    private lateinit var editTextGridBarcodes: EditText
    private lateinit var buttonExportToExcel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_input)

        editTextGridBarcodes = findViewById(R.id.editTextGridBarcodes)
        buttonExportToExcel = findViewById(R.id.buttonSubmitBarcodes)

        buttonExportToExcel.setOnClickListener { exportToExcel() }
    }

    private fun exportToExcel() {
        val barcodesText = editTextGridBarcodes.text.toString().trim()
        if (barcodesText.isEmpty()) {
            showToast("Por favor, pega los códigos de barras.")
            return
        }

        val barcodes = barcodesText.split("\n").mapNotNull { it.takeIf { it.isNotEmpty() } }

        if (barcodes.isEmpty()) {
            showToast("No se encontraron códigos de barras válidos.")
            return
        }

        lifecycleScope.launch {
            try {
                val fileName = generateExcelFile(barcodes)
                shareFile(fileName)
                showToast("Archivo Excel exportado exitosamente.")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error al exportar el archivo Excel: ${e.message}")
            }
        }
    }

    private suspend fun generateExcelFile(barcodes: List<String>): String {
        val formattedDate = SimpleDateFormat("yyyy-MMM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Códigos de Barras_$formattedDate")
        val headerCellStyle = createHeaderCellStyle(workbook)

        createHeaderRow(sheet, headerCellStyle)

        val barcodeMap = mutableMapOf<String, Int>()
        barcodes.forEach { barcode ->
            val productInfo = fetchProductInfo(barcode)
            updateSheetWithBarcode(sheet, barcode, productInfo, barcodeMap)
        }

        val file = File(filesDir, "data_$formattedDate.xlsx")
        FileOutputStream(file).use { fos -> workbook.write(fos) }
        workbook.close()

        return file.absolutePath
    }

    private fun createHeaderCellStyle(workbook: XSSFWorkbook) = workbook.createCellStyle().apply {
        fillForegroundColor = 0xA0A0A0.toShort()
        fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
    }

    private fun createHeaderRow(sheet: org.apache.poi.ss.usermodel.Sheet, headerCellStyle: org.apache.poi.ss.usermodel.CellStyle) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).apply { setCellValue("Código de barras"); cellStyle = headerCellStyle }
        headerRow.createCell(1).apply { setCellValue("Nombre del producto"); cellStyle = headerCellStyle }
        headerRow.createCell(2).apply { setCellValue("Unidades"); cellStyle = headerCellStyle }
        headerRow.createCell(3).apply { setCellValue("Precio Unitario"); cellStyle = headerCellStyle }
    }

    private fun updateSheetWithBarcode(sheet: org.apache.poi.ss.usermodel.Sheet, barcode: String, productInfo: ProductInfo, barcodeMap: MutableMap<String, Int>) {
        var rowIndex = findRowIndex(sheet, barcode)
        if (rowIndex == -1) {
            rowIndex = sheet.physicalNumberOfRows
            val newRow = sheet.createRow(rowIndex)
            newRow.createCell(0).setCellValue(barcode)
            newRow.createCell(1).setCellValue(productInfo.name)
            newRow.createCell(2, CellType.NUMERIC).setCellValue(1.0)
            newRow.createCell(3).setCellValue(productInfo.price)
        } else {
            val existingRow = sheet.getRow(rowIndex)
            val currentUnits = existingRow?.getCell(2)?.numericCellValue?.toInt() ?: 0
            existingRow?.getCell(2)?.setCellValue((currentUnits + 1).toDouble())
        }

        barcodeMap[barcode] = barcodeMap.getOrDefault(barcode, 0) + 1
    }

    private fun findRowIndex(sheet: org.apache.poi.ss.usermodel.Sheet, barcode: String): Int {
        for (i in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(i)
            if (row?.getCell(0)?.stringCellValue == barcode) return i
        }
        return -1
    }

    private suspend fun fetchProductInfo(barcode: String): ProductInfo {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect("https://www.chunichicomics.com/busqueda?controller=search&s=$barcode").get()
                val productName = document.selectFirst("div.product-description h3.product-title")?.text() ?: "No encontrado en la web"
                val price = document.selectFirst("span.product-price")?.text() ?: "Precio no disponible"
                ProductInfo(productName, price)
            } catch (e: Exception) {
                ProductInfo("Error: ${e.message ?: "Error desconocido"}", "")
            }
        }
    }

    private fun shareFile(filePath: String) {
        val file = File(filePath)
        val fileUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(Intent.createChooser(sharingIntent, "Compartir archivo"))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
