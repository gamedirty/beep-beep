package org.thechance.service_taxi.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.thechance.service_taxi.domain.entity.Color
import org.thechance.service_taxi.domain.entity.Location
import org.thechance.service_taxi.domain.entity.Taxi
import org.thechance.service_taxi.domain.entity.Trip
import org.thechance.service_taxi.domain.gateway.DataBaseGateway


object FakeGateway : DataBaseGateway {
    var taxes = mutableListOf(
        Taxi(
            id = "64d111a60f294c4b8f718973",
            plateNumber = "1234 ABC",
            color = Color.BLACK,
            type = "Sedan",
            driverId = "123456789123456789123471",
            isAvailable = true,
            seats = 4
        )
    )
    var trips = mutableListOf(
        Trip(
            id = "64d111a60f294c4b8f718973",
            taxiId = null,
            driverId = null,
            clientId = "123456789123456789123471",
            startPoint = Location(30.0, 150.0),
            destination = Location(50.0, 170.0),
            price = 100.0,
        )
    )

    override suspend fun addTaxi(taxi: Taxi): Boolean {
        taxes.add(taxi); return true
    }

    override suspend fun getTaxiById(taxiId: String): Taxi? {
        return taxes.find { it.id == taxiId }
    }

    override suspend fun getAllTaxes(page: Int, limit: Int): List<Taxi> {
        return taxes.toList()
    }

    override suspend fun deleteTaxi(taxiId: String): Boolean {
        return getTaxiById(taxiId)?.let { taxes.remove(it); true } ?: false
    }

    override suspend fun addTrip(trip: Trip): Boolean {
        trips.add(trip); return true
    }

    override suspend fun getTripById(tripId: String): Trip? {
        return trips.find { it.id == tripId }
    }

    override suspend fun getAllTrips(page: Int, limit: Int): List<Trip> {
        return trips.toList()
    }

    override suspend fun getDriverTripsHistory(
        driverId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return trips.filter { it.driverId == driverId }
    }

    override suspend fun getClientTripsHistory(
        clientId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return trips.filter { it.clientId == clientId }
    }

    override suspend fun deleteTrip(tripId: String): Boolean {
        getTripById(tripId)?.let { trips.remove(it); return true }; return false
    }

    override suspend fun approveTrip(tripId: String, taxiId: String, driverId: String): Trip? {
        val target = trips.find { it.id == tripId } ?: return null
        val position = trips.indexOf(target)
        trips.removeAt(position)
        trips.add(
            position,
            target.copy(
                taxiId = taxiId,
                driverId = driverId,
                startDate = Clock.System.now().toLocalDateTime(
                    TimeZone.currentSystemDefault()
                )
            )
        )
        return trips[position]
    }

    override suspend fun finishTrip(tripId: String, driverId: String): Trip? {
        val target = trips.find { it.id == tripId } ?: return null
        val position = trips.indexOf(target)
        trips.removeAt(position)
        trips.add(
            position,
            target.copy(
                endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )
        return trips[position]
    }

    override suspend fun rateTrip(tripId: String, rate: Double): Trip? {
        val target = trips.find { it.id == tripId } ?: return null
        val position = trips.indexOf(target)
        trips.removeAt(position)
        trips.add(
            position,
            target.copy(
                rate = rate
            )
        )
        return trips[position]
    }

    fun reset() {
        taxes = mutableListOf(
            Taxi(
                id = "64d111a60f294c4b8f718973",
                plateNumber = "1234 ABC",
                color = Color.BLACK,
                type = "Sedan",
                driverId = "123456789123456789123471",
                isAvailable = true,
                seats = 4
            )
        )
        trips = mutableListOf(
            Trip(
                id = "64d111a60f294c4b8f718973",
                taxiId = null,
                driverId = null,
                clientId = "123456789123456789123471",
                startPoint = Location(30.0, 150.0),
                destination = Location(50.0, 170.0),
                price = 100.0,
            )
        )
    }
}