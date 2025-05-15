package com.example.sportevents.data.models

/**
 * Класс для обработки пагинированных ответов от API Django REST Framework
 */
data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
) 