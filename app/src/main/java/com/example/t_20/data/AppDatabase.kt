package com.example.t_20.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.t_20.R
import com.example.t_20.model.Product
import java.util.concurrent.Executors

@Database(entities = [Product::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

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
                Product(name = "Black Ring", price = 99.00, imageRes = R.drawable.ic_launcher_background, category = "accesorios"),
                Product(name = "Mate Bracelet", price = 24.99, imageRes = R.drawable.ic_launcher_background, category = "accesorios"),
                Product(name = "Military Necklace", price = 69.99, imageRes = R.drawable.ic_launcher_background, category = "accesorios"),
                Product(name = "Necklace", price = 55.75, imageRes = R.drawable.ic_launcher_background, category = "accesorios"),

                // Camisas
                Product(name = "Camisa Casual", price = 45.99, imageRes = R.drawable.ic_launcher_background, category = "camisas"),
                Product(name = "Camisa Formal", price = 59.99, imageRes = R.drawable.ic_launcher_background, category = "camisas"),
                Product(name = "Camisa Manga Corta", price = 35.50, imageRes = R.drawable.ic_launcher_background, category = "camisas"),
                Product(name = "Camisa Estampada", price = 42.00, imageRes = R.drawable.ic_launcher_background, category = "camisas"),

                // Pantalones
                Product(name = "Jean Clásico", price = 79.99, imageRes = R.drawable.ic_launcher_background, category = "pantalones"),
                Product(name = "Pantalón Formal", price = 89.99, imageRes = R.drawable.ic_launcher_background, category = "pantalones"),
                Product(name = "Jogger Deportivo", price = 55.00, imageRes = R.drawable.ic_launcher_background, category = "pantalones"),
                Product(name = "Short Casual", price = 39.99, imageRes = R.drawable.ic_launcher_background, category = "pantalones"),

                // Poleras
                Product(name = "Polera Básica", price = 25.99, imageRes = R.drawable.ic_launcher_background, category = "poleras"),
                Product(name = "Polera Estampada", price = 32.99, imageRes = R.drawable.ic_launcher_background, category = "poleras"),
                Product(name = "Polera Oversize", price = 38.50, imageRes = R.drawable.ic_launcher_background, category = "poleras"),
                Product(name = "Polera Deportiva", price = 29.99, imageRes = R.drawable.ic_launcher_background, category = "poleras"),

                // Polos
                Product(name = "Polo Clásico", price = 49.99, imageRes = R.drawable.ic_launcher_background, category = "polos"),
                Product(name = "Polo Deportivo", price = 45.00, imageRes = R.drawable.ic_launcher_background, category = "polos"),
                Product(name = "Polo Slim Fit", price = 52.99, imageRes = R.drawable.ic_launcher_background, category = "polos"),
                Product(name = "Polo Casual", price = 47.50, imageRes = R.drawable.ic_launcher_background, category = "polos")
            )
        }
    }
}
