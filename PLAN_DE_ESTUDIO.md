# Plan de Estudio - App T-20 (7 horas)

## Resumen de la App
Una tienda de ropa con:
- Login/Registro de usuarios
- Catálogo de productos por categorías
- Carrito de compras
- Checkout y órdenes
- Sistema de tickets/reclamos
- Panel de administrador
- Sincronización con Firebase

---

## HORA 1: Modelos de Datos (model/*.kt)

### Conceptos clave que debes saber:
- `@Entity` = Tabla en la base de datos Room
- `@PrimaryKey` = Identificador único
- `data class` = Clase que solo guarda datos

### Archivos a estudiar:

#### 1. Product.kt
```kotlin
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val imageRes: Int = 0,       // Imagen local (drawable)
    val imageUrl: String?,       // Imagen de internet (opcional)
    val category: String,
    val stock: Int = 10
)
```
**Pregunta típica:** ¿Qué es @Entity? R: Define una tabla en Room.

#### 2. User.kt
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String = "cliente"  // "cliente" o "admin"
)
```
**Pregunta típica:** ¿Cómo diferencias admin de cliente? R: Por el campo `role`.

#### 3. CartItem.kt
```kotlin
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val quantity: Int = 1
)
```
**Nota:** Solo guarda el ID del producto y cantidad, no toda la info.

#### 4. Order.kt y OrderItem.kt
- `Order` = La orden completa (fecha, total, dirección, estado)
- `OrderItem` = Cada producto dentro de la orden

#### 5. Ticket.kt
- Para reclamos/soporte del usuario

---

## HORA 2: Base de Datos Room (data/*.kt)

### Conceptos clave:
- `Room` = Base de datos local de Android (SQLite simplificado)
- `DAO` = Data Access Object (métodos para acceder a la BD)
- `@Query`, `@Insert`, `@Update`, `@Delete` = Operaciones SQL

### Archivos a estudiar:

#### 1. AppDatabase.kt
```kotlin
@Database(
    entities = [Product::class, User::class, CartItem::class, Order::class, OrderItem::class, Ticket::class],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    // ... más DAOs

    companion object {
        // Patrón Singleton - una sola instancia
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(...)
                    .fallbackToDestructiveMigration()  // Si cambia versión, borra y recrea
                    .build()
            }
        }
    }
}
```
**Pregunta típica:** ¿Qué es Singleton? R: Patrón que garantiza una sola instancia.

#### 2. ProductDao.kt
```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getByCategory(category: String): List<Product>

    @Insert
    fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(product: Product)  // Insert o Update

    @Delete
    fun delete(product: Product)
}
```
**Pregunta típica:** ¿Qué hace upsert? R: Si existe actualiza, si no existe inserta.

#### 3. CartDao.kt, UserDao.kt, OrderDao.kt
- Misma lógica que ProductDao pero para otras tablas

---

## HORA 3: Firebase (data/FirebaseRepository.kt)

### Conceptos clave:
- `Firebase Firestore` = Base de datos en la nube de Google
- `suspend fun` = Función que puede pausarse (coroutines)
- `await()` = Espera el resultado de Firebase

### Estructura en Firebase:
```
/products/{productId}          → Productos
/users/{email}/cart/{id}       → Carrito del usuario
/users/{email}/orders/{id}     → Órdenes del usuario
/users/{email}/tickets/{id}    → Tickets del usuario
```

### Métodos principales:
```kotlin
class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // PRODUCTOS
    suspend fun syncProducts(products: List<Product>) {
        products.forEach { product ->
            db.collection("products")
                .document(product.id.toString())
                .set(productMap, SetOptions.merge())  // Merge = no sobreescribe todo
                .await()
        }
    }

    suspend fun getProducts(): List<Product> {
        val snapshot = db.collection("products").get().await()
        return snapshot.documents.mapNotNull { doc ->
            // Convertir documento a Product
        }
    }

    suspend fun deleteProduct(productId: Int) {
        db.collection("products")
            .document(productId.toString())
            .delete()
            .await()
    }

    // CARRITO, ÓRDENES, TICKETS - misma lógica
}
```

**Pregunta típica:** ¿Por qué usas Room Y Firebase?
R: Room = offline/rápido, Firebase = sincronización entre dispositivos.

---

## HORA 4: Flujo de Login/Registro

### LoginActivity.kt
```kotlin
// 1. Usuario ingresa email y password
// 2. Busca en Room: userDao.login(email, password)
// 3. Si existe y es admin → AdminActivity
// 4. Si existe y es cliente → MainActivity
// 5. Si no existe → Error
```

### RegisterActivity.kt
```kotlin
// 1. Usuario ingresa datos
// 2. Verifica que email no exista
// 3. Inserta en Room: userDao.insert(user)
// 4. Sube a Firebase: firebaseRepo.saveUserProfile(user)
// 5. Guarda sesión en SharedPreferences
```

### SharedPreferences (guardar sesión):
```kotlin
val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
prefs.edit()
    .putInt("user_id", user.id)
    .putString("user_email", user.email)
    .putString("user_role", user.role)
    .apply()
```

**Pregunta típica:** ¿Cómo mantienes la sesión? R: SharedPreferences.

---

## HORA 5: MainActivity y Fragments

### MainActivity.kt
```kotlin
// Navegación con BottomNavigation
// 4 fragments: Home, FAQ, Account, Cart

private fun setupBottomNavigation() {
    binding.bottomNavigation.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> loadFragment(homeFragment)
            R.id.nav_faq -> loadFragment(faqFragment)
            R.id.nav_account -> loadFragment(accountFragment)
            R.id.nav_cart -> loadFragment(cartFragment)
        }
    }
}

// Al iniciar, sincroniza productos con Firebase
onCreate {
    // 1. Descarga productos de Firebase
    // 2. Los guarda en Room
    // 3. Sube productos locales a Firebase
}
```

### HomeFragment.kt
- Muestra productos en RecyclerView
- Filtro por categorías (Spinner)
- Búsqueda por nombre
- Botón para agregar al carrito

### CartFragment.kt
- Lista productos del carrito
- Modificar cantidad (+/-)
- Eliminar productos
- Botón "Proceder al pago" → CheckoutActivity

### AccountFragment.kt
- Muestra nombre y email del usuario
- Lista de órdenes anteriores
- Opción de cerrar sesión

### FaqFragment.kt
- Lista de preguntas frecuentes (expandibles)

---

## HORA 6: Carrito y Checkout

### Flujo del carrito:
```
1. HomeFragment: Click "Agregar al carrito"
   → cartDao.insert(CartItem(productId, quantity))
   → firebaseRepo.saveCartItem(email, cartItem)

2. CartFragment: Muestra productos
   → cartDao.getCartWithProducts() // JOIN con productos

3. CartFragment: Click "Proceder al pago"
   → Intent a CheckoutActivity con lista de productos
```

### CheckoutActivity.kt
```kotlin
// 1. Muestra resumen de productos
// 2. Usuario ingresa dirección
// 3. Click "Confirmar"
//    → Crea Order en Room
//    → Crea OrderItems en Room
//    → Sube a Firebase
//    → Limpia carrito
//    → Resta stock de productos
```

### Actualizar stock:
```kotlin
products.forEach { item ->
    val product = productDao.getById(item.productId)
    val newStock = product.stock - item.quantity
    productDao.updateStock(item.productId, newStock)
}
```

---

## HORA 7: Panel de Admin y Adapters

### AdminActivity.kt
```kotlin
// CRUD de productos
// - Ver lista de productos
// - Agregar nuevo producto (FAB +)
// - Editar producto (click en lápiz)
// - Eliminar producto (click en basura)

// Cada cambio:
// 1. Actualiza Room
// 2. Sincroniza con Firebase
```

### Adapters (RecyclerView):
```kotlin
// Patrón ViewHolder
class ProductAdapter(
    private val onAddToCart: (Product) -> Unit  // Callback
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var products = listOf<Product>()

    // Crea la vista
    override fun onCreateViewHolder(parent, viewType): ViewHolder {
        val binding = ItemProductBinding.inflate(...)
        return ViewHolder(binding)
    }

    // Llena datos en la vista
    override fun onBindViewHolder(holder, position) {
        val product = products[position]
        holder.binding.txtName.text = product.name
        holder.binding.txtPrice.text = "$${product.price}"
        holder.binding.btnAdd.setOnClickListener {
            onAddToCart(product)  // Llama al callback
        }
    }

    // Actualiza lista
    fun updateProducts(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }
}
```

**Pregunta típica:** ¿Por qué usas RecyclerView? R: Eficiente para listas largas, recicla vistas.

---

## Conceptos Clave para el Examen

### 1. Arquitectura
```
Vista (Activity/Fragment)
    ↓ llama a
Base de Datos (Room DAO)
    ↓ sincroniza con
Firebase (FirebaseRepository)
```

### 2. Coroutines
```kotlin
lifecycleScope.launch {           // Inicia coroutine
    withContext(Dispatchers.IO) { // Cambia a hilo de fondo
        // Operación de BD o red
    }
    // Aquí vuelve al hilo principal (UI)
}
```

### 3. ViewBinding
```kotlin
// En lugar de findViewById
binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)

// Acceder a vistas:
binding.btnLogin.setOnClickListener { ... }
binding.editEmail.text.toString()
```

### 4. Room vs Firebase
| Room | Firebase |
|------|----------|
| Local | En la nube |
| Rápido | Requiere internet |
| Offline | Sincronización |
| SQLite | NoSQL (documentos) |

### 5. Flujos principales
- **Login:** LoginActivity → (admin) AdminActivity / (cliente) MainActivity
- **Compra:** HomeFragment → CartFragment → CheckoutActivity → Order creada
- **Admin:** AdminActivity → CRUD productos → Sync Firebase

---

## Preguntas que te pueden hacer

1. **¿Qué tecnologías usaste?**
   - Kotlin, Room, Firebase Firestore, RecyclerView, ViewBinding, Coroutines

2. **¿Por qué Room y Firebase juntos?**
   - Room para velocidad y offline, Firebase para sincronizar entre dispositivos

3. **¿Cómo funciona el login?**
   - Busca en Room, verifica password, guarda sesión en SharedPreferences

4. **¿Cómo agregas un producto al carrito?**
   - Inserta CartItem en Room y en Firebase

5. **¿Qué pasa al hacer checkout?**
   - Crea Order, OrderItems, sube a Firebase, limpia carrito, resta stock

6. **¿Cómo diferencias admin de cliente?**
   - Campo `role` en User: "admin" o "cliente"

7. **¿Qué es un DAO?**
   - Data Access Object, interfaz con métodos para acceder a la BD

8. **¿Qué son las coroutines?**
   - Forma de hacer operaciones asíncronas sin bloquear la UI

---

## Tips finales

1. **Ejecuta la app** mientras estudias - ve qué hace cada pantalla
2. **Sigue el flujo** de una compra completa
3. **Entiende la relación** Room ↔ Firebase
4. **No memorices** - entiende el POR QUÉ de cada cosa

¡Éxito en tu examen!
