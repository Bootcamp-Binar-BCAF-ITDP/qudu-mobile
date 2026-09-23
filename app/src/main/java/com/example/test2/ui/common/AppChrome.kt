package com.example.test2.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.R
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.navigation.TopLevelDestination


private val GreenPill = com.example.test2.ui.theme.Brand200
private val RedDot = com.example.test2.ui.theme.StatusDanger


@Composable
fun QuDuTopBar(
    unreadCount: Long = 0,
    showActions: Boolean = true,
    onNotifications: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "QuickDuit",
                modifier = Modifier.height(36.dp),
            )
            Spacer(Modifier.weight(1f))

            if (showActions) {
                Box {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onNotifications),
                    )
                    if (unreadCount > 0) {
                        UnreadBadge(
                            count = unreadCount,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4).dp),
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(com.example.test2.ui.theme.BrandHighlight)
                        .border(1.dp, com.example.test2.ui.theme.BrandFieldBorder, CircleShape)
                        .clickable(onClick = onProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        HairLine()
    }
}

@Composable
fun UnreadBadge(count: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(RedDot)
            .padding(horizontal = if (count > 9) 5.dp else 0.dp)
            .size(if (count > 9) 18.dp else 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun QuDuBottomBar(
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
) {
    Column {
        HairLine()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isSelected) { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GreenPill else Color.Transparent)
                            .padding(horizontal = 22.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) Green else TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
fun HairLine(color: Color = CardBorder) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}
