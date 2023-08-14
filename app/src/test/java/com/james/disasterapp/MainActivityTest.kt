package com.james.disasterapp

import android.view.View
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.test.espresso.ViewAction
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.ui.UiController
import org.bouncycastle.math.raw.Nat.eq
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.EnumSet.allOf
import java.util.regex.Matcher

@RunWith(MockitoJUnitRunner::class)
class MainActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Mock
    private lateinit var mainViewModel: MainViewModel

    @Mock
    private lateinit var mockObserver: Observer<Boolean>

    @Test
    fun testSearchViewTextChange() {
        val scenario = activityScenarioRule.scenario

        val newText = "jawa timur"

        onView(withId(R.id.searchView)).perform(typeSearchViewText(newText))
        verify(mainViewModel).getSearchingDisaster(eq(newText))
    }


    // Helper function to simulate typing into a SearchView
    private fun typeSearchViewText(text: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
            }

            override fun getDescription(): String {
                return "Type text into a SearchView"
            }

            override fun perform(uiController: UiController?, view: View?) {
                val searchView = view as SearchView
                searchView.setQuery(text, false)
            }
        }
    }
}