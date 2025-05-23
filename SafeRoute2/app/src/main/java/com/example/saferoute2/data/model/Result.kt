package com.example.saferoute2.data.model

data class Result(

    val include_total: Boolean,
    val limit: Int,
    val records: List<Record>,
    val records_format: String,
    val resource_id: String,
    val total: Int,
    val total_estimation_threshold: Any,
    val total_was_estimated: Boolean
)