package com.zeros.basheer.feature.quizbank.data.mapper

import com.zeros.basheer.feature.quizbank.data.entity.QuestionConceptEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionResponseEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionStatsEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuizAttemptEntity
import com.zeros.basheer.feature.quizbank.domain.model.ExamAttemptStatus
import com.zeros.basheer.feature.quizbank.domain.model.QuestionConcept
import com.zeros.basheer.feature.quizbank.domain.model.QuestionResponse
import com.zeros.basheer.feature.quizbank.domain.model.QuestionStats
import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt

// ── QuizAttempt ───────────────────────────────────────────────────────────────

fun QuizAttemptEntity.toDomain(): QuizAttempt = QuizAttempt(
    id = id,
    examId = examId,
    startedAt = startedAt,
    completedAt = completedAt,
    score = score,
    totalPoints = totalPoints,
    percentage = percentage,
    timeSpentSeconds = timeSpentSeconds,
    status = try {
        ExamAttemptStatus.valueOf(status)
    } catch (e: Exception) {
        ExamAttemptStatus.IN_PROGRESS
    },
    flaggedQuestions = flaggedQuestions
)

fun QuizAttempt.toEntity(): QuizAttemptEntity = QuizAttemptEntity(
    id = id,
    examId = examId,
    startedAt = startedAt,
    completedAt = completedAt,
    score = score,
    totalPoints = totalPoints,
    percentage = percentage,
    timeSpentSeconds = timeSpentSeconds,
    status = status.name,
    flaggedQuestions = flaggedQuestions
)

// ── QuestionResponse ──────────────────────────────────────────────────────────

fun QuestionResponseEntity.toDomain(): QuestionResponse = QuestionResponse(
    id = id,
    attemptId = attemptId,
    questionId = questionId,
    userAnswer = userAnswer,
    isCorrect = isCorrect,
    pointsEarned = pointsEarned,
    timeSpentSeconds = timeSpentSeconds,
    answeredAt = answeredAt
)

fun QuestionResponse.toEntity(): QuestionResponseEntity = QuestionResponseEntity(
    id = id,
    attemptId = attemptId,
    questionId = questionId,
    userAnswer = userAnswer,
    isCorrect = isCorrect,
    pointsEarned = pointsEarned,
    timeSpentSeconds = timeSpentSeconds,
    answeredAt = answeredAt
)

// ── QuestionStats ─────────────────────────────────────────────────────────────

fun QuestionStatsEntity.toDomain(): QuestionStats = QuestionStats(
    questionId = questionId,
    timesAsked = timesAsked,
    timesCorrect = timesCorrect,
    avgTimeSeconds = avgTimeSeconds,
    successRate = successRate,
    lastShownInFeed = lastShownInFeed,
    feedShowCount = feedShowCount,
    lastAskedAt = lastAskedAt,
    updatedAt = updatedAt
)

fun QuestionStats.toEntity(): QuestionStatsEntity = QuestionStatsEntity(
    questionId = questionId,
    timesAsked = timesAsked,
    timesCorrect = timesCorrect,
    avgTimeSeconds = avgTimeSeconds,
    successRate = successRate,
    lastShownInFeed = lastShownInFeed,
    feedShowCount = feedShowCount,
    lastAskedAt = lastAskedAt,
    updatedAt = updatedAt
)

// ── QuestionConcept junction ──────────────────────────────────────────────────

fun QuestionConceptEntity.toDomain(): QuestionConcept = QuestionConcept(
    questionId = questionId,
    conceptId = conceptId,
    isPrimary = isPrimary
)

fun QuestionConcept.toEntity(): QuestionConceptEntity = QuestionConceptEntity(
    questionId = questionId,
    conceptId = conceptId,
    isPrimary = isPrimary
)
