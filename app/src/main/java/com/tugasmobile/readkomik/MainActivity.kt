package com.tugasmobile.readkomik

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tugasmobile.readkomik.adapter.PdfAdapter
import com.tugasmobile.readkomik.data.database.Comik
import com.tugasmobile.readkomik.databinding.ActivityMainBinding
import com.tugasmobile.readkomik.pdf.PdfReaderActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pdfAdapter: PdfAdapter
    private val prefs by lazy { getSharedPreferences("pdf_prefs", MODE_PRIVATE) }
    private lateinit var mainViewModel: ComicViewModel
    private val comicList = mutableListOf<Comik>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[ComicViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        setupRecycler()

        mainViewModel.displayComics.observe(this) { comics ->
            comicList.clear()
            comicList.addAll(comics)
            pdfAdapter.notifyDataSetChanged()
        }
        val savedUri = prefs.getString("folder_uri", null)
        if (savedUri == null) {
            openFolderLauncher.launch(null)
        }
        binding.fabTambah.setOnClickListener {
            openFolderLauncher.launch(null)
        }
        setSupportActionBar(binding.toolbar)




    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun bukaPdf(comik: Comik) {
        val intent = Intent(this, PdfReaderActivity::class.java).apply {
            putExtra("comic_id", comik.id)
            putExtra("sort_type", mainViewModel.getSortType())
        }
        startActivity(intent)
    }

    private fun setupRecycler() {
        pdfAdapter = PdfAdapter(comicList) { comic, _ ->
            bukaPdf(comic)
        }
        binding.rvPdf.layoutManager = LinearLayoutManager(this)
        binding.rvPdf.adapter = pdfAdapter


        binding.rvPdf.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pdfAdapter
        }
    }

    private val openFolderLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                mainViewModel.deleteAll()
                loadPdfFromFolder(it)
            }

        }
    private fun loadPdfFromFolder(folderUri: Uri) {

        binding.loadingLayout.visibility = View.VISIBLE
        binding.rvPdf.visibility = View.GONE
        binding.fabTambah.isEnabled = false
        binding.txtProgress.text = "Memindai folder..."

        lifecycleScope.launch {
            val pdfList = withContext(Dispatchers.IO) {
                scanPdf(folderUri)
            }

            val total = pdfList.size
            var current = 0

            for ((name, uri) in pdfList) {
                current++

                binding.txtProgress.text = "Memproses $current / $total"

                val (totalPages, thumbPath) = withContext(Dispatchers.IO) {
                    extractPdfInfo(uri)
                }

                val comic = Comik(
                    pdfUrl = uri.toString(),
                    judul = name,
                    progress = 0,
                    totalHalaman = totalPages,
                    gambar = thumbPath
                )

                mainViewModel.insert(comic)
            }

            binding.loadingLayout.visibility = View.GONE
            binding.rvPdf.visibility = View.VISIBLE
            binding.fabTambah.isEnabled = true
        }
    }
    private fun scanPdf(folderUri: Uri): List<Pair<String, Uri>> {
        val result = mutableListOf<Pair<String, Uri>>()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri)
        )

        contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val name = cursor.getString(1)
                val mime = cursor.getString(2)

                if (mime == "application/pdf") {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                        folderUri,
                        docId
                    )
                    result.add(name to fileUri)
                }
            }
        }

        return result
    }
    private fun extractPdfInfo(uri: Uri): Pair<Int, String?> {
        val fileDescriptor =
            contentResolver.openFileDescriptor(uri, "r") ?: return 0 to null

        val renderer = PdfRenderer(fileDescriptor)
        val totalPages = renderer.pageCount

        val page = renderer.openPage(0)

        val bitmap = Bitmap.createBitmap(
            page.width,
            page.height,
            Bitmap.Config.ARGB_8888
        )

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        val fileName = "thumb_${System.currentTimeMillis()}.png"
        val file = File(filesDir, fileName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        page.close()
        renderer.close()
        fileDescriptor.close()

        return totalPages to file.absolutePath
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_sort_name -> {
                mainViewModel.setSortType(2)
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }




}