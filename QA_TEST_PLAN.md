# Plan de Pruebas QA - T-20 App

## 1. Autenticación

### 1.1 Registro
- [x] Registrar usuario con datos válidos
- [x] Intentar registrar con email ya existente (debe mostrar error)
- [x] Intentar registrar con campos vacíos
- [x] Intentar registrar con email inválido (sin @)
- [x] Intentar registrar con contraseña muy corta
- [x] Verificar que el usuario se guarda en Firebase

### 1.2 Login
- [x] Login con credenciales correctas
- [x] Login con contraseña incorrecta
- [x] Login con email no registrado
- [x] Login con campos vacíos
- [x] Verificar que la sesión persiste al cerrar y abrir la app

### 1.3 Logout
- [x] Cerrar sesión correctamente
- [?] Verificar que se limpia SharedPreferences
- [x] Verificar que no se puede acceder a funciones de usuario logueado

---

## 2. Catálogo de Productos

### 2.1 Visualización
- [x] Verificar que todas las categorías cargan productos
- [x] Verificar que las imágenes cargan correctamente (no placeholder)
- [-] Verificar precios se muestran con formato correcto ($XX.XX)
- [x] Verificar nombres de productos visibles

### 2.2 Categorías
- [x] Cambiar entre categorías (Accesorios, Camisas, Pantalones, Poleras, Polos)
- [x] Verificar que los productos corresponden a la categoría
- [x] Verificar que el chip seleccionado se resalta

### 2.3 Búsqueda
- [x] Buscar producto existente por nombre
- [x] Buscar producto inexistente (lista vacía)
- [x] Limpiar búsqueda y ver todos los productos
- [x] Búsqueda con mayúsculas/minúsculas

---

## 3. Carrito de Compras

### 3.1 Agregar al carrito
- [x] Agregar producto al carrito
- [x] Agregar mismo producto múltiples veces
- [x] Intentar agregar más del stock disponible (debe mostrar error)
- [x] Verificar toast de confirmación

### 3.2 Visualización del carrito
- [x] Ver productos en el carrito
- [x] Verificar imágenes correctas
- [x] Verificar cantidades correctas
- [x] Verificar precios individuales
- [x] Verificar total del carrito

### 3.3 Modificar carrito
- [x] Aumentar cantidad (+)
- [x] Disminuir cantidad (-)
- [x] Intentar aumentar más del stock (debe mostrar error)
- [x] Eliminar producto del carrito
- [x] Verificar actualización del total

### 3.4 Persistencia
- [x] Agregar productos, cerrar app, abrir app → carrito debe persistir
- [x] Login en otro dispositivo → carrito debe sincronizar (Firebase)

---

## 4. Checkout

### 4.1 Proceso de compra
- [x] Acceder a checkout con productos en carrito
- [x] Ver resumen de productos
- [x] Ingresar dirección válida
- [x] Intentar confirmar sin dirección (debe mostrar error)
- [x] Confirmar pedido exitosamente

### 4.2 Post-compra
- [x] Verificar que el carrito se vacía
- [x] Verificar que el stock se reduce
- [x] Verificar que el pedido aparece en historial
- [x] Verificar toast de confirmación

---

## 5. Historial de Pedidos

### 5.1 Visualización
- [x] Ver lista de pedidos
- [x] Verificar número de pedido correcto
- [x] Verificar fecha del pedido
- [x] Verificar estado del pedido
- [x] Verificar total del pedido
- [x] Verificar miniaturas de productos

### 5.2 Múltiples pedidos
- [x] Crear varios pedidos y verificar orden (más reciente primero)
- [x] Verificar que cada pedido tiene su número único

---

## 6. Sistema de Tickets

### 6.1 Crear ticket
- [x] Acceder a crear ticket
- [x] Seleccionar pedido del dropdown (deben aparecer todos los pedidos)
- [x] Seleccionar motivo
- [x] Subir imagen de evidencia
- [x] Escribir descripción
- [x] Enviar ticket exitosamente

### 6.2 Validaciones
- [x] Intentar enviar sin seleccionar pedido
- [x] Intentar enviar sin seleccionar motivo
- [x] Intentar enviar sin imagen
- [x] Intentar enviar sin descripción
- [x] Verificar que descripción se habilita después de subir imagen

### 6.3 Ver tickets
- [x] Ver lista de tickets creados
- [x] Verificar estado del ticket
- [x] Verificar información del ticket

---

## 7. Panel de Administrador

### 7.1 Acceso
- [x] Login con cuenta admin
- [x] Verificar que aparece botón "Panel Admin"
- [x] Login con cuenta cliente → no debe aparecer botón

### 7.2 Gestión de productos
- [x] Ver lista de todos los productos
- [x] Editar producto existente (precio, stock, nombre)
- [ ] Eliminar producto
- [x] Verificar que cambios se reflejan en catálogo

### 7.3 Crear producto nuevo
- [x] Crear producto con URL de imagen
- [x] Verificar que aparece en categoría correcta
- [x] Verificar sincronización con Firebase

---

## 8. Casos Edge / Límite

### 8.1 Stock
- [x] Producto con stock = 0 (no debe poder agregarse)
- [x] Comprar todo el stock de un producto
- [x] Verificar que stock no puede ser negativo

### 8.2 Carrito vacío
- [x] Intentar ir a checkout con carrito vacío
- [x] Visualización de carrito vacío

### 8.3 Sin conexión
- [ ] Usar app sin internet (funcionalidad offline)
- [ ] Reconectar y verificar sincronización

### 8.4 Sesión
- [x] Intentar crear ticket sin login
- [x] Intentar checkout sin login

---

## 9. UI/UX

### 9.1 Navegación
- [x] Bottom navigation funciona correctamente
- [x] Botón back funciona en todas las pantallas
- [x] No hay pantallas rotas o crashes

### 9.2 Visual
- [x] Textos legibles
- [x] Botones clickeables
- [x] Imágenes no se cortan
- [x] Scroll funciona donde corresponde

### 9.3 Responsividad
- [ ] Probar en diferentes tamaños de pantalla
- [ ] Rotar pantalla (si aplica)

---

## Bugs Encontrados

| # | Pantalla | Descripción | Severidad | Estado |
|---|----------|-------------|-----------|--------|
| 1 |          |             |           |        |
| 2 |          |             |           |        |
| 3 |          |             |           |        |

**Severidad:** Crítico / Alto / Medio / Bajo

---

## Notas de Testing

- **Cuenta Admin:** Crear con role = "admin"
- **Cuenta Cliente:** Cualquier registro normal
- **Firebase Console:** Verificar datos en Firestore
- **Limpiar datos:** Desinstalar app o borrar datos para empezar de cero
