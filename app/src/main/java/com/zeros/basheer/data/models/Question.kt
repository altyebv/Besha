package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Quiz questions of all types.
 * Questions are first-class citizens - they link to concepts and can be reused across exams.
 */
@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index("unitId"),
        Index("type"),
        Index("difficulty")
    ]
)
data class Question(
    @PrimaryKey
    val id: String,                         // e.g., "geo_q_001"
    val subjectId: String,
    val unitId: String? = null,             // Optional: which unit this question is from
    val type: QuestionType,
    val textAr: String,                     // Question text in Arabic
    val textEn: String? = null,             // Optional English version
    
    // Answer fields - structure depends on type
    val correctAnswer: String,              // For T/F: "true"/"false", MCQ: the correct option
    val options: String? = null,            // For MCQ/MATCH: JSON array of options
    val explanation: String? = null,        // Why this answer is correct
    
    // Media
    val imageUrl: String? = null,           // For figure-based questions
    val tableData: String? = null,          // For table questions: JSON
    
    // Metadata
    val difficulty: Int = 1,                // 1-5 scale
    val points: Int = 1,                    // Point value in exam
    val estimatedSeconds: Int = 60          // Expected time to answer
)

enum class QuestionType {
    TRUE_FALSE,         // صح/خطأ - Swipe left/right in feeds
    MCQ,                // اختر الإجابة الصحيحة - Multiple choice
    FILL_BLANK,         // أكمل الفراغ - Fill in the blank
    MATCH,              // وصّل - Match items (A with 1, B with 2)
    SHORT_ANSWER,       // أجب بإيجاز - Short written answer
    EXPLAIN,            // اشرح/علل - Explanation required
    LIST,               // اذكر اثنين/ثلاثة - List items
    TABLE,              // أكمل الجدول - Complete the table
    FIGURE,             // من الشكل - Figure-based question
    COMPARE,            // قارن بين - Comparison question
    ORDER               // رتب - Put in correct order
}
