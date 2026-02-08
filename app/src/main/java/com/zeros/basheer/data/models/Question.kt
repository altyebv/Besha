package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity

/**
 * Quiz questions of all types.
 * Questions are first-class citizens - they link to concepts and can be reused across exams.
 * 
 * The Question Bank is organized by:
 * 1. SOURCE: Where did this question come from? (ministry, school, revision sheet, etc.)
 * 2. CURRICULUM: What does it test? (subject, unit, lesson, concepts)
 * 3. CHARACTERISTICS: What kind of question? (type, difficulty, cognitive level)
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
        ),
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index("unitId"),
        Index("lessonId"),
        Index("type"),
        Index("difficulty"),
        Index("source"),
        Index("sourceExamId")
    ]
)
data class Question(
    @PrimaryKey
    val id: String,                         // e.g., "geo_q_001"
    val subjectId: String,
    val unitId: String? = null,             // Which unit this question is from
    val lessonId: String? = null,           // Optional: specific lesson for fine-grained filtering
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
    
    // Source tracking - "Where did this question come from?"
    val source: QuestionSource = QuestionSource.ORIGINAL,
    val sourceExamId: String? = null,       // If from an exam, which one?
    val sourceDetails: String? = null,      // Extra info: "مراجعة أستاذ أحمد", school name, etc.
    val sourceYear: Int? = null,            // Year of source exam/sheet
    
    // Characteristics
    val difficulty: Int = 1,                // 1-5 scale
    val cognitiveLevel: CognitiveLevel = CognitiveLevel.RECALL,
    val points: Int = 1,                    // Point value in exam
    val estimatedSeconds: Int = 60,         // Expected time to answer
    
    // Feed eligibility - can this question appear in review feeds?
    val feedEligible: Boolean = true,       // Only T/F and MCQ should be true
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Types of questions supported.
 */
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

/**
 * Where did this question originate from?
 */
enum class QuestionSource {
    MINISTRY_FINAL,     // امتحان الشهادة النهائي
    MINISTRY_SEMIFINAL, // امتحان نصف السنة
    SCHOOL_EXAM,        // امتحانات المدارس
    REVISION_SHEET,     // أوراق المراجعة
    TEACHER_CONTRIB,    // أسئلة من معلمين
    ORIGINAL            // أسئلة جديدة للتطبيق
}

/**
 * Bloom's taxonomy cognitive levels.
 */
enum class CognitiveLevel {
    RECALL,      // تذكر - Just remember the fact
    UNDERSTAND,  // فهم - Explain in own words
    APPLY,       // تطبيق - Use in new situation
    ANALYZE      // تحليل - Break down, compare
}

/**
 * Helper to check if a question type is suitable for feeds.
 */
fun QuestionType.isFeedEligible(): Boolean {
    return this == QuestionType.TRUE_FALSE || this == QuestionType.MCQ
}
