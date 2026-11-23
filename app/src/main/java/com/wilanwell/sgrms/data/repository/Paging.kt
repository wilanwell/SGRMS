package com.wilanwell.sgrms.data.repository

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Opaque position in a Firestore query, so ViewModels can ask for the next
 * page without depending on Firestore types.
 */
class PageCursor internal constructor(internal val lastDocument: DocumentSnapshot)

data class Page<T>(
    val items: List<T>,
    val nextCursor: PageCursor?
) {
    val hasMore: Boolean get() = nextCursor != null
}

const val DEFAULT_PAGE_SIZE = 25L
