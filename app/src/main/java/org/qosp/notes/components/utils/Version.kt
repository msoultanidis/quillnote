package org.qosp.notes.components.utils

class Version(val version: String) : Comparable<Version> {

    init {
        require(version.matches(Regex("[0-9]+(\\.[0-9]+)*"))) { "Given string is not a valid version." }
    }

    override operator fun compareTo(other: Version): Int {
        val sections = version.split("\\.")
        val otherSections = other.version.split("\\.")
        val length = sections.size.coerceAtLeast(otherSections.size)

        for (i in 0 until length) {
            val section = sections.getOrNull(i)?.toInt() ?: 0
            val otherSection = otherSections.getOrNull(i)?.toInt() ?: 0

            when {
                section < otherSection -> return -1
                section > otherSection -> return 1
            }
        }

        return 0
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is Version -> this.compareTo(other) == 0
            else -> false
        }
    }
}
