package dev.tulis.readbear.routes.menu.quotes.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.entities.snippets.Snippet
import dev.tulis.readbear.db.entities.snippets.SnippetDao
import dev.tulis.readbear.db.relations.SnippetWithBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SnippetViewModel @Inject constructor (
    private val snippetDao: SnippetDao,
    private val filesDir: File
) : ViewModel() {
    fun createSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetDao.insert(snippet)
        }
    }

    fun removeSnippet(snippet: Snippet) {
        viewModelScope.launch {
            filesDir.resolve("snippets").resolve(snippet.path).delete()
            snippetDao.delete(snippet)
        }
    }

    fun getSnippetWithBookById(snippetId: Long): Flow<SnippetWithBook?> {
        return snippetDao.getWithBook(snippetId)
    }

    fun updateSnippet(snippet: Snippet) {
        viewModelScope.launch {
            snippetDao.update(snippet)
        }
    }

    fun getSnippetById(snippetId: Long): Flow<Snippet?> {
        return snippetDao.getFlow(snippetId)
    }

    fun getSnippets(): Flow<List<SnippetWithBook>> {
        return snippetDao.getAllFlow()
    }
}
