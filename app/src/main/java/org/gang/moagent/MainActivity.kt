package org.gang.moagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.gang.moagent.screen.MainScreen
import org.gang.moagent.ui.theme.MoAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoAgentTheme {
                MainScreen()
            }
        }
    }
}
