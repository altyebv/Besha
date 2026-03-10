package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.user.domain.model.StudentGrade

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
    schoolNameError: String?,
    isHomeSchooled: Boolean,
    selectedGrade: StudentGrade?,
    onStateSelected: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onHomeSchooledChanged: (Boolean) -> Unit,
    onGradeSelected: (StudentGrade) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.height(40.dp))

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
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                SUDAN_STATES.forEach { state ->
                    DropdownMenuItem(
                        text = { Text(state) },
                        onClick = { onStateSelected(state); dropdownExpanded = false }
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
            value = if (isHomeSchooled) "تعليم منزلي" else schoolName,
            onValueChange = onSchoolNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("اسم المدرسة *") },
            isError = schoolNameError != null,
            supportingText = schoolNameError?.let { { Text(it) } },
            enabled = !isHomeSchooled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ── Homeschool toggle ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHomeSchooledChanged(!isHomeSchooled) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "طالب في تعليم منزلي",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = isHomeSchooled,
                onCheckedChange = onHomeSchooledChanged
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Grade ─────────────────────────────────────────────────────────────
        Text(
            text = "الصف الدراسي *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudentGrade.entries.forEach { g ->
                val selected = selectedGrade == g
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onGradeSelected(g) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = g.displayAr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Navigation ────────────────────────────────────────────────────────
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
                enabled = selectedState != null && selectedGrade != null &&
                        (isHomeSchooled || schoolName.isNotBlank())
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