package com.tugasmobile.readkomik.pdf

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.tugasmobile.readkomik.ComicViewModel
import com.tugasmobile.readkomik.R
import com.tugasmobile.readkomik.data.database.Comik
import com.tugasmobile.readkomik.databinding.ActivityPdfReaderBinding
import kotlinx.coroutines.launch


class PdfReaderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfReaderBinding

    private var comicList: List<Comik> = emptyList()
    private var lastSavedPage = -1
    private var totalPageSaved = false
    private var currentIndex = 0
    private var currentComicID: Int = -1

    private var isAutoScrollEnabled = false
    private val autoScrollHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val AUTO_SCROLL_DELAY_MS = 16L
    private val SCROLL_SPEED = 2f

    private lateinit var comicViewModel: ComicViewModel
    private var isAppbar = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        comicViewModel = ViewModelProvider(this).get(ComicViewModel::class.java)
        setSupportActionBar(binding.toolbar)

        val toolbar = binding.toolbar
        val intentComicID = intent.getIntExtra("comic_id", -1)
        if (intentComicID == -1) return
        val sortType = intent.getIntExtra("sort_type", 0)
        comicViewModel.setSortType(sortType)
        comicViewModel.displayComics.observe(this) { list ->
            comicList = list

            currentIndex = comicList.indexOfFirst {
                it.id == currentComicID
            }.coerceAtLeast(0)
        }
        loadPdf(intentComicID)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.visibility = View.INVISIBLE
        isAppbar = false

        binding.btnAutoScroll.setOnClickListener {
            toggleAutoScroll(!isAutoScrollEnabled)

        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pdf_reader, menu)
        return true
    }
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (!isAutoScrollEnabled) return

            val currentY = binding.pdfView.currentYOffset
            val currentX = binding.pdfView.currentXOffset
            val nexty = currentY - SCROLL_SPEED
            binding.pdfView.moveTo(currentX,nexty)
            binding.pdfView.loadPages()
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS)

        }

    }
    private fun toggleAutoScroll(enable: Boolean) {
        isAutoScrollEnabled = enable
        if(enable) {
            binding.btnAutoScroll.text = "Berhenti Auto Scroll"
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS)
        } else {
            binding.btnAutoScroll.text = "Mulai Auto Scroll"
            autoScrollHandler.removeCallbacks(autoScrollRunnable)
        }
    }
    private fun stopEverything() {
        toggleAutoScroll(false)
    }

    private fun loadPdf(comicID: Int) {
        if (comicID == currentComicID) return
        stopEverything()
        saveCurrentProgress()
        currentComicID = comicID
        lastSavedPage = -1
        totalPageSaved = false
        binding.pdfView.recycle()
        lifecycleScope.launch {
            val comic= comicViewModel.getComicById(comicID)?:return@launch
            val uri = comic.pdfUrl?.toUri()

            binding.pdfView.fromUri(uri)
                .defaultPage(comic.progress)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(false)
                .onLoad { totalPages ->
                    if (comic.totalHalaman == 0 && !totalPageSaved) {
                        comicViewModel.updateTotalHalaman(comicID, totalPages)
                        totalPageSaved = true
                    }
                }
                .onPageChange { page, _ ->
                    if (page != lastSavedPage) {
                        lastSavedPage = page
                    }
                }
                .onTap {
                    binding.toolbar.visibility =
                        if (isAppbar) View.INVISIBLE else View.VISIBLE
                    isAppbar = !isAppbar
                    true
                }
                .load()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> {
                finish()
                true
            }

            R.id.action_next -> {
                if (currentIndex < comicList.lastIndex) {
                    currentIndex++
                    loadPdf(comicList[currentIndex].id)
                }
                true
            }

            R.id.action_prev -> {
                if (currentIndex > 0) {
                    currentIndex--
                    loadPdf(comicList[currentIndex].id)
                }
                true
            }

            R.id.action_list -> {
                showPdfListDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showPdfListDialog() {
        val names = comicList.map { it.judul ?: "Tanpa Judul" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Daftar Bacaan")
            .setSingleChoiceItems(
                names,
                currentIndex
            ) { dialog, which ->
                if (which != currentIndex) {
                    saveCurrentProgress()
                    currentIndex = which
                    loadPdf(comicList[currentIndex].id)
                }
                dialog.dismiss()
            }
            .show()
    }
    override fun onStop() {
        super.onStop()
        stopEverything()
        saveCurrentProgress()

    }
    private fun saveCurrentProgress() {
        if (currentComicID != -1 && lastSavedPage >= 0) {
            comicViewModel.updateProgress(currentComicID, lastSavedPage)
        }
    }




}