package com.zeros.basheer.data.repository

import android.content.Context
import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles seeding the database from JSON files exported by the React authoring tool.
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
    private val examQuestionDao: ExamQuestionDao
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    /**
     * Seed database from a JSON string (from assets or network)
     */
    suspend fun seedFromJson(jsonString: String) {
        val data = json.decodeFromString<BasheerExportData>(jsonString)
        seedFromData(data)
    }

    /**
     * Seed database from assets file
     */
    suspend fun seedFromAssets(context: Context, fileName: String) {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        seedFromJson(jsonString)
    }

    /**
     * Seed database from parsed data
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
    }
}

// ==========================================
// JSON DATA CLASSES (for parsing)
// ==========================================

@Serializable
data class BasheerExportData(
    val version: String = "1.0",
    val subject: SubjectJson,
    val tags: List<TagJson> = emptyList(),
    val concepts: List<ConceptJson> = emptyList(),
    val units: List<UnitJson> = emptyList(),
    val questions: List<QuestionJson> = emptyList(),
    val exams: List<ExamJson> = emptyList()
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
