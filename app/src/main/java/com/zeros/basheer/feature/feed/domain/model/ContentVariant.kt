package com.zeros.basheer.feature.feed.domain.model

data class ContentVariant(
    val id: String,
    val conceptId: String,
    val type: VariantType,
    val source: ContentSource,
    val contentAr: String,
    val contentEn: String? = null,
    val imageUrl: String? = null,
    val authorName: String? = null,
    val authorTitle: String? = null,
    val upvotes: Int = 0,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false
)

enum class VariantType {
    EXPLANATION,
    EXAMPLE,
    ANALOGY,
    MNEMONIC,
    VISUAL,
    VIDEO_LINK,
    SUMMARY
}

enum class ContentSource {
    OFFICIAL,
    TEACHER,
    CHEATSHEET,
    COMMUNITY
}