package com.zeros.basheer.feature.quizbank.data.mapper

import com.zeros.basheer.feature.quizbank.data.entity.QuestionEntity
import com.zeros.basheer.feature.quizbank.domain.model.CognitiveLevel
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionSource
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun QuestionEntity.toDomain(): Question = Question(
    id = id,
    subjectId = subjectId,
    unitId = unitId,
    lessonId = lessonId,
    sectionId = sectionId,
    type = QuestionType.valueOf(type),
    textAr = textAr,
    textEn = textEn,
    correctAnswer = correctAnswer,
    options = options,
    explanation = explanation,
    imageUrl = imageUrl,
    tableData = tableData,
    source = QuestionSource.valueOf(source),
    sourceExamId = sourceExamId,
    sourceDetails = sourceDetails,
    sourceYear = sourceYear,
    difficulty = difficulty,
    cognitiveLevel = CognitiveLevel.valueOf(cognitiveLevel),
    points = points,
    estimatedSeconds = estimatedSeconds,
    feedEligible = feedEligible,
    isCheckpoint = isCheckpoint,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── Domain → Entity ───────────────────────────────────────────────────────────

fun Question.toEntity(): QuestionEntity = QuestionEntity(
    id = id,
    subjectId = subjectId,
    unitId = unitId,
    lessonId = lessonId,
    sectionId = sectionId,
    type = type.name,
    textAr = textAr,
    textEn = textEn,
    correctAnswer = correctAnswer,
    options = options,
    explanation = explanation,
    imageUrl = imageUrl,
    tableData = tableData,
    source = source.name,
    sourceExamId = sourceExamId,
    sourceDetails = sourceDetails,
    sourceYear = sourceYear,
    difficulty = difficulty,
    cognitiveLevel = cognitiveLevel.name,
    points = points,
    estimatedSeconds = estimatedSeconds,
    feedEligible = feedEligible,
    isCheckpoint = isCheckpoint,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── Flow helpers ──────────────────────────────────────────────────────────────

fun Flow<List<QuestionEntity>>.toDomainList(): Flow<List<Question>> =
    map { entities -> entities.map { it.toDomain() } }
