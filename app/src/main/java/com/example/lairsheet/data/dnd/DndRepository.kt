package com.example.lairsheet.data.dnd

import android.content.Context
import com.example.lairsheet.R
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Читает res/raw/dnd_sources.json и парсит в модели.
 */
class DndRepository(private val context: Context) {

    fun load(): DndSources {
        val json = context.resources.openRawResource(R.raw.dnd_sources)
            .bufferedReader(Charset.forName("UTF-8"))
            .use { it.readText() }
        return parse(json)
    }

    private fun parse(json: String): DndSources {
        val root = JSONObject(json)
        val keys = buildList {
            val it = root.keys()
            while (it.hasNext()) add(it.next())
        }

        val weight = mapOf(
            "Основные правила 5e 2014 (Player’s Handbook 2014)" to 0,
            "Основные правила 5e 2024 (Player’s Handbook 2024)" to 1,
            "Расширенные источники" to 2
        )
        val ordered = keys.sortedBy { weight[it] ?: Int.MAX_VALUE }

        val sources = ordered.map { title ->
            val node = root.getJSONObject(title)
            val races = parseRaces(node.optJSONArray("races") ?: JSONArray())
            val classes = parseClasses(node.optJSONArray("classes") ?: JSONArray())
            DndSource(title = title, races = races, classes = classes)
        }

        return DndSources(sources)
    }

    private fun parseRaces(arr: JSONArray): List<Race2014> {
        val out = ArrayList<Race2014>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Race2014(
                nameEn = o.optString("name_en"),
                nameRu = o.optString("name_ru"),
                subraces = parseSubraces(o.optJSONArray("subraces") ?: JSONArray())
            )
        }
        return out
    }

    private fun parseSubraces(arr: JSONArray): List<Subrace2014> {
        val out = ArrayList<Subrace2014>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Subrace2014(
                nameEn = o.optString("name_en"),
                nameRu = o.optString("name_ru")
            )
        }
        return out
    }

    private fun parseClasses(arr: JSONArray): List<DndClass2014> {
        val out = ArrayList<DndClass2014>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += DndClass2014(
                nameEn = o.optString("name_en"),
                nameRu = o.optString("name_ru"),
                subclasses = parseSubclasses(o.optJSONArray("subclasses") ?: JSONArray())
            )
        }
        return out
    }

    private fun parseSubclasses(arr: JSONArray): List<Subclass2014> {
        val out = ArrayList<Subclass2014>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Subclass2014(
                nameEn = o.optString("name_en"),
                nameRu = o.optString("name_ru")
            )
        }
        return out
    }
}
