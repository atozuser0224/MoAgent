package org.gang.moagent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.gang.moagent.dto.DeepSeekResponse
import kotlin.time.Duration.Companion.seconds

// 1. 메시지 데이터 클래스 (UI 상태 관리용)
data class ChatMessageData(
    val role: String,
    val content: String,
    val checkTasks: List<String> = emptyList(), // [ ] 형태의 설명글
    val rawLogicTasks: List<String> = emptyList() // 실제 에이전트 실행 명령어
)

// 2. ViewModel: 비즈니스 로직 및 API 통신
class DeepSeekViewModel : ViewModel() {
    private val config = OpenAIConfig(
        token = "",
        host = OpenAIHost("https://api.deepseek.com"),
        timeout = Timeout(socket = 60.seconds)
    )
    private val openAI = OpenAI(config)
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    var chatMessages = mutableStateListOf<ChatMessageData>()
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        chatMessages.add(ChatMessageData("User", userText))
        isLoading = true

        viewModelScope.launch {
            try {
                val request = ChatCompletionRequest(
                    model = ModelId("deepseek-chat"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """
                               You are a dedicated Android AI agent with system-level access. Your role is to analyze and decompose user requests into specific, executable tasks and a user-friendly checklist.

                               ### CAPABILITIES & CONSTRAINTS:
                               1. FILE ACCESS: You can open and read ALL files directly. You do NOT need to launch specific apps to access or process file content.
                               2. RESPONSE FORMAT: You must respond ONLY in valid JSON. No preamble, no conversational filler.
                               3. LOGIC SEPARATION: 
                                  - 'checkTasks': A list of tasks written in natural language with a checkbox [ ] for user visibility.
                                  - 'tasks': Detailed, concrete, line-by-line explanations of how the system will perform each step. These must be written in the USER'S LANGUAGE, avoiding abstract English function calls or pseudo-code.
                               4. USER CONFIRMATION: You MUST NOT begin the actual task execution immediately. You must present the proposed plan and explicitly ask the user for permission to proceed in the 'answer' field.
                               5. NO OMISSIONS: Do not skip or omit any code or text. Provide the full, complete JSON response without using placeholders.

                               ### OUTPUT STRUCTURE:
                               Every response must strictly follow this JSON schema:
                               {
                                 "answer": "A concise summary of the plan, ending with a question asking if it is okay to proceed.",
                                 "checkTasks": [
                                   "[ ] Task description for user",
                                   "[ ] Next task..."
                                 ],
                                 "tasks": [
                                   "Concrete explanation of step 1 in the user's language",
                                   "Concrete explanation of step 2 in the user's language",
                                   "Concrete explanation of step 3 in the user's language"
                                 ],
                                 "confidence": 100
                               }

                               ### EXAMPLE:
                               User: "Find the 'invoice.pdf' in my documents and extract the total amount." (Assuming user communicates in Korean)
                               Response:
                               {
                                 "answer": "문서 폴더에서 'invoice.pdf' 파일을 찾아 총액을 추출할 계획입니다. 작업을 시작해도 될까요?",
                                 "checkTasks": [
                                   "[ ] Documents 폴더에서 invoice.pdf 찾기",
                                   "[ ] 파일에서 총액 추출하기",
                                   "[ ] 결과 표시하기"
                                 ],
                                 "tasks": [
                                   "/storage/emulated/0/Documents 경로를 탐색하여 이름이 'invoice.pdf'인 파일을 찾습니다.",
                                   "해당 PDF 파일의 내용을 텍스트 형식으로 읽어옵니다.",
                                   "정규표현식을 사용하여 텍스트 데이터 내에서 'Total' 또는 금액을 나타내는 숫자 패턴을 추출합니다."
                                 ],
                                 "confidence": 100
                               }
                            """.trimIndent()
                        )
                    ) + chatMessages.map {
                        ChatMessage(
                            role = if (it.role == "User") ChatRole.User else ChatRole.Assistant,
                            content = it.content
                        )
                    },
                    responseFormat = ChatResponseFormat.JsonObject // JSON 강제 모드
                )

                val response = openAI.chatCompletion(request)
                val rawJson = response.choices.first().message.content ?: ""

                // DTO를 통한 파싱
                val parsed = jsonParser.decodeFromString<DeepSeekResponse>(rawJson)

                chatMessages.add(
                    ChatMessageData(
                        role = "DeepSeek",
                        content = parsed.answer,
                        checkTasks = parsed.checkTasks,
                        rawLogicTasks = parsed.tasks
                    )
                )
            } catch (e: Exception) {
                chatMessages.add(ChatMessageData("Error", "오류 발생: ${e.localizedMessage}"))
            } finally {
                isLoading = false
            }
        }
    }
}
