package com.example.sportevents.util

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    // Утилита для упрощения обработки NetworkResult в when-выражениях
    inline fun handle(
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        onLoading: () -> Unit
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(message)
            is Loading -> onLoading()
        }
    }
}

// Функция-расширение для создания успешного результата с типом Void
fun createVoidSuccess(): NetworkResult<Void> {
    @Suppress("UNCHECKED_CAST")
    return NetworkResult.Success(null) as NetworkResult<Void>
} 