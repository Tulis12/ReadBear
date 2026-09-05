package dev.tulis.readbear.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.PdfDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val pdfDao: PdfDao
) : ViewModel() {
    suspend fun getPdf(pdfId: Long): Pdf {
        return pdfDao.get(pdfId)
    }

    fun getPdfFlow(pdfId: Long): Flow<Pdf> {
        return pdfDao.getFlow(pdfId)
    }

    fun updatePdf(pdf: Pdf) {
        viewModelScope.launch {
            pdfDao.update(pdf)
        }
    }
}