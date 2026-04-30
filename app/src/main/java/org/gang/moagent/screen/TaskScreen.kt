package org.gang.moagent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.gang.moagent.nav.TaskData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen() {
    // 상태 관리를 위한 임시 리스트
    var tasks by remember {
        mutableStateOf(listOf(
            TaskData(1, "시스템 로그 분석하기"),
            TaskData(2, "불필요한 캐시 파일 정리")
        ))
    }

    // 수정 다이얼로그 상태
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskData?>(null) }
    var editText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("시스템 태스크 관리") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(tasks) { task ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(task.content, modifier = Modifier.weight(1f))

                        // 수정 버튼
                        IconButton(onClick = {
                            editingTask = task
                            editText = task.content
                            showDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "수정")
                        }

                        // 삭제 버튼
                        IconButton(onClick = {
                            tasks = tasks.filter { it.id != task.id }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // 수정 다이얼로그
        if (showDialog && editingTask != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("태스크 수정") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        tasks = tasks.map { if (it.id == editingTask!!.id) it.copy(content = editText) else it }
                        showDialog = false
                    }) {
                        Text("저장")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}