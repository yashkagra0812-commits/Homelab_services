package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpInputBoxes(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    onComplete: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .testTag("otp_input_container")
            .clickable { focusRequester.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        // Invisible real text field capturing input
        BasicTextField(
            value = otpValue,
            onValueChange = { newValue ->
                val digitsOnly = newValue.filter { it.isDigit() }.take(length)
                onOtpChange(digitsOnly)
                if (digitsOnly.length == length) {
                    onComplete()
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpValue.length == length) {
                        onComplete()
                    }
                }
            ),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .testTag("hidden_otp_field"),
            decorationBox = { /* Invisible behind our custom boxes */ }
        )

        // Visible individual boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until length) {
                val char = otpValue.getOrNull(i)?.toString() ?: ""
                val isFocused = otpValue.length == i || (i == length - 1 && otpValue.length == length)

                val borderColor = when {
                    isFocused -> MaterialTheme.colorScheme.primary
                    char.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                val backgroundColor = when {
                    isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    char.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(56.dp)
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("otp_digit_box_$i"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
