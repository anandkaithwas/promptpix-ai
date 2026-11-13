package com.promptpix.ai

import android.graphics.Bitmap
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
private val violet = Color(0xFF9A70F5)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { PromptoraApp() } }
}

@Composable
fun PromptoraApp() {
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
  Scaffold(containerColor = ink, bottomBar = {
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
     Column { Text("PROMPTORA",fontSize=18.sp,fontWeight=FontWeight.Black,letterSpacing=1.4.sp); Text("AI PHOTO EFFECTS",fontSize=9.sp,color=Color(0xFFB9A9D1),letterSpacing=2.sp) }
     Spacer(Modifier.weight(1f))
     IconButton(onClick={}) { Icon(Icons.Rounded.Tune,null,tint=Color(0xFFCBB7F7)) }
    }
    Text("Make every photo",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color.White)
    Text("extraordinary.",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Color(0xFFC5A5FF))
    Text("Choose a look. Preview the style. Keep your favourite.",fontSize=13.sp,color=Color(0xFFB9B0C7),modifier=Modifier.padding(top=5.dp,bottom=16.dp))
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
    Text("Effect cards are style samples. Full AI transformation is coming next.",fontSize=10.sp,color=Color(0xFF93899F),modifier=Modifier.padding(bottom=8.dp))
   }
  }
 }
}
