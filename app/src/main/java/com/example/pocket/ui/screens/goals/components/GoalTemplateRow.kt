package com.example.pocket.ui.screens.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.GoalTemplate
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Radius
import com.example.pocket.ui.theme.Spacing

@Composable
fun GoalTemplateRow(
    template: GoalTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Radius.xl),
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(Radius.xl),
            )
            .clickable(onClick = onClick)
            .padding(Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(template.accentColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = template.emoji, fontSize = 22.sp)
        }
        Column(modifier = Modifier.padding(start = Spacing.sm)) {
            Text(
                text = template.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = template.subtitle,
                style = PocketType.timeIndicator,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}