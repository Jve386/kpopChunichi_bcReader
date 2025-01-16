# kpopChunichi_bcReader
Aplicación ligera para Android que permite escanear códigos de barras de productos de la tienda online Chunichi Comics y obtener información sobre esos productos, como su nombre, precio, e imagen. Además, permite importar códigos de barras desde un archivo de texto, realizar búsquedas de productos, y exportar los datos obtenidos en un archivo Excel.

## Funcionalidades
**Escaneo de Códigos de Barras**: Utiliza la cámara del dispositivo para escanear códigos de barras y obtener detalles del producto asociado.
**Búsqueda Manual de Productos**: Introduce un código de barras manualmente y obtén la información del producto.
**Ingreso de Códigos de Barras**: Permite ingresar múltiples códigos de barras como una hilera de texto donde cada código se separa por saltos de línea.
**Exportar a Excel**: Permite exportar los productos escaneados a un archivo Excel, que incluye el código de barras, el nombre del producto, la cantidad y el precio unitario.
**Visualización de Imágenes de Productos**: Muestra las imágenes de los productos junto con su nombre y precio.
**Interfaz Intuitiva**: Una UI simple y funcional para facilitar la interacción con la aplicación.

# Dependencias
**Glide**: Para cargar imágenes de productos de forma eficiente.
**Jsoup**: Para realizar scraping en la página web de Chunichi Comics y obtener la información de los productos.
**Apache POI**: Para generar archivos Excel con los datos de los productos.
**JourneyApps Barcode Scanner**: Para escanear códigos de barras de forma sencilla y rápida.

## Licencia
Este proyecto está bajo la Licencia MIT - consulta el archivo [LICENSE](LICENSE) para más detalles.
