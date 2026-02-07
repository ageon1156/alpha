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
package org.meshtastic.core.navigation

import kotlinx.serialization.Serializable

const val DEEP_LINK_BASE_URI = "meshtastic://meshtastic"

interface Route

interface Graph : Route

object ContactsRoutes {
    @Serializable data object ContactsGraph : Graph

    @Serializable data object Contacts : Route

    @Serializable data class Messages(val contactKey: String, val message: String = "") : Route

    @Serializable data class Share(val message: String) : Route

    @Serializable data object QuickChat : Route
}

object MapRoutes {
    @Serializable data class Map(val waypointId: Int? = null) : Route
}

object EmergencyRoutes {
    @Serializable data object EmergencyGraph : Graph

    @Serializable data object EmergencyHome : Route

    @Serializable data class EmergencyTopic(val section: String, val topicId: String) : Route
}

object SOSRoutes {
    @Serializable data object SOSGraph : Graph

    @Serializable data object SOSHome : Route
}

