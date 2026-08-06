package com.example.documentscanner.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.documentscanner.ui.theme.DocVaultColors

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isPlaceholder: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.DocumentList.route, "Vault", Icons.Outlined.FolderOpen),
    BottomNavItem(Screen.Exports.route, "Exports", Icons.Filled.Upload),
    BottomNavItem("profile", "Profile", Icons.Filled.Person, isPlaceholder = true)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = DocVaultColors.CardSurface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!item.isPlaceholder && currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DocVaultColors.ElectricIndigo,
                    selectedTextColor = DocVaultColors.ElectricIndigo,
                    unselectedIconColor = DocVaultColors.TextTertiary,
                    unselectedTextColor = DocVaultColors.TextTertiary,
                    indicatorColor = DocVaultColors.ElectricIndigo.copy(alpha = 0.12f)
                )
            )
        }
    }
}