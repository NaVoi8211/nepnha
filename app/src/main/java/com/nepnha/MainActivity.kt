package com.nepnha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nepnha.ui.NepNhaShell
import com.nepnha.ui.theme.NepNhaTheme

/**
 * Activity duy nhất của app. Mọi màn hình là Composable trong một NavHost.
 *
 * Xoay màn hình: app **không khoá hướng**. Compose tự dựng lại, `rememberNavController`
 * và `rememberScrollState` khôi phục qua `SavedInstanceState`, nên không cần
 * `configChanges` thủ công.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NepNhaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NepNhaShell(container = (application as NepNhaApp).container)
                }
            }
        }
    }
}
