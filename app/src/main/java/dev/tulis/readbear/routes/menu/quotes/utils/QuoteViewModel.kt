package dev.tulis.readbear.routes.menu.quotes.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.quotes.QuoteDao
import dev.tulis.readbear.db.quotes.snippets.Snippet
import dev.tulis.readbear.db.quotes.snippets.SnippetDao
import dev.tulis.readbear.db.relations.SnippetWithBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor (
    private val quoteDao: QuoteDao,
    private val snippetDao: SnippetDao,
    private val filesDir: File
) : ViewModel() {
    fun createSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetDao.insert(snippet)
        }
    }

    fun getSnippets(): Flow<List<SnippetWithBook>> {
        return snippetDao.getAllFlow()
    }
}
