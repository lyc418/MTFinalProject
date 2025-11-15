package com.example.mtfinalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.mtfinalproject.ui.theme.MTFinalProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MTFinalProjectTheme {
                MTFinalProjectApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MTFinalProjectApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.DASHBOARD) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.DASHBOARD -> {
                    DashboardScreen(modifier = Modifier.padding(innerPadding))
                }
                AppDestinations.TRAINING -> {
                    TrainingScreen(modifier = Modifier.padding(innerPadding))
                }
                AppDestinations.QUESTIONNAIRE -> {
                    QuestionnaireScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "老人健身APP",
            style = MaterialTheme.typography.headlineLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "歡迎使用",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "請先完成問卷以獲得個人化的訓練計劃",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "今日概覽",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "訓練進度: 0%")
                Text(text = "完成訓練: 0 次")
            }
        }
    }
}

@Composable
fun TrainingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "訓練",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "訓練內容即將推出",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun QuestionnaireScreen(modifier: Modifier = Modifier) {
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var age by rememberSaveable { mutableStateOf("") }
    var healthCondition by rememberSaveable { mutableStateOf("") }
    var exerciseHabit by rememberSaveable { mutableStateOf("") }
    var isCompleted by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isCompleted) {
            // Progress indicator
            Text(
                text = "問卷 ($currentStep / 3)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Question content
            when (currentStep) {
                1 -> AgeQuestion(
                    selectedAge = age,
                    onAgeSelected = { age = it }
                )
                2 -> HealthConditionQuestion(
                    selectedCondition = healthCondition,
                    onConditionSelected = { healthCondition = it }
                )
                3 -> ExerciseHabitQuestion(
                    selectedHabit = exerciseHabit,
                    onHabitSelected = { exerciseHabit = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("上一步")
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            isCompleted = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (currentStep) {
                        1 -> age.isNotEmpty()
                        2 -> healthCondition.isNotEmpty()
                        3 -> exerciseHabit.isNotEmpty()
                        else -> false
                    }
                ) {
                    Text(if (currentStep < 3) "下一步" else "完成")
                }
            }
        } else {
            // Completion screen
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✓ 問卷完成",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "您的資料",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("年齡: $age")
                        Text("健康狀況: $healthCondition")
                        Text("運動習慣: $exerciseHabit")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    currentStep = 1
                    age = ""
                    healthCondition = ""
                    exerciseHabit = ""
                    isCompleted = false
                }) {
                    Text("重新填寫")
                }
            }
        }
    }
}

@Composable
fun AgeQuestion(selectedAge: String, onAgeSelected: (String) -> Unit) {
    val ageRanges = listOf("60-65歲", "66-70歲", "71-75歲", "76-80歲", "80歲以上")

    QuestionCard(
        title = "請選擇您的年齡範圍",
        options = ageRanges,
        selectedOption = selectedAge,
        onOptionSelected = onAgeSelected
    )
}

@Composable
fun HealthConditionQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("良好", "普通", "需要特別注意")

    QuestionCard(
        title = "請選擇您的健康狀況",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

@Composable
fun ExerciseHabitQuestion(selectedHabit: String, onHabitSelected: (String) -> Unit) {
    val habits = listOf("每天運動", "每週3-5次", "每週1-2次", "很少運動")

    QuestionCard(
        title = "請選擇您的運動習慣",
        options = habits,
        selectedOption = selectedHabit,
        onOptionSelected = onHabitSelected
    )
}

@Composable
fun QuestionCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("主頁", Icons.Default.Home),
    TRAINING("訓練", Icons.Default.Favorite),
    QUESTIONNAIRE("問卷", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MTFinalProjectTheme {
        Greeting("Android")
    }
}