package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: SourdoughViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val calcResult by viewModel.calculationResult.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    SourdoughScreen(
                        uiState = uiState,
                        result = calcResult,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SourdoughScreen(
    uiState: SourdoughUiState,
    result: SourdoughCalculationResult,
    viewModel: SourdoughViewModel,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBg) // Natural Tones background
    ) {
        val isWideLayout = maxWidth > 680.dp

        if (isWideLayout) {
            // Adaptive wide layout: side-by-side split screen
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Inputs panel (Scrollable left column)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderBanner()

                    PresetsSection(
                        selectedHydration = uiState.targetHydration,
                        onPresetClick = { viewModel.applyPreset(it) }
                    )

                    MainFlourAndHydrationInputs(
                        uiState = uiState,
                        viewModel = viewModel
                    )

                    LevainConfigurationCard(
                        uiState = uiState,
                        viewModel = viewModel,
                        result = result
                    )

                    EnrichmentsConfigurationCard(
                        uiState = uiState,
                        viewModel = viewModel,
                        result = result
                    )

                    TogglesCard(
                        uiState = uiState,
                        viewModel = viewModel
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Results & visual compilation panel (Scrollable right column)
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DoughCompositionChartCard(result = result)

                    MainOutputCard(result = result)

                    DoughStatsGrid(result = result)

                    CrumbCharacteristicsCard(
                        targetHydration = uiState.targetHydration,
                        description = result.crumbDescription
                    )

                    ResetButtonSection(
                        onReset = { viewModel.reset() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Standard portrait mobile layout: a single list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderBanner()

                PresetsSection(
                    selectedHydration = uiState.targetHydration,
                    onPresetClick = { viewModel.applyPreset(it) }
                )

                MainFlourAndHydrationInputs(
                    uiState = uiState,
                    viewModel = viewModel
                )

                LevainConfigurationCard(
                    uiState = uiState,
                    viewModel = viewModel,
                    result = result
                )

                EnrichmentsConfigurationCard(
                    uiState = uiState,
                    viewModel = viewModel,
                    result = result
                )

                TogglesCard(
                    uiState = uiState,
                    viewModel = viewModel
                )

                DoughCompositionChartCard(result = result)

                MainOutputCard(result = result)

                DoughStatsGrid(result = result)

                CrumbCharacteristicsCard(
                    targetHydration = uiState.targetHydration,
                    description = result.crumbDescription
                )

                ResetButtonSection(
                    onReset = { viewModel.reset() }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_banner"),
        colors = CardDefaults.cardColors(
            containerColor = SoftCream
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderAccentLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stylized Sourdough Loaf vector circle using theme colors
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(CustomGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Drawing simple custom bread lines inside the box
                Canvas(modifier = Modifier.size(32.dp)) {
                    drawCircle(
                        color = HighlightYellow,
                        radius = 16.dp.toPx()
                    )
                    // score line
                    drawLine(
                        color = CustomGold,
                        start = Offset(6.dp.toPx(), 16.dp.toPx()),
                        end = Offset(26.dp.toPx(), 16.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sourdough Pro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextDark
                )
                Text(
                    text = "Artisanal Hydration Tool",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmBrownLabel
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetsSection(
    selectedHydration: Double,
    onPresetClick: (Double) -> Unit
) {
    val presets = listOf(
        Triple("Bagel", 55.0, "🥯 55%"),
        Triple("Pizza", 62.0, "🍕 62%"),
        Triple("Sourdough", 72.0, "🌾 72%"),
        Triple("Ciabatta", 80.0, "🥖 80%")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Dough Presets",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WarmBrownLabel,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { (name, percent, label) ->
                val isSelected = selectedHydration == percent
                val background = if (isSelected) CustomGold else NaturalBg
                val textColor = if (isSelected) Color.White else CustomGold

                Box(
                    modifier = Modifier
                        .testTag("preset_$name")
                        .clip(RoundedCornerShape(16.dp))
                        .background(background)
                        .border(1.5.dp, if (isSelected) Color.Transparent else SoftCream, RoundedCornerShape(16.dp))
                        .clickable { onPresetClick(percent) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun MainFlourAndHydrationInputs(
    uiState: SourdoughUiState,
    viewModel: SourdoughViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Primary Flour & Hydration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextDark
            )

            // Base Flour input (g)
            NumericInputWithIncrement(
                label = "Base Flour Target",
                value = uiState.baseFlour,
                unit = "g",
                step = 50.0,
                testTagPrefix = "base_flour",
                onValueChange = { viewModel.updateBaseFlour(it) }
            )

            // Target Hydration Slider
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Hydration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmBrownLabel
                    )
                    Text(
                        text = "${uiState.targetHydration.toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = CustomGold
                    )
                }

                Slider(
                    value = uiState.targetHydration.toFloat(),
                    onValueChange = { viewModel.updateTargetHydration(it.toDouble()) },
                    valueRange = 50.0f..90.0f,
                    steps = 39, // Increments of 1%
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hydration_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CustomGold,
                        activeTrackColor = CustomGold,
                        inactiveTrackColor = SoftCream
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stiff (50%)", fontSize = 11.sp, color = WarmBrownLabel)
                    Text("Artisan Boule (72%)", fontSize = 11.sp, color = CustomGold)
                    Text("Wet (90%)", fontSize = 11.sp, color = WarmBrownLabel)
                }
            }
        }
    }
}

@Composable
fun LevainConfigurationCard(
    uiState: SourdoughUiState,
    viewModel: SourdoughViewModel,
    result: SourdoughCalculationResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Preferment (Levain / Starter)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextDark
                    )
                    if (uiState.includeLevain) {
                        Text(
                            text = "Flour: ${result.flourFromPreferment.toInt()}g  |  Water: ${result.waterFromPreferment.toInt()}g",
                            fontSize = 12.sp,
                            color = CustomGold,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Preferment ignored from baker's calculation",
                            fontSize = 12.sp,
                            color = SubtextTan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            NumericInputWithIncrement(
                label = "Starter Quantity",
                value = uiState.prefermentWeight,
                unit = "g",
                step = 20.0,
                testTagPrefix = "levain_weight",
                onValueChange = { viewModel.updatePrefermentWeight(it) }
            )

            NumericInputWithIncrement(
                label = "Starter Hydration",
                value = uiState.prefermentHydration,
                unit = "%",
                step = 10.0,
                testTagPrefix = "levain_hydration",
                onValueChange = { viewModel.updatePrefermentHydration(it) }
            )
        }
    }
}

@Composable
fun EnrichmentsConfigurationCard(
    uiState: SourdoughUiState,
    viewModel: SourdoughViewModel,
    result: SourdoughCalculationResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Enriched Liquids & Fats",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextDark
            )

            if (uiState.accountEnriched && result.hiddenWater > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DelicateCream)
                        .border(1.dp, BorderAccentLight, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CustomGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Adding ${result.hiddenWater.toInt()}g water present in enriched fats.",
                        fontSize = 12.sp,
                        color = DarkBrownText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            NumericInputWithIncrement(
                label = "Whole Milk (87% Water)",
                value = uiState.milkWeight,
                unit = "g",
                step = 50.0,
                testTagPrefix = "milk_weight",
                onValueChange = { viewModel.updateMilkWeight(it) }
            )

            NumericInputWithIncrement(
                label = "Whole Eggs (75% Water)",
                value = uiState.eggsWeight,
                unit = "g",
                step = 50.0,
                testTagPrefix = "eggs_weight",
                onValueChange = { viewModel.updateEggsWeight(it) }
            )

            NumericInputWithIncrement(
                label = "Butter (16% Water)",
                value = uiState.butterWeight,
                unit = "g",
                step = 10.0,
                testTagPrefix = "butter_weight",
                onValueChange = { viewModel.updateButterWeight(it) }
            )
        }
    }
}

@Composable
fun TogglesCard(
    uiState: SourdoughUiState,
    viewModel: SourdoughViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Calculation Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextDark
            )

            // Levain toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include Levain in Hydration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBrownText
                    )
                    Text(
                        text = "Factor flour & water from sourdough starter",
                        fontSize = 11.sp,
                        color = SubtextTan
                    )
                }
                Switch(
                    checked = uiState.includeLevain,
                    onCheckedChange = { viewModel.toggleIncludeLevain(it) },
                    modifier = Modifier.testTag("toggle_include_levain"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CustomGold,
                        uncheckedThumbColor = SubtextTan,
                        uncheckedTrackColor = SoftCream
                    )
                )
            }

            // Enriched toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Account for Enriched Liquids",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkBrownText
                    )
                    Text(
                        text = "Extract hidden hydration from milk, eggs & butter",
                        fontSize = 11.sp,
                        color = SubtextTan
                    )
                }
                Switch(
                    checked = uiState.accountEnriched,
                    onCheckedChange = { viewModel.toggleAccountEnriched(it) },
                    modifier = Modifier.testTag("toggle_account_enriched"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CustomGold,
                        uncheckedThumbColor = SubtextTan,
                        uncheckedTrackColor = SoftCream
                    )
                )
            }
        }
    }
}

@Composable
fun DoughCompositionChartCard(result: SourdoughCalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Dough Composition Ratio",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextDark
            )

            val fVal = result.totalNetFlour
            val wVal = result.totalNetWater
            val sum = fVal + wVal

            val flourRatio = if (sum > 0) fVal / sum else 0.5
            val waterRatio = if (sum > 0) wVal / sum else 0.5

            // Horizontal layered stacked bar custom Canvas drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, SoftCream, RoundedCornerShape(10.dp))
                    .testTag("ratio_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val widthVal = size.width
                    val heightVal = size.height

                    val flourWidth = (flourRatio * widthVal).toFloat()

                    // Draw Flour segment (FlourBeige)
                    drawRoundRect(
                        color = FlourBeige,
                        size = Size(flourWidth, heightVal),
                        cornerRadius = CornerRadius(0f)
                    )

                    // Draw Water segment (WaterTeal)
                    drawRoundRect(
                        color = WaterTeal,
                        topLeft = Offset(flourWidth, 0f),
                        size = Size(widthVal - flourWidth, heightVal),
                        cornerRadius = CornerRadius(0f)
                    )
                }
            }

            // Legend labels & percentages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(FlourBeige, RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "Net Flour: ${(flourRatio * 100).toInt()}% (${result.totalNetFlour.toInt()}g)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmBrownLabel
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(WaterTeal, RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "Net Water: ${(waterRatio * 100).toInt()}% (${result.totalNetWater.toInt()}g)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaterTeal
                    )
                }
            }
        }
    }
}

@Composable
fun MainOutputCard(result: SourdoughCalculationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("water_output_card"),
        colors = CardDefaults.cardColors(
            containerColor = DarkContrastBg // Solid dark contrast container
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "REQUIRED WATER TO ADD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = SubtextTan,
                letterSpacing = 1.2.sp
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${result.requiredWaterToAdd.toInt()}",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = HighlightYellow, // Standout yellow highlight
                    lineHeight = 56.sp
                )
                Text(
                    text = "g",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubtextTan,
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )
            }

            Text(
                text = "Pour directly into your mixing container",
                fontSize = 12.sp,
                color = NaturalBg.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DoughStatsGrid(result: SourdoughCalculationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat 1: Total Dough Weight
            StatCard(
                label = "Total Dough Weight",
                value = "${result.totalDoughWeight.toInt()}g",
                testTag = "stat_total_weight",
                modifier = Modifier.weight(1f)
            )

            // Stat 2: Dry Flour to Add
            StatCard(
                label = "Dry Flour to Add",
                value = "${result.dryFlourToAdd.toInt()}g",
                testTag = "stat_dry_flour",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat 3: Total Net Water
            StatCard(
                label = "Total Net Water",
                value = "${result.totalNetWater.toInt()}g",
                testTag = "stat_total_water",
                modifier = Modifier.weight(1f)
            )

            // Stat 4: Estimated Yield
            StatCard(
                label = "Estimated Yield",
                value = "${result.estimatedLoaves} loaves",
                subtext = "~${result.estimatedLoafWeight}g each",
                testTag = "stat_est_yield",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    testTag: String,
    subtext: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WarmBrownLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = NaturalTextDark
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    color = SubtextTan,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CrumbCharacteristicsCard(
    targetHydration: Double,
    description: String
) {
    // Determine dynamic category, title, and emoji representation matching the HTML design spec
    val (headerTitle, emoji) = when {
        targetHydration < 61.0 -> Pair("Stiff Dough / Bagels", "🥯")
        targetHydration < 68.0 -> Pair("Medium Dough / Pizza", "🍕")
        targetHydration < 76.0 -> Pair("Artisan Sourdough", "🍞")
        else -> Pair("Ciabatta / Rustica", "🥖")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crumb_card"),
        colors = CardDefaults.cardColors(containerColor = DelicateCream),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderAccentLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circle avatar icon badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = headerTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrownText
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = WarmBrownLabel,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ResetButtonSection(onReset: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        FilledTonalButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_button")
                .height(48.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = SoftCream,
                contentColor = CustomGold
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reset Default Formula",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun NumericInputWithIncrement(
    label: String,
    value: Double,
    unit: String,
    step: Double,
    testTagPrefix: String,
    onValueChange: (Double) -> Unit
) {
    // Retain a local typing buffer so that clearing the field or typing backspaces doesn't trigger instant keyboard jumping
    var textVal by remember(value) {
        mutableStateOf(if (value == 0.0) "" else value.toInt().toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmBrownLabel
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Decrement button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SoftCream)
                    .clickable {
                        val newVal = (value - step).coerceAtLeast(0.0)
                        onValueChange(newVal)
                    }
                    .testTag("${testTagPrefix}_decrement"),
                contentAlignment = Alignment.Center
            ) {
                // Drawing thin horizontal line for minus
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawLine(
                        color = CustomGold,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // Text input container
            OutlinedTextField(
                value = textVal,
                onValueChange = { inputStr ->
                    textVal = inputStr
                    val parsed = inputStr.filter { it.isDigit() }.toDoubleOrNull()
                    if (parsed != null) {
                        onValueChange(parsed)
                    } else if (inputStr.isEmpty()) {
                        onValueChange(0.0)
                    }
                },
                maxLines = 1,
                trailingIcon = {
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubtextTan,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("${testTagPrefix}_input"),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomGold,
                    unfocusedBorderColor = SoftCream,
                    focusedTextColor = NaturalTextDark,
                    unfocusedTextColor = NaturalTextDark
                )
            )

            // Increment button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SoftCream)
                    .clickable {
                        val newVal = value + step
                        onValueChange(newVal)
                    }
                    .testTag("${testTagPrefix}_increment"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment",
                    tint = CustomGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
