package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.service.MayaBackgroundService
import com.example.ui.MayaMainScreen
import com.example.ui.MayaViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MayaViewModel by viewModels()

    companion object {
        const val ACTION_WAKE_WORD_ACTIVATED = "com.example.ACTION_WAKE_WORD_ACTIVATED"
        const val EXTRA_VOICE_COMMAND = "extra_voice_command"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Allow app to wake the device screen and show over lock screen when Maya is called
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Start background wake-word listening service if microphone permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            MayaBackgroundService.start(this)
        }

        handleIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MayaMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // When MainActivity is active in the foreground, pause background service recognizer
        // so MayaVoiceManager has full uninterrupted microphone access!
        MayaBackgroundService.pauseListening(this)
    }

    override fun onStop() {
        super.onStop()
        // When app is in background or screen is turned off, resume background wake-word listening
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            MayaBackgroundService.resumeListening(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_WAKE_WORD_ACTIVATED) {
            val command = intent.getStringExtra(EXTRA_VOICE_COMMAND)?.trim()
            if (!command.isNullOrBlank()) {
                viewModel.processUserPrompt(command, isVoice = true)
            } else {
                viewModel.onWakeWordTriggered()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Maya AI") }
}

