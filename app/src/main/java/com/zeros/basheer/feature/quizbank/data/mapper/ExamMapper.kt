package com.zeros.basheer.feature.quizbank.data.mapper

import com.zeros.basheer.feature.quizbank.data.entity.ExamEntity
import com.zeros.basheer.feature.quizbank.data.entity.ExamQuestionEntity
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamQuestion

// ── Exam ──────────────────────────────────────────────────────────────────────

fun ExamEntity.toDomain(): Exam = Exam(
    id = id,
    subjectId = subjectId,
    titleAr = titleAr,
    titleEn = titleEn,
    source = source,
    year = year,
    schoolName = schoolName,
    duration = duration,
    totalPoints = totalPoints,
    description = description,
    examType = examType,
    sectionsJson = sectionsJson
)

fun Exam.toEntity(): ExamEntity = ExamEntity(
    id = id,
    subjectId = subjectId,
    titleAr = titleAr,
    titleEn = titleEn,
    source = source,
    year = year,
    schoolName = schoolName,
    duration = duration,
    totalPoints = totalPoints,
    description = description,
    examType = examType,
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