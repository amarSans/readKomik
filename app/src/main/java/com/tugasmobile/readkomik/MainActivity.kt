package com.tugasmobile.readkomik

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.tugasmobile.readkomik.adapter.FolderAdapter
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik
import com.tugasmobile.readkomik.databinding.ActivityMainBinding
import com.tugasmobile.readkomik.page.comic.ComicActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var mainViewModel: MainViewModel
    private val folderList = mutableListOf<FolderComik>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        setupRecycler()

        mainViewModel.displayFolder.observe(this) { comics ->
            folderList.clear()
            folderList.addAll(comics)
            folderAdapter.notifyDataSetChanged()
        }
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            startRefreshAnimation()
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                scanAllPdf()
                runOnUiThread { stopRefreshAnimation() }
            }
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }
        setSupportActionBar(binding.toolbar)

        binding.refresh.setOnClickListener {
            startRefreshAnimation()
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                scanAllPdf()
                runOnUiThread { stopRefreshAnimation() }
            }
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


    private suspend fun scanAllPdf() {

        val folderMap = mutableMapOf<String, MutableList<Pair<String, Uri>>>()

        val projection =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
            } else {
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
            }

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE}=?"
        val selectionArgs = arrayOf("application/pdf")

        val collection = MediaStore.Files.getContentUri("external")

        contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Files.FileColumns._ID
            )

            val nameColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Files.FileColumns.DISPLAY_NAME
            )

            val pathColumn =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                } else {
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                }

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val fullPath = cursor.getString(pathColumn) ?: continue
                val fileName = fullPath.substringAfterLast("/")
                    .removeSuffix(".pdf")
                val folderPath = fullPath.substringBeforeLast("/")
                val uri = Uri.withAppendedPath(collection, id.toString())

                if (!folderMap.containsKey(folderPath)) {
                    folderMap[folderPath] = mutableListOf()
                }

                folderMap[folderPath]!!.add(fileName to uri)
            }
        }

        folderMap.forEach { (folderPath, pdfs) ->

            val folder = FolderComik(
                folderName = folderPath.substringAfterLast("/"),
                folderPath = folderPath,
                totalPdf = pdfs.size
            )

            val existingFolder = mainViewModel.getFolderByPath(folderPath)

            val folderId = if (existingFolder != null) {
                existingFolder.idFolder
            } else {
                mainViewModel.insertFolder(folder)
            }
            pdfs.forEach { (name, uri) ->
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

    private fun startRefreshAnimation() {
        val refreshAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation)
        binding.refresh.startAnimation(refreshAnimation)
    }

    private fun stopRefreshAnimation() {
        binding.refresh.clearAnimation()
    }

}