package com.jve386.kpopchunichi_bcreader

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

class BarcodeInputActivity : AppCompatActivity() {

    private lateinit var editTextGridBarcodes: EditText
    private lateinit var buttonExportToExcel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_input)

        editTextGridBarcodes = findViewById(R.id.editTextGridBarcodes)
        buttonExportToExcel = findViewById(R.id.buttonSubmitBarcodes)

        buttonExportToExcel.setOnClickListener {
            exportToExcel()
        }
    }

    private fun exportToExcel() {
        val barcodesText = editTextGridBarcodes.text.toString()
        if (barcodesText.isEmpty()) {
            Toast.makeText(this, "Por favor, pega los códigos de barras.", Toast.LENGTH_SHORT).show()
            return
        }

        val barcodes = barcodesText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (barcodes.isEmpty()) {
            Toast.makeText(this, "No se encontraron códigos de barras válidos.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Crear el archivo Excel
                val timeZoneDate = SimpleDateFormat("yyyy-MMM-dd_HH-mm-ss", Locale.getDefault())
                val formattedDate = timeZoneDate.format(Date())

                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Códigos de Barras_$formattedDate")

                // Estilo de las celdas
                val headerCellStyle = workbook.createCellStyle()
                headerCellStyle.fillForegroundColor = 0xA0A0A0.toShort()
                headerCellStyle.fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

                // Crear la fila de cabecera
                val headerRow = sheet.createRow(0)
                headerRow.createCell(0).apply {
                    setCellValue("Código de barras")
                    cellStyle = headerCellStyle
                }
                headerRow.createCell(1).apply {
                    setCellValue("Nombre del producto")
                    cellStyle = headerCellStyle
                }
                headerRow.createCell(2).apply {
                    setCellValue("Unidades")
                    cellStyle = headerCellStyle
                }
                headerRow.createCell(3).apply {
                    setCellValue("Precio Unitario")
                    cellStyle = headerCellStyle
                }

                // Map para contar las unidades de cada código de barras
                val barcodeMap = mutableMapOf<String, Int>()

                // Procesar cada código de barras
                barcodes.forEachIndexed { index, barcode ->
                    val productInfo = fetchProductInfo(barcode)
                    val productName = productInfo.name
                    val unitPrice = productInfo.price

                    // Comprobar si el código ya existe en el Excel
                    var rowIndex = -1
                    for (i in 1 until sheet.physicalNumberOfRows) {
                        val row = sheet.getRow(i)
                        val codeCell = row?.getCell(0)?.stringCellValue
                        if (codeCell != null && codeCell == barcode) {
                            rowIndex = i
                            break
                        }
                    }

                    if (rowIndex == -1) {
                        // Si no se encuentra el código de barras, se agrega como nueva fila
                        val newRow = sheet.createRow(sheet.physicalNumberOfRows)
                        newRow.createCell(0).setCellValue(barcode)
                        newRow.createCell(1).setCellValue(productName)

                        // Crear la celda de unidades como un tipo numérico
                        val unitsCell = newRow.createCell(2, CellType.NUMERIC)
                        unitsCell.setCellValue(1.0) // Empezar con 1 unidad

                        // Crear la celda de precio unitario
                        newRow.createCell(3).setCellValue(unitPrice)

                        barcodeMap[barcode] = 1  // Inicializar contador para este código
                    } else {
                        // Si el código ya está, actualizar la cantidad de unidades
                        val existingRow = sheet.getRow(rowIndex)
                        val currentUnits = existingRow?.getCell(2)?.numericCellValue?.toInt() ?: 0

                        // Asegurarse de que la celda de unidades sea numérica
                        val existingCell = existingRow.getCell(2) ?: existingRow.createCell(2, CellType.NUMERIC)
                        existingCell.setCellValue((currentUnits + 1).toDouble())  // Incrementar unidades de forma numérica
                    }
                }

                // Guardar el archivo en el directorio de archivos de la aplicación
                val fileName = "data_$formattedDate.xlsx"
                val file = File(filesDir, fileName)
                val fos = FileOutputStream(file)
                workbook.write(fos)
                workbook.close()
                fos.close()

                // Crear la URI del archivo para compartir
                val fileUri = FileProvider.getUriForFile(
                    this@BarcodeInputActivity,
                    "$packageName.fileprovider",  // Asegúrate de que esto coincida con el nombre en el Manifest
                    file
                )

                // Crear un Intent para compartir el archivo
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                startActivity(Intent.createChooser(sharingIntent, "Compartir archivo"))

                Toast.makeText(this@BarcodeInputActivity, "Archivo Excel exportado exitosamente.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@BarcodeInputActivity, "Error al exportar el archivo Excel: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Función suspendida para obtener el nombre del producto y su precio desde la web
    private suspend fun fetchProductInfo(barcode: String): ProductInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://www.chunichicomics.com/busqueda?controller=search&s=$barcode"
                val document = Jsoup.connect(url).get()

                val nameElement = document.selectFirst("div.product-description h3.product-title")
                val productName = nameElement?.text() ?: "No encontrado en la web"

                val priceElement = document.selectFirst("span.product-price")
                val price = priceElement?.text() ?: "Precio no disponible"

                ProductInfo(productName, price)
            } catch (e: Exception) {
                ProductInfo("Error: ${e.message ?: "Error desconocido"}", "")
            }
        }
    }

    // Data class para almacenar la información del producto
    data class ProductInfo(val name: String, val price: String)
}
