package com.example.t_20.data

import com.example.t_20.model.CartItem
import com.example.t_20.model.Order
import com.example.t_20.model.OrderItem
import com.example.t_20.model.Product
import com.example.t_20.model.Ticket
import com.example.t_20.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // ==================== USERS ====================
    suspend fun saveUser(user: User): String {
        val userMap = hashMapOf(
            "name" to user.name,
            "email" to user.email,
            "password" to user.password,
            "localId" to user.id
        )
        val docRef = db.collection("users").document(user.email)
        docRef.set(userMap, SetOptions.merge()).await()
        return user.email
    }

    suspend fun getUserByEmail(email: String): User? {
        val doc = db.collection("users").document(email).get().await()
        return if (doc.exists()) {
            User(
                id = (doc.getLong("localId") ?: 0).toInt(),
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                password = doc.getString("password") ?: ""
            )
        } else null
    }

    suspend fun loginUser(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user != null && user.password == password) user else null
    }

    // ==================== CART ====================
    suspend fun saveCartItem(userEmail: String, cartItem: CartItem) {
        val itemMap = hashMapOf(
            "productId" to cartItem.productId,
            "quantity" to cartItem.quantity
        )
        db.collection("users").document(userEmail)
            .collection("cart")
            .document(cartItem.productId.toString())
            .set(itemMap)
            .await()
    }

    suspend fun getCartItems(userEmail: String): List<CartItem> {
        val snapshot = db.collection("users").document(userEmail)
            .collection("cart")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            CartItem(
                productId = (doc.getLong("productId") ?: return@mapNotNull null).toInt(),
                quantity = (doc.getLong("quantity") ?: 1).toInt()
            )
        }
    }

    suspend fun removeCartItem(userEmail: String, productId: Int) {
        db.collection("users").document(userEmail)
            .collection("cart")
            .document(productId.toString())
            .delete()
            .await()
    }

    suspend fun clearCart(userEmail: String) {
        val cartRef = db.collection("users").document(userEmail).collection("cart")
        val snapshot = cartRef.get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    // ==================== ORDERS ====================
    suspend fun saveOrder(userEmail: String, order: Order, items: List<OrderItem>): String {
        val orderMap = hashMapOf(
            "localId" to order.id,
            "userId" to order.userId,
            "date" to order.date,
            "total" to order.total,
            "address" to order.address,
            "status" to order.status
        )
        val orderRef = db.collection("users").document(userEmail)
            .collection("orders")
            .add(orderMap)
            .await()

        items.forEach { item ->
            val itemMap = hashMapOf(
                "localId" to item.id,
                "orderId" to item.orderId,
                "productName" to item.productName,
                "productPrice" to item.productPrice,
                "quantity" to item.quantity,
                "productImageRes" to item.productImageRes
            )
            orderRef.collection("items").add(itemMap).await()
        }
        return orderRef.id
    }

    suspend fun getOrders(userEmail: String): List<Pair<Order, List<OrderItem>>> {
        val ordersSnapshot = db.collection("users").document(userEmail)
            .collection("orders")
            .get()
            .await()

        return ordersSnapshot.documents.mapNotNull { orderDoc ->
            val order = Order(
                id = (orderDoc.getLong("localId") ?: 0).toInt(),
                userId = (orderDoc.getLong("userId") ?: 0).toInt(),
                date = orderDoc.getLong("date") ?: System.currentTimeMillis(),
                total = orderDoc.getDouble("total") ?: 0.0,
                address = orderDoc.getString("address") ?: "",
                status = orderDoc.getString("status") ?: "Confirmado"
            )

            val itemsSnapshot = orderDoc.reference.collection("items").get().await()
            val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                OrderItem(
                    id = (itemDoc.getLong("localId") ?: 0).toInt(),
                    orderId = (itemDoc.getLong("orderId") ?: 0).toInt(),
                    productName = itemDoc.getString("productName") ?: "",
                    productPrice = itemDoc.getDouble("productPrice") ?: 0.0,
                    quantity = (itemDoc.getLong("quantity") ?: 1).toInt(),
                    productImageRes = (itemDoc.getLong("productImageRes") ?: 0).toInt()
                )
            }
            Pair(order, items)
        }
    }

    // ==================== PRODUCTS ====================
    suspend fun syncProducts(products: List<Product>) {
        products.forEach { product ->
            val productMap = hashMapOf(
                "localId" to product.id,
                "name" to product.name,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "imageRes" to product.imageRes,
                "category" to product.category,
                "stock" to product.stock
            )
            db.collection("products")
                .document(product.id.toString())
                .set(productMap, SetOptions.merge())
                .await()
        }
    }

    suspend fun getProducts(): List<Product> {
        val snapshot = db.collection("products").get().await()
        return snapshot.documents.mapNotNull { doc ->
            Product(
                id = (doc.getLong("localId") ?: 0).toInt(),
                name = doc.getString("name") ?: return@mapNotNull null,
                price = doc.getDouble("price") ?: 0.0,
                originalPrice = doc.getDouble("originalPrice"),
                imageRes = (doc.getLong("imageRes") ?: 0).toInt(),
                category = doc.getString("category") ?: "",
                stock = (doc.getLong("stock") ?: 0).toInt()
            )
        }
    }

    // ==================== TICKETS ====================
    suspend fun saveTicket(userEmail: String, ticket: Ticket): String {
        val ticketMap = hashMapOf(
            "localId" to ticket.id,
            "userId" to ticket.userId,
            "orderId" to ticket.orderId,
            "motivo" to ticket.motivo,
            "descripcion" to ticket.descripcion,
            "imagePath" to ticket.imagePath,
            "fecha" to ticket.fecha,
            "status" to ticket.status
        )
        val docRef = db.collection("users").document(userEmail)
            .collection("tickets")
            .add(ticketMap)
            .await()
        return docRef.id
    }

    suspend fun getTickets(userEmail: String): List<Ticket> {
        val snapshot = db.collection("users").document(userEmail)
            .collection("tickets")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            Ticket(
                id = (doc.getLong("localId") ?: 0).toInt(),
                userId = (doc.getLong("userId") ?: 0).toInt(),
                orderId = (doc.getLong("orderId") ?: 0).toInt(),
                motivo = doc.getString("motivo") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                imagePath = doc.getString("imagePath") ?: "",
                fecha = doc.getLong("fecha") ?: System.currentTimeMillis(),
                status = doc.getString("status") ?: "Pendiente"
            )
        }
    }

    suspend fun updateTicketStatus(userEmail: String, ticketLocalId: Int, newStatus: String) {
        val snapshot = db.collection("users").document(userEmail)
            .collection("tickets")
            .whereEqualTo("localId", ticketLocalId)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.reference?.update("status", newStatus)?.await()
    }
}
