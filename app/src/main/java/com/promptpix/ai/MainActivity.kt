package com.promptpix.ai

import android.graphics.Bitmap
import android.app.ActivityManager
import android.os.Build
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch

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
 val loaded = (0 until array.length()).map { i ->
  val item = array.getJSONObject(i); val category = item.getString("category")
  Effect(item.getString("id"),item.getString("name"),category,iconForStyle(item.getString("name"),category),colorsForCategory(category),category.uppercase(),"/"+item.getString("id"),item.getString("instruction"))
 }
 return StyleCatalogueValidator.validate(loaded)
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
 var subjectDescription by remember { mutableStateOf("") }
 var generationState by remember { mutableStateOf<GenerationUiState>(GenerationUiState.Idle) }
 var generationIntensity by remember { mutableIntStateOf(50) }
 var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
 var originalCompareBitmap by remember { mutableStateOf<Bitmap?>(null) }
 var lastGenerationRequest by remember { mutableStateOf<GenerationRequest?>(null) }
 var variantBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
 val generationScope = rememberCoroutineScope()
 val legacySavePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
  val bitmap = generatedBitmap
  if (granted && bitmap != null) {
   try {
    generatedImageUri = V1MediaActions.saveGeneratedImage(context, bitmap)
    saveStatus = "Saved to Pictures/Aura Studio."
   } catch (error: Exception) { saveStatus = error.message ?: "Could not save image." }
  } else saveStatus = "Storage permission was not granted; image was not saved."
 }
 val generationCoordinator = remember { V1GenerationCoordinator(UnconfiguredLocalImageGenerationEngine(), UnconfiguredSuperResolutionEngine()) }
 var editStatus by remember { mutableStateOf("Choose a photo, then describe the change you want.") }
 var generatedImageUri by remember { mutableStateOf<Uri?>(null) }
 var saveStatus by remember { mutableStateOf<String?>(null) }
 var selectedCategory by remember { mutableStateOf("All") }
 var selectedStyleIds = remember { mutableStateListOf<String>() }
 var catalogueSearch by remember { mutableStateOf("") }
 var photoUri by remember { mutableStateOf<Uri?>(null) }
 var cameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
 var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
 var showPhotoPreview by remember { mutableStateOf(false) }
 // Prefer Android Photo Picker (no broad media permission); the contract supplies
 // a compatible fallback on older devices, including API 26–32.
 val gallery = rememberLauncherForActivityResult(
  ActivityResultContracts.PickVisualMedia()
 ) { uri ->
  photoUri = uri
  cameraBitmap = null
  generatedImageUri = null
  editStatus = if (uri != null) "Photo selected. Your original remains unchanged." else "No photo selected."
 }
 val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
  val capturedUri = pendingCameraUri
  pendingCameraUri = null
  if (success && capturedUri != null) {
   photoUri = capturedUri
   cameraBitmap = null
   generatedImageUri = null
   editStatus = "Full-resolution photo captured. Your original is preserved."
  } else {
   capturedUri?.let { context.contentResolver.delete(it, null, null) }
   editStatus = "Camera capture cancelled or failed."
  }
 }
 val effects = remember { loadEffects(context) }
 val categories = listOf("All") + effects.map { it.category }.distinct()
 val shown = effects.filter { (selectedCategory == "All" || it.category == selectedCategory) && StyleCatalogueSearch.matches(it, catalogueSearch) }
 val selectedStyles = MultiStyleSelection.resolve(selectedStyleIds, effects)
 val selectedEffect = selectedStyles.firstOrNull() ?: effects.first()
 val contextSuggestions = remember(subjectDescription, effects) { V1ContextSuggestionService(UnconfiguredOfflineImageLabeler()).suggestFromUserDescription(subjectDescription, effects) }
 val hardwarePlan = remember { V1HardwareSetup.plan(V1DeviceManager.inspect(context)) }
 MaterialTheme(colorScheme = darkColorScheme(
  primary = Color(0xFFC7A8FF), secondary = Color(0xFFFFB77D),
  background = ink, surface = Color(0xFF241F32), onSurface = Color(0xFFF7F1FF)
 )) {
  Scaffold(containerColor = ink, bottomBar = {
   NavigationBar(containerColor = Color(0xFF211B2D)) {
    NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Rounded.AutoAwesome,null)},label={Text("Discover")})
    NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.FavoriteBorder,null)},label={Text("Saved")})
    NavigationBarItem(selected=false,onClick={},icon={Icon(Icons.Rounded.Collections,null)},label={Text("My edits")})
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
    Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(selectedEffect.colors))
     .clickable(enabled = photoUri != null || cameraBitmap != null) { showPhotoPreview = true }) {
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
     Button(onClick={gallery.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.PhotoLibrary,null); Spacer(Modifier.width(7.dp)); Text("Gallery") }
     OutlinedButton(onClick={
      val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
      val outputFile = File.createTempFile("aura_capture_", ".jpg", cameraDir)
      val output = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
      pendingCameraUri = output
      camera.launch(output)
     },modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)) { Icon(Icons.Rounded.CameraAlt,null); Spacer(Modifier.width(7.dp)); Text("Camera") }
    }
    if (showPhotoPreview && (photoUri != null || cameraBitmap != null)) {
     Dialog(onDismissRequest = { showPhotoPreview = false }) {
      Box(
       Modifier.fillMaxWidth().fillMaxHeight(0.82f)
        .clip(RoundedCornerShape(18.dp))
        .background(Color.Black)
        .clickable { showPhotoPreview = false },
       contentAlignment = Alignment.Center
      ) {
       if (photoUri != null) {
        AsyncImage(
         model = photoUri,
         contentDescription = "Full-screen source photo preview",
         modifier = Modifier.fillMaxSize(),
         contentScale = ContentScale.Fit,
         onError = { editStatus = "Could not decode this photo. Try another image format." }
        )
       } else {
        androidx.compose.foundation.Image(
         cameraBitmap!!.asImageBitmap(),
         contentDescription = "Full-screen camera photo preview",
         modifier = Modifier.fillMaxSize(),
         contentScale = ContentScale.Fit
        )
       }
       Text(
        "Tap outside or back to close",
        Modifier.align(Alignment.BottomCenter).padding(12.dp)
         .background(Color.Black.copy(alpha = .65f), RoundedCornerShape(8.dp))
         .padding(horizontal = 10.dp, vertical = 7.dp),
        color = Color.White, fontSize = 11.sp
       )
      }
     }
    }
    OutlinedTextField(
     value=editDescription,
     onValueChange={editDescription=it.take(CustomInstructionPolicy.MAX_LENGTH)},
     modifier=Modifier.fillMaxWidth(),
     label={Text("Describe the change you want")},
     placeholder={Text("e.g. Make this a warm cinematic portrait at sunset...")},
     leadingIcon={Icon(Icons.Rounded.AutoAwesome,null)},
     minLines=2,
     maxLines=3,
     shape=RoundedCornerShape(16.dp)
    )
    Button(
     onClick={
      val source = photoUri
      if (source == null) {
       editStatus = "Select a photo from Gallery or Camera before generating."
      } else {
       val instruction = if (editDescription.isBlank() && selectedStyles.isNotEmpty()) "" else when (val validation = CustomInstructionPolicy.validate(editDescription)) {
        is InstructionValidation.Invalid -> {
         editStatus = validation.reason
         null
        }
        is InstructionValidation.Valid -> validation.value
       }
       if (instruction != null) {
         val prompt = V1PromptInterpreter.interpret(instruction, selectedStyles)
         val request = GenerationRequest(
          source = source,
          prompt = prompt,
          selectedStyleIds = selectedStyles.map { it.id },
          intensity = generationIntensity,
          seed = kotlin.random.Random.nextLong(),
          requestedWidth = 512,
          requestedHeight = 512
         )
         generatedImageUri = null
         generatedBitmap = null
         variantBitmaps = emptyList()
         lastGenerationRequest = request
         editStatus = "Preparing local image generation…"
         generationScope.launch {
          generationState = GenerationUiState.Preparing()
          generationState = generationCoordinator.generate(request) { generationState = it }
          editStatus = when (val state = generationState) {
           is GenerationUiState.Success -> {
            generatedBitmap = state.result.image
            "Generation complete. Your original photo remains unchanged."
           }
           is GenerationUiState.Failed -> state.message
           else -> "Generation stopped."
          }
         }
       }
      }
     },
     enabled=photoUri!=null && (editDescription.isNotBlank() || selectedStyleIds.isNotEmpty()) && generationState !is GenerationUiState.Preparing && generationState !is GenerationUiState.Running,
     modifier=Modifier.fillMaxWidth().padding(top=7.dp,bottom=8.dp),
     shape=RoundedCornerShape(14.dp)
    ) { Icon(Icons.Rounded.AutoAwesome,null); Spacer(Modifier.width(8.dp)); Text("Generate") }
    if (generationState is GenerationUiState.Running) {
     val progress = (generationState as GenerationUiState.Running).progress
     if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
     else LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
    if (generationState is GenerationUiState.Preparing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    Text(editStatus,fontSize=10.sp,color=Color(0xFFFFD18A),modifier=Modifier.padding(bottom=8.dp))
    generatedBitmap?.let { result ->
     Text("GENERATED RESULT", color=Color(0xFFC7A8FF), fontSize=11.sp, fontWeight=FontWeight.Bold)
     androidx.compose.foundation.Image(result.asImageBitmap(), "Generated image", Modifier.fillMaxWidth().height(240.dp), contentScale=ContentScale.Fit)
    }
    generatedBitmap?.let { result ->
     Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      Button(onClick={
       if (Build.VERSION.SDK_INT <= 28 && context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        legacySavePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
       } else {
        try {
         val uri=V1MediaActions.saveGeneratedImage(context,result)
         generatedImageUri=uri
         saveStatus="Saved to Pictures/Aura Studio."
         editStatus="Generated image saved. Original photo was not changed."
        } catch(e:Exception) { saveStatus=e.message ?: "Could not save image." }
       }
      }, modifier=Modifier.weight(1f)) { Icon(Icons.Rounded.SaveAlt,null); Text("Save image") }
      OutlinedButton(onClick={
       val uri=generatedImageUri
       if(uri!=null) try { V1MediaActions.shareGeneratedImage(context,uri) } catch(e:Exception) { saveStatus=e.message ?: "Could not share image." }
       else saveStatus="Save the generated image first, then share it."
      }, modifier=Modifier.weight(1f), enabled=generatedImageUri!=null) { Icon(Icons.Rounded.Share,null); Text("Share") }
     }
     saveStatus?.let { Text(it,fontSize=11.sp,color=Color(0xFFFFD18A),modifier=Modifier.padding(top=4.dp)) }
    }
    if (generatedBitmap != null && photoUri != null) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      OutlinedButton(onClick={ generationScope.launch {
       generationState=GenerationUiState.Preparing("Creating another variation…")
       generationState=generationCoordinator.shuffle { generationState=it }
       if(generationState is GenerationUiState.Success) { generatedBitmap=(generationState as GenerationUiState.Success).result.image; editStatus="New variation generated with a different seed." }
       else if(generationState is GenerationUiState.Failed) editStatus=(generationState as GenerationUiState.Failed).message
      } }, modifier=Modifier.weight(1f), enabled=lastGenerationRequest!=null && generationState !is GenerationUiState.Preparing && generationState !is GenerationUiState.Running) { Icon(Icons.Rounded.Refresh,null); Text("Try again") }
      OutlinedButton(onClick={ val current=generatedBitmap; if(current!=null) generationScope.launch {
       generationState=GenerationUiState.Preparing("Enhancing resolution…")
       try { val enhanced=generationCoordinator.enhanceHd(current); generatedBitmap=enhanced; generationState=GenerationUiState.Success(GenerationResult(enhanced,lastGenerationRequest?.seed ?: 0L,enhanced.width,enhanced.height)); editStatus="HD enhancement completed." }
       catch(e:Exception) { editStatus=e.message ?: "HD enhancement unavailable." }
      } }, modifier=Modifier.weight(1f), enabled=generatedBitmap!=null && generationState !is GenerationUiState.Preparing && generationState !is GenerationUiState.Running) { Icon(Icons.Rounded.AutoAwesome,null); Text("HD Enhance") }
     }
     OutlinedButton(onClick={ val request=lastGenerationRequest; if(request!=null) generationScope.launch {
      try { generationState=GenerationUiState.Preparing("Generating four genuine variations…"); val results=generationCoordinator.generateVariants(request,4) { generationState=it }; variantBitmaps=results.map { it.image }; generatedBitmap=results.first().image; generationState=GenerationUiState.Success(results.first()); editStatus="Four variations generated." }
      catch(e:Exception) { generationState=GenerationUiState.Failed(e.message ?: "Multiple preview unavailable.",e is ModelNotConfiguredException); editStatus=e.message ?: "Multiple preview unavailable." }
     } }, modifier=Modifier.fillMaxWidth(), enabled=lastGenerationRequest!=null && generationState !is GenerationUiState.Preparing && generationState !is GenerationUiState.Running) { Icon(Icons.Rounded.GridView,null); Text("Generate 2×2 variations") }
     if(variantBitmaps.size==4) {
      Text("FOUR GENERATED VARIATIONS",fontSize=11.sp,color=Color(0xFFC7A8FF),fontWeight=FontWeight.Bold)
      Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { variantBitmaps.chunked(2).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { row.forEach { bitmap -> androidx.compose.foundation.Image(bitmap.asImageBitmap(),"Generated variation",Modifier.weight(1f).height(130.dp),contentScale=ContentScale.Crop) } } } }
     }
     OutlinedButton(onClick={ val uri=photoUri; if(uri!=null) generationScope.launch { try { originalCompareBitmap=V1ImageInput.decodeForInference(context,uri,1024) } catch(e:Exception) { editStatus=e.message ?: "Could not load original for comparison." } } },modifier=Modifier.fillMaxWidth()) { Text("Compare original and result") }
     if(originalCompareBitmap!=null && generatedBitmap!=null) BeforeAfterCompare(originalCompareBitmap!!,generatedBitmap!!)
    }
    DeviceReadinessCard(context, photoUri, cameraBitmap)
    HardwareSetupCard(hardwarePlan)
    Text("PHOTO CONTEXT & STYLE SUGGESTIONS", fontSize=11.sp, color=Color(0xFFC7A8FF), fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=12.dp))
    OutlinedTextField(value=subjectDescription, onValueChange={ subjectDescription=it.take(160) }, modifier=Modifier.fillMaxWidth(), label={ Text("What is in your photo? (optional)") }, placeholder={ Text("e.g. portrait, dog, landscape, product") }, singleLine=true, shape=RoundedCornerShape(14.dp))
    if (contextSuggestions.recommendations.isNotEmpty()) {
     Text("Suggested from your description (not automatic image recognition)", fontSize=10.sp, color=Color.LightGray)
     Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      contextSuggestions.recommendations.take(8).forEach { recommendation ->
       AssistChip(onClick={
        try {
         val updated=MultiStyleSelection.toggle(selectedStyleIds.toList(), recommendation.id, effects)
         selectedStyleIds.clear(); selectedStyleIds.addAll(updated)
         editStatus=recommendation.title+" added from suggestions."
        } catch (error: IllegalArgumentException) { editStatus=error.message ?: "Could not add suggestion." }
       }, label={ Text(recommendation.title) })
      }
     }
    }
    V1CreativeControls(
     styles = selectedStyles,
     availableStyles = effects,
     customPrompt = editDescription,
     onPromptPreview = { plan -> editStatus = "Prompt prepared (${plan.combined.length} characters). Generate sends this prompt to the local engine." },
     onIntensityChange = { generationIntensity = it.coerceIn(0, 100) },
     onRoulette = { picks ->
      selectedStyleIds.clear()
      selectedStyleIds.addAll(picks.map { it.id })
      editStatus = "Style roulette selected ${picks.size} compatible styles."
     }
    )
    V1FeatureStatusPanel()

    Row(verticalAlignment=Alignment.CenterVertically) {
     Text("Explore effects",fontSize=20.sp,fontWeight=FontWeight.Bold)
     Spacer(Modifier.weight(1f)); Text("${effects.size} styles",fontSize=11.sp,color=Color(0xFFB9A9D1))
    }
    OutlinedTextField(value=catalogueSearch,onValueChange={catalogueSearch=it},modifier=Modifier.fillMaxWidth().padding(top=10.dp,bottom=8.dp),placeholder={Text("Search 275+ styles...")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(14.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     categories.forEach { category ->
      val count = if (category == "All") effects.size else effects.count { it.category == category }
      FilterChip(selected=selectedCategory==category,onClick={selectedCategory=category},label={Text("$category · $count")},shape=RoundedCornerShape(20.dp))
     }
    }
    if(selectedStyles.isNotEmpty()) {
     Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
      Text("SELECTED STYLES (${selectedStyles.size}/${MultiStyleSelection.MAX_STYLES})",fontSize=10.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC7A8FF),letterSpacing=1.sp,modifier=Modifier.weight(1f))
      TextButton(onClick={selectedStyleIds.clear(); editStatus="All selected styles cleared."}) { Text("Clear all") }
     }
     Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      selectedStyles.forEach { style ->
       AssistChip(onClick={selectedStyleIds.remove(style.id); editStatus=style.title+" removed from your edit."},label={Text(style.title)},leadingIcon={Icon(Icons.Rounded.CheckCircle,null)},trailingIcon={Icon(Icons.Rounded.Close,null)})
      }
     }
    }
    Text("Tap any style to add it to your edit. Select more than one.",fontSize=11.sp,color=Color(0xFFB9B0C7),modifier=Modifier.padding(bottom=9.dp))
    shown.chunked(2).forEach { rowEffects ->
     Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      rowEffects.forEach { effect ->
       val active = effect.id in selectedStyleIds
       Column(Modifier.weight(1f).clip(RoundedCornerShape(17.dp)).background(if(active) Color(0xFF493563) else Color(0xFF272132)).clickable {
        try {
         val updated = MultiStyleSelection.toggle(selectedStyleIds.toList(), effect.id, effects)
         selectedStyleIds.clear(); selectedStyleIds.addAll(updated)
         editStatus = if(active) effect.title + " removed from your edit." else effect.title + " added. " + updated.size + " of " + MultiStyleSelection.MAX_STYLES + " styles selected."
        } catch (error: IllegalArgumentException) {
         editStatus = error.message ?: "This style cannot be selected."
        }
       }.padding(10.dp)) {
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

