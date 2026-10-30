# StockDate

StockDate es el esqueleto funcional de una aplicacion web para consultar y
registrar lotes de alimentos y revisar sus fechas de caducidad. El frontend
interactua con una API Spring Boot. En esta fase los lotes solo se guardan en
memoria: al parar la aplicacion se pierden.

## Requisitos y herramientas

- **Java/JDK:** JDK 17 o posterior instalado en el equipo. Gradle compila el
  codigo para Java 17 (`--release 17`). `run.bat` intenta localizar un JDK
  existente; no descarga ni instala Java.
- **Gradle:** se utiliza Gradle Wrapper 9.8.2, incluido en el proyecto. No hace
  falta instalar Gradle por separado. Gradle descarga las dependencias del
  proyecto la primera vez que se ejecuta.
- **Spring Boot 4.1.2:** inicia el servidor web y publica los archivos estaticos
  y la API.
- **HTML, CSS y JavaScript:** tecnologias del frontend, servidas por Spring Boot.
- **IntelliJ IDEA:** entorno de desarrollo recomendado; abre el proyecto Gradle
  desde la carpeta que contiene `settings.gradle`.
- **Git:** control de versiones del proyecto. No se incluye configuracion de un
  tablero de GitHub Projects.
- **SQLite y Docker:** previstos para fases futuras, pero todavia no se usan.
- **Navegador:** al iniciar, la aplicacion intenta abrir `http://localhost:8080`
  en el navegador predeterminado del sistema. Si Chrome es el predeterminado se
  abrira en Google Chrome; tambien se puede abrir esa direccion manualmente.

## Iniciar y detener la aplicacion

En Windows, abre una terminal de IntelliJ en la carpeta del proyecto y ejecuta:

```powershell
.\run.bat
```

## Que se puede hacer ahora

- **Resumen:** ver la cantidad de lotes, los que caducan en los proximos siete
  dias y la siguiente fecha de caducidad.
- **Inventario:** ver los lotes ordenados por fecha y buscar por producto,
  categoria o ubicacion.
- **Registro:** crear un lote con producto, categoria, cantidad, ubicacion,
  fecha y tipo de fecha (`USE_BY` o `BEST_BEFORE`).
- **Validacion:** no permite campos obligatorios vacios ni cantidades menores
  que uno. Tambien impide fechas pasadas para productos `USE_BY`.

La API implementada es `GET /api/inventory`, para consultar los lotes, y
`POST /api/inventory`, para registrar uno. Si los datos no son validos, devuelve
un error HTTP 400 con un mensaje.

Los lotes solo persisten mientras la aplicacion esta abierta. No se han
implementado login, roles, movimientos de salida o descarte, informes ni
persistencia en SQLite.

## Estructura del proyecto

```text
Stockdate/
├── Information/                         # Documentacion de referencia del proyecto
├── gradle/wrapper/                      # Componentes del Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/stockdate/
│   │   │   ├── config/                  # Apertura del navegador y futuras configuraciones
│   │   │   ├── persistence/             # Paquetes reservados para SQLite
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── presentation/            # Comunicacion HTTP y DTOs
│   │   │   │   ├── controller/
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       └── response/
│   │   │   ├── service/                 # Reglas y gestion de inventario
│   │   │   ├── ExpirationDateType.java
│   │   │   └── StockdateApplication.java
│   │   └── resources/
│   │       ├── static/                  # Interfaz web servida por Spring Boot
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── index.html
│   │       └── application.properties   # Configuracion de Spring Boot
│   └── test/
│       └── java/com/example/stockdate/  # Pruebas unitarias y de integracion
├── .gitattributes                       # Reglas de atributos de Git
├── .gitignore                           # Archivos generados o locales ignorados
├── build.gradle                         # Plugins, dependencias y compilacion Java
├── gradlew / gradlew.bat                # Gradle Wrapper para Unix y Windows
├── run.bat                              # Lanzador de StockDate en Windows
├── settings.gradle                      # Nombre y configuracion raiz de Gradle
├── Stockdate.iml                        # Modulo local de IntelliJ IDEA
└── README.md                            # Esta guia
```

### Archivos Java principales

| Archivo | Funcion en esta fase |
|---|---|
| `StockdateApplication.java` | Punto de entrada; inicia Spring Boot. |
| `ExpirationDateType.java` | Tipos de fecha admitidos: `USE_BY` y `BEST_BEFORE`. |
| `config/BrowserLauncher.java` | Al iniciar la aplicacion intenta abrir el navegador predeterminado; si no esta disponible, registra la direccion en el log. |
| `config/SecurityConfig.java` | Marcador para configuracion de seguridad futura; esta vacio y no configura autenticacion. |
| `config/SQLiteDialectConfig.java` | Marcador para SQLite; esta vacio y no configura ni conecta una base de datos. |
| `presentation/controller/InventoryController.java` | Expone `GET` y `POST /api/inventory`. |
| `presentation/controller/ApiExceptionHandler.java` | Convierte errores de validacion en respuestas HTTP 400 con un mensaje. |
| `presentation/controller/AuthController.java` | Marcador para autenticacion futura; no expone endpoints. |
| `presentation/controller/AdminController.java` | Marcador para funciones de administracion futuras; no expone endpoints. |
| `presentation/controller/ReportController.java` | Marcador para informes futuros; no expone endpoints. |
| `presentation/dto/request/CreateBatchRequest.java` | Datos que recibe la API al registrar un lote. |
| `presentation/dto/request/DispatchRequest.java` | Marcador para solicitudes de salida de stock; vacio. |
| `presentation/dto/request/DiscardRequest.java` | Marcador para solicitudes de descarte; vacio. |
| `presentation/dto/request/CreateUserRequest.java` | Marcador para altas de usuario; vacio. |
| `presentation/dto/response/BatchDTO.java` | Datos del lote que devuelve la API, incluido su estado de caducidad. |
| `presentation/dto/response/ApiError.java` | Mensaje de error devuelto por la API. |
| `presentation/dto/response/DashboardDTO.java` | Marcador para respuestas de resumen; vacio. |
| `presentation/dto/response/ReportDTO.java` | Marcador para respuestas de informes; vacio. |
| `presentation/dto/response/UserDTO.java` | Marcador para respuestas de usuario; vacio. |
| `service/InventoryManager.java` | Valida, guarda temporalmente y ordena los lotes en memoria; calcula dias y estado de caducidad. |
| `service/ExpirationAlertService.java` | Marcador para alertas futuras; vacio. |
| `service/ReportService.java` | Marcador para informes futuros; vacio. |
| `service/UserService.java` | Marcador para gestion de usuarios futura; vacio. |
| `persistence/entity/BatchEntity.java` | Marcador de entidad de lote para persistencia futura; vacio y no conectado a una base de datos. |
| `persistence/entity/StockMovementEntity.java` | Marcador de movimiento de inventario futuro; vacio. |
| `persistence/entity/UserEntity.java` | Marcador de entidad de usuario futura; vacio. |
| `persistence/entity/CategoryEntity.java` | Marcador de entidad de categoria futura; vacio. |
| `persistence/repository/InventoryRepository.java` | Marcador de repositorio de inventario; vacio y no conectado a SQLite. |
| `persistence/repository/MovementRepository.java` | Marcador de repositorio de movimientos; vacio. |
| `persistence/repository/UserRepository.java` | Marcador de repositorio de usuarios; vacio. |

### Frontend y recursos

- `static/index.html`: estructura de la pagina, menu de navegacion, resumen,
  tabla de inventario y formulario de registro.
- `static/css/styles.css`: estilos y adaptacion a pantallas pequenas.
- `static/js/app.js`: navegacion entre secciones, busqueda, carga del inventario
  y envio del formulario a la API.
- `application.properties`: define el nombre Spring `Stockdate` y activa la
  apertura automatica del navegador con `stockdate.browser.open=true`.

### Pruebas

- `StockdateApplicationTests.java`: comprueba que el contexto Spring inicia; la
  apertura del navegador se desactiva en esta prueba.
- `service/InventoryManagerTests.java`: prueba registro, orden, estado de
  caducidad y validaciones del inventario en memoria.
- `presentation/controller/InventoryControllerTests.java`: prueba HTTP el alta,
  la consulta del inventario y el rechazo de cantidades no validas.
- Los archivos `package-info.java` en los directorios de pruebas conservan la
  estructura de paquetes; no contienen pruebas por si mismos.

## Archivos de construccion y configuracion

- `build.gradle`: usa los plugins Java, Spring Boot y gestion de dependencias;
  fija compatibilidad Java 17, agrega Spring Web y dependencias de pruebas, y
  conecta la entrada estandar de `bootRun`.
- `settings.gradle`: establece el nombre Gradle `Stockdate`.
- `gradlew`, `gradlew.bat` y `gradle/wrapper/`: ejecutan la version de Gradle
  fijada por el wrapper sin una instalacion global.
- `run.bat`: busca un JDK existente en `JAVA_HOME`, `.jdks` de IntelliJ o
  ubicaciones habituales de Windows; despues ejecuta Gradle con salida simple.
- `.gitignore`: excluye `.gradle/`, `build/`, configuraciones locales de IDE y
  otros archivos generados.
- `.gitattributes`: define como Git gestiona los atributos de los archivos.
- `.idea/` y `Stockdate.iml`: configuracion local que IntelliJ IDEA puede
  generar o modificar al abrir el proyecto.
- `HELP.md`: ayuda generica generada al crear el proyecto Spring; la guia
  especifica de StockDate esta en este `README.md`.
- `Information/`: PDF y presentacion usados como referencia de requisitos y
  arquitectura.
- `.gradle/` y `build/`: datos locales y resultados generados por Gradle; no
  forman parte del codigo fuente de la aplicacion.

## Ejecutar las pruebas

En Windows:

```powershell
.\gradlew.bat test
```

En Linux o macOS:

```text
./gradlew test
```
