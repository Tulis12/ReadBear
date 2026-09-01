package dev.tulis.readbear

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.compose.ThemeType
import com.example.compose.ReadBearTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import dagger.hilt.android.AndroidEntryPoint
import io.github.yuroyami.kitepdf.PdfDocument
import io.github.yuroyami.kitepdf.content.ContentStreamParser
import io.github.yuroyami.kitepdf.core.font.FontSpec
import io.github.yuroyami.kitepdf.core.font.KiteFontFamily
import io.github.yuroyami.kitepdf.core.parser.PdfName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            enableEdgeToEdge()

            ReadBearTheme(
                themeType = ThemeType.SYSTEM
            ) {
                PDFBoxResourceLoader.init(applicationContext)
                App(navController = navController)
            }
        }
    }
}

@Composable
fun sample() {
    val context = LocalContext.current

    var document: PdfDocument? by remember { mutableStateOf(null) }
    var documentX: PDDocument? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            document = PdfDocument.open(context.filesDir.resolve("sample.pdf").readBytes())
            documentX = PDDocument.load(context.filesDir.resolve("sample.pdf").readBytes())
        }
    }

    val documentNotNull = document
    val documentXNotNull = documentX

    if(documentNotNull == null || documentXNotNull == null) {
        CircularProgressIndicator()
        return
    }

    val textLines = mutableListOf<@Composable () -> Unit>()
    val images = HashMap<String, Bitmap>()

    var isSupported by remember { mutableStateOf(true) }
    var isProbablyNotSupported = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if(!isSupported) {
            Text("Sorry boss")
            return
        }

        val pageIndex = 25

        val pageX: PDPage = documentXNotNull.getPage(pageIndex)

        val resources = pageX.resources
        resources.xObjectNames.forEach {
            println(it)

            val xObject = resources.getXObject(it)

            if (xObject is PDImageXObject) {
                try {
                    images[it.name] = xObject.image
                } catch (_: IOException) {
                    isSupported = false
                    return
                }
            }
        }

        val page = documentNotNull.pages[pageIndex]
        val operations = ContentStreamParser.parse(page.contentBytes)

        page.structuredText.blocks.forEach { block ->
            var lineText = ""
            var fontSize: Double = 10.toDouble()
            var fontSpec = FontSpec(
                family = KiteFontFamily.Monospace,
                bold = false,
                italic = false,
                name = ""
            )

            block.lines.forEach { line ->
                line.spans.forEach { span ->
                    lineText += span.text

                    fontSize = span.fontSize
                    fontSpec = span.fontSpec
                }

                if(!lineText.endsWith(" ")) lineText += " "
            }

            textLines.add @Composable {
                Text(
                    lineText,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    fontSize = fontSize.sp,
                    fontWeight = if(fontSpec.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if(fontSpec.italic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = when(fontSpec.family) {
                        KiteFontFamily.Monospace -> FontFamily.Monospace
                        KiteFontFamily.SansSerif -> FontFamily.SansSerif
                        KiteFontFamily.Serif -> FontFamily.Serif
                    }
                )
            }
        }

        operations.forEach { op ->
            if(op.operator.equals("tj", ignoreCase = true)) {
                var text: @Composable () -> Unit = {
                    Text("coś się wypieprzyło")
                }

                try {
                    text = textLines.first()
                } catch (e: NoSuchElementException) {}

                text()
                textLines.remove(text)
            }

            if(op.operator.equals("do", ignoreCase = true)) {
                val operand = op.operands.first()

                if(operand !is PdfName) {
                    return
                }

                val image = images[operand.value.replace("/", "")]
                if(image == null) {
                    println("kurwa dupa chuj nie ma tego")
                    return
                }

                println("jest zdjęcie?")

                Image(bitmap = image.asImageBitmap(), contentDescription = null)
            }
        }
    }
}