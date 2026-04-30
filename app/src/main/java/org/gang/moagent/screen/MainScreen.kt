package org.gang.moagent.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import org.gang.moagent.nav.BottomNavItem
import org.gang.moagent.nav.Routes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 채팅 화면이나 생성 화면에서는 하단 바를 숨깁니다.
    val showBottomBar = currentRoute in listOf(
        BottomNavItem.AgentList.route,
        BottomNavItem.Task.route,
        BottomNavItem.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(BottomNavItem.AgentList, BottomNavItem.Task, BottomNavItem.Settings)
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.AgentList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. 에이전트 목록 화면
            composable(BottomNavItem.AgentList.route) {
                AgentListScreen(
                    onCreateClick = { navController.navigate(Routes.CreateAgent) },
                    onAgentClick = { agent ->
                        navController.navigate(
                            Routes.chatRoute(
                                agent.name,
                                agent.model
                            )
                        )
                    }
                )
            }

            // 2. 에이전트 생성 화면
            composable(Routes.CreateAgent) {
                CreateAgentScreen(
                    onBack = { navController.popBackStack() },
                    onAgentCreated = { name, model ->
                        navController.navigate(Routes.chatRoute(name, model)) {
                            popUpTo(BottomNavItem.AgentList.route) // 생성 후 뒤로가기 누르면 목록으로
                        }
                    }
                )
            }

            // 3. 채팅 화면 (이름과 모델명을 파라미터로 받음)
            composable(
                route = Routes.Chat,
                arguments = listOf(
                    navArgument("agentName") { type = NavType.StringType },
                    navArgument("modelName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("agentName") ?: "Agent"
                val model = backStackEntry.arguments?.getString("modelName") ?: "Unknown"
                ChatScreen(
                    agentName = name,
                    modelName = model,
                    onBack = { navController.popBackStack() })
            }

            // 4. 태스크 관리 화면
            composable(BottomNavItem.Task.route) {
                TaskScreen()
            }

            // 5. 설정 화면
            composable(BottomNavItem.Settings.route) {
                Text("설정 화면", modifier = Modifier.padding(16.dp))
            }
        }
    }
}