# Plan de Pruebas QA - T-20 App

## 1. Autenticación

### 1.1 Registro
- [ ] Registrar usuario con datos válidos
- [ ] Intentar registrar con email ya existente (debe mostrar error)
- [ ] Intentar registrar con campos vacíos
- [ ] Intentar registrar con email inválido (sin @)
- [ ] Intentar registrar con contraseña muy corta
- [ ] Verificar que el usuario se guarda en Firebase

### 1.2 Login
- [ ] Login con credenciales correctas
- [ ] Login con contraseña incorrecta
- [ ] Login con email no registrado
- [ ] Login con campos vacíos
- [ ] Verificar que la sesión persiste al cerrar y abrir la app

### 1.3 Logout
- [ ] Cerrar sesión correctamente
- [ ] Verificar que se limpia SharedPreferences
- [ ] Verificar que no se puede acceder a funciones de usuario logueado

---

## 2. Catálogo de Productos

### 2.1 Visualización
- [ ] Verificar que todas las categorías cargan productos
- [ ] Verificar que las imágenes cargan correctamente (no placeholder)
- [ ] Verificar precios se muestran con formato correcto ($XX.XX)
- [ ] Verificar nombres de productos visibles

### 2.2 Categorías
- [ ] Cambiar entre categorías (Accesorios, Camisas, Pantalones, Poleras, Polos)
- [ ] Verificar que los productos corresponden a la categoría
- [ ] Verificar que el chip seleccionado se resalta

### 2.3 Búsqueda
- [ ] Buscar producto existente por nombre
- [ ] Buscar producto inexistente (lista vacía)
- [ ] Limpiar búsqueda y ver todos los productos
- [ ] Búsqueda con mayúsculas/minúsculas

---

## 3. Carrito de Compras

### 3.1 Agregar al carrito
- [ ] Agregar producto al carrito
- [ ] Agregar mismo producto múltiples veces
- [ ] Intentar agregar más del stock disponible (debe mostrar error)
- [ ] Verificar toast de confirmación

### 3.2 Visualización del carrito
- [ ] Ver productos en el carrito
- [ ] Verificar imágenes correctas
- [ ] Verificar cantidades correctas
- [ ] Verificar precios individuales
- [ ] Verificar total del carrito

### 3.3 Modificar carrito
- [ ] Aumentar cantidad (+)
- [ ] Disminuir cantidad (-)
- [ ] Intentar aumentar más del stock (debe mostrar error)
- [ ] Eliminar producto del carrito
- [ ] Verificar actualización del total

### 3.4 Persistencia
- [ ] Agregar productos, cerrar app, abrir app → carrito debe persistir
- [ ] Login en otro dispositivo → carrito debe sincronizar (Firebase)

---

## 4. Checkout

### 4.1 Proceso de compra
- [ ] Acceder a checkout con productos en carrito
- [ ] Ver resumen de productos
- [ ] Ingresar dirección válida
- [ ] Intentar confirmar sin dirección (debe mostrar error)
- [ ] Confirmar pedido exitosamente

### 4.2 Post-compra
- [ ] Verificar que el carrito se vacía
- [ ] Verificar que el stock se reduce
- [ ] Verificar que el pedido aparece en historial
- [ ] Verificar toast de confirmación

---

## 5. Historial de Pedidos

### 5.1 Visualización
- [ ] Ver lista de pedidos
- [ ] Verificar número de pedido correcto
- [ ] Verificar fecha del pedido
- [ ] Verificar estado del pedido
- [ ] Verificar total del pedido
- [ ] Verificar miniaturas de productos

### 5.2 Múltiples pedidos
- [ ] Crear varios pedidos y verificar orden (más reciente primero)
- [ ] Verificar que cada pedido tiene su número único

---

## 6. Sistema de Tickets

### 6.1 Crear ticket
- [ ] Acceder a crear ticket
- [ ] Seleccionar pedido del dropdown (deben aparecer todos los pedidos)
- [ ] Seleccionar motivo
- [ ] Subir imagen de evidencia
- [ ] Escribir descripción
- [ ] Enviar ticket exitosamente

### 6.2 Validaciones
- [ ] Intentar enviar sin seleccionar pedido
- [ ] Intentar enviar sin seleccionar motivo
- [ ] Intentar enviar sin imagen
- [ ] Intentar enviar sin descripción
- [ ] Verificar que descripción se habilita después de subir imagen

### 6.3 Ver tickets
- [ ] Ver lista de tickets creados
- [ ] Verificar estado del ticket
- [ ] Verificar información del ticket

---

## 7. Panel de Administrador

### 7.1 Acceso
- [ ] Login con cuenta admin
- [ ] Verificar que aparece botón "Panel Admin"
- [ ] Login con cuenta cliente → no debe aparecer botón

### 7.2 Gestión de productos
- [ ] Ver lista de todos los productos
- [ ] Editar producto existente (precio, stock, nombre)
- [ ] Eliminar producto
- [ ] Verificar que cambios se reflejan en catálogo

### 7.3 Crear producto nuevo
- [ ] Crear producto con URL de imagen
- [ ] Verificar que aparece en categoría correcta
- [ ] Verificar sincronización con Firebase

---

## 8. Casos Edge / Límite

### 8.1 Stock
- [ ] Producto con stock = 0 (no debe poder agregarse)
- [ ] Comprar todo el stock de un producto
- [ ] Verificar que stock no puede ser negativo

### 8.2 Carrito vacío
- [ ] Intentar ir a checkout con carrito vacío
- [ ] Visualización de carrito vacío

### 8.3 Sin conexión
- [ ] Usar app sin internet (funcionalidad offline)
- [ ] Reconectar y verificar sincronización

### 8.4 Sesión
- [ ] Intentar crear ticket sin login
- [ ] Intentar checkout sin login

---

## 9. UI/UX

### 9.1 Navegación
- [ ] Bottom navigation funciona correctamente
- [ ] Botón back funciona en todas las pantallas
- [ ] No hay pantallas rotas o crashes

### 9.2 Visual
- [ ] Textos legibles
- [ ] Botones clickeables
- [ ] Imágenes no se cortan
- [ ] Scroll funciona donde corresponde

### 9.3 Responsividad
- [ ] Probar en diferentes tamaños de pantalla
- [ ] Rotar pantalla (si aplica)

---

## Bugs Encontrados

| # | Pantalla | Descripción | Severidad | Estado |
|---|----------|-------------|-----------|--------|
| 1 | Admin Panel, Cart | Imágenes de productos se mezclan/muestran incorrectamente. Productos con URL muestran imagen de otro producto. En carrito productos con URL muestran error 404 | Alto | CORREGIDO |
| 2 |          |             |           |        |
| 3 |          |             |           |        |

**Severidad:** Crítico / Alto / Medio / Bajo

---

## Notas de Testing

- **Cuenta Admin:** Crear con role = "admin"
- **Cuenta Cliente:** Cualquier registro normal
- **Firebase Console:** Verificar datos en Firestore
- **Limpiar datos:** Desinstalar app o borrar datos para empezar de cero
