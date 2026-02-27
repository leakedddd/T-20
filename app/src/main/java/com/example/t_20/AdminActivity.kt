package com.example.t_20

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val categories = listOf("accesorios", "camisas", "pantalones", "poleras", "polos")

    private val availableImages = mapOf(
        "black_ring" to R.drawable.black_ring,
        "mate_bracelet" to R.drawable.mate_bracelet,
        "military_necklace" to R.drawable.military_necklace,
        "necklace" to R.drawable.necklace,
        "beanie" to R.drawable.beanie,
        "sunglasses" to R.drawable.sunglasses,
        "scarf" to R.drawable.scarf,
        "new_era_cap" to R.drawable.new_era_cap,
        "skyblue_shirt" to R.drawable.skyblue_shirt,
        "black_shirt" to R.drawable.black_shirt,
        "brown_shirt" to R.drawable.brown_shirt,
        "vintage" to R.drawable.vintage,
        "dark_blue_shirt" to R.drawable.dark_blue_shirt,
        "sage_shirt" to R.drawable.sage_shirt,
        "camisa_casual" to R.drawable.camisa_casual,
        "sky_blue_jeans" to R.drawable.sky_blue_jeans,
        "dark_jean" to R.drawable.dark_jean,
        "baggy_street_pants" to R.drawable.baggy_street_pants,
        "black_cargo_pants" to R.drawable.black_cargo_pants,
        "beige_cargo_pants" to R.drawable.beige_cargo_pants,
        "black_jogger" to R.drawable.black_jogger,
        "wind_pants" to R.drawable.wind_pants,
        "vintage_sweatpants" to R.drawable.vintage_sweatpants,
        "boston" to R.drawable.boston,
        "galaxy_hoodie" to R.drawable.galaxy_hoodie,
        "personality_hoodie" to R.drawable.personality_hoodie,
        "macracosm" to R.drawable.macracosm,
        "blue_sweater" to R.drawable.blue_sweater,
        "universe_hoodie" to R.drawable.universe_hoodie,
        "breakout" to R.drawable.breakout,
        "fearless" to R.drawable.fearless,
        "manchester_united" to R.drawable.manchester_united,
        "barcelona" to R.drawable.barcelona,
        "green_palm" to R.drawable.green_palm,
        "basic_gray_tshirt" to R.drawable.basic_gray_tshirt,
        "formula1" to R.drawable.formula1,
        "human_vs_human" to R.drawable.human_vs_human,
        "today_you_inspired_me" to R.drawable.today_you_inspired_me,
        "olive_jacket" to R.drawable.olive_jacket
    )

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
    }

    private fun loadProducts() {
        Executors.newSingleThreadExecutor().execute {
            val products = db.productDao().getAll()
            runOnUiThread {
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

        val imageNames = availableImages.keys.toList()
        val imageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            imageNames.map { it.replace("_", " ").replaceFirstChar { c -> c.uppercase() } }
        )
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerImage.adapter = imageAdapter

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

            val imageEntry = availableImages.entries.find { it.value == product.imageRes }
            val imageIndex = imageNames.indexOf(imageEntry?.key)
            if (imageIndex >= 0) {
                dialogBinding.spinnerImage.setSelection(imageIndex)
            }

            dialogBinding.imgPreview.visibility = View.VISIBLE
            dialogBinding.imgPreview.setImageResource(product.imageRes)
        }

        dialogBinding.spinnerImage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedImageName = imageNames[position]
                val imageRes = availableImages[selectedImageName] ?: return
                dialogBinding.imgPreview.visibility = View.VISIBLE
                dialogBinding.imgPreview.setImageResource(imageRes)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.editName.text.toString().trim()
            val priceStr = dialogBinding.editPrice.text.toString().trim()
            val originalPriceStr = dialogBinding.editOriginalPrice.text.toString().trim()
            val stockStr = dialogBinding.editStock.text.toString().trim()
            val categoryIndex = dialogBinding.spinnerCategory.selectedItemPosition
            val imageIndex = dialogBinding.spinnerImage.selectedItemPosition

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

            val originalPrice = originalPriceStr.toDoubleOrNull()
            val category = categories[categoryIndex]
            val imageRes = availableImages[imageNames[imageIndex]] ?: R.drawable.black_ring

            val newProduct = Product(
                id = product?.id ?: 0,
                name = name,
                price = price,
                originalPrice = originalPrice,
                imageRes = imageRes,
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
