package com.example.anilist.networking

import android.util.Log
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer
import java.nio.charset.Charset

private const val TAG = "MyURLRequestCallback"

class MyUrlRequestCallback: UrlRequest.Callback() {

    /**
     * Invoked whenever a redirect is encountered. This will only be invoked
     * between the call to [UrlRequest.start] and
     * [onResponseStarted()][Callback.onResponseStarted].
     * The body of the redirect response, if it has one, will be ignored.
     *
     * The redirect will not be followed until the URLRequest's
     * [UrlRequest.followRedirect] method is called, either
     * synchronously or asynchronously.
     *
     * @param request Request being redirected.
     * @param info Response information.
     * @param newLocationUrl Location where request is redirected.
     * @throws Exception if an error occurs while processing a redirect. [.onFailed]
     * will be called with the thrown exception set as the cause of the
     * [CallbackException].
     */
    override fun onRedirectReceived(request: UrlRequest?, info: UrlResponseInfo?, newLocationUrl: String?) {
        Log.i(TAG, "onRedirectReceived method called.")
        // You should call the request.followRedirect() method to continue
        // processing the request.
        request?.followRedirect()
    }

    /**
     * Invoked when the final set of headers, after all redirects, is received.
     * Will only be invoked once for each request.
     *
     * With the exception of [onCanceled()][Callback.onCanceled],
     * no other [Callback] method will be invoked for the request,
     * including [onSucceeded()][Callback.onSucceeded] and [ ][Callback.onFailed], until [ UrlRequest.read()][UrlRequest.read] is called to attempt to start reading the response
     * body.
     *
     * @param request Request that started to get response.
     * @param info Response information.
     * @throws Exception if an error occurs while processing response start. [.onFailed]
     * will be called with the thrown exception set as the cause of the
     * [CallbackException].
     */
    override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
        Log.i(TAG, "onResponseStarted method called.")
        val myBuffer = ByteBuffer.allocateDirect(102400)
        val httpStatusCode = info?.httpStatusCode
        if (httpStatusCode == 200) {
            // The request was fulfilled. Start reading the response.
            request?.read(myBuffer)
        } else if (httpStatusCode == 503) {
            // The service is unavailable. You should still check if the request
            // contains some data.
            request?.read(myBuffer)
        } else if (httpStatusCode == 404) {
            Log.i(TAG, "URL: ${info.url} was not found")
            request?.cancel()
        }
        val responseHeaders = info?.allHeaders
        if (responseHeaders != null) {
            val headerList = buildString {
                for (header in responseHeaders) {
                    append(header.value)
                }
            }
            Log.i(TAG, "Received these headers: $headerList")
        }


        // You should call the request.read() method before the request can be
        // further processed. The following instruction provides a ByteBuffer object
        // with a capacity of 102400 bytes for the read() method. The same buffer
        // with data is passed to the onReadCompleted() method.
    }

    /**
     * Invoked whenever part of the response body has been read. Only part of
     * the buffer may be populated, even if the entire response body has not yet
     * been consumed.
     *
     * With the exception of [onCanceled()][Callback.onCanceled],
     * no other [Callback] method will be invoked for the request,
     * including [onSucceeded()][Callback.onSucceeded] and [ ][Callback.onFailed], until [ ][UrlRequest.read] is called to attempt to continue
     * reading the response body.
     *
     * @param request Request that received data.
     * @param info Response information.
     * @param byteBuffer The buffer that was passed in to
     * [UrlRequest.read()][UrlRequest.read], now containing the
     * received data. The buffer's position is updated to the end of
     * the received data. The buffer's limit is not changed.
     * @throws Exception if an error occurs while processing a read completion.
     * [.onFailed] will be called with the thrown exception set as the cause of
     * the [CallbackException].
     */
    override fun onReadCompleted(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        byteBuffer: ByteBuffer?
    ) {
        Log.i(TAG, "onReadCompleted method called.")
        Log.i(TAG, "#1 " + String(byteBuffer?.array() ?: byteArrayOf(), Charset.defaultCharset()))
        // You should keep reading the request until there's no more data.
        byteBuffer?.clear()
        request?.read(byteBuffer)
    }

    /**
     * Invoked when request is completed successfully. Once invoked, no other
     * [Callback] methods will be invoked.
     *
     * @param request Request that succeeded.
     * @param info Response information.
     */
    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
        Log.i(TAG, "onSucceeded method called.")
    }

    /**
     * Invoked if request failed for any reason after [UrlRequest.start].
     * Once invoked, no other [Callback] methods will be invoked.
     * `error` provides information about the failure.
     *
     * @param request Request that failed.
     * @param info Response information. May be `null` if no response was
     * received.
     * @param error information about error.
     */
    override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
        Log.i(TAG, "onFailed method called.")
        Log.i(TAG, "Status code received on fail: ${info?.httpStatusCode}")
    }
}