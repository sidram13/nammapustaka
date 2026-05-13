package com.example.nammapustaka1.utils

object OpenLibraryHelper {

    /**
     * Returns the Open Library cover URL for a given ISBN.
     * Size: S (small), M (medium), L (large)
     */
    fun getCoverUrlByIsbn(isbn: String, size: String = "M"): String {
        val cleanIsbn = isbn.replace("-", "").trim()
        return "https://covers.openlibrary.org/b/isbn/$cleanIsbn-$size.jpg"
    }

    /**
     * Returns the Open Library cover URL for a given OLID (Open Library ID).
     */
    fun getCoverUrlByOlid(olid: String, size: String = "M"): String {
        return "https://covers.openlibrary.org/b/olid/$olid-$size.jpg"
    }

    /**
     * Returns the Open Library cover URL by title-based cover ID.
     */
    fun getCoverUrlById(coverId: String, size: String = "M"): String {
        return "https://covers.openlibrary.org/b/id/$coverId-$size.jpg"
    }

    /**
     * Generates a unique QR code string for a new book.
     */
    fun generateQrCode(title: String, isbn: String): String {
        val sanitized = title.take(10).replace(" ", "_").uppercase()
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "BOOK_${sanitized}_$timestamp"
    }
}
