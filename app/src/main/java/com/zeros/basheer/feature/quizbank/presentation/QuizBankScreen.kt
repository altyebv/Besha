package com.zeros.basheer.feature.quizbank.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.basheer.feature.quizbank.presentation.components.*
import com.zeros.basheer.feature.quizbank.presentation.exams.ExamsModeContent
import com.zeros.basheer.feature.quizbank.presentation.foundation.*
import com.zeros.basheer.feature.quizbank.presentation.practice.PracticeModeContent
import com.zeros.basheer.ui.navigation.Screen

@Composable
fun QuizBankScreen(
    navController: NavController,
    viewModel: QuizBankViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedMode by remember { mutableStateOf(QuizMode.EXAMS) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuizBankViewModel.NavigationEvent.NavigateToPractice ->
                    navController.navigate(Screen.Practice.createRoute(event.sessionId))
                is QuizBankViewModel.NavigationEvent.NavigateToExam ->
                    navController.navigate(Screen.ExamEntry.createRoute(event.examId))
                is QuizBankViewModel.NavigationEvent.NavigateToPracticeBuilder ->
                    navController.navigate(
                        Screen.PracticeBuilder.createRoute(event.subjectId, event.mode)
                    )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBase),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "مركز التقييم",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "تدرّب، قيّم نفسك، تقدّم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            }
        }

        // Calibration strip
        item {
            PulseStrip(
                averageScore = state.averageScore,
                questionCounts = state.questionCounts,
                completedCount = state.completedSessionCount,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // Mode switcher
        item {
            ModeSwitcher(
                selected = selectedMode,
                onSelect = { selectedMode = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // Active session resume banner
        if (state.activeSession != null) {
            item {
                ActiveSessionBanner(
                    session = state.activeSession!!,
                    onClick = {
                        viewModel.onEvent(QuizBankEvent.ResumeSession(state.activeSession!!.id))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Mode content
        item {
            when (selectedMode) {
                QuizMode.EXAMS -> ExamsModeContent(
                    ministryExams = state.ministryExams,
                    schoolExams = state.schoolExams,
                    isLoading = state.isLoading,
                    onExamClick = { viewModel.onEvent(QuizBankEvent.StartExam(it)) }
                )
                QuizMode.PRACTICE -> PracticeModeContent(
                    questionCounts = state.questionCounts,
                    averageScore = state.averageScore,
                    weakAreaCount = state.weakAreaCount,
                    isWeakAreaLoading = state.isWeakAreaLoading,
                    onStartPractice = { type ->
                        viewModel.onEvent(QuizBankEvent.StartPracticeSession(generationType = type))
                    },
                    onStartWeakAreaSession = {
                        viewModel.onEvent(QuizBankEvent.StartWeakAreaSession)
                    },
                    onOpenBuilder = { mode ->
                        viewModel.onEvent(QuizBankEvent.OpenPracticeBuilder(mode))
                    }
                )
            }
        }

        // Recent sessions (always at bottom)
        if (state.recentSessions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                RecentSessionsStrip(
                    sessions = state.recentSessions.take(3),
                    onSessionClick = { viewModel.onEvent(QuizBankEvent.ResumeSession(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}