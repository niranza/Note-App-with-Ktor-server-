package com.niran.ktornoteapplication.dataset.retrofit

import com.niran.ktornoteapplication.utils.Constants.IGNORE_AUTH_URLS
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor : Interceptor {

    var email: String? = null
    var password: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        return if (request.url.encodedPath in IGNORE_AUTH_URLS) {
            chain.proceed(request)
        } else {
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", Credentials.basic(email ?: "", password ?: ""))
                .build()
            chain.proceed(authenticatedRequest)
        }
    }
}