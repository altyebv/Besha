package com.zeros.basheer.data.repository

import android.content.Context
import com.google.gson.Gson
import com.zeros.basheer.data.local.dao.*

import com.zeros.basheer.data.models.Lesson
import com.zeros.basheer.data.models.QuestionStats
import com.zeros.basheer.data.models.Section
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.data.models.Subject
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.Tag
import com.zeros.basheer.data.models.Block
import com.zeros.basheer.data.models.BlockType
import com.zeros.basheer.data.models.CognitiveLevel
import com.zeros.basheer.data.models.ConceptTag
import com.zeros.basheer.data.models.ConceptType
import com.zeros.basheer.data.models.Exam
import com.zeros.basheer.data.models.ExamQuestion
import com.zeros.basheer.data.models.ExamSource
import com.zeros.basheer.data.models.FeedItem
import com.zeros.basheer.data.models.FeedItemType
import com.zeros.basheer.data.models.InteractionType
import com.zeros.basheer.data.models.PracticeGenerationType
import com.zeros.basheer.data.models.PracticeSession
import com.zeros.basheer.data.models.PracticeSessionStatus
import com.zeros.basheer.data.models.Question
import com.zeros.basheer.data.models.QuestionConcept
import com.zeros.basheer.data.models.QuestionSource
import com.zeros.basheer.data.models.QuestionType
import com.zeros.basheer.data.models.SectionConcept
import com.zeros.basheer.data.models.StudentPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified seeder that handles both lesson content AND quiz bank data.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val sectionDao: SectionDao,
    private val blockDao: BlockDao,
    private val conceptDao: ConceptDao,
    private val tagDao: TagDao,
    private val conceptTagDao: ConceptTagDao,
    private val sectionConceptDao: SectionConceptDao,
    private val questionDao: QuestionDao,
    private val questionConceptDao: QuestionConceptDao,
    private val examDao: ExamDao,
    private val examQuestionDao: ExamQuestionDao,
    private val feedItemDao: FeedItemDao,
    private val practiceSessionDao: PracticeSessionDao,
    private val questionStatsDao: QuestionStatsDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val gson = Gson()

    // ==================== LESSON CONTENT SEEDING ====================

    /**
     * Seed lesson content from JSON string (from React authoring tool)
     */
    suspend fun seedFromJson(jsonString: String) {
        val data = json.decodeFromString<BasheerExportData>(jsonString)
        seedFromData(data)
    }

    /**
     * Seed lesson content from assets file
     */
    suspend fun seedFromAssets(context: Context, fileName: String) {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        seedFromJson(jsonString)
    }

    /**
     * Seed database from parsed lesson data
     */
    suspend fun seedFromData(data: BasheerExportData) {
        // 1. Insert subject
        subjectDao.insertSubject(data.subject.toEntity())

        // 2. Insert tags
        data.tags.forEach { tag ->
            tagDao.insertTag(tag.toEntity())
        }

        // 3. Insert concepts and their tags
        data.concepts.forEach { concept ->
            conceptDao.insertConcept(concept.toEntity(data.subject.id))
            concept.tagIds?.forEach { tagId ->
                conceptTagDao.insert(ConceptTag(concept.id, tagId))
            }
        }

        // 4. Insert units, lessons, sections, blocks
        data.units.forEach { unit ->
            unitDao.insertUnit(unit.toEntity(data.subject.id))

            unit.lessons.forEach { lesson ->
                lessonDao.insertLesson(lesson.toEntity(unit.id))

                lesson.sections.forEach { section ->
                    sectionDao.insertSection(section.toEntity(lesson.id))

                    // Insert section-concept links
                    section.conceptIds.forEachIndexed { index, conceptId ->
                        sectionConceptDao.insert(
                            SectionConcept(
                                sectionId = section.id,
                                conceptId = conceptId,
                                order = index
                            )
                        )
                    }

                    // Insert blocks
                    section.blocks.forEach { block ->
                        blockDao.insertBlock(block.toEntity(section.id))
                    }
                }
            }
        }

        // 5. Insert questions and their concept links
        data.questions.forEach { question ->
            questionDao.insertQuestion(question.toEntity(data.subject.id))
            question.conceptIds.forEachIndexed { index, conceptId ->
                questionConceptDao.insert(
                    QuestionConcept(
                        questionId = question.id,
                        conceptId = conceptId,
                        isPrimary = index == 0
                    )
                )
            }
        }

        // 6. Insert exams and their question links
        data.exams.forEach { exam ->
            examDao.insertExam(exam.toEntity(data.subject.id))
            exam.questionIds.forEachIndexed { index, questionId ->
                examQuestionDao.insert(
                    ExamQuestion(
                        examId = exam.id,
                        questionId = questionId,
                        order = index + 1
                    )
                )
            }
        }

        // 7. Insert feed items
        data.feedItems.forEach { feedItem ->
            feedItemDao.insertFeedItem(feedItem.toEntity(data.subject.id))
        }
    }

    // ==================== QUIZ BANK SEEDING ====================

    /**
     * Seed quiz bank mock data from assets (for testing/demo)
     * Reads from: assets/quiz_bank_mock_data.json
     */
    suspend fun seedQuizBankFromAssets(context: Context) = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("Exams.json")
            val reader = inputStream.bufferedReader()
            val mockData = gson.fromJson(reader, QuizBankMockData::class.java)
            reader.close()

            seedQuizBankData(mockData)
            println("✅ Quiz Bank seeded successfully from assets")
        } catch (e: Exception) {
            println("❌ Failed to seed Quiz Bank: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Seed quiz bank data
     */
    private suspend fun seedQuizBankData(mockData: QuizBankMockData) {
        // Subjects (if not already exists from lesson seeding)
        mockData.subjects.forEach { subject ->
            try {
                subjectDao.insertSubject(
                    Subject(
                        id = subject.id,
                        nameAr = subject.nameAr,
                        nameEn = subject.nameEn,
                        path = StudentPath.LITERARY
                    )
                )
            } catch (e: Exception) {
                // Subject might already exist from lesson seeding - that's fine
            }
        }

        // Units (if not already exists)
        mockData.units.forEach { unit ->
            try {
                unitDao.insertUnit(
                    Units(
                        id = unit.id,
                        subjectId = unit.subjectId,
                        title = unit.nameAr,
                        order = unit.order
                    )
                )
            } catch (e: Exception) {
                // Unit might already exist
            }
        }

        // Exams
        mockData.exams.forEach { exam ->
            examDao.insertExam(
                Exam(
                    id = exam.id,
                    subjectId = exam.subjectId,
                    titleAr = exam.titleAr,
                    titleEn = exam.titleEn,
                    source = ExamSource.valueOf(exam.source),
                    year = exam.year,
                    schoolName = exam.schoolName,
                    duration = exam.duration,
                    totalPoints = exam.totalPoints,
                    description = exam.description
                )
            )
        }

        // Questions
        mockData.questions.forEach { question ->
            questionDao.insertQuestion(
                Question(
                    id = question.id,
                    subjectId = question.subjectId,
                    unitId = question.unitId,
                    lessonId = question.lessonId,
                    type = QuestionType.valueOf(question.type),
                    textAr = question.textAr,
                    textEn = question.textEn,
                    correctAnswer = question.correctAnswer,
                    options = question.options,
                    explanation = question.explanation,
                    imageUrl = question.imageUrl,
                    tableData = question.tableData,
                    source = QuestionSource.valueOf(question.source),
                    sourceExamId = question.sourceExamId,
                    sourceDetails = question.sourceDetails,
                    sourceYear = question.sourceYear,
                    difficulty = question.difficulty,
                    cognitiveLevel = CognitiveLevel.valueOf(question.cognitiveLevel),
                    points = question.points,
                    estimatedSeconds = question.estimatedSeconds,
                    feedEligible = question.feedEligible
                )
            )
        }

        // Link questions to exams
        mockData.questions.forEachIndexed { index, question ->
            if (question.sourceExamId != null) {
                examQuestionDao.insert(
                    ExamQuestion(
                        examId = question.sourceExamId,
                        questionId = question.id,
                        order = index + 1,
                        points = question.points
                    )
                )
            }
        }

        // Practice sessions
        mockData.practiceSessions.forEach { session ->
            practiceSessionDao.insertSession(
                PracticeSession(
                    id = session.id,
                    subjectId = session.subjectId,
                    generationType = PracticeGenerationType.valueOf(session.generationType),
                    filterUnitIds = session.filterUnitIds,
                    filterLessonIds = session.filterLessonIds,
                    filterConceptIds = session.filterConceptIds,
                    filterQuestionTypes = session.filterQuestionTypes,
                    filterDifficulty = session.filterDifficulty,
                    filterSource = session.filterSource,
                    questionCount = session.questionCount,
                    timeLimitSeconds = session.timeLimitSeconds,
                    shuffled = session.shuffled,
                    difficultyDistribution = session.difficultyDistribution,
                    status = PracticeSessionStatus.valueOf(session.status),
                    currentQuestionIndex = session.currentQuestionIndex,
                    correctCount = session.correctCount,
                    wrongCount = session.wrongCount,
                    skippedCount = session.skippedCount,
                    score = session.score,
                    startedAt = session.startedAt,
                    completedAt = session.completedAt,
                    totalTimeSeconds = session.totalTimeSeconds
                )
            )
        }

        // Question stats
        mockData.questionStats.forEach { stats ->
            questionStatsDao.upsertStats(
                QuestionStats(
                    questionId = stats.questionId,
                    timesAsked = stats.timesAsked,
                    timesCorrect = stats.timesCorrect,
                    avgTimeSeconds = stats.averageTimeSeconds.toFloat(), // Convert Int to Float
                    successRate = stats.successRate,
                    lastShownInFeed = stats.lastShownInFeed,
                    feedShowCount = 0,  // Default value
                    lastAskedAt = null,  // Default value
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

// ==================== LESSON CONTENT JSON CLASSES ====================

@Serializable
data class BasheerExportData(
    val version: String = "1.0",
    val subject: SubjectJson,
    val tags: List<TagJson> = emptyList(),
    val concepts: List<ConceptJson> = emptyList(),
    val units: List<UnitJson> = emptyList(),
    val questions: List<QuestionJson> = emptyList(),
    val exams: List<ExamJson> = emptyList(),
    val feedItems: List<FeedItemJson> = emptyList()
)

@Serializable
data class SubjectJson(
    val id: String,
    val nameAr: String,
    val nameEn: String? = null,
    val path: String,
    val isMajor: Boolean = false,
    val order: Int = 0,
    val colorHex: String? = null
) {
    fun toEntity() = Subject(
        id = id,
        nameAr = nameAr,
        nameEn = nameEn,
        path = StudentPath.valueOf(path),
        isMajor = isMajor,
        order = order,
        colorHex = colorHex
    )
}

@Serializable
data class TagJson(
    val id: String,
    val nameAr: String,
    val nameEn: String? = null
) {
    fun toEntity() = Tag(id = id, nameAr = nameAr, nameEn = nameEn)
}

@Serializable
data class ConceptJson(
    val id: String,
    val type: String,
    val titleAr: String,
    val titleEn: String? = null,
    val definition: String,
    val shortDefinition: String? = null,
    val formula: String? = null,
    val imageUrl: String? = null,
    val difficulty: Int = 1,
    val extraData: String? = null,
    val tagIds: List<String>? = null
) {
    fun toEntity(subjectId: String) = Concept(
        id = id,
        subjectId = subjectId,
        type = ConceptType.valueOf(type),
        titleAr = titleAr,
        titleEn = titleEn,
        definition = definition,
        shortDefinition = shortDefinition,
        formula = formula,
        imageUrl = imageUrl,
        difficulty = difficulty,
        extraData = extraData
    )
}

@Serializable
data class UnitJson(
    val id: String,
    val title: String,
    val order: Int,
    val description: String? = null,
    val lessons: List<LessonJson> = emptyList()
) {
    fun toEntity(subjectId: String) = Units(
        id = id,
        subjectId = subjectId,
        title = title,
        order = order,
        description = description
    )
}

@Serializable
data class LessonJson(
    val id: String,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int = 15,
    val summary: String? = null,
    val sections: List<SectionJson> = emptyList()
) {
    fun toEntity(unitId: String) = Lesson(
        id = id,
        unitId = unitId,
        title = title,
        order = order,
        estimatedMinutes = estimatedMinutes,
        summary = summary
    )
}

@Serializable
data class SectionJson(
    val id: String,
    val title: String,
    val order: Int,
    val conceptIds: List<String> = emptyList(),
    val blocks: List<BlockJson> = emptyList()
) {
    fun toEntity(lessonId: String) = Section(
        id = id,
        lessonId = lessonId,
        title = title,
        order = order
    )
}

@Serializable
data class BlockJson(
    val id: String,
    val type: String,
    val content: String,
    val order: Int,
    val conceptRef: String? = null,
    val caption: String? = null,
    val metadata: String? = null
) {
    fun toEntity(sectionId: String) = Block(
        id = id,
        sectionId = sectionId,
        type = BlockType.valueOf(type),
        content = content,
        order = order,
        conceptRef = conceptRef,
        caption = caption,
        metadata = metadata
    )
}

@Serializable
data class QuestionJson(
    val id: String,
    val type: String,
    val textAr: String,
    val textEn: String? = null,
    val correctAnswer: String,
    val options: String? = null,
    val explanation: String? = null,
    val imageUrl: String? = null,
    val tableData: String? = null,
    val difficulty: Int = 1,
    val points: Int = 1,
    val conceptIds: List<String> = emptyList(),
    val unitId: String? = null
) {
    fun toEntity(subjectId: String) = Question(
        id = id,
        subjectId = subjectId,
        unitId = unitId,
        type = QuestionType.valueOf(type),
        textAr = textAr,
        textEn = textEn,
        correctAnswer = correctAnswer,
        options = options,
        explanation = explanation,
        imageUrl = imageUrl,
        tableData = tableData,
        difficulty = difficulty,
        points = points
    )
}

@Serializable
data class ExamJson(
    val id: String,
    val titleAr: String,
    val titleEn: String? = null,
    val source: String,
    val year: Int? = null,
    val schoolName: String? = null,
    val duration: Int? = null,
    val totalPoints: Int? = null,
    val questionIds: List<String> = emptyList()
) {
    fun toEntity(subjectId: String) = Exam(
        id = id,
        subjectId = subjectId,
        titleAr = titleAr,
        titleEn = titleEn,
        source = ExamSource.valueOf(source),
        year = year,
        schoolName = schoolName,
        duration = duration,
        totalPoints = totalPoints
    )
}

@Serializable
data class FeedItemJson(
    val id: String,
    val conceptId: String,
    val type: String,
    val contentAr: String,
    val contentEn: String? = null,
    val imageUrl: String? = null,
    val interactionType: String? = null,
    val correctAnswer: String? = null,
    val options: String? = null,
    val explanation: String? = null,
    val priority: Int = 1,
    val order: Int = 0
) {
    fun toEntity(subjectId: String) = FeedItem(
        id = id,
        conceptId = conceptId,
        subjectId = subjectId,
        type = FeedItemType.valueOf(type),
        contentAr = contentAr,
        contentEn = contentEn,
        imageUrl = imageUrl,
        interactionType = interactionType?.let { InteractionType.valueOf(it) },
        correctAnswer = correctAnswer,
        options = options,
        explanation = explanation,
        priority = priority,
        order = order
    )
}

// ==================== QUIZ BANK JSON CLASSES (for Gson) ====================

data class QuizBankMockData(
    val subjects: List<QuizBankSubjectJson>,
    val units: List<QuizBankUnitJson>,
    val exams: List<QuizBankExamJson>,
    val questions: List<QuizBankQuestionJson>,
    val practiceSessions: List<PracticeSessionJson>,
    val questionStats: List<QuestionStatsJson>
)

data class QuizBankSubjectJson(
    val id: String,
    val nameAr: String,
    val nameEn: String?,
    val iconUrl: String?
)

data class QuizBankUnitJson(
    val id: String,
    val subjectId: String,
    val nameAr: String,
    val nameEn: String?,
    val order: Int
)

data class QuizBankExamJson(
    val id: String,
    val subjectId: String,
    val titleAr: String,
    val titleEn: String?,
    val source: String,
    val year: Int?,
    val schoolName: String?,
    val duration: Int?,
    val totalPoints: Int?,
    val description: String?
)

data class QuizBankQuestionJson(
    val id: String,
    val subjectId: String,
    val unitId: String?,
    val lessonId: String?,
    val type: String,
    val textAr: String,
    val textEn: String?,
    val correctAnswer: String,
    val options: String?,
    val explanation: String?,
    val imageUrl: String?,
    val tableData: String?,
    val source: String,
    val sourceExamId: String?,
    val sourceDetails: String?,
    val sourceYear: Int?,
    val difficulty: Int,
    val cognitiveLevel: String,
    val points: Int,
    val estimatedSeconds: Int,
    val feedEligible: Boolean
)

data class PracticeSessionJson(
    val id: Long,
    val subjectId: String,
    val generationType: String,
    val filterUnitIds: String?,
    val filterLessonIds: String?,
    val filterConceptIds: String?,
    val filterQuestionTypes: String?,
    val filterDifficulty: String?,
    val filterSource: String?,
    val questionCount: Int,
    val timeLimitSeconds: Int?,
    val shuffled: Boolean,
    val difficultyDistribution: String?,
    val status: String,
    val currentQuestionIndex: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val score: Float?,
    val startedAt: Long,
    val completedAt: Long?,
    val totalTimeSeconds: Int?
)

data class QuestionStatsJson(
    val questionId: String,
    val timesAsked: Int,
    val timesCorrect: Int,
    val successRate: Float,
    val averageTimeSeconds: Int,
    val lastShownInFeed: Long
)