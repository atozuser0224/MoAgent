package org.gang.moagent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.gang.moagent.ChatMessageData
import org.gang.moagent.DeepSeekViewModel // 뷰모델 import 확인

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    agentName: String,
    modelName: String,
    onBack: () -> Unit,
    viewModel: DeepSeekViewModel = viewModel() // 통신 뷰모델 연결
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 메시지가 추가될 때마다 자동으로 스크롤을 맨 아래로 내림
    LaunchedEffect(viewModel.chatMessages.size) {
        if (viewModel.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(agentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (viewModel.isLoading) "분석 중..." else modelName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (viewModel.isLoading) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // 하단 입력창 (키보드 올라올 때 대응)
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("에이전트에게 명령하세요...") },
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        enabled = !viewModel.isLoading,
                        maxLines = 3 // 여러 줄 입력 가능하도록
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank() && !viewModel.isLoading) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendMessage(text) // 뷰모델에 전송 요청
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp),
                        containerColor = if (inputText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        // 채팅 리스트 영역
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.chatMessages) { msg ->
                ChatBubble(msg)
            }
        }
    }
}

// --- 말풍선 UI 컴포넌트 ---
@Composable
fun ChatBubble(msg: ChatMessageData) {
    val isUser = msg.role == "User"
    val agentActionColor = Color(0xFF00E676) // 초록색 포인트

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // 에이전트 프로필 아이콘
            Surface(
                modifier = Modifier.size(32.dp).padding(top = 4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.75f else 0.85f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 상단 이름
                Text(
                    text = if (isUser) "나" else "SYSTEM AGENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )

                // 본문 텍스트 (요약)
                Text(
                    text = msg.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // 에이전트의 태스크 체크리스트 시각화
                if (!isUser && msg.checkTasks.isNotEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Execution Plan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            msg.checkTasks.forEach { task ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.contains("[v]")) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (task.contains("[v]")) agentActionColor else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.replace("[ ]", "").replace("[v]", "").trim(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // 시스템 실행 코드 (raw 로직)
                if (!isUser && msg.rawLogicTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg.rawLogicTasks.joinToString("\n") { "> $it" },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = agentActionColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}