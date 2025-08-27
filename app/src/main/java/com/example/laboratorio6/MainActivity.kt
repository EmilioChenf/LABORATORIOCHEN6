package com.example.laboratorio6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laboratorio6.ui.theme.LABORATORIO6Theme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LABORATORIO6Theme {
                CounterScaffold(title = "Emilio Josue Chen Borrayo")
            }
        }
    }
}

/* -------------------- MODELO PARA HISTORIAL --------------------- */
data class HistoryEntry(
    val valueAfterChange: Int,
    val isIncrement: Boolean
)

/* -------------------- UI PRINCIPAL --------------------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CounterScaffold(title: String) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    var totalIncrements by rememberSaveable { mutableIntStateOf(0) }
    var totalDecrements by rememberSaveable { mutableIntStateOf(0) }
    var maxValue by rememberSaveable { mutableIntStateOf(0) }
    var minValue by rememberSaveable { mutableIntStateOf(0) }
    var hasChanges by rememberSaveable { mutableStateOf(false) }

    // ✅ Solución simple: historial con remember (sin saver personalizado)
    val history: SnapshotStateList<HistoryEntry> = remember { mutableStateListOf<HistoryEntry>() }

    fun registerChange(newValue: Int, isIncrement: Boolean) {
        count = newValue
        if (isIncrement) totalIncrements++ else totalDecrements++
        if (!hasChanges) {
            maxValue = newValue
            minValue = newValue
            hasChanges = true
        } else {
            maxValue = max(maxValue, newValue)
            minValue = min(minValue, newValue)
        }
        history.add(HistoryEntry(newValue, isIncrement))
    }

    fun onIncrement() = registerChange(count + 1, true)
    fun onDecrement() = registerChange(count - 1, false)
    fun onReset() {
        count = 0
        totalIncrements = 0
        totalDecrements = 0
        maxValue = 0
        minValue = 0
        hasChanges = false
        history.clear()
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { onReset() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        content = { Text("Reiniciar", color = MaterialTheme.colorScheme.onPrimary) }
                    )
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .widthIn(max = 360.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Title(title)
                    Spacer(Modifier.height(12.dp))
                    CounterSection(
                        value = count,
                        onMinus = { onDecrement() },
                        onPlus = { onIncrement() }
                    )
                    Spacer(Modifier.height(16.dp))
                    StatsSection(
                        totalIncrements = totalIncrements,
                        totalDecrements = totalDecrements,
                        maxValue = if (hasChanges) maxValue else 0,
                        minValue = if (hasChanges) minValue else 0,
                        totalChanges = totalIncrements + totalDecrements
                    )
                    Spacer(Modifier.height(12.dp))
                    HistorySection(items = history, maxColumns = 5)
                }
            }
        }
    }
}

/* -------------------- COMPONENTES --------------------- */

@Composable
fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun CounterSection(value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        RoundIconButton("−", onMinus)
        Text(
            text = value.toString(),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        RoundIconButton("+", onPlus)
    }
}

@Composable
fun RoundIconButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StatsSection(
    totalIncrements: Int,
    totalDecrements: Int,
    maxValue: Int,
    minValue: Int,
    totalChanges: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StatRow("Total incrementos:", totalIncrements)
        StatRow("Total decrementos:", totalDecrements)
        StatRow("Valor máximo:", maxValue)
        StatRow("Valor mínimo:", minValue)
        StatRow("Total cambios:", totalChanges)
        Text("Historial:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun StatRow(label: String, value: Int) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value.toString())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistorySection(items: List<HistoryEntry>, maxColumns: Int) {
    FlowRow(
        maxItemsInEachRow = maxColumns,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { entry -> HistoryChip(entry) }
    }
}

@Composable
fun HistoryChip(entry: HistoryEntry) {
    val bg = if (entry.isIncrement) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(entry.valueAfterChange.toString(), color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

/* -------------------- PREVIEW --------------------- */
@Preview(showBackground = true)
@Composable
fun PreviewCounter() {
    LABORATORIO6Theme {
        CounterScaffold(title = "Emilio Josue Chen")
    }
}
