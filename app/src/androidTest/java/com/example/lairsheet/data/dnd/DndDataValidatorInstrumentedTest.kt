package com.example.lairsheet.data.dnd

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DndDataValidatorInstrumentedTest {

    @Test
    fun validateDataSets_doNotHaveBrokenReferences() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val reports = DndDataValidator.validateAll(ctx)

        reports.forEach { rep ->
            assertTrue("Missing items in ${rep.source}: ${rep.missingItems}", rep.missingItems.isEmpty())
            assertTrue("Bad ability keys in ${rep.source}: ${rep.badAbilityKeys}", rep.badAbilityKeys.isEmpty())
            assertTrue("Bad advantage checks in ${rep.source}: ${rep.badAdvantageChecks}", rep.badAdvantageChecks.isEmpty())
            assertTrue("Bad advantage saves in ${rep.source}: ${rep.badAdvantageSaves}", rep.badAdvantageSaves.isEmpty())
            assertTrue("Missing features in ${rep.source}: ${rep.missingFeatures}", rep.missingFeatures.isEmpty())
        }
    }
}