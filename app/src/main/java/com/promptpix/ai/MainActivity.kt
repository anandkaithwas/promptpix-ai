package com.promptpix.ai

import android.graphics.Bitmap
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri

data class Effect(val id: String, val title: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val colors: List<Color>, val sample: String, val command: String, val instruction: String)
private fun iconForCategory(category: String): androidx.compose.ui.graphics.vector.ImageVector = when {
 category.contains("Photography") -> Icons.Rounded.PhotoCamera
 category.contains("Camera") -> Icons.Rounded.CameraAlt
 category.contains("Lighting") -> Icons.Rounded.WbSunny
 category.contains("Weather") -> Icons.Rounded.Cloud
 category.contains("Colour") -> Icons.Rounded.Palette
 category.contains("Motion") -> Icons.Rounded.Timelapse
 category.contains("Art") -> Icons.Rounded.Brush
 category.contains("Fantasy") -> Icons.Rounded.AutoAwesome
 category.contains("Nature") -> Icons.Rounded.Forest
 category.contains("Materials") -> Icons.Rounded.Diamond
 else -> Icons.Rounded.AutoAwesome
}
private fun iconForStyle(name: String, category: String): androidx.compose.ui.graphics.vector.ImageVector {
 val n = name.lowercase()
 return when {
  listOf("cinematic","film","movie","noir").any { it in n } -> Icons.Rounded.Movie
  listOf("portrait","headshot","face","beauty").any { it in n } -> Icons.Rounded.Face
  listOf("close up","macro","detail","zoom").any { it in n } -> Icons.Rounded.ZoomIn
  listOf("wide","panoramic","fisheye").any { it in n } -> Icons.Rounded.Panorama
  listOf("bird","aerial","drone","top down").any { it in n } -> Icons.Rounded.Flight
  listOf("sun","golden hour","sunrise","sunset","daylight").any { it in n } -> Icons.Rounded.WbSunny
  listOf("rain","water","ocean","underwater","waterfall").any { it in n } -> Icons.Rounded.WaterDrop
  listOf("snow","ice","blizzard").any { it in n } -> Icons.Rounded.AcUnit
  listOf("forest","tree","nature","meadow","garden").any { it in n } -> Icons.Rounded.Forest
  listOf("space","galaxy","cosmic","star","interstellar").any { it in n } -> Icons.Rounded.Public
  listOf("fire","ember","flame").any { it in n } -> Icons.Rounded.LocalFireDepartment
  listOf("light","lighting","glow","neon","flare").any { it in n } -> Icons.Rounded.LightMode
  listOf("motion","exposure","blur","trail","action").any { it in n } -> Icons.Rounded.Timelapse
  listOf("double exposure","multiple exposure","layers").any { it in n } -> Icons.Rounded.Layers
  listOf("glass","crystal","diamond","gem").any { it in n } -> Icons.Rounded.Diamond
  listOf("castle","medieval","architecture","building").any { it in n } -> Icons.Rounded.Castle
  listOf("anime","illustration","painting","watercolour","sketch","art").any { it in n } -> Icons.Rounded.Brush
  listOf("colour","color","monochrome","sepia","palette").any { it in n } -> Icons.Rounded.Palette
  listOf("cyberpunk","futuristic","electric","lightning","glitch").any { it in n } -> Icons.Rounded.Bolt
  else -> iconForCategory(category)
 }
}

private fun colorsForCategory(category: String): List<Color> = when {
 category.contains("Photography") -> listOf(Color(0xFF214C63),Color(0xFFDA956A))
 category.contains("Camera") -> listOf(Color(0xFF284D77),Color(0xFF7BB7D9))
 category.contains("Lighting") -> listOf(Color(0xFF75472E),Color(0xFFFFD27A))
 category.contains("Weather") -> listOf(Color(0xFF304B70),Color(0xFF9DB8D1))
 category.contains("Colour") -> listOf(Color(0xFF5B347A),Color(0xFFE58B9A))
 category.contains("Motion") -> listOf(Color(0xFF25466B),Color(0xFFDB795F))
 category.contains("Art") -> listOf(Color(0xFF68418C),Color(0xFFE29CBF))
 category.contains("Fantasy") -> listOf(Color(0xFF242B64),Color(0xFFB45CE0))
 category.contains("Nature") -> listOf(Color(0xFF245A4A),Color(0xFF91B97A))
 category.contains("Materials") -> listOf(Color(0xFF51566A),Color(0xFFBDA5D8))
 else -> listOf(Color(0xFF493A70),Color(0xFFB77BEE))
}
private fun loadEffects(context: android.content.Context): List<Effect> {
 val root = org.json.JSONObject(context.assets.open("catalogue/styles.json").bufferedReader().use { it.readText() })
 val array = root.getJSONArray("styles")
 return (0 until array.length()).map { i ->
  val item = array.getJSONObject(i); val category = item.getString("category")
  Effect(item.getString("id"),item.getString("name"),category,iconForStyle(item.getString("name"),category),colorsForCategory(category),category.uppercase(),"/"+item.getString("id"),item.getString("instruction"))
 }
}
private val ink = Color(0xFF171323)

data class EditPlan(
 val mainChange: String,
 val lighting: String,
 val style: String,
 val background: String,
 val preserve: String,
 val notes: String
)

private fun interpretRequestLocally(text: String): EditPlan {
 val q = text.lowercase()
 val lighting = when {
  listOf("sunset","golden hour","warm light","sunrise").any { it in q } -> "Warm, golden natural lighting"
  listOf("dark","moody","dramatic","night").any { it in q } -> "Dramatic, lower-key lighting"
  listOf("bright"," 밝","daylight","studio light").any { it in q } -> "Brighter, balanced lighting"
  else -> "Keep lighting natural unless the requested style requires a change"
 }
 val style = when {
  "cinematic" in q || "movie" in q || "film" in q -> "Cinematic photographic treatment"
  "vintage" in q || "retro" in q -> "Vintage film-inspired treatment"
  "anime" in q || "cartoon" in q -> "Illustrative transformation"
  "black and white" in q || "monochrome" in q -> "Black-and-white treatment"
  "professional" in q || "dslr" in q -> "Professional photographic finish"
  else -> "Follow the visual style described in your request"
 }
 val background = when {
  "background" in q || "replace" in q || "switzerland" in q || "beach" in q || "forest" in q || "sunset" in q -> "Background or environment changes were requested; preserve the main subject where possible"
  else -> "No specific background replacement detected"
 }
 val preserve = if (listOf("keep my face","face unchanged","don't change my face","do not change my face","preserve face","keep identity").any { it in q }) {
  "Preserve facial identity and key facial features"
 } else {
  "No explicit identity-preservation instruction detected. Review the subject carefully."
 }
 val notes = if (q.length < 12) "The request is very short. Add more detail for a more predictable edit." else "This is a basic on-device keyword interpretation preview, not an AI model's final understanding."
 return EditPlan(text.trim().replaceFirstChar { it.uppercase() }, lighting, style, background, preserve, notes)
}

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AuraStudioApp() } }
}

@Composable
fun AuraStudioApp() {
 val context = LocalContext.current
 var editDescription by remember { mutableStateOf("") }
 var editStatus by remember { mutableStateOf("Choose a photo, then describe the change you want.") }
 var generatedImageUri by remember { mutableStateOf<Uri?>(null) }
 var editPlan by remember { mutableStateOf<EditPlan?>(null) }
 var showReview by remember { mutableStateOf(false) }
 var selectedCategory by remember { mutableStateOf("All") }
 var selectedEffectTitles = remember { mutableStateListOf<String>() }
 var catalogueSearch by remember { mutableStateOf("") }
 var photoUri by remember { mutableStateOf<Uri?>(null) }
 var cameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
 val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { photoUri = it; cameraBitmap = null }
 val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { cameraBitmap = it; photoUri = null }
 val effects = remember { loadEffects(context) }
 val categories = listOf("All") + effects.map { it.category }.distinct()
 val shown = effects.filter { (selectedCategory == "All" || it.category == selectedCategory) && (catalogueSearch.isBlank() || it.title.contains(catalogueSearch,true) || it.category.contains(catalogueSearch,true)) }
 val selectedEffect = effects.firstOrNull { it.title in selectedEffectTitles } ?: effects.first()
 MaterialTheme(colorScheme = darkColorScheme(
  primary = Color(0xFFC7A8FF), secondary = Color(0xFFFFB77D),
  background = ink, surface = Color(0xFF241F32), onSurface = Color(0xFFF7F1FF)
 )) {
  if (showReview && editPlan != null) {
   EditPlanReviewScreen(
    plan = editPlan!!,
    onConfirm = { showReview = false; editStatus = "Plan confirmed. Image generation engine integration is the next milestone." },
    onEdit = { showReview = false },
    onCancel = { showReview = false; editPlan = null; editStatus = "Edit cancelled. No image was generated." }
   )
  } else Scaffold(containerColor = ink, bottomBar = {
   Column {
    if ((photoUri != null || cameraBitmap != null) && (selectedEffectTitles.isNotEmpty() || editDescription.isNotBlank())) {
     Button(
      onClick={ val chosen = effects.filter { it.title in selectedEffectTitles }; val combined = (chosen.map { it.title + ": " + it.instruction } + listOf(editDescription.trim()).filter { it.isNotBlank() }).joinToString("\n\n"); editPlan = interpretRequestLocally(combined); showReview = true; generatedImageUri=null },
      modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),
      shape=RoundedCornerShape(14.dp)
     ) { Icon(Icons.Rounded.AutoAwesome,null); Spacer(Modifier.width(8.dp)); Text("Review " + selectedEffectTitles.size + " selected style(s)") }
    }
    NavigationBar(containerColor = Color(0xFF211B2D)) {
     NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Rounded.AutoAwesome,null)},label={Text("Discover")})
     NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.FavoriteBorder,null)},label={Text("Saved")})
     NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.Collections,null)},label={Text("My edits")})
    }
   }
  }) { padding ->
   Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp).verticalScroll(rememberScrollState())) {
    Row(Modifier.fillMaxWidth().padding(top=14.dp,bottom=18.dp), verticalAlignment=Alignment.CenterVertically) {
     Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(Color(0xFF9D73FF),Color(0xFFFF9D83)))), contentAlignment=Alignment.Center) {
      Icon(Icons.Rounded.Camera, null, tint=Color.White, modifier=Modifier.size(27.dp))
     }
     Spacer(Modifier.width(11.dp))
     Column { Text("AURA STUDIO",fontSize=18.sp,fontWeight=FontWeight.Black,letterSpacing=1.4.sp); Text("AI PHOTO CREATION",fontSize=9.sp,color=Color(0xFFB9A9D1),letterSpacing=2.sp) }
     Spacer(Modifier.weight(1f))
     IconButton(onClick={}) { Icon(Icons.Rounded.Tune,null,tint=Color(0xFFCBB7F7)) }
    }
    Text("Make every photo",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color.White)
    Text("extraordinary.",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC5A5FF))
    Text("Choose from 275+ transformations or describe your own edit.",fontSize=13.sp,color=Color(0xFFB9B0C7),modifier=Modifier.padding(top=5.dp,bottom=12.dp))
    Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(selectedEffect.colors))) {
     if (photoUri != null) AsyncImage(model=photoUri,contentDescription="Your selected photo",modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
     else if (cameraBitmap != null) androidx.compose.foundation.Image(cameraBitmap!!.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
     else {
      Icon(Icons.Rounded.Portrait,null,tint=Color.White.copy(alpha=.8f),modifier=Modifier.align(Alignment.Center).size(94.dp))
      Text("YOUR PHOTO PREVIEW",Modifier.align(Alignment.BottomStart).padding(15.dp),color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)
     }
     Box(Modifier.align(Alignment.TopStart).padding(12.dp).clip(CircleShape).background(Color.Black.copy(alpha=.48f)).padding(horizontal=11.dp,vertical=6.dp)) {
      Text(if(photoUri!=null || cameraBitmap!=null) "PHOTO SELECTED" else "START WITH A PHOTO",fontSize=9.sp,color=Color.White,fontWeight=FontWeight.Bold)
     }
     if(photoUri==null && cameraBitmap==null) Text(selectedEffect.sample,Modifier.align(Alignment.BottomEnd).padding(14.dp),color=Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold)
    }
    Row(Modifier.fillMaxWidth().padding(top=11.dp,bottom=17.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
     Button(onClick={gallery.launch("image/*")},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.PhotoLibrary,null); Spacer(Modifier.width(7.dp)); Text("Gallery") }
     OutlinedButton(onClick={camera.launch(null)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.CameraAlt,null); Spacer(Modifier.width(7.dp)); Text("Camera") }
    }
    OutlinedTextField(
     value=editDescription,
     onValueChange={editDescription=it},
     modifier=Modifier.fillMaxWidth(),
     label={Text("Describe the change you want")},
     placeholder={Text("e.g. Make this a warm cinematic portrait at sunset...")},
     leadingIcon={Icon(Icons.Rounded.AutoAwesome,null)},
     minLines=2,
     maxLines=3,
     shape=RoundedCornerShape(16.dp)
    )
    Button(
     onClick={ val chosen = effects.filter { it.title in selectedEffectTitles }; val combined = (chosen.map { it.title + ": " + it.instruction } + listOf(editDescription.trim()).filter { it.isNotBlank() }).joinToString("\n\n"); editPlan = interpretRequestLocally(combined); showReview = true; generatedImageUri=null },
     enabled=(photoUri!=null || cameraBitmap!=null) && (editDescription.isNotBlank() || selectedEffectTitles.isNotEmpty()),
     modifier=Modifier.fillMaxWidth().padding(top=7.dp,bottom=8.dp),
     shape=RoundedCornerShape(14.dp)
    ) { Icon(Icons.Rounded.AutoAwesome,null); Spacer(Modifier.width(8.dp)); Text("Review my edit") }
    Text(editStatus,fontSize=10.sp,color=Color(0xFFFFD18A),modifier=Modifier.padding(bottom=8.dp))
    Row(verticalAlignment=Alignment.CenterVertically) {
     Text("Explore effects",fontSize=20.sp,fontWeight=FontWeight.Bold)
     Spacer(Modifier.weight(1f)); Text("${effects.size} styles",fontSize=11.sp,color=Color(0xFFB9A9D1))
    }
    OutlinedTextField(value=catalogueSearch,onValueChange={catalogueSearch=it},modifier=Modifier.fillMaxWidth().padding(top=10.dp,bottom=8.dp),placeholder={Text("Search 275+ styles...")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(14.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) { categories.forEach { c -> FilterChip(selected=selectedCategory==c,onClick={selectedCategory=c},label={Text(c)},shape=RoundedCornerShape(20.dp)) } }
    if(selectedEffectTitles.isNotEmpty()) {
     Text("SELECTED STYLES (${selectedEffectTitles.size})",fontSize=10.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC7A8FF),letterSpacing=1.sp,modifier=Modifier.padding(bottom=6.dp))
     Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) { selectedEffectTitles.forEach { title -> AssistChip(onClick={selectedEffectTitles.remove(title)},label={Text(title)},leadingIcon={Icon(Icons.Rounded.CheckCircle,null)},trailingIcon={Icon(Icons.Rounded.Close,null)}) } }
    }
    Text("Tap any style to add it to your edit. Select more than one.",fontSize=11.sp,color=Color(0xFFB9B0C7),modifier=Modifier.padding(bottom=9.dp))
    shown.chunked(2).forEach { rowEffects ->
     Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      rowEffects.forEach { effect ->
       val active = effect.title in selectedEffectTitles
       Column(Modifier.weight(1f).clip(RoundedCornerShape(17.dp)).background(if(active) Color(0xFF493563) else Color(0xFF272132)).clickable { if(active) { selectedEffectTitles.remove(effect.title); editStatus = effect.title + " removed from your edit." } else { selectedEffectTitles.add(effect.title); editStatus = effect.title + " added. " + selectedEffectTitles.size + " style(s) selected. Use Review to continue." } }.padding(10.dp)) {
        Box(Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(effect.colors))) {
         Icon(effect.icon,null,tint=Color.White,modifier=Modifier.align(Alignment.Center).size(34.dp))
         if(active) Icon(Icons.Rounded.CheckCircle,null,tint=Color.White,modifier=Modifier.align(Alignment.TopEnd).padding(5.dp))
        }
        Text(effect.title,Modifier.padding(top=7.dp),fontWeight=FontWeight.SemiBold,fontSize=12.sp,maxLines=2)
        Text(effect.category,Modifier.padding(top=2.dp),fontSize=10.sp,color=Color(0xFFB9A9D1),maxLines=1)
       }
      }
      if(rowEffects.size==1) Spacer(Modifier.weight(1f))
     }
    }
    Row(Modifier.fillMaxWidth().padding(bottom=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     OutlinedButton(
      onClick={
       generatedImageUri?.let { uri ->
        val send=Intent(Intent.ACTION_SEND).apply {
         type="image/*"
         putExtra(Intent.EXTRA_STREAM,uri)
         clipData=android.content.ClipData.newUri(context.contentResolver,"Edited photo",uri)
         addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send,"Share your image"))
       }
      },
      enabled=generatedImageUri!=null,
      modifier=Modifier.fillMaxWidth()
     ) { Icon(Icons.Rounded.Share,null); Spacer(Modifier.width(7.dp)); Text("Share edited image") }
    }

   }
  }
 }
}


@Composable
private fun EditPlanReviewScreen(
 plan: EditPlan,
 onConfirm: () -> Unit,
 onEdit: () -> Unit,
 onCancel: () -> Unit
) {
 MaterialTheme(colorScheme = darkColorScheme(primary=Color(0xFFC7A8FF),background=ink,surface=Color(0xFF241F32),onSurface=Color(0xFFF7F1FF))) {
  Column(
   Modifier.fillMaxSize().background(ink).padding(20.dp),
   verticalArrangement=Arrangement.spacedBy(14.dp)
  ) {
   Row(verticalAlignment=Alignment.CenterVertically) {
    Icon(Icons.Rounded.AutoAwesome,null,tint=Color(0xFFC7A8FF))
    Spacer(Modifier.width(10.dp))
    Text("AURA STUDIO",fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)
   }
   Text("Review your edit",fontSize=28.sp,fontWeight=FontWeight.Bold)
   Text("Here's what we understood. Check the plan before any image generation begins.",color=Color(0xFFB9B0C7))
   PlanCard("Main change",plan.mainChange)
   PlanCard("Lighting & mood",plan.lighting)
   PlanCard("Visual treatment",plan.style)
   PlanCard("Background & environment",plan.background)
   PlanCard("Subject preservation",plan.preserve)
   Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF392D24))) {
    Row(Modifier.padding(13.dp),verticalAlignment=Alignment.Top) {
     Icon(Icons.Rounded.Info,null,tint=Color(0xFFFFC27A))
     Spacer(Modifier.width(9.dp))
     Text(plan.notes,color=Color(0xFFFFDCA8),fontSize=12.sp)
    }
   }
   Spacer(Modifier.weight(1f))
   Button(onClick=onConfirm,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)) {
    Icon(Icons.Rounded.CheckCircle,null); Spacer(Modifier.width(8.dp)); Text("Confirm & generate")
   }
   OutlinedButton(onClick=onEdit,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)) {
    Icon(Icons.Rounded.Edit,null); Spacer(Modifier.width(8.dp)); Text("Edit request")
   }
   TextButton(onClick=onCancel,modifier=Modifier.fillMaxWidth()) { Text("Cancel",color=Color(0xFFB9B0C7)) }
   Text("Prototype note: this preview uses simple local keyword rules. It is not yet a trained language model, and generation is not connected.",fontSize=10.sp,color=Color(0xFF93899F))
  }
 }
}

@Composable
private fun PlanCard(title: String, body: String) {
 Card(
  modifier=Modifier.fillMaxWidth(),
  colors=CardDefaults.cardColors(containerColor=Color(0xFF282236)),
  shape=RoundedCornerShape(15.dp)
 ) {
  Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
   Text(title.uppercase(),fontSize=10.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC7A8FF),letterSpacing=1.sp)
   Text(body,fontSize=14.sp,color=Color(0xFFF7F1FF))
  }
 }
}
