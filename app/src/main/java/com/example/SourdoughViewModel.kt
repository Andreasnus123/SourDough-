package com.example

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SourdoughUiState(
    val baseFlour: Double = 1000.0,
    val targetHydration: Double = 72.0,
    val prefermentWeight: Double = 200.0,
    val prefermentHydration: Double = 100.0,
    val milkWeight: Double = 0.0,
    val eggsWeight: Double = 0.0,
    val butterWeight: Double = 0.0,
    val includeLevain: Boolean = true,
    val accountEnriched: Boolean = true
)

data class SourdoughCalculationResult(
    val totalDoughWeight: Double,
    val requiredWaterToAdd: Double,
    val totalNetFlour: Double,
    val totalNetWater: Double,
    val flourFromPreferment: Double,
    val waterFromPreferment: Double,
    val hiddenWater: Double,
    val dryFlourToAdd: Double,
    val actualHydration: Double,
    val crumbDescription: String,
    val estimatedLoaves: Int,
    val estimatedLoafWeight: Int
)

class SourdoughViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences(
        "sourdough_calculator_prefs",
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(SourdoughUiState())
    val uiState: StateFlow<SourdoughUiState> = _uiState

    val calculationResult: StateFlow<SourdoughCalculationResult> = _uiState.map { state ->
        calculateResults(state)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = calculateResults(SourdoughUiState())
    )

    init {
        loadStateFromPrefs()
    }

    private fun loadStateFromPrefs() {
        _uiState.value = SourdoughUiState(
            baseFlour = prefs.getFloat("base_flour", 1000.0f).toDouble(),
            targetHydration = prefs.getFloat("target_hydration", 72.0f).toDouble(),
            prefermentWeight = prefs.getFloat("preferment_weight", 200.0f).toDouble(),
            prefermentHydration = prefs.getFloat("preferment_hydration", 100.0f).toDouble(),
            milkWeight = prefs.getFloat("milk_weight", 0.0f).toDouble(),
            eggsWeight = prefs.getFloat("eggs_weight", 0.0f).toDouble(),
            butterWeight = prefs.getFloat("butter_weight", 0.0f).toDouble(),
            includeLevain = prefs.getBoolean("include_levain", true),
            accountEnriched = prefs.getBoolean("account_enriched", true)
        )
    }

    private fun saveToPrefs() {
        viewModelScope.launch {
            val state = _uiState.value
            prefs.edit().apply {
                putFloat("base_flour", state.baseFlour.toFloat())
                putFloat("target_hydration", state.targetHydration.toFloat())
                putFloat("preferment_weight", state.prefermentWeight.toFloat())
                putFloat("preferment_hydration", state.prefermentHydration.toFloat())
                putFloat("milk_weight", state.milkWeight.toFloat())
                putFloat("eggs_weight", state.eggsWeight.toFloat())
                putFloat("butter_weight", state.butterWeight.toFloat())
                putBoolean("include_levain", state.includeLevain)
                putBoolean("account_enriched", state.accountEnriched)
                apply()
            }
        }
    }

    fun updateBaseFlour(weight: Double) {
        _uiState.update { it.copy(baseFlour = weight.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun updateTargetHydration(percentage: Double) {
        _uiState.update { it.copy(targetHydration = percentage.coerceIn(50.0, 90.0)) }
        saveToPrefs()
    }

    fun updatePrefermentWeight(weight: Double) {
        _uiState.update { it.copy(prefermentWeight = weight.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun updatePrefermentHydration(percentage: Double) {
        _uiState.update { it.copy(prefermentHydration = percentage.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun updateMilkWeight(weight: Double) {
        _uiState.update { it.copy(milkWeight = weight.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun updateEggsWeight(weight: Double) {
        _uiState.update { it.copy(eggsWeight = weight.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun updateButterWeight(weight: Double) {
        _uiState.update { it.copy(butterWeight = weight.coerceAtLeast(0.0)) }
        saveToPrefs()
    }

    fun toggleIncludeLevain(include: Boolean) {
        _uiState.update { it.copy(includeLevain = include) }
        saveToPrefs()
    }

    fun toggleAccountEnriched(account: Boolean) {
        _uiState.update { it.copy(accountEnriched = account) }
        saveToPrefs()
    }

    fun applyPreset(hydration: Double) {
        _uiState.update { it.copy(targetHydration = hydration.coerceIn(50.0, 90.0)) }
        saveToPrefs()
    }

    fun reset() {
        _uiState.value = SourdoughUiState(
            baseFlour = 1000.0,
            targetHydration = 72.0,
            prefermentWeight = 200.0,
            prefermentHydration = 100.0,
            milkWeight = 0.0,
            eggsWeight = 0.0,
            butterWeight = 0.0,
            includeLevain = true,
            accountEnriched = true
        )
        saveToPrefs()
    }

    private fun calculateResults(state: SourdoughUiState): SourdoughCalculationResult {
        // 1) Isolate preferment Mass (Flour FP, Water WP)
        val fp = if (state.includeLevain && state.prefermentWeight > 0) {
            state.prefermentWeight / (1.0 + (state.prefermentHydration / 100.0))
        } else {
            0.0
        }
        val wp = if (state.includeLevain && state.prefermentWeight > 0) {
            state.prefermentWeight - fp
        } else {
            0.0
        }

        // 2) Isolate hidden water (Milk 87%, Eggs 75%, Butter 16%)
        val wHidden = if (state.accountEnriched) {
            (state.milkWeight * 0.87) + (state.eggsWeight * 0.75) + (state.butterWeight * 0.16)
        } else {
            0.0
        }

        // 3) Calculate required water to add
        val targetWater = state.baseFlour * (state.targetHydration / 100.0)
        val rawWaterToAdd = targetWater - wp - wHidden
        val requiredWaterToAdd = rawWaterToAdd.coerceAtLeast(0.0)

        // 4) Flour and Water totals for baseline presentation:
        // Total Net Flour is represented by the Base Flour Weight (the 100% baseline).
        val totalNetFlour = state.baseFlour
        
        // Total net water weight present in the recipe dough:
        val totalNetWater = requiredWaterToAdd + wp + wHidden

        // 5) Dry flour to add
        val dryFlourToAdd = (state.baseFlour - fp).coerceAtLeast(0.0)

        // 6) Actual Hydration %
        val actualHydration = if (totalNetFlour > 0) {
            (totalNetWater / totalNetFlour) * 100.0
        } else {
            0.0
        }

        // 7) Crumb Description (based on selected Target Hydration)
        val crumbDescription = when {
            state.targetHydration < 50.0 -> "Extremely dry, dense dough. Hard to hydrate."
            state.targetHydration in 50.0..60.0 -> "Dense, chewy crumb. Ideal for bagels and pretzels."
            state.targetHydration in 60.01..67.0 -> "Moderate, tight crumb. Forgiving dough, ideal for sandwich bread."
            state.targetHydration in 67.01..75.0 -> "Open crumb with irregular holes. Classic artisan sourdough boule."
            state.targetHydration in 75.01..85.0 -> "Very open, wet dough structure. Best for rustic focaccia or ciabatta."
            else -> "Super wet, airy dough. Ideal for long-fermentation focaccia or rustic loafs."
        }

        // 8) Total physical ingredients bowl weight:
        val totalDoughWeight = dryFlourToAdd + state.prefermentWeight + requiredWaterToAdd +
                state.milkWeight + state.eggsWeight + state.butterWeight

        // 9) Estimated Loaves
        val estimatedLoaves = (totalDoughWeight / 850.0).toInt().coerceAtLeast(1)
        val estimatedLoafWeight = (totalDoughWeight / estimatedLoaves).toInt()

        return SourdoughCalculationResult(
            totalDoughWeight = totalDoughWeight,
            requiredWaterToAdd = requiredWaterToAdd,
            totalNetFlour = totalNetFlour,
            totalNetWater = totalNetWater,
            flourFromPreferment = fp,
            waterFromPreferment = wp,
            hiddenWater = wHidden,
            dryFlourToAdd = dryFlourToAdd,
            actualHydration = actualHydration,
            crumbDescription = crumbDescription,
            estimatedLoaves = estimatedLoaves,
            estimatedLoafWeight = estimatedLoafWeight
        )
    }
}
