package com.example.mtfinalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
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

    var isTraining by rememberSaveable { mutableStateOf(false) }

    if (isTraining) {
        TrainingScreen()

        BackHandler {
            isTraining = false
        }
    } else {
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
                        TrainingPlanScreen(modifier = Modifier.padding(innerPadding), onStartTraining = { isTraining = true })
                    }

                    AppDestinations.QUESTIONNAIRE -> {
                        QuestionnaireScreen(modifier = Modifier.padding(innerPadding))
                    }
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
fun TrainingPlanScreen(modifier: Modifier = Modifier, onStartTraining: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "訓練",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "訓練內容即將推出",
//            style = MaterialTheme.typography.bodyLarge
//        )
        Card (
            onClick = onStartTraining,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "深蹲",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "sample",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun QuestionnaireScreen(modifier: Modifier = Modifier) {
    val totalStep = 8
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var age by rememberSaveable { mutableStateOf("") }
    var healthCondition by rememberSaveable { mutableStateOf("") }
    var exerciseHabit by rememberSaveable { mutableStateOf("") }
    var strength by rememberSaveable { mutableStateOf("") }
    var assistanceInWalking by rememberSaveable { mutableStateOf("") }
    var riseFromChair by rememberSaveable { mutableStateOf("") }
    var climbStairs by rememberSaveable { mutableStateOf("") }
    var falls by rememberSaveable { mutableStateOf("") }
    var isCompleted by rememberSaveable { mutableStateOf(false) }
    val sarcfScore = listOf(strength, assistanceInWalking, riseFromChair, climbStairs, falls).sumOf{mapSarcfScore(it)}
    val sarcfRisk = if (sarcfScore >= 4) "高風險" else "低風險"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isCompleted) {
            // Progress indicator
            Text(
                text = "問卷 ($currentStep / $totalStep)",
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
                4 -> StrengthQuestion(
                    selectedCondition = strength,
                    onConditionSelected = { strength = it }
                )
                5 -> AssistanceInWalkingQuestion(
                    selectedCondition = assistanceInWalking,
                    onConditionSelected = { assistanceInWalking = it }
                )
                6 -> RiseFromChairQuestion(
                    selectedCondition = riseFromChair,
                    onConditionSelected = { riseFromChair = it }
                )
                7 -> ClimbStairsQuestion(
                    selectedCondition = climbStairs,
                    onConditionSelected = { climbStairs = it }
                )
                8 -> FallsQuestion(
                    selectedCondition = falls,
                    onConditionSelected = { falls = it }
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
                        if (currentStep < totalStep) {
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
                        4 -> strength.isNotEmpty()
                        5 -> assistanceInWalking.isNotEmpty()
                        6 -> riseFromChair.isNotEmpty()
                        7 -> climbStairs.isNotEmpty()
                        8 -> falls.isNotEmpty()
                        else -> false
                    }
                ) {
                    Text(if (currentStep < totalStep) "下一步" else "完成")
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
                        Text("sarc-f分數: $sarcfScore")
                        Text("肌少症風險: $sarcfRisk")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    currentStep = 1
                    age = ""
                    healthCondition = ""
                    exerciseHabit = ""
                    strength = ""
                    assistanceInWalking = ""
                    riseFromChair = ""
                    climbStairs = ""
                    falls = ""
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
fun StrengthQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("無困難", "有些困難", "很困難或無法做到")

    QuestionCard(
        title = "您在拿 4.5 公斤的東西是否有困難?",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

@Composable
fun AssistanceInWalkingQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("無困難", "有些困難", "很困難或無法做到")

    QuestionCard(
        title = "您於家中從這房間走到另一個房間是否有困難?",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

@Composable
fun RiseFromChairQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("無困難", "有些困難", "很困難或無法做到")

    QuestionCard(
        title = "您從椅子或床上站起來是否有困難?",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

@Composable
fun ClimbStairsQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("無困難", "有些困難", "很困難或無法做到")

    QuestionCard(
        title = "您在爬 10 個階梯是否有困難?",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

@Composable
fun FallsQuestion(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("0次", "1~3次", "4次以上")

    QuestionCard(
        title = "您過去一年的跌倒次數?",
        options = conditions,
        selectedOption = selectedCondition,
        onOptionSelected = onConditionSelected
    )
}

fun mapSarcfScore(ans: String): Int = when (ans) {
    "無困難" -> 0
    "有些困難" -> 1
    "很困難或無法做到" -> 2
    "0次" -> 0
    "1~3次" -> 1
    "4次以上" -> 2
    else -> 0
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