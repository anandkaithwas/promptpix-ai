package com.promptpix.ai
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Prompt(val title:String,val category:String,val command:String,val instruction:String)
val starterPrompts = listOf(
 Prompt("Cinematic Portrait","Photography","/cinematic","Film-inspired composition, controlled contrast and natural skin tones."),
 Prompt("Golden Hour","Lighting","/goldenhour","Warm low-angle golden-hour sunlight."),
 Prompt("Soft Bokeh","Lens","/bokeh","Shallow depth of field with optical background blur."),
 Prompt("Rembrandt Light","Lighting","/rembrandt","Directional portrait lighting with a triangular cheek highlight."),
 Prompt("Rim Light","Lighting","/rimlight","Subtle edge lighting separating subject and background."),
 Prompt("35mm Film","Photography","/35mmfilm","Natural film grain and gentle highlight roll-off."),
 Prompt("Monochrome","Colour","/monochrome","Expressive black and white with rich tonal separation."),
 Prompt("Teal & Orange","Colour","/tealorange","Balanced teal shadows and warm highlights."),
 Prompt("Editorial","Photography","/editorial","Polished fashion-editorial composition."),
 Prompt("Soft Light","Lighting","/softlight","Broad diffused light and gentle shadows."),
 Prompt("Macro Detail","Camera","/macro","Fine close-up detail and macro depth of field."),
 Prompt("Long Exposure","Camera","/longexposure","Intentional motion trails in moving elements."),
 Prompt("Atmospheric Mist","Atmosphere","/mist","Subtle atmospheric mist and light scattering."),
 Prompt("Rainy Mood","Atmosphere","/rain","Rain atmosphere with wet-surface reflections."),
 Prompt("Cyberpunk","Art","/cyberpunk","Futuristic neon palette with magenta and cyan lighting."),
 Prompt("Watercolour","Art","/watercolor","Traditional watercolour pigment texture."),
 Prompt("Anime","Art","/anime","Polished hand-drawn anime illustration."),
 Prompt("Surreal Dreamscape","Art","/surrealism","Coherent surrealist dreamlike interpretation."),
 Prompt("Product Hero","Commercial","/producthero","Premium commercial product photography."),
 Prompt("Vintage Photo","Photography","/vintage","Faded tones and subtle analogue grain.")
)
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{PromptPixApp()}} }
@Composable fun PromptPixApp(){
 var selected by remember{mutableStateOf(starterPrompts.first())}
 var category by remember{mutableStateOf("All")}
 var photoChosen by remember{mutableStateOf(false)}
 val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){photoChosen=it!=null}
 val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()){photoChosen=it!=null}
 val cats=listOf("All")+starterPrompts.map{it.category}.distinct()
 val list=starterPrompts.filter{category=="All"||it.category==category}
 MaterialTheme(colorScheme=darkColorScheme()){
  Scaffold { padding-> LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Text("PROMPTPIX AI",style=MaterialTheme.typography.labelLarge);Text("Your photo. A thousand possibilities.",style=MaterialTheme.typography.headlineSmall);Text("Free • Offline-first • No account")}
   item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={gallery.launch("image/*")}){Text("Gallery")};OutlinedButton(onClick={camera.launch(null)}){Text("Camera")}}}
   item{Text(if(photoChosen)"Photo selected" else "Select a photo to begin")}
   item{Text("Categories",style=MaterialTheme.typography.titleLarge);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){cats.forEach{c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})}}}
   items(list){p->Card(onClick={selected=p},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(p.title,style=MaterialTheme.typography.titleMedium);Text("${p.category} • ${p.command}");Text(p.instruction,style=MaterialTheme.typography.bodySmall)}}}
   item{Text("Selected: ${selected.title}",style=MaterialTheme.typography.titleLarge);Text("AI image-to-image inference is not connected yet. This prototype will not fake an edited result.");Text("Prompt: ${selected.instruction}")}
  }}
 }
}
