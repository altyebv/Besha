package com.zeros.basheer.data.local.dao

import com.zeros.basheer.feature.streak.data.entity.DailyActivityEntity
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.data.entity.StreakThresholds

/**
 * Extension function to recalculate streak level based on activity.
 */
private fun DailyActivityEntity.withRecalculatedLevel(): DailyActivityEntity {
    val level = when {
        // FLAME: Deep work achieved
        lessonsCompleted >= StreakThresholds.LESSONS_FOR_FLAME -> StreakLevel.FLAME
        examsCompleted >= StreakThresholds.EXAMS_FOR_FLAME -> StreakLevel.FLAME
        
        // SPARK: Light engagement
        feedCardsReviewed >= StreakThresholds.CARDS_FOR_SPARK -> StreakLevel.SPARK
        quizQuestionsAnswered >= StreakThresholds.QUESTIONS_FOR_SPARK -> StreakLevel.SPARK
        timeSpentSeconds >= StreakThresholds.TIME_SECONDS_FOR_SPARK -> StreakLevel.SPARK
        
        // COLD: Not enough activity
        else -> StreakLevel.COLD
    }
    return copy(streakLevel = level)
}
