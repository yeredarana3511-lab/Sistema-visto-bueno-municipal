# Sistema de Visto Bueno Municipal - Gobierno de Tonalá
## Auditoría Integral de Requerimientos y Diseño Institucional

---

### 1. Paleta de Colores y Diseño Institucional del Gobierno de Tonalá

| Elemento | Código HEX | Rol en el Sistema |
|---|---|---|
| **Color Principal Institucional** | `#822828` | Barras de navegación superior, botones principales de acción (`btn-tonala-primary`), tarjetas destacadas y acentos de títulos. |
| **Color Secundario / Fondo Oscuro** | `#212529` | Encabezados de tablas, pie de página institucional, botones secundarios (`btn-tonala-secondary`) y degradados de banners. |
| **Acento Dorado Tonalteca** | `#c59b27` | Borde oficial de escudos, badges de roles institucionales y destacados de estado. |
| **Fondo General del Sistema** | `#f8f9fa` | Contenedores limpios, legibles y de alto contraste. |

> Todas las vistas del sistema (`inicio.html`, `login.html`, `oficios.html`, `solicitudes/nueva.html`, `solicitudes/lista.html`, `solicitudes/detalle.html`, `plantillas/lista.html`, `plantillas/formulario.html`, `usuarios/listado.html`, `usuarios/formulario.html`, `roles/listado.html`, `roles/formulario.html`, `dias_inhabiles/lista.html`, `auditoria/lista.html`) comparten la misma barra de navegación unificada `fragments/navbar.html`, estilos CSS en `tonala-theme.css` y pie de página institucional.

---

### 2. Chequeo Punto por Punto de los Requerimientos (Google Classroom)

| # | Requerimiento Oficial | Estado | Implementación en el Código |
|---|---|:---:|---|
| **1** | **Inicio de Sesión y Autenticación**<br>Acceso mediante número de empleado y contraseña alfanumérica de 8 caracteres. Cuenta activa obligatoria. Asignación centralizada de rol (el usuario no elige rol al ingresar). | ✅ **Cumplido al 100%** | Implementado en `CustomUserDetailsService.java` y `SecurityConfig.java`. Valida `numero_empleado`, contraseña con hash `BCryptPasswordEncoder` y campo `activo = true`. El rol viene cargado directamente de la base de datos. |
| **2** | **Usuarios y Permisos (Segregación)**<br>Roles base: Super Administrador, Director (Supervisor) y Cajero (Secretaría/Operador). El Super Admin configura permisos por módulo y acción. | ✅ **Cumplido al 100%** | Entidades `UsuarioSistema` y `RolSistema`. Control granular en `RolController.java`, con matriz de permisos por módulo (`SOLICITUDES_VER`, `SOLICITUDES_CREAR`, `OCR_EJECUTAR`, `OFICIOS_VER`, `OFICIOS_CREAR`, `OFICIOS_ENVIAR`, `DICTAMEN_EMITIR`, etc.). |
| **3** | **Captura y OCR con Validación Humana**<br>Cargar imagen de solicitud, extraer datos con motor OCR y **exigir revisión y corrección humana obligatoria antes de confirmar**. | ✅ **Cumplido al 100%** | Controlador `SolicitudController.java` y servicio `OcrService.java`. El flujo en `solicitudes/nueva.html` divide el proceso en 2 pasos: primero procesa la imagen y presenta los datos extraídos; el usuario revisa, corrige cualquier campo y selecciona dependencias antes de presionar "Confirmar y Guardar". |
| **4** | **Solicitudes y Establecimientos**<br>Guardar datos confirmados y mantener relación entre solicitudes, establecimientos y giros cuando corresponda. | ✅ **Cumplido al 100%** | Entidades `Solicitud.java` y `Establecimiento.java` vinculadas con clave foránea `establecimiento_id`. Guarda razón social, giro comercial, dirección completa y porcentaje de fiabilidad OCR. |
| **5** | **Oficios y Plantillas Editables**<br>Generar oficios dirigidos a las 3 dependencias (Ecología, Reglamentos y Protección Civil). Plantillas editables desde el panel de administración. Decisión de oficios separados o agrupados. | ✅ **Cumplido al 100%** | `PlantillaController.java` y `plantillas/formulario.html` permiten editar el HTML de cada dependencia con etiquetas dinámicas `{{FOLIO}}`, `{{RAZON_SOCIAL}}`, `{{GIRO}}`, `{{DIRECCION}}`. `OficioService` cuenta con `generarYGuardarOficio()` y `generarYGuardarOficioAgrupado()`. |
| **6** | **Folios y Documentos**<br>Asignación de folios mediante secuencia configurable por dependencia. Conservación de archivos de origen e historial. | ✅ **Cumplido al 100%** | Prefijos oficiales configurables (`ECO`, `IR`, `PCB`) con secuencia autoincremental guardada en `dependencias.secuencia_actual`. Formato de folio: `PREFIJO-AÑO-SECUENCIA` (ej. `ECO-2026-0001`). Archivos guardados físicamente en `/uploads/solicitudes/` y servidos con `WebMvcConfig.java`. |
| **7** | **Envío por Correo (Gmail)**<br>Integrar envío mediante Gmail (`spring-boot-starter-mail`), registrar fecha, destinatario y estatus. Indicar que la aceptación de Gmail no es garantía de entrega final. | ✅ **Cumplido al 100%** | `OficioService.enviarCorreoOficio()` y modal de envío en `oficios.html`. Registra `email_destino`, `fecha_envio` y muestra la leyenda de deslinde legal requerida. |
| **8** | **Bandeja de Seguimiento y Semáforo de Días Hábiles**<br>Bandejas independientes por dependencia. Semáforo: Verde (1-20 días), Amarillo (21-29 días), Rojo (30-37 días) y Morado (38+ días). Exclusión de fines de semana y días inhábiles. | ✅ **Cumplido al 100%** | `DiaInhabilService.java` y `OficioService.determinarColorSemaforo()`. En `oficios.html` se muestran 3 pestañas independientes para Ecología, Inspección y Protección Civil con indicadores visuales del semáforo. |
| **9** | **Respuestas y Trazabilidad**<br>Registro de respuestas formales recibidas de dependencias y cierre de oficio. Bitácora de auditoría inmutable de eventos. | ✅ **Cumplido al 100%** | Endpoint `/oficios/registrar-respuesta` para registrar número de oficio de respuesta, fecha y observaciones. Registro en `bitacora_auditoria` con usuario, IP, módulo, acción y detalle antes/después. |
| **10** | **Acción Única de Supervisión / Dictamen**<br>El supervisor emite el Visto Bueno definitivo (Aprobar o Rechazar) actualizando el estado de la solicitud. | ✅ **Cumplido al 100%** | Endpoint `/solicitudes/dictaminar` y modal de dictamen en `solicitudes/lista.html` y `solicitudes/detalle.html`. Registra estado (`CONFIRMADO`, `EN_PROCESO`, `CERRADO`) y audita la resolución. |

---

### 3. Cuentas Preconfiguradas para Pruebas del Docente y Evaluadores

| Rol | Número de Empleado | Contraseña | Privilegios Principales |
|---|---|---|---|
| **Super Administrador** | `ADMIN01` | `admin123` | Mantenimiento técnico, gestión de usuarios, edición de roles y permisos, personalización de plantillas de dependencias y auditoría. |
| **Secretaría / Operador (Cajero)** | `SEC01` | `secretaria123` | Carga de solicitudes, extracción OCR, validación y corrección de datos de establecimientos, generación y envío de oficios por Gmail. |
| **Director / Supervisor** | `SUP01` | `supervisor123` | Revisión de expedientes de solicitudes con folio generado, dictamen de Visto Bueno Municipal (**Aprobar o Rechazar**) y seguimiento del semáforo. |
