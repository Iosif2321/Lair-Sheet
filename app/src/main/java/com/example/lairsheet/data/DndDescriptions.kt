package com.example.lairsheet.data

object DndDescriptions {

    fun getRaceDescription(name: String, description: String): String {
        return description.ifBlank { "Описание расы $name" }
    }

    fun getClassDescription(name: String, description: String): String {
        return description.ifBlank { "Описание класса $name" }
    }

    fun getSubraceDescription(name: String, description: String): String {
        return description.ifBlank { "Описание подрасы $name" }
    }

    fun getSubclassDescription(name: String, description: String): String {
        return description.ifBlank { "Описание подкласса $name" }
    }
}