package com.example.t_20

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.t_20.adapter.AdminProductAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.data.FirebaseRepository
import com.example.t_20.databinding.ActivityAdminBinding
import com.example.t_20.databinding.DialogProductFormBinding
import com.example.t_20.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: AdminProductAdapter
    private val firebaseRepo = FirebaseRepository()

    private var allProducts = listOf<Product>()

    private val categories = listOf("accesorios", "camisas", "pantalones", "poleras", "polos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getInstance(this)

        setupRecyclerView()
        setupListeners()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(
            onEditClick = { product -> showProductDialog(product) },
            onDeleteClick = { product -> showDeleteConfirmation(product) }
        )
        binding.recyclerProducts.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = this@AdminActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.fabAdd.setOnClickListener { showProductDialog(null) }
        setupSearch()
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                if (query.isEmpty()) {
                    adapter.updateProducts(allProducts)
                } else {
                    val filtered = allProducts.filter {
                        it.name.lowercase().contains(query) ||
                        it.category.lowercase().contains(query)
                    }
                    adapter.updateProducts(filtered)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                // First sync from Firebase
                val firebaseProducts = withContext(Dispatchers.IO) {
                    firebaseRepo.getProducts()
                }
                if (firebaseProducts.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        db.productDao().upsertAll(firebaseProducts)
                    }
                }

                // Then load all products
                val products = withContext(Dispatchers.IO) {
                    db.productDao().getAll()
                }
                allProducts = products

                if (products.isEmpty()) {
                    binding.txtEmpty.visibility = View.VISIBLE
                    binding.recyclerProducts.visibility = View.GONE
                } else {
                    binding.txtEmpty.visibility = View.GONE
                    binding.recyclerProducts.visibility = View.VISIBLE
                    adapter.updateProducts(products)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: load only local products
                val products = withContext(Dispatchers.IO) {
                    db.productDao().getAll()
                }
                allProducts = products
                if (products.isEmpty()) {
                    binding.txtEmpty.visibility = View.VISIBLE
                    binding.recyclerProducts.visibility = View.GONE
                } else {
                    binding.txtEmpty.visibility = View.GONE
                    binding.recyclerProducts.visibility = View.VISIBLE
                    adapter.updateProducts(products)
                }
            }
        }
    }

    private fun showProductDialog(product: Product?) {
        val dialogBinding = DialogProductFormBinding.inflate(LayoutInflater.from(this))
        val isEdit = product != null

        dialogBinding.txtDialogTitle.text = if (isEdit) "Editar Producto" else "Nuevo Producto"

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories.map { it.replaceFirstChar { c -> c.uppercase() } }
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        // Preview button para ver la imagen antes de guardar
        dialogBinding.btnPreviewImage.setOnClickListener {
            val url = dialogBinding.editImageUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                dialogBinding.imgPreview.visibility = View.VISIBLE
                dialogBinding.imgPreview.load(url) {
                    error(R.drawable.black_ring)
                }
            } else {
                Toast.makeText(this, "Ingresa una URL primero", Toast.LENGTH_SHORT).show()
            }
        }

        if (isEdit) {
            dialogBinding.editName.setText(product!!.name)
            dialogBinding.editPrice.setText(product.price.toString())
            product.originalPrice?.let {
                dialogBinding.editOriginalPrice.setText(it.toString())
            }
            dialogBinding.editStock.setText(product.stock.toString())

            val categoryIndex = categories.indexOf(product.category)
            if (categoryIndex >= 0) {
                dialogBinding.spinnerCategory.setSelection(categoryIndex)
            }

            // Mostrar URL existente
            if (!product.imageUrl.isNullOrEmpty()) {
                dialogBinding.editImageUrl.setText(product.imageUrl)
                dialogBinding.imgPreview.visibility = View.VISIBLE
                dialogBinding.imgPreview.load(product.imageUrl)
            } else if (product.imageRes != 0) {
                dialogBinding.imgPreview.visibility = View.VISIBLE
                dialogBinding.imgPreview.setImageResource(product.imageRes)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.editName.text.toString().trim()
            val priceStr = dialogBinding.editPrice.text.toString().trim()
            val originalPriceStr = dialogBinding.editOriginalPrice.text.toString().trim()
            val stockStr = dialogBinding.editStock.text.toString().trim()
            val categoryIndex = dialogBinding.spinnerCategory.selectedItemPosition
            val imageUrl = dialogBinding.editImageUrl.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0) {
                Toast.makeText(this, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stock = stockStr.toIntOrNull()
            if (stock == null || stock < 0) {
                Toast.makeText(this, "El stock debe ser mayor o igual a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que haya imagen (URL o existente)
            val finalImageUrl = if (imageUrl.isNotEmpty()) {
                imageUrl
            } else {
                product?.imageUrl
            }

            if (finalImageUrl.isNullOrEmpty() && (product?.imageRes ?: 0) == 0) {
                Toast.makeText(this, "Debes ingresar una URL de imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val originalPrice = originalPriceStr.toDoubleOrNull()
            val category = categories[categoryIndex]

            val newProduct = Product(
                id = product?.id ?: 0,
                name = name,
                price = price,
                originalPrice = originalPrice,
                imageRes = product?.imageRes ?: 0,
                imageUrl = finalImageUrl,
                category = category,
                stock = stock
            )

            Executors.newSingleThreadExecutor().execute {
                if (isEdit) {
                    db.productDao().update(newProduct)
                } else {
                    db.productDao().insert(newProduct)
                }

                runOnUiThread {
                    Toast.makeText(
                        this,
                        if (isEdit) "Producto actualizado" else "Producto agregado",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    loadProducts()
                    syncToFirebase()
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${product.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                Executors.newSingleThreadExecutor().execute {
                    db.productDao().delete(product)
                    runOnUiThread {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        loadProducts()
                        syncToFirebase()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun syncToFirebase() {
        lifecycleScope.launch {
            try {
                val products = withContext(Dispatchers.IO) { db.productDao().getAll() }
                withContext(Dispatchers.IO) { firebaseRepo.syncProducts(products) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
