package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val SUDAN_STATES = listOf(
    "الخرطوم", "الجزيرة", "النيل الأبيض", "النيل الأزرق",
    "القضارف", "كسلا", "البحر الأحمر", "نهر النيل",
    "الشمالية", "شمال كردفان", "جنوب كردفان", "غرب كردفان",
    "شمال دارفور", "جنوب دارفور", "وسط دارفور",
    "شرق دارفور", "غرب دارفور", "سنار"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationStep(
    selectedState: String?,
    city: String,
    schoolName: String,
    onStateSelected: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "أين تدرس؟",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "يساعدنا هذا على فهم احتياجات طلابنا",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── State dropdown ────────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedState ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("الولاية") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                SUDAN_STATES.forEach { state ->
                    DropdownMenuItem(
                        text = { Text(state) },
                        onClick = {
                            onStateSelected(state)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── City ──────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("المدينة (اختياري)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── School name ───────────────────────────────────────────────────────
        OutlinedTextField(
            value = schoolName,
            onValueChange = onSchoolNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("اسم المدرسة (اختياري)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("رجوع") }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f).height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = selectedState != null
            ) {
                Text(
                    text = "التالي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}