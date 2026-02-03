package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Alternative explanations, examples, and content for a concept.
 *
 * This enables the "multiple versions" feature where students can:
 * - See different explanations of the same concept
 * - View examples from different sources (official, teacher, cheatsheet)
 * - Access mnemonics and analogies that help them remember
 *
 * Contributors can add new variants without modifying the core lesson content.
 */
@Entity(
    tableName = "content_variants",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId"),
        Index("type"),
        Index("source")
    ]
)
data class ContentVariant(
    @PrimaryKey
    val id: String,                          // e.g., "geo_gravity_explain_v2"
    val conceptId: String,                   // Links to the parent concept
    val type: VariantType,
    val source: ContentSource,

    // Content
    val contentAr: String,                   // The explanation/example in Arabic
    val contentEn: String? = null,           // Optional English version
    val imageUrl: String? = null,            // Optional diagram/illustration

    // Attribution & Quality
    val authorName: String? = null,          // "أ. محمد أحمد" - for teacher contributions
    val authorTitle: String? = null,         // "معلم فيزياء - ثانوية الخرطوم"
    val upvotes: Int = 0,                    // Community rating (future feature)
    val order: Int = 0,                      // Display order within same type

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false          // Reviewed by moderator
)

/**
 * Types of content variants.
 */
enum class VariantType {
    EXPLANATION,     // شرح بديل - Alternative explanation
    EXAMPLE,         // مثال - Worked example
    ANALOGY,         // تشبيه - Analogy to help understand
    MNEMONIC,        // طريقة حفظ - Memory technique
    VISUAL,          // رسم توضيحي - Diagram or visual aid
    VIDEO_LINK,      // رابط فيديو - Link to external video (future)
    SUMMARY          // ملخص - Brief summary
}

/**
 * Source of the content.
 */
enum class ContentSource {
    OFFICIAL,        // المنهج الرسمي - From official curriculum
    TEACHER,         // من المعلمين - Teacher contributed
    CHEATSHEET,      // الملخصات - From study guides/cheatsheets
    COMMUNITY        // من المساهمين - Community contributed
}