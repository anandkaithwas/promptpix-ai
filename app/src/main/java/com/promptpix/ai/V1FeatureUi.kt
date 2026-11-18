package com.promptpix.ai

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun V1CreativeControls(
    styles: List<Effect>,
    availableStyles: List<Effect> = styles,
    customPrompt: String,
    onPromptPreview: (PromptBundle) -> Unit,
    onRoulette: (List<Effect>) -> Unit,
    onIntensityChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var intensity by remember { mutableFloatStateOf(50f) }
    var promptPreview by remember { mutableStateOf<PromptBundle?>(null) }
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF252033)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFFC7A8FF))
                Spacer(Modifier.width(8.dp))
                Text("CREATIVE ASSIST", style = MaterialTheme.typography.titleMedium)
            }
            Text("Transformation intensity", fontSize = 12.sp)
            Slider(value = intensity, onValueChange = { intensity = it; onIntensityChange(it.toInt()) }, valueRange = 0f..100f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtle", fontSize = 10.sp, color = Color.LightGray)
                Text("${intensity.toInt()} / 100", fontSize = 11.sp, color = Color(0xFFC7A8FF))
                Text("Wild", fontSize = 10.sp, color = Color.LightGray)
            }
            Text("Engine denoising target: %.2f".format(CreativeSelection(emptyList(), intensity.toInt()).normalizedDenoisingStrength()), fontSize = 10.sp, color = Color.LightGray)
            Text("This value is prepared for the generation engine; no image is changed until inference is connected.", fontSize = 10.sp, color = Color(0xFFFFD18A))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val plan = V1PromptInterpreter.interpret(customPrompt, styles)
                    promptPreview = plan
                    onPromptPreview(plan)
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.AutoAwesome, null); Spacer(Modifier.width(5.dp)); Text("Enhance prompt")
                }
                OutlinedButton(onClick = { onRoulette(StyleRoulette.spin(availableStyles, Random.Default, 3)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Casino, null); Spacer(Modifier.width(5.dp)); Text("Style roulette")
                }
            }
            promptPreview?.let { plan ->
                Text("PROMPT PREVIEW", color = Color(0xFFC7A8FF), fontSize = 10.sp)
                Text(plan.combined, fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun BeforeAfterCompare(
    original: Bitmap,
    generated: Bitmap,
    modifier: Modifier = Modifier
) {
    var reveal by remember { mutableFloatStateOf(0.5f) }
    BoxWithConstraints(modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(18.dp))) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        Image(original.asImageBitmap(), contentDescription = "Original photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxHeight().fillMaxWidth(reveal).clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))) {
            Image(generated.asImageBitmap(), contentDescription = "Generated photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(Modifier.offset(x = (maxWidth * reveal) - 1.dp).fillMaxHeight().width(2.dp).background(Color.White))
        Box(Modifier.align(Alignment.BottomStart).padding(10.dp).background(Color.Black.copy(alpha=.65f), RoundedCornerShape(6.dp)).padding(6.dp)) { Text("AI RESULT", color=Color.White, fontSize=9.sp) }
        Box(Modifier.align(Alignment.BottomEnd).padding(10.dp).background(Color.Black.copy(alpha=.65f), RoundedCornerShape(6.dp)).padding(6.dp)) { Text("ORIGINAL", color=Color.White, fontSize=9.sp) }
        Slider(value=reveal, onValueChange={ reveal=it }, modifier=Modifier.align(Alignment.TopCenter).padding(horizontal=12.dp), colors=SliderDefaults.colors(thumbColor=Color.White, activeTrackColor=Color.White, inactiveTrackColor=Color.White.copy(alpha=.4f)))
    }
}

@Composable
fun V1FeatureStatusPanel(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val grouped = V1FeatureRegistry.all.groupBy { it.group }
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF211D2C)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("V1.0 FEATURE FOUNDATION", color=Color(0xFFC7A8FF), fontSize=11.sp)
                    Text("33 feature modules tracked", style=MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick={expanded=!expanded}) { Text(if(expanded) "Hide" else "View") }
            }
            if(expanded) grouped.forEach { (group, features) ->
                Text(group.uppercase(), color=Color(0xFFC7A8FF), fontSize=10.sp, modifier=Modifier.padding(top=10.dp,bottom=4.dp))
                features.forEach { feature ->
                    Row(Modifier.fillMaxWidth().padding(vertical=3.dp), verticalAlignment=Alignment.CenterVertically) {
                        Text(feature.title, modifier=Modifier.weight(1f), fontSize=11.sp)
                        Text(when(feature.state) {
                            V1FeatureState.EXISTING -> "Existing"
                            V1FeatureState.SCAFFOLDED -> "Scaffolded"
                            V1FeatureState.MODEL_REQUIRED -> "Model needed"
                            V1FeatureState.INTEGRATION_REQUIRED -> "Integration"
                        }, fontSize=9.sp, color=when(feature.state) {
                            V1FeatureState.EXISTING -> Color(0xFF8ED6A5)
                            V1FeatureState.SCAFFOLDED -> Color(0xFFC7A8FF)
                            else -> Color(0xFFFFD18A)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareSetupCard(plan: HardwareExecutionPlan, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF211D2C)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("DEVICE AUTO-SETUP", color = Color(0xFFC7A8FF), fontSize = 11.sp)
            Text("${plan.profile.apiLevel} • ${plan.profile.totalRamMb} MB RAM • ${plan.profile.cpuCores} CPU cores", fontSize = 12.sp)
            Text("ABI: ${plan.profile.supportedAbis.firstOrNull() ?: "Unknown"}", fontSize = 11.sp, color = Color.LightGray)
            Text("Profile: ${plan.inferenceProfile.name.replace('_', ' ')}", fontSize = 11.sp)
            Text(if (plan.maxInputEdge > 0) "Suggested input limit: ${plan.maxInputEdge}px" else "Inference unavailable", fontSize = 11.sp)
            Text(plan.explanation, fontSize = 10.sp, color = Color(0xFFFFD18A))
            Text(if (plan.allowGpuDelegate) "GPU delegate may be tested; CPU fallback remains required." else "GPU delegate not recommended by preflight.", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}
