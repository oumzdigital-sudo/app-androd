package com.example

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FormUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactFormUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `form components are visible and interactive`() {
        var nombreState = ""
        var correoState = ""
        var submitClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                ContactFormView(
                    uiState = FormUiState(
                        nombre = nombreState,
                        correo = correoState
                    ),
                    onNombreChange = { nombreState = it },
                    onCorreoChange = { correoState = it },
                    onSubmit = { submitClicked = true },
                    onReset = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Check EditTexts and Button exist and perform interaction
        composeTestRule.onNodeWithTag("editNombre", useUnmergedTree = true).performTextInput("Carlos Ruiz")
        composeTestRule.onNodeWithTag("editCorreo", useUnmergedTree = true).performTextInput("carlos@test.com")
        composeTestRule.onNodeWithTag("btnEnviar", useUnmergedTree = true).performClick()
    }

    @Test
    fun `shows custom feedback message when submitted`() {
        val testMessage = "¡Hola Carlos! Tu correo (carlos@test.com) ha sido registrado correctamente."

        composeTestRule.setContent {
            MyApplicationTheme {
                ContactFormView(
                    uiState = FormUiState(
                        nombre = "Carlos",
                        correo = "carlos@test.com",
                        mensajeFeedback = testMessage,
                        isSubmitted = true
                    ),
                    onNombreChange = {},
                    onCorreoChange = {},
                    onSubmit = {},
                    onReset = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("textMensaje", useUnmergedTree = true)
            .assertTextContains("¡Hola Carlos!", substring = true)
    }
}
