package com.example.codeforces.models

data class ApiResponse<T>(
    val status: String,
    val result: T
)