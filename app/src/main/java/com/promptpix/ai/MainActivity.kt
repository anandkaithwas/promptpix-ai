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

data class Effect(val title: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val colors: List<Color>, val sample: String)
private val effects = listOf(
 Effect("Cinematic", "Portrait", Icons.Rounded.Movie, listOf(Color(0xFF123C4A),Color(0xFFE58B55)), "FILM LOOK"),
 Effect("Dreamy Bokeh", "Portrait", Icons.Rounded.BlurOn, listOf(Color(0xFF6D4A9B),Color(0xFFF4B5C8)), "SOFT FOCUS"),
 Effect("Golden Hour", "Nature", Icons.Rounded.WbSunny, listOf(Color(0xFFB95E35),Color(0xFFFFD27A)), "WARM GLOW"),
 Effect("Cyberpunk", "Art", Icons.Rounded.Bolt, listOf(Color(0xFF32106A),Color(0xFF00C9D8)), "NEON WORLD"),
 Effect("Vintage Film", "Classic", Icons.Rounded.PhotoCamera, listOf(Color(0xFF76543D),Color(0xFFE0B887)), "35MM GRAIN"),
 Effect("Fantasy", "Art", Icons.Rounded.AutoAwesome, listOf(Color(0xFF293A85),Color(0xFFCB8BDA)), "DREAM WORLD"),
 Effect("Monochrome", "Classic", Icons.Rounded.Contrast, listOf(Color(0xFF252936),Color(0xFF9EA5B3)), "B&W"),
 Effect("Editorial", "Portrait", Icons.Rounded.Camera, listOf(Color(0xFF8D345F),Color(0xFFF0A6B9)), "MAGAZINE")
)
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
 var editPlan by remember { mutableStateOf<EditPlan?>(null) }
 var showReview by remember { mutableStateOf(false) }
 var selectedCategory by remember { mutableStateOf("All") }
 var selectedEffect by remember { mutableStateOf(effects.first()) }
 var photoUri by remember { mutableStateOf<Uri?>(null) }
 var cameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
 val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { photoUri = it; cameraBitmap = null }
 val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { cameraBitmap = it; photoUri = null }
 val categories = listOf("All","Portrait","Nature","Art","Classic")
 val shown = effects.filter { selectedCategory == "All" || it.category == selectedCategory }
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
   NavigationBar(containerColor = Color(0xFF211B2D)) {
    NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Rounded.AutoAwesome,null)},label={Text("Discover")})
    NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.FavoriteBorder,null)},label={Text("Saved")})
    NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.Collections,null)},label={Text("My edits")})
   }
  }) { padding ->
   Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp)) {
    Row(Modifier.fillMaxWidth().padding(top=14.dp,bottom=18.dp), verticalAlignment=Alignment.CenterVertically) {
     Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(Color(0xFF9D73FF),Color(0xFFFF9D83)))), contentAlignment=Alignment.Center) {
      Icon(Icons.Rounded.AutoAwesome, null, tint=Color.White, modifier=Modifier.size(27.dp))
     }
     Spacer(Modifier.width(11.dp))
     Column { Text("AURA STUDIO",fontSize=18.sp,fontWeight=FontWeight.Black,letterSpacing=1.4.sp); Text("AI PHOTO CREATION",fontSize=9.sp,color=Color(0xFFB9A9D1),letterSpacing=2.sp) }
     Spacer(Modifier.weight(1f))
     IconButton(onClick={}) { Icon(Icons.Rounded.Tune,null,tint=Color(0xFFCBB7F7)) }
    }
    Text("Make every photo",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color.White)
    Text("extraordinary.",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC5A5FF))
    Text("Choose a look or describe what you imagine.",fontSize=13.sp,color=Color(0xFFB9B0C7),modifier=Modifier.padding(top=5.dp,bottom=12.dp))
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
     onClick={ editPlan = interpretRequestLocally(editDescription); showReview = true; generatedImageUri=null },
     enabled=(photoUri!=null || cameraBitmap!=null) && editDescription.isNotBlank(),
     modifier=Modifier.fillMaxWidth().padding(top=7.dp,bottom=8.dp),
     shape=RoundedCornerShape(14.dp)
    ) { Icon(Icons.Rounded.AutoAwesome,null); Spacer(Modifier.width(8.dp)); Text("Create my edit") }
    Text(editStatus,fontSize=10.sp,color=Color(0xFFFFD18A),modifier=Modifier.padding(bottom=8.dp))
    Row(verticalAlignment=Alignment.CenterVertically) {
     Text("Explore effects",fontSize=20.sp,fontWeight=FontWeight.Bold)
     Spacer(Modifier.weight(1f)); Text("8 styles",fontSize=11.sp,color=Color(0xFFB9A9D1))
    }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top=10.dp,bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     categories.forEach { c -> FilterChip(selected=selectedCategory==c,onClick={selectedCategory=c},label={Text(c)},shape=RoundedCornerShape(20.dp)) }
    }
    LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=14.dp)) {
     items(shown) { effect ->
      val active = selectedEffect.title == effect.title
      Column(Modifier.clip(RoundedCornerShape(18.dp)).background(if(active) Color(0xFF3A2D52) else Color(0xFF272132)).clickable { selectedEffect=effect }.padding(8.dp)) {
       Box(Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(effect.colors))) {
        Icon(effect.icon,null,tint=Color.White.copy(alpha=.9f),modifier=Modifier.align(Alignment.Center).size(39.dp))
        Text(effect.sample,Modifier.align(Alignment.BottomStart).padding(8.dp),color=Color.White,fontSize=8.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
        if(active) Icon(Icons.Rounded.CheckCircle, null,tint=Color.White,modifier=Modifier.align(Alignment.TopEnd).padding(7.dp))
       }
       Text(effect.title,Modifier.padding(top=8.dp,start=3.dp),fontWeight=FontWeight.SemiBold,fontSize=14.sp)
       Text(effect.category,Modifier.padding(start=3.dp,top=2.dp),fontSize=11.sp,color=Color(0xFFB9A9D1))
      }
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
