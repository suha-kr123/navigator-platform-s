package com.nivasafinance.features.taskdefinitions.dto

data class TaskOutcomesResponse(
    val taskDefinitionKey: String,
    val taskDefinitionName: String,
    val outcomes: List<String>
)
