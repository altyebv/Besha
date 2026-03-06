package com.zeros.basheer.feature.practice.domain.usecase

import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionStats
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import javax.inject.Inject

/**
 * Identifies the student's weakest questions for a subject and creates a
 * targeted WEAK_AREAS practice session from them.
 *
 * ── How weakness is measured ─────────────────────────────────────────────────
 * Source of truth: [QuestionStats] in Room — updated live after every answer
 * across all surfaces (checkpoints, practice, exams, feed).
 *
 * Weakness score = (1 - successRate) × timesAsked
 *
 * This ranks questions that are BOTH hard AND frequently seen above questions
 * the student has only attempted once. A question answered wrong 7 out of 10
 * times is a much stronger weak signal than one answered wrong 1 out of 1 time.
 *
 * Questions with fewer than [Config.minAttempts] attempts are excluded — there
 * is not enough signal yet to call them weak.
 *
 * Checkpoint questions (isCheckpoint = true) are excluded. They are inline
 * lesson gates, not standalone practice questions.
 *
 * ── Usage ────────────────────────────────────────────────────────────────────
 * ```kotlin
 * // Simple: create a session for the worst 20 questions
 * val sessionId = useCase(subjectId = "physics")
 *
 * // With overrides
 * val sessionId = useCase(
 *     subjectId    = "physics",
 *     maxQuestions = 15,
 *     config       = GetWeakAreaQuestionsUseCase.Config(weakThreshold = 0.5f)
 * )
 *
 * // Inspect without creating a session (for UI preview, recommendations)
 * val weakStats = useCase.getWeakStats(subjectId = "physics")
 * ```
 *
 * ── Output ───────────────────────────────────────────────────────────────────
 * Returns the new [sessionId] (Long). The session is immediately ready to
 * navigate to — pass it to [PracticeSessionViewModel] via savedStateHandle.
 */
class GetWeakAreaQuestionsUseCase @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    private val practiceRepository: PracticeRepository,
) {

    /**
     * Tunable parameters. Defaults are conservative and suitable for early-stage
     * data (small question banks, few attempts). Loosen [minAttempts] as the app
     * matures and students have answered more questions.
     *
     * @param minAttempts    Minimum attempts before a question qualifies as "weak".
     *                       Prevents noise from first-time questions.
     * @param weakThreshold  successRate below this = weak. 0.6 = answered wrong >40% of time.
     * @param candidateLimit How many weak stats rows to fetch from Room before
     *                       filtering to [maxQuestions]. Wider net → better ranking.
     */
    data class Config(
        val minAttempts: Int   = 3,
        val weakThreshold: Float = 0.60f,
        val candidateLimit: Int  = 50,
    )

    /**
     * Fetch weak [QuestionStats] for a subject without creating a session.
     * Use this for recommendation previews, weak-area counts, or any UI that
     * needs to show "you have N weak questions in physics" without launching
     * a session.
     *
     * Returns stats sorted by weakness severity (worst first).
     */
    suspend fun getWeakStats(
        subjectId: String,
        config: Config = Config(),
    ): List<QuestionStats> =
        quizBankRepository.getWeakQuestionsForSubject(
            subjectId   = subjectId,
            minAttempts = config.minAttempts,
            threshold   = config.weakThreshold,
            limit       = config.candidateLimit,
        )

    /**
     * Fetch weak [Question] objects for a subject without creating a session.
     * Useful when the caller needs the full question payload (text, type, etc.)
     * rather than just stats — e.g., showing a preview list in the UI.
     *
     * Returns questions sorted by weakness severity (worst first).
     * Returns an empty list if there are no qualifying weak questions yet.
     */
    suspend fun getWeakQuestions(
        subjectId: String,
        maxQuestions: Int = 20,
        config: Config = Config(),
    ): List<Question> {
        val weakStats = getWeakStats(subjectId, config)
        if (weakStats.isEmpty()) return emptyList()

        val questionIds = weakStats
            .take(maxQuestions)
            .map { it.questionId }

        // Fetch full Question objects, preserving the weakness rank order
        return questionIds
            .mapNotNull { quizBankRepository.getQuestionById(it) }
    }

    /**
     * Create a WEAK_AREAS practice session for a subject.
     *
     * @param subjectId    The subject to analyze.
     * @param maxQuestions Maximum questions in the session. Default 20.
     * @param shuffled     Whether to shuffle the question order. Default true.
     *                     Pass false to preserve weakness rank (worst first).
     * @param config       Tunable thresholds. See [Config].
     * @return The new session ID, or null if there are no qualifying weak questions.
     */
    suspend operator fun invoke(
        subjectId: String,
        maxQuestions: Int = 20,
        shuffled: Boolean = true,
        config: Config = Config(),
    ): Long? {
        val questions = getWeakQuestions(
            subjectId    = subjectId,
            maxQuestions = maxQuestions,
            config       = config,
        )

        if (questions.isEmpty()) return null

        return practiceRepository.createSessionFromQuestionIds(
            subjectId      = subjectId,
            questionIds    = questions.map { it.id },
            generationType = PracticeGenerationType.WEAK_AREAS,
            shuffled       = shuffled,
        )
    }
}