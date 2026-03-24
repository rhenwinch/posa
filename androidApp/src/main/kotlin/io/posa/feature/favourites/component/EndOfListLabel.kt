package io.posa.feature.favourites.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
internal fun EndOfListLabel() {
    Text(
        text = "— You've seen them all —",
        style = MaterialTheme.typography.labelSmall,
        color = LocalTextStyle.current.color.copy(alpha = 0.6f),
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .wrapContentSize(Alignment.Center),
    )
}