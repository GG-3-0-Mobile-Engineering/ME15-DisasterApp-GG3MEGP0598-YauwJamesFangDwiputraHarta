package com.james.disasterapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @Before
    fun start() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun checkFilterActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_filter)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(FilterActivity::class.java.name))
        Espresso.onView(ViewMatchers.withId(R.id.layout_province)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.edt_province)).perform(click())
        Espresso.onView(ViewMatchers.withText("flood"))
    }

    @Test
    fun checkSettingActivity() {
        Intents.init()
        Espresso.onView(ViewMatchers.withId(R.id.fab_setting)).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(SettingActivity::class.java.name))
        Espresso.onView(ViewMatchers.withId(R.id.settings)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()))
    }

    @Test
    fun testSearchSuggestion() {
        Espresso.onView(ViewMatchers.withId(R.id.searchView)).perform(typeText("jawa timur"))
        Espresso.onView(ViewMatchers.withText("jawa timur")).perform(click())
    }
}