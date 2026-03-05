package com.zeros.basheer.feature.quizbank.data.mapper

import com.zeros.basheer.feature.quizbank.data.entity.ExamEntity
import com.zeros.basheer.feature.quizbank.data.entity.ExamQuestionEntity
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamQuestion
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.ExamType

// ── Exam ──────────────────────────────────────────────────────────────────────

fun ExamEntity.toDomain(): Exam = Exam(
    id = id,
    subjectId = subjectId,
    titleAr = titleAr,
    titleEn = titleEn,
    source = ExamSource.valueOf(source),
    year = year,
    schoolName = schoolName,
    duration = duration,
    totalPoints = totalPoints,
    description = description,
    examType = examType?.let { ExamType.valueOf(it) },
    sectionsJson = sectionsJson
)

fun Exam.toEntity(): ExamEntity = ExamEntity(
    id = id,
    subjectId = subjectId,
    titleAr = titleAr,
    titleEn = titleEn,
    source = source.name,
    year = year,
    schoolName = schoolName,
    duration = duration,
    totalPoints = totalPoints,
    description = description,
    examType = examType?.name,
    sectionsJson = sectionsJson
)

// ── ExamQuestion junction ─────────────────────────────────────────────────────

fun ExamQuestionEntity.toDomain(): ExamQuestion = ExamQuestion(
    examId = examId,
    questionId = questionId,
    order = order,
    sectionLabel = sectionLabel,
    points = points
)

fun ExamQuestion.toEntity(): ExamQuestionEntity = ExamQuestionEntity(
    examId = examId,
    questionId = questionId,
    order = order,
    sectionLabel = sectionLabel,
    points = points
)
