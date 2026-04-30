package org.gang.moagent.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekResponse(
    val answer: String,
    val checkTasks: List<String> = emptyList(), // 모델이 분할한 할 일 목록
    val tasks: List<String> = emptyList(),
    val confidence: Int
)