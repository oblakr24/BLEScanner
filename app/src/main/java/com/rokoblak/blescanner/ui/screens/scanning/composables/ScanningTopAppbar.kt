package com.rokoblak.blescanner.ui.screens.scanning.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.rokoblak.blescanner.R


const val TAG_NAV_BUTTON = "tag-nav-button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanningTopAppbar(
    onNavIconClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = stringResource(id = R.string.appbar_title),
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.semantics {
                    testTag = TAG_NAV_BUTTON
                },
                onClick = {
                    onNavIconClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Open Navigation Drawer"
                )
            }
        }
    )
}
