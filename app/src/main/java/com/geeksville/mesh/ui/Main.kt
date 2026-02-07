/*
 * Copyright (c) 2025-2026 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("MatchingDeclarationName")

package com.geeksville.mesh.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.recalculateWindowInsets
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.geeksville.mesh.BuildConfig
import com.geeksville.mesh.model.BTScanModel
import com.geeksville.mesh.model.UIViewModel
import com.geeksville.mesh.navigation.contactsGraph
import com.geeksville.mesh.navigation.emergencyGraph
import com.geeksville.mesh.navigation.sosGraph
import com.geeksville.mesh.navigation.mapGraph
import com.geeksville.mesh.service.MeshService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.meshtastic.core.model.DeviceVersion
import org.meshtastic.core.navigation.ContactsRoutes
import org.meshtastic.core.navigation.EmergencyRoutes
import org.meshtastic.core.navigation.MapRoutes
import org.meshtastic.core.navigation.SOSRoutes
import org.meshtastic.core.navigation.Route
import org.meshtastic.core.service.ConnectionState
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.app_too_old
import org.meshtastic.core.strings.client_notification
import org.meshtastic.core.strings.close
import org.meshtastic.core.strings.compromised_keys
import org.meshtastic.core.strings.conversations
import org.meshtastic.core.strings.emergency_help
import org.meshtastic.core.strings.firmware_old
import org.meshtastic.core.strings.firmware_too_old
import org.meshtastic.core.strings.map
import org.meshtastic.core.strings.must_update
import org.meshtastic.core.strings.okay
import org.meshtastic.core.strings.should_update
import org.meshtastic.core.strings.should_update_firmware
import org.meshtastic.core.strings.sos
import org.meshtastic.core.ui.component.MultipleChoiceAlertDialog
import org.meshtastic.core.ui.component.ScrollToTopEvent
import org.meshtastic.core.ui.component.SimpleAlertDialog
import org.meshtastic.core.ui.qr.ScannedQrCodeDialog
import org.meshtastic.core.ui.share.SharedContactDialog

enum class TopLevelDestination(val label: StringResource, val icon: ImageVector, val route: Route) {
    Map(Res.string.map, Icons.Outlined.Explore, MapRoutes.Map()),
    Conversations(Res.string.conversations, Icons.Outlined.ChatBubbleOutline, ContactsRoutes.ContactsGraph),
    Emergency(Res.string.emergency_help, Icons.Outlined.HealthAndSafety, EmergencyRoutes.EmergencyGraph),
    SOS(Res.string.sos, Icons.Outlined.Sos, SOSRoutes.SOSGraph),
    ;

    companion object {
        fun fromNavDestination(destination: NavDestination?): TopLevelDestination? =
            entries.find { dest -> destination?.hierarchy?.any { it.hasRoute(dest.route::class) } == true }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun MainScreen(uIViewModel: UIViewModel = hiltViewModel(), scanModel: BTScanModel = hiltViewModel()) {
    val navController = rememberNavController()
    val connectionState by uIViewModel.connectionState.collectAsStateWithLifecycle()
    val requestChannelSet by uIViewModel.requestChannelSet.collectAsStateWithLifecycle()
    val sharedContactRequested by uIViewModel.sharedContactRequested.collectAsStateWithLifecycle()
    val unreadMessageCount by uIViewModel.unreadMessageCount.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(connectionState, notificationPermissionState) {
            if (connectionState == ConnectionState.Connected && !notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    if (connectionState == ConnectionState.Connected) {
        sharedContactRequested?.let {
            SharedContactDialog(sharedContact = it, onDismiss = { uIViewModel.clearSharedContactRequested() })
        }

        requestChannelSet?.let { newChannelSet ->
            ScannedQrCodeDialog(newChannelSet, onDismiss = { uIViewModel.clearRequestChannelUrl() })
        }
    }

    uIViewModel.AddNavigationTrackingEffect(navController)

    VersionChecks(uIViewModel)
    val alertDialogState by uIViewModel.currentAlert.collectAsStateWithLifecycle()
    alertDialogState?.let { state ->
        if (state.choices.isNotEmpty()) {
            MultipleChoiceAlertDialog(
                title = state.title,
                message = state.message,
                choices = state.choices,
                onDismissRequest = { state.onDismiss?.let { it() } },
            )
        } else {
            SimpleAlertDialog(
                title = state.title,
                message = state.message,
                html = state.html,
                onConfirmRequest = { state.onConfirm?.let { it() } },
                onDismissRequest = { state.onDismiss?.let { it() } },
            )
        }
    }

    val clientNotification by uIViewModel.clientNotification.collectAsStateWithLifecycle()
    clientNotification?.let { notification ->
        var message = notification.message
        val compromisedKeys =
            if (notification.hasLowEntropyKey() || notification.hasDuplicatedPublicKey()) {
                message = stringResource(Res.string.compromised_keys)
                true
            } else {
                false
            }
        SimpleAlertDialog(
            title = Res.string.client_notification,
            text = { Text(text = message) },
            onConfirm = {
                uIViewModel.clearClientNotification(notification)
            },
            onDismiss = { uIViewModel.clearClientNotification(notification) },
        )
    }

    val navSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val topLevelDestination = TopLevelDestination.fromNavDestination(currentDestination)

    val minimalItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            indicatorColor = colorScheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            indicatorColor = colorScheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
    )

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = destination == topLevelDestination
                item(
                    colors = minimalItemColors,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (destination == TopLevelDestination.Conversations) {
                                    var lastNonZeroCount by remember { mutableIntStateOf(unreadMessageCount) }
                                    if (unreadMessageCount > 0) {
                                        lastNonZeroCount = unreadMessageCount
                                    }
                                    AnimatedVisibility(
                                        visible = unreadMessageCount > 0,
                                        enter = scaleIn() + fadeIn(),
                                        exit = scaleOut() + fadeOut(),
                                    ) {
                                        Badge { Text(lastNonZeroCount.toString()) }
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.label),
                            )
                        }
                    },
                    selected = isSelected,
                    label = {
                        Text(
                            text = stringResource(destination.label),
                            modifier =
                            if (navSuiteType == NavigationSuiteType.ShortNavigationBarCompact) {
                                Modifier.width(1.dp)
                                    .height(1.dp) // hide on phone - min 1x1 or talkback won't see it.
                            } else {
                                Modifier
                            },
                        )
                    },
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = MapRoutes.Map(),
            modifier = Modifier.fillMaxSize().recalculateWindowInsets().safeDrawingPadding().imePadding(),
        ) {
            contactsGraph(navController, uIViewModel.scrollToTopEventFlow)
            mapGraph(navController)
            emergencyGraph(navController)
            sosGraph(navController)
        }
    }
}

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun VersionChecks(viewModel: UIViewModel) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val myNodeInfo by viewModel.myNodeInfo.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val myFirmwareVersion = myNodeInfo?.firmwareVersion

    val firmwareEdition by viewModel.firmwareEdition.collectAsStateWithLifecycle(null)

    val latestStableFirmwareRelease by
        viewModel.latestStableFirmwareRelease.collectAsStateWithLifecycle(DeviceVersion("2.6.4"))
    LaunchedEffect(connectionState, firmwareEdition) {
        if (connectionState == ConnectionState.Connected) {
            firmwareEdition?.let { edition ->
                Logger.d { "FirmwareEdition: ${edition.name}" }
                when (edition) {
                    MeshProtos.FirmwareEdition.VANILLA -> {
                        // Handle any specific logic for VANILLA firmware edition if needed
                    }

                    else -> {
                        // Handle other firmware editions if needed
                    }
                }
            }
        }
    }

    // Check if the device is running an old app version or firmware version
    LaunchedEffect(connectionState, myNodeInfo) {
        if (connectionState == ConnectionState.Connected) {
            Logger.i {
                "[FW_CHECK] Connection state: $connectionState, " +
                    "myNodeInfo: ${if (myNodeInfo != null) "present" else "null"}, " +
                    "firmwareVersion: ${myFirmwareVersion ?: "null"}"
            }

            myNodeInfo?.let { info ->
                val isOld = info.minAppVersion > BuildConfig.VERSION_CODE && BuildConfig.DEBUG.not()
                Logger.d {
                    "[FW_CHECK] App version check - minAppVersion: ${info.minAppVersion}, " +
                        "currentVersion: ${BuildConfig.VERSION_CODE}, isOld: $isOld"
                }

                if (isOld) {
                    Logger.w { "[FW_CHECK] App too old - showing update prompt" }
                    viewModel.showAlert(
                        getString(Res.string.app_too_old),
                        getString(Res.string.must_update),
                        dismissable = false,
                        onConfirm = {
                            val service = viewModel.meshService ?: return@showAlert
                            MeshService.changeDeviceAddress(context, service, "n")
                        },
                    )
                } else {
                    myFirmwareVersion?.let { fwVersion ->
                        val curVer = DeviceVersion(fwVersion)
                        Logger.i {
                            "[FW_CHECK] Firmware version comparison - " +
                                "device: $curVer (raw: $fwVersion), " +
                                "absoluteMin: ${MeshService.absoluteMinDeviceVersion}, " +
                                "min: ${MeshService.minDeviceVersion}"
                        }

                        if (curVer < MeshService.absoluteMinDeviceVersion) {
                            Logger.w {
                                "[FW_CHECK] Firmware too old - " +
                                    "device: $curVer < absoluteMin: ${MeshService.absoluteMinDeviceVersion}"
                            }
                            val title = getString(Res.string.firmware_too_old)
                            val message = getString(Res.string.firmware_old)
                            viewModel.showAlert(
                                title = title,
                                html = message,
                                dismissable = false,
                                onConfirm = {
                                    val service = viewModel.meshService ?: return@showAlert
                                    MeshService.changeDeviceAddress(context, service, "n")
                                },
                            )
                        } else if (curVer < MeshService.minDeviceVersion) {
                            Logger.w {
                                "[FW_CHECK] Firmware should update - " +
                                    "device: $curVer < min: ${MeshService.minDeviceVersion}"
                            }
                            val title = getString(Res.string.should_update_firmware)
                            val message = getString(Res.string.should_update, latestStableFirmwareRelease.asString)
                            viewModel.showAlert(title = title, message = message, dismissable = false, onConfirm = {})
                        } else {
                            Logger.i { "[FW_CHECK] Firmware version OK - device: $curVer meets requirements" }
                        }
                    } ?: run { Logger.w { "[FW_CHECK] Firmware version is null despite myNodeInfo being present" } }
                }
            } ?: run { Logger.d { "[FW_CHECK] myNodeInfo is null, skipping firmware check" } }
        } else {
            Logger.d { "[FW_CHECK] Not connected (state: $connectionState), skipping firmware check" }
        }
    }
}

