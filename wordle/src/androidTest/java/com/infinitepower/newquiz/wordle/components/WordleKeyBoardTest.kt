package com.infinitepower.newquiz.wordle.components

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinitepower.newquiz.model.wordle.WordleQuizType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@SmallTest
@RunWith(AndroidJUnit4::class)
internal class WordleKeyBoardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWordleKeyBoard_displayCorrectNumberOfItems() {
        val keys = charArrayOf('a', 'b', 'c')

        composeTestRule.setContent {
            WordleKeyBoard(
                keys = keys,
                disabledKeys = emptySet(),
                onKeyClick = {},
                windowWidthSizeClass = WindowWidthSizeClass.Compact,
                wordleQuizType = WordleQuizType.TEXT
            )
        }

        composeTestRule
            .onAllNodesWithTag(WordleKeyBoardTestingTags.KEY)
            .assertCountEquals(3)
    }

    @Test
    fun testWordleKeyBoardItem_displayCorrectKey() {
        composeTestRule.setContent {
            WordleKeyboardKey(
                key = 'a',
                disabled = true,
                onKeyClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag(WordleKeyBoardTestingTags.KEY)
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertTextContains("a")
    }

    @Test
    fun testWordleKeyBoardItem_disabled() {
        composeTestRule.setContent {
            WordleKeyboardKey(
                key = 'a',
                disabled = true,
                onKeyClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag(WordleKeyBoardTestingTags.KEY)
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertTextContains("a")
    }

    @Test
    fun testWordleKeyBoardItem_callsOnClickWhenClicked() {
        var wasOnClickCalled = false

        composeTestRule.setContent {
            WordleKeyboardKey(
                key = 'a',
                disabled = false,
                onKeyClick = { wasOnClickCalled = true }
            )
        }

        composeTestRule
            .onNodeWithText("a")
            .assertHasClickAction()
            .performClick()

        assertTrue(wasOnClickCalled, "Button as not clicked")
    }
}