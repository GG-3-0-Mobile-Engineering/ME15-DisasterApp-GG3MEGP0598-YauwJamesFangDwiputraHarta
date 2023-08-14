package com.james.disasterapp

import org.junit.Assert.*

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class FilterActivityTest {

    private lateinit var activityScenario: ActivityScenario<FilterActivity>

    @Before
    fun setup() {
        activityScenario = ActivityScenario.launch(FilterActivity::class.java)
    }

    @Test
    fun testActivityInView() {
        activityScenario.onActivity { activity ->
            val view = activity.binding.root
            assertEquals(View.VISIBLE, view.visibility)
        }
    }

    @Test
    fun testActionBarTitle() {
        activityScenario.onActivity { activity ->
            val actionBar: ActionBar? = activity.supportActionBar
            assertEquals("Filter Activity", actionBar?.title)
        }
    }

    @Test
    fun testExtraSelectedValue() {
        val expectedValue = "flood"
        val resultData = Intent().apply {
            putExtra(FilterActivity.EXTRA_SELECTED_VALUE, expectedValue)
        }

        activityScenario.onActivity { activity ->
            // Simulate the onActivityResult call
            activity(FilterActivity.RESULT_CODE, AppCompatActivity.RESULT_OK, resultData)

            // Check the result
            val resultCode = activity.activityResult.re
            val data = activity.activityResult.resultData
            assertEquals(AppCompatActivity.RESULT_OK, resultCode)
            assertEquals(expectedValue, data?.getStringExtra(FilterActivity.EXTRA_SELECTED_VALUE))
        }
    }
}