# C12 — Pruebas manuales del showcase

Responsable: **autor del proyecto**. Este documento conserva los casos y resultados de la revisión; [las tasks de C12](../openspec/changes/validate-showcase-delivery/tasks.md) enlazan a sus resultados. Estado: **pruebas manuales validadas por el usuario el 2026-10-02**.

Los casos marcados reflejan la confirmación del usuario. Los no reproducidos mantienen ese estado. Este registro es evidencia fechada, no una segunda lista activa de tareas; nuevas comprobaciones deben identificar su revisión y condiciones.

## Datos de la ejecución

- Fecha: 2026-10-02
- Persona que prueba: autor del proyecto.
- Commit/revisión y cambios locales relevantes: `b95a5bcd1475ef1c3e27cb87a608458e7d62c855` más las correcciones locales de foco de Home y recuperación de paginación ante HTTP 429.
- APK/versionName/versionCode del retest de foco/paginación: `app/build/outputs/apk/debug/app-debug.apk`; `0.1.0` / `1`.
- Variante del retest de foco/paginación: debug (instalado y abierto en el Pixel). SHA-256: `8fbca399e9c964f29672b0444424e1950d31956bf16d94306caffeea28e029fd`. Los casos ya marcados se probaron antes del cambio de variante; el registro de C12 conserva el APK de esa ejecución.
- Dispositivo físico/modelo: Google Pixel 9a (`55211JEBF14578`).
- Android/API: API 37.
- Tamaño de pantalla/escala de visualización: resolución 1080 × 2424; escala de visualización no registrada.
- Tamaño de letra: predeterminado (`font_scale=1.0`).
- Navegación del sistema: no registrada en la confirmación manual.
- Red y caché: condiciones no registradas en la confirmación manual.
- Emulador usado como complemento, si lo hay: API 37 arm64; automatización y smoke registrados en el design de C12.

La revisión principal se realizó en un teléfono físico y terminó en debug. La ampliación de accesibilidad de Home y Detail está registrada abajo. No se infiere una matriz de dispositivos ni restauración tras muerte del proceso.

## Preparación y arranque

- [x] **QA01 — Arranque:** instala/abre el APK identificado con conexión. Aparece Home, no se abre el teclado automáticamente y no hay cierre inesperado ni bloqueo. Si la red tarda, los controles siguen disponibles mientras cargan los skeletons.
- [x] **QA02 — Layout base:** revisa Home a tamaño de letra predeterminado. Search, All/Alive/Dead/Unknown, tarjetas y counter se leen; las barras del sistema no tapan controles. Las imágenes reales quedan dentro de sus bordes.

## Búsqueda, filtros y teclado

- [x] **QA03 — Búsqueda por nombre:** escribe `Rick`, espera la búsqueda y luego prueba Search del teclado. El campo conserva lo escrito, aparecen resultados del nombre y Search cierra el teclado.
- [x] **QA04 — Cambios rápidos:** cambia el nombre varias veces y selecciona un estado mientras carga. El resultado final corresponde a los filtros visibles; no reaparecen tarjetas de una búsqueda anterior como si fueran resultados actuales.
- [x] **QA05 — Estados:** prueba All, Alive, Dead y Unknown. Se reconoce el seleccionado y los resultados corresponden a él. Volver a pulsar el seleccionado no reinicia el listado.
- [x] **QA06 — Filtros combinados:** con `Rick` y Alive, borra el nombre con Clear: Alive se conserva. Vuelve a buscar `Rick` y pulsa All: el nombre se conserva. Cada cambio real empieza el nuevo listado desde arriba y el counter refleja ese resultado.
- [x] **QA07 — Teclado y scroll:** abre el teclado y arrastra manualmente el grid: se oculta sin borrar nombre/filtro. El counter no compite con el teclado; al tocar Search puedes volver a escribir.
- [x] **QA08 — Sin coincidencias:** usa All y un nombre improbable, por ejemplo `zzzxxyy-no-match`. Se muestran “No characters found”, el mensaje y Rick/Morty/Beth/Summer; Search y filtros permanecen visibles y no aparece Retry ni otro destino. En un ancho equivalente a 412dp y letra predeterminada, las cuatro sugerencias caben en una fila; en un ancho menor pueden envolver sin recortarse.
- [x] **QA09 — Sugerencias:** pulsa una sugerencia desde sin coincidencias. El campo se actualiza, la búsqueda se aplica y el estado seleccionado se conserva. Repite con los otros nombres; puede seguir sin coincidencias si el estado retenido excluye esos personajes.

## Paginación y contexto

- [x] **QA10 — Varias páginas:** vuelve a All sin nombre y baja por varias páginas. Aparecen más tarjetas, el número cargado aumenta y no supera el total. No hay saltos al inicio ni duplicados visibles durante append.
- [x] **QA11 — Final y counter:** usa una búsqueda con pocos resultados, por ejemplo `Pickle Rick` con All. Al llegar al final no queda una carga infinita; la última tarjeta y cualquier footer pueden quedar por encima del counter al desplazarte.
- [x] **QA12 — Abrir desde abajo y volver:** en un resultado paginado, anota nombre/estado/posición y abre una tarjeta lejos del inicio. Vuelve con el Back de Detail: recuperas esos filtros y la posición, sin abrir el teclado automáticamente.
- [x] **QA13 — Back del sistema:** repite el viaje y vuelve mediante gesto/botón del sistema. Conserva el contexto igual que Back de Detail. Prueba además abrir una tarjeta mientras el teclado está visible: se cierra al entrar en Detail.

## Detail y episodios

- [x] **QA14 — Identidad y datos:** abre Rick Sanchez. Nombre, retrato, status/species y facts pertenecen al personaje elegido. Gender, Origin, Last known location y Episode appearances se leen. Un Type vacío no deja una fila vacía; busca además un personaje con Type informado y comprueba su texto.
- [x] **QA15 — Retrato:** observa tamaño, nitidez, recorte y bordes del cuadrado. No hay deformación ni invasión de Back. Al cargar, los facts no dependen de que la foto ya esté visible.
- [x] **QA16 — Scroll vertical y Back fijo:** recorre inicio, medio y final de Detail. Todos los facts y la sección de episodios son alcanzables; Back permanece fijo, legible y pulsable. El parallax se mantiene sutil y dentro del recorte, sin tirones o saltos evidentes. Es una observación visual, no una medición de rendimiento.
- [x] **QA17 — Episodios:** desplaza horizontalmente varias tarjetas de Rick. Se leen código, título y fecha; los títulos largos no pisan la fecha. El swipe horizontal no desplaza Back y las tarjetas no abren destinos. El contador de apariciones sigue presente mientras carga la sección.
- [x] **QA18 — Repetir navegación:** alterna varios personajes y vuelve por ambos Back. La identidad/retrato/episodios corresponden al personaje actual; no queda información del anterior ni se duplica la navegación.

## Continuidad y revisión final

- [x] **QA19 — Segundo plano:** desde Home filtrado y luego desde Detail, envía la app al fondo brevemente y vuelve sin cerrar su proceso. El flujo sigue utilizable y conserva el contexto de navegación; no hay un cierre inesperado. Esto no verifica restauración tras matar el proceso.
- [x] **QA20 — Revisión visual/movimiento:** observa skeleton → contenido, aparición de imágenes, chips, cambio de resultados y entrada/salida de Detail. No hay parpadeos, huecos o desplazamientos inesperados. Los controles visibles responden y pueden alcanzarse a tamaño normal.
- [x] **QA21 — Capturas para entrega:** revisa las capturas reales que vas a publicar de Home y Detail contra el APK probado. Representan la app actual; no son Stitch ni fixtures de Paparazzi. Capturas publicadas: [Home](screenshots/home-2026-10-02.png) y [Detail](screenshots/detail-2026-10-02.png).

## Casos condicionados por red, caché o datos

Estos casos pueden ser difíciles de provocar con la API real. Usa una petición no cacheada y/o desconecta antes de solicitar nuevos datos. La caché puede permitir que la petición funcione; eso no equivale a haber probado el estado de error. No se exige un modo offline, snackbar de conectividad ni cobertura manual artificial de todos los estados. Registra los no reproducidos en la tabla; C12 los contrasta con los tests controlados existentes.

- [x] **QC01 — Error inicial/de búsqueda:** provoca un fallo de petición sin datos cacheados. El mensaje y Retry aparecen en Home; Search/filtros conservan sus valores. Restaura conexión y pulsa Retry: recupera el mismo resultado solicitado.
- [x] **QC02 — Append fallido:** con tarjetas ya cargadas, desconecta antes de pedir una página nueva no cacheada. Se retienen las tarjetas/counter y aparece Retry en el footer, alcanzable. Al reconectar y reintentar, el listado continúa sin saltar al inicio.
- [x] **QC03 — Error de Detail:** abre un personaje cuyos datos aún no estén cacheados sin conexión. Back sigue disponible; cuando aparece el error, reconecta y pulsa Retry. Se carga ese personaje sin cambiar de destino.
- [ ] **QC04 — Solo episodios fallidos:** si consigues interrumpir episodios después de cargar el personaje, facts/retrato/contador permanecen. “Retry episodes” recupera únicamente la sección. Si no puedes inducir ese momento, registra no reproducido; no añadas controles de depuración a la app para hacerlo. No reproducido
- [x] **QC05 — Foto pendiente/fallida:** si la imagen tarda o falla mientras hay metadata, solo su zona muestra carga/fallback y la tarjeta/Detail sigue usable. No confundas skeleton del personaje con fallo de su foto.
- [ ] **QC06 — Sin apariciones:** si encuentras datos sin referencias de episodios, aparece el mensaje local sin Retry. Si la API no ofrece un caso accesible, registra no reproducido; no inventes un ID ni cambies la app. No reproducido

## Incidencias, casos no ejecutados y observaciones

Las observaciones manuales y los resultados automatizados conservan evidencias separadas.

| Caso | Estado | Qué hiciste / esperado y observado | Captura o evidencia | Repetición / resultado |
|---|---|---|---|---|
| QA10 / QC02 | Pasó tras repetir | El usuario observa append Retry que sigue fallando mientras cargan las imágenes de las tarjetas anteriores; recupera después. | Reporte durante QA física | HTTP 429 reproducido en emulador. Corrección instalada en debug: respeta Retry-After y reintenta una vez. El usuario confirma que repitió las pruebas manuales y todo funciona correctamente. |

## Home accessibility demonstration

Alcance seleccionado el 2026-10-02: mejoras de accesibilidad en Home, sin añadir una pantalla de demostración. El usuario confirmó el 2026-10-02 que la validación de accesibilidad está completada; esta confirmación no modifica los resultados manuales anteriores. Existen tests instrumentados de texto al 200%, controles y semántica; los resultados ejecutados y las limitaciones de las nuevas pruebas se registran por separado en el design de C12. No sustituyen TalkBack ni la revisión de contraste.

La confirmación se refiere a Home real. El usuario no aportó un hash adicional del APK de accesibilidad ni detalles individuales de ajustes/mediciones; no se infieren del hash del retest anterior.

- [x] **HA01 — TalkBack:** recorre búsqueda, limpiar, filtros y tarjetas. Comprueba nombres comprensibles, estado seleccionado anunciado, orden de foco coherente y activación de cada control. Abre un personaje para comprobar que la tarjeta permite navegar; la verificación de Detail se registra en DA01/DA02.
- [x] **HA02 — Texto grande:** configura la fuente al 200%. Busca, cambia filtros y desplázate hasta el final. Comprueba que los controles siguen disponibles, el contador no tapa acciones y los mensajes y Retry se pueden leer y activar.
- [x] **HA03 — Contraste y objetivos táctiles:** usa Accessibility Scanner sobre Home con resultados y, cuando sean reproducibles, estados vacíos y de error. Revisa contraste del texto y objetivos táctiles de al menos 48dp; registra los avisos y su resolución o justificación. Un estado no reproducido queda identificado como tal.

| Caso | Revisión/APK y dispositivo | Ajustes | Resultado y evidencia |
|---|---|---|---|
| HA01 | Candidato local de accesibilidad | No detallados | Validado por el usuario el 2026-10-02 |
| HA02 | Candidato local de accesibilidad | No detallados | Validado por el usuario el 2026-10-02 |
| HA03 | Candidato local de accesibilidad | No detallados | Validado por el usuario el 2026-10-02 |

## Detail accessibility verification

Ampliación autorizada el 2026-10-02; las condiciones adicionales no comunicadas figuran como no detalladas. El usuario confirmó el 2026-10-02 que la validación de accesibilidad está completada; esta confirmación no reemplaza la validación manual anterior.

- [x] **DA01 — Lectura por grupos:** con TalkBack, abre un personaje. Comprueba nombre y secciones como encabezados, estado/especie juntos, cada etiqueta/valor en una sola parada y cada episodio con código/título/fecha juntos. Back debe ser independiente y alcanzable; el retrato decorativo no debe añadir paradas.
- [x] **DA02 — Recuperación y texto grande:** con fuente al 200%, comprueba que hechos, episodios y Back siguen disponibles. Si puedes reproducir un error, verifica su anuncio y Retry como control independiente; un fallo de episodios debe mantener disponible la ficha. Registra los estados que no puedas reproducir.

| Caso | Revisión/APK y dispositivo | Ajustes | Resultado y evidencia |
|---|---|---|---|
| DA01 | Candidato local de accesibilidad | No detallados | Validado por el usuario el 2026-10-02 |
| DA02 | Candidato local de accesibilidad | No detallados | Validado por el usuario el 2026-10-02 |

La validación manual se cierra por confirmación explícita del usuario. No se atribuyen herramientas, mediciones o capturas específicas que no haya comunicado; las pruebas instrumentadas conservan sus resultados independientes.
