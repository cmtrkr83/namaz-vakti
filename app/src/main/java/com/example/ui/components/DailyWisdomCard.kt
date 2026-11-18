package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.local.DailyWisdom
import com.example.ui.theme.AppSerifFamily
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmGoldAccent

@Composable
fun DailyWisdomCard(
    wisdom: DailyWisdom,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                clip = false,
                ambientColor = Color(0x1C172A22),
                spotColor = Color(0x24172A22)
            )
            .testTag("daily_wisdom_card"),
        shape = cardShape,
        color = SageCardSurface,
        border = BorderStroke(0.8.dp, Color(0xB3FFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        tint = WarmGoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = wisdom.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmGoldAccent
                    )
                }

                val clipLabel = stringResource(R.string.daily_wisdom_clip_label)
                val copiedToast = stringResource(R.string.toast_text_copied)
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(clipLabel, "${wisdom.text}\n— ${wisdom.source}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_wisdom_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.cd_copy),
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            wisdom.arabic?.let { arabicText ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = arabicText,
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    fontFamily = AppSerifFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${wisdom.text}”",
                fontFamily = AppSerifFamily,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 23.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = wisdom.source,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
