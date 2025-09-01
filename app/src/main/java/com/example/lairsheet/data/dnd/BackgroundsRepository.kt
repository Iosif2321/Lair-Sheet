package com.example.lairsheet.data.dnd

import com.example.lairsheet.data.Ruleset
import com.example.lairsheet.ui.create.Background

class BackgroundsRepository {

    /**
     * Минимальный источник предысторий.
     * - Для 2024/EXPANDED возвращает список с гибкими схемами [+2,+1] и [+1,+1,+1].
     * - Для 2014 возвращает пустой список (в 2014 предыстории не дают бонусы к хар-кам RAW).
     */
    fun getAllFor(selectedSources: Set<Ruleset>): List<Background> {
        val use2024 = selectedSources.contains(Ruleset.R5E_2024) || selectedSources.contains(Ruleset.EXPANDED)
        if (!use2024) return emptyList()

        return listOf(
            Background(
                nameRu = "Аколит",
                shortDescription = "Служитель религиозной организации, знаком с обрядами и канонами.",
                scheme = listOf(2, 1),
                feat = "Устойчивость уму (пример)"
            ),
            Background(
                nameRu = "Солдат",
                shortDescription = "Профессиональный воин, прошедший строевую и полевую подготовку.",
                scheme = listOf(1, 1, 1),
                feat = "Боевая выучка (пример)"
            ),
            Background(
                nameRu = "Учёный",
                shortDescription = "Исследователь знаний, библиотекарь, алхимик или историк.",
                scheme = listOf(2, 1),
                feat = "Книжная смекалка (пример)"
            )
        )
    }
}