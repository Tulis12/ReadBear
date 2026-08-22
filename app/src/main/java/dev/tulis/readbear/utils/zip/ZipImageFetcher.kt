package dev.tulis.readbear.utils.zip

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.FileSystem
import okio.buffer
import okio.source

class ZipImageFetcher(
    private val data: ZipImage
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val entry = data.zipFile.getEntry(data.path)
            ?: return null

        val inputStream = data.zipFile.getInputStream(entry)

        return SourceFetchResult(
            source = ImageSource(
                source = inputStream.source().buffer(),
                fileSystem = FileSystem.SYSTEM
            ),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<ZipImage> {
        override fun create(
            data: ZipImage,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return ZipImageFetcher(data)
        }
    }
}