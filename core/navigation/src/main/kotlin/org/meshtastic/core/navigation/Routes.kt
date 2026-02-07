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
