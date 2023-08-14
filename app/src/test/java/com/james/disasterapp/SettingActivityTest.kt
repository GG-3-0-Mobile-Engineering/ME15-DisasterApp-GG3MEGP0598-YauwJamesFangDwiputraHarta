package com.james.disasterapp

import org.junit.Assert.*

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.james.disasterapp.R
import com.james.disasterapp.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.shadows.ShadowApplication

class SettingsFragmentTest {

    private lateinit var scenario: ActivityScenario<SettingActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(SettingActivity::class.java)
    }

    @Test
    fun testChangeThemePreference() {
        scenario.moveToState(Lifecycle.State.CREATED)

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.settings)
            if (fragment is SettingsFragment) {
                val preferencesTheme = fragment.findPreference<ListPreference>(
                    activity.getString(R.string.pref_key_dark)
                )

                // Change theme preference to "on"
                preferencesTheme?.onPreferenceChangeListener?.onPreferenceChange(
                    preferencesTheme,
                    "on"
                )

                assertEquals(
                    AppCompatDelegate.MODE_NIGHT_YES,
                    AppCompatDelegate.getDefaultNightMode()
                )

                // Change theme preference to "off"
                preferencesTheme?.onPreferenceChangeListener?.onPreferenceChange(
                    preferencesTheme,
                    "off"
                )

                assertEquals(
                    AppCompatDelegate.MODE_NIGHT_NO,
                    AppCompatDelegate.getDefaultNightMode()
                )
            }
        }
    }

    @Test
    fun testNotificationPreference() {
        scenario.moveToState(Lifecycle.State.CREATED)

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.settings)
            if (fragment is SettingsFragment) {
                val notifPreferences = fragment.findPreference<SwitchPreference>(
                    activity.getString(R.string.pref_key_notify)
                )

                // Enable notification
                notifPreferences?.onPreferenceChangeListener?.onPreferenceChange(
                    notifPreferences,
                    true
                )

                // Verify if daily reminder is set
                val reminderIntent = ShadowApplication.getInstance().nextStartedService
                assertNotNull(reminderIntent)
                assertEquals(DailyAlert::class.java.name, reminderIntent.component?.className)

                // Disable notification
                notifPreferences?.onPreferenceChangeListener?.onPreferenceChange(
                    notifPreferences,
                    false
                )

                // Verify if daily reminder is canceled
                val canceledIntent = ShadowApplication.getInstance().nextStoppedService
                assertNotNull(canceledIntent)
                assertEquals(DailyAlert::class.java.name, canceledIntent.component?.className)
            }
        }
    }
}
