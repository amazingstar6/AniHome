package com.example.anilist.networking

import android.util.Log
import org.chromium.net.UploadDataProvider
import org.chromium.net.UploadDataSink
import java.nio.ByteBuffer
import java.nio.charset.Charset

private val body: String = """
    {
        "query" : "query getTrending(${'$'}page:Int) {\n  Page(page: ${'$'}page, perPage: 10) {\n    media(sort: TRENDING_DESC) {\n      id\n      title {\n        romaji\n        english\n        native\n      }\n      coverImage {\n        extraLarge\n      }\n    }\n  }\n}\n",
        "variables": {
            "page": 1
        }
    }
""".trimIndent()
private const val TAG = "MyUploadDataProvider"

class MyUploadDataProvider: UploadDataProvider() {
    /**
     * If this is a non-chunked upload, returns the length of the upload. Must
     * always return -1 if this is a chunked upload.
     *
     * @return the length of the upload for non-chunked uploads, -1 otherwise.
     * @throws IOException if any IOException occurred during the process.
     */
    override fun getLength(): Long {
        return body.length.toLong()
    }

    /**
     * Reads upload data into `byteBuffer`. Upon completion, the buffer's
     * position is updated to the end of the bytes that were read. The buffer's
     * limit is not changed. Each call of this method must be followed be a
     * single call, either synchronous or asynchronous, to
     * `uploadDataSink`: [UploadDataSink.onReadSucceeded] on success
     * or [UploadDataSink.onReadError] on failure. Neither read nor rewind
     * will be called until one of those methods or the other is called. Even if
     * the associated [UrlRequest] is canceled, one or the other must
     * still be called before resources can be safely freed. Throwing an
     * exception will also result in resources being freed and the request being
     * errored out.
     *
     * @param uploadDataSink The object to notify when the read has completed,
     * successfully or otherwise.
     * @param byteBuffer The buffer to copy the read bytes into. Do not change
     * byteBuffer's limit.
     * @throws IOException if any IOException occurred during the process.
     * [UrlRequest.Callback.onFailed] will be called with the
     * thrown exception set as the cause of the
     * [CallbackException].
     */
    override fun read(uploadDataSink: UploadDataSink?, byteBuffer: ByteBuffer?) {
        Log.i(TAG, "Size of the bytebuffer to write in: " + byteBuffer?.capacity().toString())
        Log.i(TAG, "Size of the array of the json body: " + body.toByteArray(Charset.defaultCharset()).size.toString())
        byteBuffer!!.put(body.toByteArray(Charset.defaultCharset()))

        uploadDataSink!!.onReadSucceeded(false)
    }

    /**
     * Rewinds upload data. Each call must be followed be a single
     * call, either synchronous or asynchronous, to `uploadDataSink`:
     * [UploadDataSink.onRewindSucceeded] on success or
     * [UploadDataSink.onRewindError] on failure. Neither read nor rewind
     * will be called until one of those methods or the other is called.
     * Even if the associated [UrlRequest] is canceled, one or the other
     * must still be called before resources can be safely freed. Throwing an
     * exception will also result in resources being freed and the request being
     * errored out.
     *
     *
     * If rewinding is not supported, this should call
     * [UploadDataSink.onRewindError]. Note that rewinding is required to
     * follow redirects that preserve the upload body, and for retrying when the
     * server times out stale sockets.
     *
     * @param uploadDataSink The object to notify when the rewind operation has
     * completed, successfully or otherwise.
     * @throws IOException if any IOException occurred during the process.
     * [UrlRequest.Callback.onFailed] will be called with the
     * thrown exception set as the cause of the
     * [CallbackException].
     */
    override fun rewind(uploadDataSink: UploadDataSink?) {
        Log.i(TAG, "Rewind is called")
        uploadDataSink!!.onRewindSucceeded()
    }
}