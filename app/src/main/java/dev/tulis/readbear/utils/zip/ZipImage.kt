package dev.tulis.readbear.utils.zip

import java.util.zip.ZipFile

data class ZipImage(
    val zipFile: ZipFile,
    val path: String
)