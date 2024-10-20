package com.infinitepower.newquiz.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infinitepower.newquiz.core.common.annotation.compose.PreviewNightLight
import com.infinitepower.newquiz.core.navigation.NavigationItem
import com.infinitepower.newquiz.core.theme.NewQuizTheme
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.launch

/**
 * Container with navigation bar and modal drawer
 */
@Composable
@ExperimentalMaterial3Api
internal fun CompactContainer(
    navController: NavController,
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem.Item?,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()

    val primaryItems = remember(key1 = navigationItems) {
        navigationItems
            .filterIsInstance<NavigationItem.Item>()
            .filter { it.primary }
    }

    val text = selectedItem?.text?.let { id ->
        stringResource(id = id)
    } ?: "NewQuiz"

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                modifier = Modifier.fillMaxHeight(),
                permanent = false,
                selectedItem = selectedItem,
                items = navigationItems,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    navController.navigate(item.direction)
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = text)
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Open menu"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    primaryItems.forEach { item ->
                        NavigationBarItem(
                            selected = item == selectedItem,
                            onClick = { navController.navigate(item.direction) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(id = item.text)
                                )
                            }
                        )
                    }
                }
            },
            content = content
        )
    }
}

@Composable
@PreviewNightLight
@OptIn(ExperimentalMaterial3Api::class)
private fun CompactContainerPreview() {
    val selectedItem = navigationItems
        .filterIsInstance<NavigationItem.Item>()
        .firstOrNull()

    NewQuizTheme {
        Surface {
            CompactContainer(
                navController = rememberNavController(),
                content = {
                    Text(text = "NewQuiz")
                },
                navigationItems = navigationItems,
                selectedItem = selectedItem
            )
        }
    }
}