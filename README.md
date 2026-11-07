# Simulador de Ecosistema (Swing + MVC)

Aplicación Java de escritorio que conserva las reglas del simulador original y permite configurar, avanzar, intervenir y consultar el reporte final desde una interfaz Swing.

## Capas

- `proyecto.modelo`: entidades, reglas de población y evolución por turno.
- `proyecto.vista`: interfaz Swing, componentes y eventos de usuario.
- `proyecto.controlador`: valida acciones y coordina modelo, vista y persistencia.
- `proyecto.dao`: lectura, escritura y eliminación del archivo de estado.
- `proyecto.principal`: ensamblado de las piezas y creación de la ventana principal.

## Ejecutar

Abrir el proyecto en NetBeans y ejecutar `proyecto.principal.Main`, o compilar el contenido de `src` con JDK 17 o posterior. La simulación se guarda en `ecosistema.txt` en el directorio de ejecución; al iniciar, se retoma ese estado si el archivo existe.

Las cantidades válidas son 5–30 plantas, 2–15 conejos, 1–5 lobos y 10–50 turnos. Se permite una intervención después de cada tercer turno.
