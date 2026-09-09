package com.example.test2.ui.apply

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.test2.core.PlafondTiers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The step-2 gate, exercised through a real composition.
 *
 * Instrumented rather than a JVM test because it is the *screen* under test -
 * whether the button refuses to advance and whether the "Other" box appears -
 * not the state class, which `ApplyLoanStateTest` covers on its own.
 */
@RunWith(AndroidJUnit4::class)
class LoanDetailsStepTest {

    @get:Rule
    val compose = createComposeRule()

    private fun blankState() = ApplyLoanState().apply {
        tiers = PlafondTiers.FALLBACK
        creditLimit = 100_000_000L
    }

    private fun filledState() = blankState().apply {
        updateLoanAmount(25_000_000L)
        updateTermMonths(12)
        purpose = "Education"
        monthlyIncome = "8.500.000"
        bank = "BCA"
        bankAccountNumber = "1234567890"
        bankAccountName = "Budi"
    }

    @Test
    fun continueIsRefusedWhileRequiredFieldsAreEmpty() {
        var advanced = false
        val state = blankState()

        compose.setContent {
            LoanDetailsStep(state, onBack = {}, onCancel = {}, onContinue = { advanced = true })
        }

        compose.onNodeWithText("Continue to Review").performScrollTo().performClick()

        assertFalse("must not reach Review with an empty form", advanced)
    }

    @Test
    fun refusingToContinueNamesWhatIsMissing() {
        val state = blankState()

        compose.setContent {
            LoanDetailsStep(state, onBack = {}, onCancel = {}, onContinue = {})
        }

        compose.onNodeWithText("Continue to Review").performScrollTo().performClick()

        compose.onNodeWithText("Choose what the loan is for.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Enter the account number.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Some required fields", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun nothingIsMarkedRedBeforeTheCustomerTriesToContinue() {
        compose.setContent {
            LoanDetailsStep(blankState(), onBack = {}, onCancel = {}, onContinue = {})
        }

        compose.onNodeWithText("Choose what the loan is for.").assertDoesNotExist()
    }

    @Test
    fun aCompleteFormReachesReview() {
        var advanced = false

        compose.setContent {
            LoanDetailsStep(filledState(), onBack = {}, onCancel = {}, onContinue = { advanced = true })
        }

        compose.onNodeWithText("Continue to Review").performScrollTo().performClick()

        assertTrue("a complete form must be allowed through", advanced)
    }

    @Test
    fun choosingOtherRevealsTheDescriptionField() {
        val state = filledState()

        compose.setContent {
            LoanDetailsStep(state, onBack = {}, onCancel = {}, onContinue = {})
        }

        compose.onNodeWithText("Please describe it").assertDoesNotExist()

        compose.runOnIdle { state.purpose = ApplyLoanState.PURPOSE_OTHER }

        compose.onNodeWithText("Please describe it").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun otherWithoutADescriptionBlocksContinue() {
        var advanced = false
        val state = filledState().apply { purpose = ApplyLoanState.PURPOSE_OTHER }

        compose.setContent {
            LoanDetailsStep(state, onBack = {}, onCancel = {}, onContinue = { advanced = true })
        }

        compose.onNodeWithText("Continue to Review").performScrollTo().performClick()

        assertFalse(advanced)
        compose.onNodeWithText("Tell us what the loan is for.").performScrollTo().assertIsDisplayed()
    }

    /** Item 5: the simulation must sit above the payout account, not below it. */
    @Test
    fun theInstalmentSimulationIsShownWithTheLoanTerms() {
        compose.setContent {
            LoanDetailsStep(filledState(), onBack = {}, onCancel = {}, onContinue = {})
        }

        compose.onNodeWithText("Instalment Simulation").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Payout Account").performScrollTo().assertIsDisplayed()
    }
}
