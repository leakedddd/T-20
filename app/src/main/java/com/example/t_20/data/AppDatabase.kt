package com.example.t_20.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.t_20.R
import com.example.t_20.model.CartItem
import com.example.t_20.model.Product
import com.example.t_20.model.User
import java.util.concurrent.Executors

@Database(entities = [Product::class, CartItem::class, User::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "t20_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                getInstance(context).productDao().insertAll(getInitialProducts())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getInitialProducts(): List<Product> {
            return listOf(
                // Accesorios
                Product(name = "Black Ring", price = 99.00, imageRes = R.drawable.black_ring, category = "accesorios", stock = 15),
                Product(name = "Mate Bracelet", price = 24.99, imageRes = R.drawable.mate_bracelet, category = "accesorios", stock = 20),
                Product(name = "Military Necklace", price = 69.99, imageRes = R.drawable.military_necklace, category = "accesorios", stock = 8),
                Product(name = "Necklace", price = 55.75, imageRes = R.drawable.necklace, category = "accesorios", stock = 12),

                // Camisas
                Product(name = "Sky Blue Shirt", price = 45.99, imageRes = R.drawable.skyblue_shirt, category = "camisas", stock = 25),
                Product(name = "Black Shirt", price = 59.99, imageRes = R.drawable.black_shirt, category = "camisas", stock = 18),
                Product(name = "Brown Shirt", price = 35.50, imageRes = R.drawable.brown_shirt, category = "camisas", stock = 30),
                Product(name = "Vintage Shirt", price = 42.00, imageRes = R.drawable.vintage, category = "camisas", stock = 10),

                // Pantalones
                Product(name = "Sky Blue Jeans", price = 79.99, imageRes = R.drawable.sky_blue_jeans, category = "pantalones", stock = 20),
                Product(name = "Dark Jean", price = 89.99, imageRes = R.drawable.dark_jean, category = "pantalones", stock = 15),
                Product(name = "Baggy Street Pants", price = 55.00, imageRes = R.drawable.baggy_street_pants, category = "pantalones", stock = 22),
                Product(name = "Black Cargo Pants", price = 39.99, imageRes = R.drawable.black_cargo_pants, category = "pantalones", stock = 28),

                // Poleras
                Product(name = "Boston", price = 25.99, imageRes = R.drawable.boston, category = "poleras", stock = 35),
                Product(name = "Galaxy Hoodie", price = 32.99, imageRes = R.drawable.galaxy_hoodie, category = "poleras", stock = 18),
                Product(name = "Personality Hoodie", price = 38.50, imageRes = R.drawable.personality_hoodie, category = "poleras", stock = 12),
                Product(name = "Blue Sweatshirt", price = 29.99, imageRes = R.drawable.macracosm, category = "poleras", stock = 25),

                // Polos
                Product(name = "Manchester United Jersey", price = 49.99, imageRes = R.drawable.manchester_united, category = "polos", stock = 20),
                Product(name = "Barcelona Sweatshirt", price = 45.00, imageRes = R.drawable.barcelona, category = "polos", stock = 16),
                Product(name = "Green Palm T-Shirt", price = 52.99, imageRes = R.drawable.green_palm, category = "polos", stock = 14),
                Product(name = "Basic Gray T-Shirt", price = 47.50, imageRes = R.drawable.basic_gray_tshirt, category = "polos", stock = 22)
            )
        }
    }
}
