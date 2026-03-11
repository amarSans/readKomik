package com.tugasmobile.readkomik.page.comic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tugasmobile.readkomik.R
import com.tugasmobile.readkomik.adapter.PdfAdapter
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.databinding.ActivityComicBinding
import com.tugasmobile.readkomik.page.pdf.PdfReaderActivity

class ComicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityComicBinding
    private lateinit var pdfAdapter: PdfAdapter
    private val prefs by lazy { getSharedPreferences("pdf_prefs", MODE_PRIVATE) }
    private lateinit var comicViewModel: ComicViewModel
    private val comicList = mutableListOf<Comik>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        comicViewModel = ViewModelProvider(this)[ComicViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        setupRecycler()
        val folderId = intent.getIntExtra("comic_id", -1)

        if (folderId != -1) {
            comicViewModel.getComicsByFolder(folderId).observe(this) { comics ->
                comicList.clear()
                comicList.addAll(comics)
                pdfAdapter.notifyDataSetChanged()
            }
        }


        val savedUri = prefs.getString("folder_uri", null)


        setSupportActionBar(binding.toolbar)




    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun bukaPdf(comik: Comik) {
        val intent = Intent(this, PdfReaderActivity::class.java).apply {
            putExtra("comic_id", comik.id)
        }
        startActivity(intent)
    }
    private fun lastRead() {
        val prefs = getSharedPreferences("pdf_prefs", MODE_PRIVATE)
        val lastId = prefs.getInt("last_comic_id", -1)

        if (lastId == -1) return

        val intent = Intent(this, PdfReaderActivity::class.java)
        intent.putExtra("comic_id", lastId)
        startActivity(intent)
    }

    private fun setupRecycler() {
        pdfAdapter = PdfAdapter(comicList) { comic, _ ->
            bukaPdf(comic)
        }
        binding.rvPdf.layoutManager = LinearLayoutManager(this)
        binding.rvPdf.adapter = pdfAdapter


        binding.rvPdf.apply {
            layoutManager = LinearLayoutManager(this@ComicActivity)
            adapter = pdfAdapter
        }
    }


    private fun loadPdfFromFolder(folderUri: Uri) {

        contentResolver.takePersistableUriPermission(
            folderUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        prefs.edit().putString("folder_uri", folderUri.toString()).apply()

        val hasil = mutableListOf<Pair<String, Uri>>()
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

                if (mime == "application/pdf")
                {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                        folderUri,
                        docId
                    )
                    hasil.add(name to fileUri)

                    val comic = Comik(
                        pdfUrl = fileUri.toString(),
                        judul = name,
                        progress = 0,
                        totalHalaman = 0,
                        folderId = 0
                    )
                    comicViewModel.insert(comic)

                }
            }
        }
    }
    private fun scrollToUnread() {
        val prefs = getSharedPreferences("pdf_prefs", MODE_PRIVATE)
        val lastId = prefs.getInt("last_comic_id", -1)
        val position = comicList.indexOfFirst { comic ->
            comicList.indexOf(comic) != -1 && comic.id == lastId
        }
        if (position != -1) {
            binding.rvPdf.post{
                binding.rvPdf.smoothScrollToPosition(position)
            }
        } else {
            binding.rvPdf.smoothScrollToPosition(0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_last ->{
                scrollToUnread()
                true
            }
            R.id.action_last_read -> {
                lastRead()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }
}