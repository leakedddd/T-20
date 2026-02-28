package com.example.t_20

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: AdminProductAdapter
    private val firebaseRepo = FirebaseRepository()
    private val storage = FirebaseStorage.getInstance()

    private var allProducts = listOf<Product>()
    private var selectedImageUri: Uri? = null
    private var currentDialogBinding: DialogProductFormBinding? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentDialogBinding?.imgPreview?.visibility = View.VISIBLE
            currentDialogBinding?.imgPreview?.load(it)
        }
    }

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
        Executors.newSingleThreadExecutor().execute {
            val products = db.productDao().getAll()
            allProducts = products
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
        currentDialogBinding = dialogBinding
        selectedImageUri = null
        val isEdit = product != null

        dialogBinding.txtDialogTitle.text = if (isEdit) "Editar Producto" else "Nuevo Producto"

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories.map { it.replaceFirstChar { c -> c.uppercase() } }
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        dialogBinding.btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
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

            dialogBinding.imgPreview.visibility = View.VISIBLE
            if (!product.imageUrl.isNullOrEmpty()) {
                dialogBinding.imgPreview.load(product.imageUrl)
            } else if (product.imageRes != 0) {
                dialogBinding.imgPreview.setImageResource(product.imageRes)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            currentDialogBinding = null
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.editName.text.toString().trim()
            val priceStr = dialogBinding.editPrice.text.toString().trim()
            val originalPriceStr = dialogBinding.editOriginalPrice.text.toString().trim()
            val stockStr = dialogBinding.editStock.text.toString().trim()
            val categoryIndex = dialogBinding.spinnerCategory.selectedItemPosition

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

            if (!isEdit && selectedImageUri == null) {
                Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val originalPrice = originalPriceStr.toDoubleOrNull()
            val category = categories[categoryIndex]

            dialogBinding.btnSave.isEnabled = false
            dialogBinding.btnSave.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val imageUrl = if (selectedImageUri != null) {
                        withContext(Dispatchers.IO) {
                            uploadImageToStorage(selectedImageUri!!, name)
                        }
                    } else {
                        product?.imageUrl
                    }

                    val newProduct = Product(
                        id = product?.id ?: 0,
                        name = name,
                        price = price,
                        originalPrice = originalPrice,
                        imageRes = product?.imageRes ?: 0,
                        imageUrl = imageUrl,
                        category = category,
                        stock = stock
                    )

                    withContext(Dispatchers.IO) {
                        if (isEdit) {
                            db.productDao().update(newProduct)
                        } else {
                            db.productDao().insert(newProduct)
                        }
                    }

                    Toast.makeText(
                        this@AdminActivity,
                        if (isEdit) "Producto actualizado" else "Producto agregado",
                        Toast.LENGTH_SHORT
                    ).show()
                    currentDialogBinding = null
                    dialog.dismiss()
                    loadProducts()
                    syncToFirebase()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AdminActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    dialogBinding.btnSave.isEnabled = true
                    dialogBinding.btnSave.text = "Guardar"
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

    private suspend fun uploadImageToStorage(uri: Uri, productName: String): String {
        val fileName = "products/${System.currentTimeMillis()}_${productName.replace(" ", "_")}.jpg"
        val ref = storage.reference.child(fileName)

        return suspendCoroutine { cont ->
            ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUrl ->
                    cont.resume(downloadUrl.toString())
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
    }
}
