package com.hydra.water

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(ReminderReceiver.PREFS, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (prefs.getString("day", "") != today) {
            prefs.edit().putString("day", today).putInt("water", 0).apply()
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (prefs.getBoolean(ReminderReceiver.KEY_REMINDERS, true)) {
            ReminderReceiver.start(this, prefs.getInt(ReminderReceiver.KEY_INTERVAL, 60))
        }
        setContent { HydraApp() }
    }

    @Composable
    private fun HydraApp() {
        val prefs = getSharedPreferences(ReminderReceiver.PREFS, Context.MODE_PRIVATE)
        var water by rememberSaveable { mutableIntStateOf(prefs.getInt("water", 0)) }
        var goal by rememberSaveable { mutableIntStateOf(prefs.getInt("goal", 2500)) }
        var reminders by rememberSaveable { mutableStateOf(prefs.getBoolean(ReminderReceiver.KEY_REMINDERS, true)) }
        var interval by rememberSaveable { mutableIntStateOf(prefs.getInt(ReminderReceiver.KEY_INTERVAL, 60)) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        val percent = (water.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)

        MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF168BD0), secondary = Color(0xFF00B9A9), background = Color(0xFFF6FBFF))) {
            Box(Modifier.fillMaxSize().background(Color(0xFFF6FBFF))) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("HYDRA", fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = Color(0xFF0A557D))
                            Text("Daily hydration", fontSize = 13.sp, color = Color(0xFF6C7F8D))
                        }
                        Text("⚙", fontSize = 27.sp, modifier = Modifier.clickable { showSettings = true }.padding(8.dp))
                    }
                    Spacer(Modifier.height(18.dp))

                    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF168BD0)), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(190.dp).clip(CircleShape).background(Color(0xFF4CB7F1).copy(alpha = 0.28f)), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(154.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$water", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color(0xFF0A557D))
                                        Text("ml today", color = Color(0xFF6C7F8D))
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("${(percent * 100).toInt()}% of your ${goal} ml goal", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(if (water >= goal) "Great job — goal reached! ✨" else "Small sips. Big difference.", color = Color.White.copy(alpha = 0.92f))
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text("Add water", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF13384D))
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(listOf(150, 250, 350, 500, 750)) { amount ->
                            Card(modifier = Modifier.clickable {
                                water += amount
                                prefs.edit().putInt("water", water).apply()
                            }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                                Column(Modifier.padding(horizontal = 17.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💧", fontSize = 20.sp)
                                    Text("$amount ml", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Next reminder", fontWeight = FontWeight.Bold, color = Color(0xFF13384D))
                                Text(if (reminders) "Every $interval minutes" else "Reminders are paused", color = Color(0xFF6C7F8D), fontSize = 13.sp)
                            }
                            Switch(checked = reminders, onCheckedChange = {
                                reminders = it
                                prefs.edit().putBoolean(ReminderReceiver.KEY_REMINDERS, it).apply()
                                if (it) ReminderReceiver.start(this@MainActivity, interval) else ReminderReceiver.cancel(this@MainActivity)
                            })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF8FF)), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Today", fontWeight = FontWeight.Bold, color = Color(0xFF13384D))
                                Text("$water / $goal ml", fontWeight = FontWeight.Bold, color = Color(0xFF168BD0))
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White)) {
                                Box(Modifier.fillMaxWidth(percent).height(10.dp).clip(CircleShape).background(Color(0xFF21B7A4)))
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Hydra • Simple hydration habits", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF91A2AD), fontSize = 12.sp)
                }

                AnimatedVisibility(visible = showSettings) {
                    SettingsDialog(goal, interval, { goal = it; prefs.edit().putInt("goal", it).apply() }, { interval = it; prefs.edit().putInt(ReminderReceiver.KEY_INTERVAL, it).apply(); if (reminders) ReminderReceiver.start(this@MainActivity, it) }, { showSettings = false })
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(goal: Int, interval: Int, onGoalChange: (Int) -> Unit, onIntervalChange: (Int) -> Unit, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text("Hydra settings", fontWeight = FontWeight.Bold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Daily goal: $goal ml")
            Slider(value = goal.toFloat(), onValueChange = { onGoalChange((it / 100).toInt() * 100) }, valueRange = 1500f..5000f, steps = 34)
            Text("Reminder interval: $interval min")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 45, 60, 90, 120).forEach { m -> FilterChip(selected = interval == m, onClick = { onIntervalChange(m) }, label = { Text("$m") }) }
            }
        }
    }, confirmButton = { Button(onClick = onClose) { Text("Done") } })
}
