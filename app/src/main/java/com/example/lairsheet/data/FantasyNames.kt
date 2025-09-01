package com.example.lairsheet.data

import kotlin.random.Random

/**
 * Генератор стабильного списка из 1000 фэнтези-имён длиной 4..10 символов.
 * Используется для быстрого создания персонажа.
 */
object FantasyNames {
    private const val SEED = 172943
    private val vowels = arrayOf("a","e","i","o","u","y")
    private val cons = arrayOf("b","c","d","f","g","h","j","k","l","m","n","p","r","s","t","v","w","z")
    private val blends = arrayOf(
        "dr","kr","gr","tr","st","str","th","kh","gh","sh","zh",
        "sk","sm","sn","sw","wr","cl","gl","pl","pr","bl","br","cr","fr"
    )

    // Готовый список из 1000 имён, генерируется детерминированно (по SEED)
    val NAMES: List<String> by lazy { generateList() }

    private fun generateList(): List<String> {
        val rnd = Random(SEED)
        val set = LinkedHashSet<String>()
        while (set.size < 1000) {
            val len = rnd.nextInt(4, 11)
            val name = buildName(len, rnd)
            set.add(name)
        }
        return set.toList()
    }

    private fun buildName(len: Int, rnd: Random): String {
        val sb = StringBuilder()
        var useCons = rnd.nextBoolean()
        while (sb.length < len) {
            if (useCons) {
                val useBlend = rnd.nextInt(100) < 20 // 20% — стеченье согласных
                val piece = if (useBlend) blends[rnd.nextInt(blends.size)] else cons[rnd.nextInt(cons.size)]
                sb.append(piece)
            } else {
                sb.append(vowels[rnd.nextInt(vowels.size)])
            }
            useCons = !useCons
        }
        var s = sb.toString().take(len)

        // Гарантируем наличие гласной
        if (!s.any { it.lowercaseChar() in charArrayOf('a','e','i','o','u','y') }) {
            val pos = rnd.nextInt(s.length)
            val arr = s.toCharArray()
            arr[pos] = vowels[rnd.nextInt(vowels.size)][0]
            s = String(arr)
        }
        // Не допускаем тройного повторения символов
        s = s.replace(Regex("(.)\\1\\1+")) { it.value.take(2) }

        return s.replaceFirstChar { it.uppercase() }
    }

    private var idx = 0
    fun next(): String {
        val name = NAMES[idx % NAMES.size]
        idx++
        return name
    }
}