package com.tugasmobile.readkomik

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.tugasmobile.readkomik.adapter.FolderAdapter
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik
import com.tugasmobile.readkomik.databinding.ActivityMainBinding
import com.tugasmobile.readkomik.page.comic.ComicActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var mainViewModel: MainViewModel
    private val folderList = mutableListOf<FolderComik>()
    private var animator: ObjectAnimator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        setupRecycler()


        setSupportActionBar(binding.toolbar)

        binding.refresh.setOnClickListener {
            folderPicker.launch(null)
        }
        mainViewModel.displayFolder.observe(this) { folders ->
            folderList.clear()
            folderList.addAll(folders)
            folderAdapter.notifyDataSetChanged()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun bukaFolder(folder: FolderComik) {
        val intent = Intent(this, ComicActivity::class.java).apply {
            putExtra("comic_id", folder.idFolder)
        }
        startActivity(intent)
    }


    private fun setupRecycler() {
        folderAdapter = FolderAdapter(folderList) { folder, _ ->
            bukaFolder(folder)
        }
        binding.rvPdf.layoutManager = GridLayoutManager(this, 2)
        binding.rvPdf.adapter = folderAdapter

    }

    private suspend fun scanFolder(folderUri: Uri) {


        val folderMap = mutableMapOf<String, MutableList<Pair<String, Uri>>>()

        val root = DocumentFile.fromTreeUri(this, folderUri) ?: return

        fun scanRecursive(folder: DocumentFile) {
            folder.listFiles().forEach { file ->

                if (file.isDirectory) {
                    scanRecursive(file)
                } else {

                    // ✅ SAFE NAME
                    val name = file.name?.trim()
                    if (name.isNullOrEmpty()) return@forEach

                    // ✅ FILTER PDF
                    if (!name.lowercase().endsWith(".pdf")) return@forEach

                    // ✅ SAFE PARENT
                    val parent = file.parentFile?.name
                        ?.trim()
                        .takeUnless { it.isNullOrEmpty() }
                        ?: "Unknown"

                    // ✅ SAFE TITLE (tanpa .pdf)
                    val title = name.removeSuffix(".pdf").ifBlank { "No Title" }

                    // ✅ SAFE URI
                    val uri = file.uri ?: return@forEach
                    val uriString = uri.toString().ifBlank { return@forEach }

                    // DEBUG (opsional tapi disarankan)
                    Log.d("PDF_DEBUG", "TITLE: $title | PARENT: $parent")

                    if (!folderMap.containsKey(parent)) {
                        folderMap[parent] = mutableListOf()
                    }

                    folderMap[parent]!!.add(title to uri)
                }
            }
        }

        scanRecursive(root)

        // =========================
        // SIMPAN KE ROOM
        // =========================
        folderMap.forEach { (folderName, pdfs) ->

            val folder = FolderComik(
                folderName = folderName,
                folderPath = folderName,
                totalPdf = pdfs.size
            )

            val existingFolder = mainViewModel.getFolderByPath(folderName)

            val folderId = if (existingFolder != null) {
                existingFolder.idFolder
            } else {
                mainViewModel.insertFolder(folder)
            }

            pdfs.forEach { (name, uri) ->

                val existingComic = mainViewModel.getComicByUrl(uri.toString())

                if (existingComic == null) {
                    val comic = Comik(
                        folderId = folderId,
                        pdfUrl = uri.toString(),
                        judul = name,
                        progress = 0,
                        totalHalaman = 0
                    )

                    mainViewModel.insert(comic)
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_last -> {
                true
            }

            R.id.action_last_read -> {
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }
    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    scanFolder(it)
                }
            }
        }



}