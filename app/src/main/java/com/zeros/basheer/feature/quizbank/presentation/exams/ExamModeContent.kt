package com.zeros.basheer.feature.quizbank.presentation.exams

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.ExamType
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun ExamsModeContent(
    ministryExams: List<Exam>,
    schoolExams: List<Exam>,
    isLoading: Boolean,
    onExamClick: (String) -> Unit
) {
    val allExams = remember(ministryExams, schoolExams) { ministryExams + schoolExams }
    var sourceFilter by remember { mutableStateOf<ExamSource?>(null) }
    var typeFilter by remember { mutableStateOf<ExamType?>(null) }

    val filtered = remember(allExams, sourceFilter, typeFilter) {
        allExams
            .filter { sourceFilter == null || it.source == sourceFilter }
            .filter { typeFilter == null || it.examType == typeFilter }
            .sortedByDescending { it.year ?: 0 }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilterChipRow(
            options = listOf(
                null              to "الكل",
                ExamSource.MINISTRY to "وزارة",
                ExamSource.SCHOOL   to "مدارس"
            ),
            selected = sourceFilter,
            accentColor = AccentExam,
            onSelect = { sourceFilter = it }
        )

        FilterChipRow(
            options = listOf(
                null               to "كل الأنواع",
                ExamType.FINAL     to "نهائي",
                ExamType.SEMI_FINAL to "نصف سنوي",
                ExamType.MONTHLY   to "شهري"
            ),
            selected = typeFilter,
            accentColor = AccentExam.copy(alpha = 0.7f),
            onSelect = { typeFilter = it }
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentExam)
                }
            }

            filtered.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📋", fontSize = 40.sp)
                    Text(
                        text = "لا توجد امتحانات",
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "لم يتم إضافة امتحانات لهذا الفلتر بعد",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filtered.forEach { exam ->
                        ExamCard(
                            exam = exam,
                            score = null, // TODO: wire attempt history
                            onClick = { onExamClick(exam.id) }
                        )
                    }
                }
            }
        }
    }
}