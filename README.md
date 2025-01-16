# kpopChunichi_bcReader
Aplicación ligera para Android que permite escanear códigos de barras y obtener información de productos de la tienda online Chunichi Comics, como su nombre, precio, e imagen. Además, permite importar códigos de barras desde un archivo de texto, realizar búsquedas de productos, y exportar los datos obtenidos en un archivo Excel.


## Funcionalidades
- **Escaneo de Códigos de Barras**: Utiliza la cámara del dispositivo para escanear códigos de barras y obtener detalles del producto asociado.
- **Búsqueda Manual de Productos**: Introduce un código de barras manualmente y obtén la información del producto.
- **Ingreso de Códigos de Barras**: Permite ingresar múltiples códigos de barras como una hilera de texto donde cada código se separa por saltos de línea.
- **Exportar a Excel**: Permite exportar los productos escaneados a un archivo Excel, que incluye el código de barras, el nombre del producto, la cantidad y el precio unitario.
- **Visualización de Imágenes de Productos**: Muestra las imágenes de los productos junto con su nombre y precio.
- **Complemento para el ERP**: Esta aplicación fue desarrollada para complementar el sistema ERP de **Chunichi Comics**.
**Interfaz Intuitiva**: Una UI simple y funcional para facilitar la interacción con la aplicación.


### Lenguajes y Frameworks

- **Kotlin**.
- **Android SDK**.

### Bibliotecas

- **Glide**: Una biblioteca para la carga eficiente de imágenes en la interfaz de usuario.
  - [Glide en GitHub](https://github.com/bumptech/glide)
  
- **Jsoup**: Utilizada para realizar el scraping de datos de la web de Chunichi Comics y obtener la información del producto a partir de los códigos de barras.
  - [Jsoup en GitHub](https://github.com/jhy/jsoup)
  
- **Apache POI**: Para la creación y manipulación de archivos Excel (.xlsx), permitiendo la exportación de los productos y sus detalles.
  - [Apache POI en GitHub](https://github.com/apache/poi)

- **JourneyApps Barcode Scanner**: Biblioteca para el escaneo de códigos de barras dentro de la aplicación.
  - [JourneyApps Barcode Scanner en GitHub](https://github.com/journeyapps/zxing-android-embedded)

### Otras Herramientas

- **Coroutines (Kotlin)**: Utilizado para la programación asíncrona y la gestión de operaciones de red, como la obtención de información de productos desde la web.
  
- **Android Lifecycle**: Utilizado para gestionar los ciclos de vida de las actividades y optimizar el uso de recursos.

### Arquitectura

- **Model-view-viewmodel (MVVM)**: La aplicación sigue el patrón de arquitectura MVVM para una mejor separación de responsabilidades y un flujo de datos más limpio.


## Licencia
Este proyecto está bajo la Licencia MIT - consulta el archivo [LICENSE](LICENSE) para más detalles.
