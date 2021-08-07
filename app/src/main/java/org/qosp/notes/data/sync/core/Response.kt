package org.qosp.notes.data.sync.core

import retrofit2.HttpException
import java.lang.Exception

sealed class Response<T>(val message: String? = null) {
    companion object {
        inline fun <T> from(block: () -> T): Response<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                when (e) {
                    ServerNotSupportedException -> ServerNotSupported()
                    is HttpException -> {
                        when (e.code()) {
                            401 -> Unauthorized()
                            else -> ApiError(e.message(), e.code())
                        }
                    }
                    else -> GenericError(e.message.toString())
                }
            }
        }
    }
}

inline fun <T> Response<T>.bodyOrElse(block: (Response<T>) -> T): T {
    return if (this is Success && body != null) body else block(this)
}

class Success<T>(val body: T? = null) : Response<T>()

class NoConnectivity<T> : Response<T>()
class SyncingNotEnabled<T> : Response<T>()
class InvalidConfig<T> : Response<T>()
class MutationWhileSyncing<T> : Response<T>()

class ServerNotSupported<T> : Response<T>()
class OperationNotSupported<T> : Response<T>()

class Unauthorized<T> : Response<T>()
class ApiError<T>(msg: String, val code: Int) : Response<T>(msg)
class GenericError<T>(msg: String = "Something went wrong") : Response<T>(msg)

object ServerNotSupportedException : Exception()


