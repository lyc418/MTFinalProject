package com.example.mtfinalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.mtfinalproject.ui.theme.SignInGreen
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
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

    var trainingExercise by rememberSaveable { mutableStateOf<ExerciseType?>(null) }

    if (trainingExercise != null) {
        TrainingScreen(exerciseType = trainingExercise!!, onClose = { trainingExercise = null })

        BackHandler {
            trainingExercise = null
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
                        TrainingPlanScreen(
                            modifier = Modifier.padding(innerPadding),
                            onStartTraining = { selectedExercise ->
                                trainingExercise = selectedExercise
                            }
                        )
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
    var isLoading by remember { mutableStateOf(true) }
    var hasCompletedQuestionnaire by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableStateOf(0) }
    var signInDates by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val signInJob = async { SignInRepository.recordSignIn() }
            val questionnaireJob = async { QuestionnaireRepository.isCompleted() }
            val streakJob = async { SignInRepository.calculateStreak() }
            val historyJob = async { SignInRepository.getSignInHistory() }

            signInJob.await()
            hasCompletedQuestionnaire = questionnaireJob.await()
            currentStreak = streakJob.await()
            val history = historyJob.await()
            signInDates = history?.dates?.toSet() ?: emptySet()
            isLoading = false
        }
    }

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
                when {
                    isLoading -> {
                        Text(
                            text = "載入中...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    hasCompletedQuestionnaire -> {
                        Text(
                            text = "到訓練頁面開始今日訓練",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        Text(
                            text = "請先完成問卷以獲得個人化的訓練計劃",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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
                Text(text = "連續簽到: $currentStreak 天")
                Text(text = "訓練進度: 0%")
                Spacer(modifier = Modifier.height(16.dp))
                SignInCalendar(signInDates = signInDates)
            }
        }
    }
}

@Composable
fun SignInCalendar(signInDates: Set<String>) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.from(today)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1 + 7) % 7

    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else if (dayCounter <= daysInMonth) {
                        val currentDay = dayCounter
                        val dateString = yearMonth.atDay(currentDay).toString()
                        val isSignedIn = signInDates.contains(dateString)
                        val isToday = currentDay == today.dayOfMonth

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSignedIn -> SignInGreen
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSignedIn -> Color.White
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingPlanScreen(modifier: Modifier = Modifier, onStartTraining: (selectedExercise: ExerciseType) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var scarfScore by remember { mutableIntStateOf(-1) }
    LaunchedEffect(Unit) {
        val data = QuestionnaireRepository.getAnswers()
        if (data != null) {
            scarfScore = data.sarcfScore
        }
        isLoading = false
    }
    val exercises = remember<List<ExerciseType>>(scarfScore) {
        if (scarfScore == -1) {
            emptyList<ExerciseType>()
        } else {
            val resultList = mutableListOf<ExerciseType>()
            resultList.add(ExerciseType.BOTTLE_LIFT)
            resultList.add(ExerciseType.OVERHEAD_EXTENSION)
            if (scarfScore >= 4) {
                resultList.add(ExerciseType.ANKLE_WEIGHT_LEG_EXTENSION_LEFT)
                resultList.add(ExerciseType.ANKLE_WEIGHT_LEG_EXTENSION_RIGHT)
            } else {
                resultList.add(ExerciseType.CHAIR_STAND)
            }
            resultList
        }
    }
    if (isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "載入中...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else if (!exercises.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "選擇訓練項目",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(exercises) { exercise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartTraining(exercise) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = exercise.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exercise.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "請先完成問卷",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("主頁", Icons.Default.Home),
    TRAINING("訓練", Icons.Default.FitnessCenter),
    QUESTIONNAIRE("問卷", Icons.AutoMirrored.Filled.Assignment),
}

