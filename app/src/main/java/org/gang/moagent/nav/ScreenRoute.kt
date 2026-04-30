package org.gang.moagent.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// 하단 바에 들어갈 메뉴
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object AgentList : BottomNavItem("agent_list", Icons.Default.SmartToy, "에이전트")
    object Task : BottomNavItem("task_list", Icons.Default.List, "작업")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "설정")
}

// 전체 화면 라우트 (하단 바에 없는 화면 포함)
object Routes {
    const val CreateAgent = "create_agent"
    const val Chat = "chat/{agentName}/{modelName}"
    fun chatRoute(name: String, model: String) = "chat/$name/$model"
}
@Serializable
data class AgentData(val name: String, val model: String)
data class TaskData(val id: Int, var content: String)