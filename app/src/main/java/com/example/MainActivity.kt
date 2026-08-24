package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ContactSubmission
import com.example.data.model.AppLog
import com.example.data.model.LogLevel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ContactFormViewModel
import com.example.ui.viewmodel.FormUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val context = LocalContext.current.applicationContext as Application
          val viewModel: ContactFormViewModel = viewModel(
            factory = ContactFormViewModel.Factory(context)
          )
          MainAppScreen(
            viewModel = viewModel,
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
  viewModel: ContactFormViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val logs by viewModel.logs.collectAsState()
  val submissions by viewModel.savedSubmissions.collectAsState()
  val submissionCount by viewModel.submissionsCount.collectAsState()

  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var showInfoDialog by rememberSaveable { mutableStateOf(false) }

  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Navigation Tabs: Formulario, Persistencia Room, Log de Errores, Flujo Animado
      PrimaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Formulario", maxLines = 1) },
          icon = {
            Icon(Icons.Default.ContactMail, contentDescription = "Formulario")
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Room DB", maxLines = 1) },
          icon = {
            BadgedBox(
              badge = {
                if (submissionCount > 0) {
                  Badge { Text("$submissionCount") }
                }
              }
            ) {
              Icon(Icons.Default.Storage, contentDescription = "Persistencia")
            }
          }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Logs & Errores", maxLines = 1) },
          icon = {
            val errorCount = logs.count { it.level == LogLevel.ERROR }
            BadgedBox(
              badge = {
                if (errorCount > 0) {
                  Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text("$errorCount")
                  }
                }
              }
            ) {
              Icon(Icons.Default.BugReport, contentDescription = "Logs")
            }
          }
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Flujo Animado", maxLines = 1) },
          icon = {
            Icon(Icons.Default.PlayArrow, contentDescription = "Flujo")
          }
        )
      }

      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
          fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "TabTransition",
        modifier = Modifier.weight(1f)
      ) { targetIndex ->
        when (targetIndex) {
          0 -> ContactFormView(
            uiState = uiState,
            onNombreChange = viewModel::onNombreChange,
            onCorreoChange = viewModel::onCorreoChange,
            onSubmit = viewModel::submitForm,
            onReset = viewModel::resetForm
          )
          1 -> RoomDatabaseView(
            submissions = submissions,
            onDelete = viewModel::deleteSubmission,
            onClearAll = viewModel::clearAllSubmissions
          )
          2 -> LogsAndErrorsView(
            logs = logs,
            onClearLogs = viewModel::clearLogs
          )
          3 -> AnimatedFlowSimulationView()
        }
      }
    }
  }
}

@Composable
fun ContactFormView(
  uiState: FormUiState,
  onNombreChange: (String) -> Unit,
  onCorreoChange: (String) -> Unit,
  onSubmit: () -> Unit,
  onReset: () -> Unit,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .imePadding(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(24.dp)
        .widthIn(max = 480.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Hero Header
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ContactMail,
          contentDescription = "Ícono de contacto",
          modifier = Modifier.size(36.dp),
          tint = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Formulario de Contacto",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Ingresa tu nombre y correo para enviar y persistir tus datos",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      // Step Progress Indicator
      StepIndicator(activeStep = uiState.activeStep)

      Spacer(modifier = Modifier.height(20.dp))

      // Form Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // EditText para el nombre del usuario
          OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onNombreChange,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("editNombre"),
            label = { Text("Tu nombre") },
            placeholder = { Text("Ingresa tu nombre") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Nombre"
              )
            },
            trailingIcon = {
              if (uiState.nombre.isNotEmpty()) {
                IconButton(onClick = { onNombreChange("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Borrar nombre"
                  )
                }
              }
            },
            singleLine = true,
            isError = uiState.nombreError != null,
            supportingText = {
              uiState.nombreError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
              }
            },
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.Words,
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          // EditText para la dirección de correo
          OutlinedTextField(
            value = uiState.correo,
            onValueChange = onCorreoChange,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("editCorreo"),
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@correo.com") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Correo"
              )
            },
            trailingIcon = {
              if (uiState.correo.isNotEmpty()) {
                IconButton(onClick = { onCorreoChange("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Borrar correo"
                  )
                }
              }
            },
            singleLine = true,
            isError = uiState.correoError != null,
            supportingText = {
              uiState.correoError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
              }
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                focusManager.clearFocus()
                onSubmit()
              }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Button para enviar los datos
          Button(
            onClick = {
              focusManager.clearFocus()
              onSubmit()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .testTag("btnEnviar"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            if (uiState.isSubmitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Enviar",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // TextView para mostrar el mensaje personalizado
      if (uiState.mensajeFeedback.isNotEmpty()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Éxito",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = uiState.mensajeFeedback,
              fontSize = 16.sp,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("textMensaje")
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
              onClick = onReset,
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Nuevo envío / Limpiar")
            }
          }
        }
      } else {
        Text(
          text = "",
          fontSize = 16.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("textMensaje")
        )
      }
    }
  }
}

@Composable
fun StepIndicator(activeStep: Int) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    StepItem(step = 1, title = "1. Ingreso", isActive = activeStep >= 1)
    StepDivider(isDone = activeStep >= 2)
    StepItem(step = 2, title = "2. Validación", isActive = activeStep >= 2)
    StepDivider(isDone = activeStep >= 3)
    StepItem(step = 3, title = "3. Room DB", isActive = activeStep >= 3)
  }
}

@Composable
fun StepItem(step: Int, title: String, isActive: Boolean) {
  val bgColor by animateColorAsState(
    if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    label = "StepBg"
  )
  val textColor by animateColorAsState(
    if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "StepText"
  )

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(bgColor),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "$step",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = textColor
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun StepDivider(isDone: Boolean) {
  val color by animateColorAsState(
    if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
    label = "DividerColor"
  )
  Box(
    modifier = Modifier
      .width(36.dp)
      .height(2.dp)
      .background(color)
  )
}

@Composable
fun RoomDatabaseView(
  submissions: List<ContactSubmission>,
  onDelete: (ContactSubmission) -> Unit,
  onClearAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Persistencia Room DB",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${submissions.size} registros guardados localmente",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (submissions.isNotEmpty()) {
        TextButton(
          onClick = onClearAll,
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Vaciar")
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (submissions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No hay registros en la base de datos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Completa el formulario y presiona 'Enviar' para persistir los datos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(submissions, key = { it.id }) { submission ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = submission.nombre,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = submission.correo,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Registrado: ${submission.getFormattedDate()}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(onClick = { onDelete(submission) }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Eliminar",
                  tint = MaterialTheme.colorScheme.error
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun LogsAndErrorsView(
  logs: List<AppLog>,
  onClearLogs: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedFilter by rememberSaveable { mutableStateOf<LogLevel?>(null) }

  val filteredLogs = remember(logs, selectedFilter) {
    if (selectedFilter == null) logs else logs.filter { it.level == selectedFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Log de Errores y Eventos",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${logs.size} eventos registrados en tiempo real",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (logs.isNotEmpty()) {
        TextButton(onClick = onClearLogs) {
          Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Limpiar")
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterLogChip(
        label = "Todos",
        isSelected = selectedFilter == null,
        onClick = { selectedFilter = null }
      )
      FilterLogChip(
        label = "Errores",
        color = MaterialTheme.colorScheme.error,
        isSelected = selectedFilter == LogLevel.ERROR,
        onClick = { selectedFilter = if (selectedFilter == LogLevel.ERROR) null else LogLevel.ERROR }
      )
      FilterLogChip(
        label = "Alertas",
        color = Color(0xFFD97706),
        isSelected = selectedFilter == LogLevel.WARNING,
        onClick = { selectedFilter = if (selectedFilter == LogLevel.WARNING) null else LogLevel.WARNING }
      )
      FilterLogChip(
        label = "Éxito",
        color = Color(0xFF059669),
        isSelected = selectedFilter == LogLevel.SUCCESS,
        onClick = { selectedFilter = if (selectedFilter == LogLevel.SUCCESS) null else LogLevel.SUCCESS }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredLogs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No hay registros con el filtro seleccionado.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(filteredLogs, key = { it.id }) { log ->
          LogItemCard(log = log)
        }
      }
    }
  }
}

@Composable
fun FilterLogChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  color: Color = MaterialTheme.colorScheme.primary
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
      color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun LogItemCard(log: AppLog) {
  var isExpanded by rememberSaveable { mutableStateOf(false) }

  val (icon, tintColor) = when (log.level) {
    LogLevel.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
    LogLevel.WARNING -> Icons.Default.Warning to Color(0xFFD97706)
    LogLevel.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF059669)
    LogLevel.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded },
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = log.message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = log.getFormattedTime(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontFamily = FontFamily.Monospace
        )
      }

      if (log.details != null && isExpanded) {
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = log.details,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun AnimatedFlowSimulationView(modifier: Modifier = Modifier) {
  var currentStep by rememberSaveable { mutableIntStateOf(0) }
  var isPlaying by rememberSaveable { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()

  val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ScalePulse"
  )

  // Simulation steps
  val steps = listOf(
    FlowStepData(
      title = "Paso 1: Entrada de Datos",
      description = "El usuario escribe su nombre en 'editNombre' y correo en 'editCorreo'.",
      icon = Icons.Default.Person,
      sampleData = "Nombre: Carlos Ruiz\nCorreo: carlos.ruiz@ejemplo.com"
    ),
    FlowStepData(
      title = "Paso 2: Validación Reactiva",
      description = "El ViewModel valida la longitud del nombre y la estructura del email vía Regex.",
      icon = Icons.Default.CheckCircle,
      sampleData = "✓ Nombre válido (>= 2 caracteres)\n✓ Formato de correo válido (@dominio.com)"
    ),
    FlowStepData(
      title = "Paso 3: Persistencia en Room",
      description = "Se crea la entidad ContactSubmission y se inserta en SQLite mediante ContactSubmissionDao.",
      icon = Icons.Default.Storage,
      sampleData = "INSERT INTO contact_submissions VALUES(id=1, nombre='Carlos Ruiz', ...)"
    ),
    FlowStepData(
      title = "Paso 4: Mensaje Personalizado",
      description = "Se actualiza el TextView 'textMensaje' con animación y confirmación instantánea.",
      icon = Icons.AutoMirrored.Filled.Send,
      sampleData = "¡Hola Carlos Ruiz! Tu correo (carlos.ruiz@ejemplo.com) ha sido registrado."
    )
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Demostración de Flujo Animado",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      textAlign = TextAlign.Center
    )
    Text(
      text = "Simulación paso a paso del comportamiento de la aplicación",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    // Visual animated presentation card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .scale(if (isPlaying) pulseScale else 1.0f),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val activeData = steps[currentStep]

        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = activeData.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = activeData.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = activeData.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surface
        ) {
          Text(
            text = activeData.sampleData,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(12.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Step navigator buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Button(
        onClick = {
          currentStep = if (currentStep > 0) currentStep - 1 else steps.lastIndex
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Anterior")
      }

      Text(
        text = "${currentStep + 1} / ${steps.size}",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
      )

      Button(
        onClick = {
          currentStep = (currentStep + 1) % steps.size
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Siguiente")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Information on Version Control and CocoaPods note
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Arquitectura & Control de Versiones",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "• Android Gradle: Kotlin DSL con Version Catalog (libs.versions.toml).\n• Persistencia: Room con KSP.\n• Pruebas: Robolectric + Roborazzi para JVM y verificación visual.\n• Nota sobre CocoaPods: CocoaPods es el gestor de dependencias para iOS (Cocoa / Swift / Obj-C). En Android se utiliza Gradle con Version Catalog para gestión declarativa de dependencias.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

data class FlowStepData(
  val title: String,
  val description: String,
  val icon: ImageVector,
  val sampleData: String
)

@Preview(showBackground = true)
@Composable
fun ContactFormPreview() {
  MyApplicationTheme {
    ContactFormView(
      uiState = FormUiState(
        nombre = "María Gómez",
        correo = "maria@ejemplo.com",
        mensajeFeedback = "¡Hola María Gómez! Tu correo (maria@ejemplo.com) ha sido registrado correctamente."
      ),
      onNombreChange = {},
      onCorreoChange = {},
      onSubmit = {},
      onReset = {}
    )
  }
}
