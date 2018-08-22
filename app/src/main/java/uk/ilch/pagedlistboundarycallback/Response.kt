package uk.ilch.pagedlistboundarycallback

import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.regex.Pattern

/**
 * Created by Olexandr Ilchuk on 3/29/18.
 */
class Response<T> {

    companion object {
        private val LINK_PATTERN = Pattern
                .compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private val PAGE_PATTERN = Pattern.compile("page=(\\d)+")
        private const val NEXT_LINK = "next"
    }


    private var code: Int
    var body: T? = null
    var errorMessage: String? = null
    var linkHeader: String? = null
    private var links = mutableMapOf<String, String>()

    constructor(error: Throwable) {
        code = 500
        body = null
        errorMessage = error.message
    }

    constructor(code: Int, body: T, errorMessage: String?) {
        this.code = code
        this.body = body
        this.errorMessage = errorMessage
    }

    constructor(response: Response<T>) {
        code = response.code()
        if (response.isSuccessful) {
            body = response.body()
            errorMessage = null
        } else {
            var message: String? = response.message()
            if (message == null || message.trim { it <= ' ' }.isEmpty()) {
                if (response.errorBody() != null) {
                    try {
                        message = response.errorBody()!!.string()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            errorMessage = message
            //body = null
        }

        linkHeader = response.headers().get("link")
        if (linkHeader != null) {
            val matcher = LINK_PATTERN.matcher(linkHeader)
            while (matcher.find()) {
                val count = matcher.groupCount()
                if (count == 2) {
                    links[matcher.group(2)] = matcher.group(1)
                }
            }
        }
    }

    fun isSuccessful(): Boolean {
        return code in 200..299
    }

    private fun getNextPageFromHeader(): Int? {
        val next = links[NEXT_LINK] ?: return null
        val matcher = PAGE_PATTERN.matcher(next)
        if (!matcher.find() || matcher.groupCount() != 1) {
            return null
        }
        return try {
            Integer.parseInt(matcher.group(1))
        } catch (ex: NumberFormatException) {
            Timber.d("cannot parse next page from $next")
            null
        }
    }
}