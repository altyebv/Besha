package com.zeros.basheer.feature.feed.domain.algorithm

import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.feed.data.mapper.FeedMapper
import com.zeros.basheer.feature.feed.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType
import com.zeros.basheer.feature.feed.domain.usecase.SelectFeedQuestionUseCase
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.subject.domain.model.Subject
import org.json.JSONArray
import javax.inject.Inject

/**
 * Groups a flat list of [FeedItem]s by concept and appends a quiz card at the
 * tail of each group — provided a suitable question exists and the session
 * quiz budget hasn't been exhausted.
 *
 * Output is a flat, ordered list of [FeedCard]s ready for the ViewModel.
 *
 * Teach → Test micro-loop per concept:
 *   [DEFINITION] [FORMULA] [FACT]  →  [MINI_QUIZ from quiz bank]
 */
class ConceptGroupBuilder @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val selectFeedQuestion: SelectFeedQuestionUseCase
) {

    /**
     * @param items       Feed items (already deduplicated, ordered by priority).
     * @param subjectMap  Subject id → Subject, used to populate [FeedCard.subjectName].
     * @param quizBudget  Max number of quiz cards to inject for this session.
     */
    suspend fun build(
        items: List<FeedItem>,
        subjectMap: Map<String, Subject>,
        quizBudget: Int
    ): List<FeedCard> {
        if (items.isEmpty()) return emptyList()

        // Pre-compute encounter order to avoid O(n²) indexOf calls.
        val firstEncounterIndex: Map<String, Int> = buildMap {
            items.forEachIndexed { index, item ->
                putIfAbsent(item.conceptId, index)
            }
        }

        val groups: Map<String, List<FeedItem>> = items
            .groupBy { it.conceptId }
            .entries
            .sortedBy { (conceptId, _) -> firstEncounterIndex[conceptId] ?: Int.MAX_VALUE }
            .associate { it.key to it.value }

        val result = mutableListOf<FeedCard>()
        var quizzesInjected = 0

        for ((conceptId, groupItems) in groups) {
            // Map content cards via the standard mapper
            result.addAll(FeedMapper.toFeedCards(groupItems, subjectMap))

            // Attempt to append a quiz card if budget remains
            if (quizzesInjected < quizBudget) {
                val review   = conceptRepository.getReviewByConcept(conceptId)
                val question = selectFeedQuestion(conceptId, review)

                if (question != null) {
                    val subject = subjectMap[groupItems.first().subjectId]
                    result.add(question.toFeedCard(conceptId, subject))
                    quizzesInjected++
                }
            }
        }

        return result
    }

    // ── Question → FeedCard ──────────────────────────────────────────────────

    private fun Question.toFeedCard(conceptId: String, subject: Subject?): FeedCard =
        FeedCard(
            id              = "quiz_$id",
            conceptId       = conceptId,
            subjectId       = subjectId,
            subjectName     = subject?.nameAr ?: "",
            type            = FeedItemType.MINI_QUIZ,
            contentAr       = textAr,
            back            = null,
            contentEn       = textEn,
            imageUrl        = imageUrl,
            interactionType = type.toInteractionType(),
            correctAnswer   = correctAnswer,
            options         = parseOptions(options),
            explanation     = explanation,
            priority        = difficulty
        )

    private fun QuestionType.toInteractionType(): InteractionType =
        when (this) {
            QuestionType.TRUE_FALSE -> InteractionType.SWIPE_TF
            else                    -> InteractionType.MCQ
        }

    private fun parseOptions(optionsJson: String?): List<String>? {
        if (optionsJson.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(optionsJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            null
        }
    }
}