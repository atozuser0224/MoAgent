package org.gang.moagent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.gang.moagent.ChatMessageData
import org.gang.moagent.DeepSeekViewModel
import org.gang.moagent.manager.DataUtil
import org.gang.moagent.manager.JsonFileManager
import org.gang.moagent.nav.AgentData

// --- 에이전트 목록 화면 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentListScreen(onCreateClick: () -> Unit, onAgentClick: (AgentData) -> Unit) {
    val context = LocalContext.current
    val agents = remember { mutableStateListOf<AgentData>() }

    LaunchedEffect(true) {
        agents.clear() // 중복 추가 방지
        val agentFolders = DataUtil.getSubDirectories(context, "agents")

        agentFolders.forEach { folderName ->
            val agentInfo = JsonFileManager.loadJson<AgentData>(
                context,
                "agents/$folderName",
                "config.json"
            )
            // 데이터가 존재하면 리스트에 추가
            agentInfo?.let { agents.add(it) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("내 에이전트") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(agents) { agent ->
                ListItem(
                    headlineContent = { Text(agent.name) },
                    supportingContent = { Text(agent.model) },
                    modifier = Modifier.clickable { onAgentClick(agent) }
                )
                HorizontalDivider()
            }
        }
    }
}

// --- 에이전트 생성 화면 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAgentScreen(onBack: () -> Unit, onAgentCreated: (String, String) -> Unit) {
    val context = LocalContext.current // 파일 저장을 위해 context 필요
    var name by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("DeepSeek-Chat") }
    var expanded by remember { mutableStateOf(false) }
    val models = listOf("DeepSeek-Chat", "DeepSeek-Coder", "GPT-4o")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 에이전트 만들기") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("에이전트 이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("모델 선택") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val newAgent = AgentData(name = name, model = selectedModel)
                    val isSaved = JsonFileManager.saveJson(
                        context = context,
                        subDir = "agents/$name",
                        fileName = "config.json",
                        data = newAgent
                    )

                    if (isSaved) {
                        onAgentCreated(name, selectedModel)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank()
            ) {
                Text("생성 및 채팅 시작")
            }
        }
    }
}

