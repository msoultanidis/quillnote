package org.qosp.notes.preferences

interface HasNameResource {
    val nameResource: Int
}

interface HasApiLevelRequirement {
    val apiLevelRequired: Int
        get() = 0
}