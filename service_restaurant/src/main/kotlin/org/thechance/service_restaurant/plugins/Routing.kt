package org.thechance.service_restaurant.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.thechance.service_restaurant.api.endpoints.addressRoutes
import org.thechance.service_restaurant.api.endpoints.categoryRoutes
import org.thechance.service_restaurant.api.endpoints.restaurantRoutes

fun Application.configureRouting(
) {
    routing {
        restaurantRoutes()
        categoryRoutes()
        addressRoutes()
    }
}
