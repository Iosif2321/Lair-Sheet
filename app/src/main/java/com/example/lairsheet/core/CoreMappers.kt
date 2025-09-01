package com.example.lairsheet.core

import com.example.lairsheet.data.AbilityType
import com.example.lairsheet.data.Ruleset as AppRuleset
import dnd.lair.core.Ability as CoreAbility
import dnd.lair.core.Ruleset as CoreRuleset

fun AbilityType.toCore(): CoreAbility = when (this) {
    AbilityType.STRENGTH -> CoreAbility.STR
    AbilityType.DEXTERITY -> CoreAbility.DEX
    AbilityType.CONSTITUTION -> CoreAbility.CON
    AbilityType.INTELLIGENCE -> CoreAbility.INT
    AbilityType.WISDOM -> CoreAbility.WIS
    AbilityType.CHARISMA -> CoreAbility.CHA
}

fun CoreAbility.toApp(): AbilityType = when (this) {
    CoreAbility.STR -> AbilityType.STRENGTH
    CoreAbility.DEX -> AbilityType.DEXTERITY
    CoreAbility.CON -> AbilityType.CONSTITUTION
    CoreAbility.INT -> AbilityType.INTELLIGENCE
    CoreAbility.WIS -> AbilityType.WISDOM
    CoreAbility.CHA -> AbilityType.CHARISMA
}

fun Map<AbilityType, Int>.toCore(): Map<CoreAbility, Int> =
    this.entries.associate { (k, v) -> k.toCore() to v }

fun Map<CoreAbility, Int>.toApp(): Map<AbilityType, Int> =
    this.entries.associate { (k, v) -> k.toApp() to v }

fun AppRuleset.toCore(): CoreRuleset = when (this) {
    AppRuleset.R5E_2014 -> CoreRuleset.R2014
    AppRuleset.R5E_2024, AppRuleset.EXPANDED -> CoreRuleset.R2024
}