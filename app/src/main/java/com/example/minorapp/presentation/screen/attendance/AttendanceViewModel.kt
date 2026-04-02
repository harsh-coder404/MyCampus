package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minorapp.data.attendance.AttendanceInsightStatData
import com.example.minorapp.data.attendance.AttendanceInsightsResult
import com.example.minorapp.data.attendance.AttendanceRepository
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.constants.DummyDataConstants
import com.example.minorapp.presentation.common.SharedAcademicMetricsResolver
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class AttendanceInsightsPeriod {
    SEMESTER,
    MONTHLY
}

data class AttendanceSummaryStatUi(
    val label: String,
    val value: String,
    val containerColor: Color,
    val labelColor: Color,
    val valueColor: Color
)

data class AttendanceSubjectUi(
    val title: String,
    val subtitle: String,
    val percentage: String,
    val status: String,
    val iconColor: Color,
    val iconTextColor: Color,
    val statusBgColor: Color,
    val statusTextColor: Color,
    val iconLetter: String
)

data class AttendanceUiState(
    val profileImageUri: String? = null,
    val selectedInsightsPeriod: AttendanceInsightsPeriod,
    val overallPresenceRatio: Float,
    val overallPresenceText: String,
    val monthlyDeltaText: String,
    val overallPresenceTargetText: String,
    val academicStandingDescription: String,
    val primaryStandingBadge: String,
    val secondaryStandingBadge: String,
    val priorityFocusDescription: String,
    val priorityFocusAction: String,
    val monthlySummaryStats: List<AttendanceSummaryStatUi>,
    val semesterSummaryStats: List<AttendanceSummaryStatUi>,
    val subjects: List<AttendanceSubjectUi>,
    val heatmapColors: List<Color>
) {
    val activeSummaryStats: List<AttendanceSummaryStatUi>
        get() = when (selectedInsightsPeriod) {
            AttendanceInsightsPeriod.SEMESTER -> semesterSummaryStats
            AttendanceInsightsPeriod.MONTHLY -> monthlySummaryStats
        }
}

class AttendanceViewModel(
    private val sessionManager: SessionManager,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    var uiState by mutableStateOf(initialAttendanceUiState(sessionManager))
        private set

    init {
        refreshInsights()
    }

    companion object {
        fun factory(
            sessionManager: SessionManager,
            attendanceRepository: AttendanceRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AttendanceViewModel(sessionManager, attendanceRepository) as T
                }
            }
        }
    }

    fun onInsightsPeriodSelected(period: AttendanceInsightsPeriod) {
        if (uiState.selectedInsightsPeriod == period) return
        uiState = uiState.copy(selectedInsightsPeriod = period)
    }

    fun onInsightsDataFetched(
        monthlyStats: List<AttendanceSummaryStatUi>,
        semesterStats: List<AttendanceSummaryStatUi>
    ) {
        uiState = uiState.copy(
            monthlySummaryStats = monthlyStats,
            semesterSummaryStats = semesterStats
        )
    }

    private fun refreshInsights() {
        viewModelScope.launch {
            val accessToken = sessionManager.getAccessToken()
            val monthlyDeferred = async { attendanceRepository.fetchMonthlyInsights(accessToken) }
            val semesterDeferred = async { attendanceRepository.fetchSemesterInsights(accessToken) }

            val monthlyStats = resolveStats(
                result = monthlyDeferred.await(),
                cachedSnapshot = sessionManager.getAttendanceMonthlyInsightsSnapshot(),
                saveSnapshot = sessionManager::saveAttendanceMonthlyInsightsSnapshot,
                defaultStats = uiState.monthlySummaryStats
            )

            val semesterStats = resolveStats(
                result = semesterDeferred.await(),
                cachedSnapshot = sessionManager.getAttendanceSemesterInsightsSnapshot(),
                saveSnapshot = sessionManager::saveAttendanceSemesterInsightsSnapshot,
                defaultStats = uiState.semesterSummaryStats
            )

            onInsightsDataFetched(monthlyStats = monthlyStats, semesterStats = semesterStats)
            val metrics = SharedAcademicMetricsResolver.fromSession(sessionManager)
            uiState = uiState.copy(
                overallPresenceRatio = metrics.attendanceProgress,
                overallPresenceText = "${metrics.attendancePercent}%"
            )
        }
    }

    private fun resolveStats(
        result: AttendanceInsightsResult,
        cachedSnapshot: String?,
        saveSnapshot: (String?) -> Unit,
        defaultStats: List<AttendanceSummaryStatUi>
    ): List<AttendanceSummaryStatUi> {
        return when (result) {
            is AttendanceInsightsResult.Success -> {
                val mapped = result.stats.toSummaryStatUi()
                if (mapped.isNotEmpty()) {
                    saveSnapshot(mapped.toSnapshotJson())
                    mapped
                } else {
                    cachedSnapshot.toSummaryStatsOrNull() ?: defaultStats
                }
            }

            is AttendanceInsightsResult.Failure -> {
                cachedSnapshot.toSummaryStatsOrNull() ?: defaultStats
            }
        }
    }
}

private fun initialAttendanceUiState(sessionManager: SessionManager): AttendanceUiState {
    val defaults = defaultAttendanceUiState()
    val cachedMonthly = sessionManager.getAttendanceMonthlyInsightsSnapshot().toSummaryStatsOrNull()
    val cachedSemester = sessionManager.getAttendanceSemesterInsightsSnapshot().toSummaryStatsOrNull()
    val metrics = SharedAcademicMetricsResolver.fromSession(sessionManager)

    return defaults.copy(
        profileImageUri = sessionManager.getProfileImageUri(),
        overallPresenceRatio = metrics.attendanceProgress,
        overallPresenceText = "${metrics.attendancePercent}%",
        monthlySummaryStats = cachedMonthly ?: defaults.monthlySummaryStats,
        semesterSummaryStats = cachedSemester ?: defaults.semesterSummaryStats
    )
}

private fun List<AttendanceInsightStatData>.toSummaryStatUi(): List<AttendanceSummaryStatUi> {
    return map { item ->
        val normalizedLabel = item.label.trim().uppercase()
        val valueColor = when {
            normalizedLabel.contains("PRESENT") -> Color(0xFF0265DC)
            normalizedLabel.contains("EXCUSED") -> Color(0xFFC2410C)
            else -> Color(0xFF0F172A)
        }

        AttendanceSummaryStatUi(
            label = normalizedLabel,
            value = item.value.trim(),
            containerColor = Color.White,
            labelColor = Color(0xFF64748B),
            valueColor = valueColor
        )
    }.filter { it.label.isNotBlank() && it.value.isNotBlank() }
}

private fun List<AttendanceSummaryStatUi>.toSnapshotJson(): String {
    val array = JSONArray()
    forEach { stat ->
        array.put(
            JSONObject()
                .put("label", stat.label)
                .put("value", stat.value)
        )
    }
    return array.toString()
}

private fun String?.toSummaryStatsOrNull(): List<AttendanceSummaryStatUi>? {
    if (this.isNullOrBlank()) return null

    return runCatching {
        val jsonArray = JSONArray(this)
        val stats = mutableListOf<AttendanceInsightStatData>()
        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(index) ?: continue
            val label = item.optString("label").trim()
            val value = item.optString("value").trim()
            if (label.isBlank() || value.isBlank()) continue
            stats.add(AttendanceInsightStatData(label = label, value = value))
        }
        stats.toSummaryStatUi().takeIf { it.isNotEmpty() }
    }.getOrNull()
}

private fun defaultAttendanceUiState(): AttendanceUiState {
    return AttendanceUiState(
        selectedInsightsPeriod = AttendanceInsightsPeriod.MONTHLY,
        overallPresenceRatio = 0.884f,
        overallPresenceText = "88.4%",
        monthlyDeltaText = "+2.4% from last month",
        overallPresenceTargetText = "TARGET: 85%",
        academicStandingDescription = "Your attendance has increased by 2.4% since last month. Keep it up to reach the Honors threshold.",
        primaryStandingBadge = "HONORS ELIGIBLE",
        secondaryStandingBadge = "TOP 15% OF COHORT",
        priorityFocusDescription = "${DummyDataConstants.dummySubjects[1]} requires immediate attention. Your attendance is currently 74%, which is 1% below the minimum target.",
        priorityFocusAction = "Schedule Catch-up",
        monthlySummaryStats = listOf(
            AttendanceSummaryStatUi(
                label = "TOTAL CLASSES",
                value = "142",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFF0F172A)
            ),
            AttendanceSummaryStatUi(
                label = "TOTAL PRESENT",
                value = "126",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFF0265DC)
            ),
            AttendanceSummaryStatUi(
                label = "EXCUSED ABSENCES",
                value = "12",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFFC2410C)
            )
        ),
        semesterSummaryStats = listOf(
            AttendanceSummaryStatUi(
                label = "TOTAL CLASSES",
                value = "684",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFF0F172A)
            ),
            AttendanceSummaryStatUi(
                label = "TOTAL PRESENT",
                value = "603",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFF0265DC)
            ),
            AttendanceSummaryStatUi(
                label = "ABSENCES",
                value = "24",
                containerColor = Color.White,
                labelColor = Color(0xFF64748B),
                valueColor = Color(0xFFC2410C)
            )
        ),
        subjects = listOf(
            AttendanceSubjectUi(
                title = DummyDataConstants.dummySubjects[0],
                subtitle = "Next Class: Tomorrow, 09:00 AM",
                percentage = "100%",
                status = "PERFECT",
                iconColor = Color(0xFFDBEAFE),
                iconTextColor = Color(0xFF1E40AF),
                statusBgColor = Color(0xFFDBEAFE),
                statusTextColor = Color(0xFF1E40AF),
                iconLetter = "A"
            ),
            AttendanceSubjectUi(
                title = DummyDataConstants.dummySubjects[1],
                subtitle = "Next Class: Today, 02:30 PM",
                percentage = "74.2%",
                status = "BELOW TARGET",
                iconColor = Color(0xFFFEE2E2),
                iconTextColor = Color(0xFF991B1B),
                statusBgColor = Color(0xFFFEE2E2),
                statusTextColor = Color(0xFF991B1B),
                iconLetter = "Σ"
            ),
            AttendanceSubjectUi(
                title = DummyDataConstants.dummySubjects[2],
                subtitle = "Next Class: Friday, 11:00 AM",
                percentage = "89.5%",
                status = "PRESENT",
                iconColor = Color(0xFFFFEDD5),
                iconTextColor = Color(0xFF9A3412),
                statusBgColor = Color(0xFFDBEAFE),
                statusTextColor = Color(0xFF1E40AF),
                iconLetter = "C"
            ),
            AttendanceSubjectUi(
                title = DummyDataConstants.dummySubjects[3],
                subtitle = "Next Class: Monday, 01:00 PM",
                percentage = "91.8%",
                status = "PRESENT",
                iconColor = Color(0xFFDBEAFE),
                iconTextColor = Color(0xFF1E40AF),
                statusBgColor = Color(0xFFDBEAFE),
                statusTextColor = Color(0xFF1E40AF),
                iconLetter = "H"
            )
        ),
        heatmapColors = listOf(
            Color(0xFFDBEAFE), Color(0xFF93C5FD), Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFFE2E8F0), Color(0xFFE2E8F0),
            Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF93C5FD), Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFE2E8F0), Color(0xFFE2E8F0),
            Color(0xFF1D4ED8), Color(0xFF93C5FD), Color(0xFF2563EB), Color(0xFFFECACA), Color(0xFF3B82F6), Color(0xFFE2E8F0), Color(0xFFE2E8F0)
        )
    )
}

