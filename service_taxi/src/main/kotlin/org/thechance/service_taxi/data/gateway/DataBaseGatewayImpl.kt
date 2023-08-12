package org.thechance.service_taxi.data.gateway

import com.mongodb.client.model.Updates
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.thechance.service_taxi.api.dto.taxi.toCollection
import org.thechance.service_taxi.api.dto.taxi.toEntity
import org.thechance.service_taxi.api.dto.trip.toCollection
import org.thechance.service_taxi.api.dto.trip.toEntity
import org.thechance.service_taxi.data.DataBaseContainer
import org.thechance.service_taxi.data.collection.TaxiCollection
import org.thechance.service_taxi.data.collection.TripCollection
import org.thechance.service_taxi.data.utils.isSuccessfullyUpdated
import org.thechance.service_taxi.data.utils.paginate
import org.thechance.service_taxi.domain.entity.Taxi
import org.thechance.service_taxi.domain.entity.Trip
import org.thechance.service_taxi.domain.gateway.DataBaseGateway

class DataBaseGatewayImpl(private val container: DataBaseContainer) : DataBaseGateway {
    // region taxi curd
    override suspend fun addTaxi(taxi: Taxi): Boolean {
        return container.taxiCollection.insertOne(taxi.toCollection()).wasAcknowledged()
    }

    override suspend fun getTaxiById(taxiId: String): Taxi? {
        return container.taxiCollection.findOneById(ObjectId(taxiId))
            ?.takeIf { !it.isDeleted }?.toEntity()
    }

    override suspend fun getAllTaxes(page: Int, limit: Int): List<Taxi> {
        return container.taxiCollection.find(TaxiCollection::isDeleted ne true)
            .paginate(page, limit).toList().toEntity()
    }

    override suspend fun deleteTaxi(taxiId: String): Boolean {
        return container.taxiCollection.updateOneById(
            id = ObjectId(taxiId),
            update = set(TaxiCollection::isDeleted setTo true),
            updateOnlyNotNullProperties = true
        ).isSuccessfullyUpdated()
    }
    //endregion

    //region trip curd
    override suspend fun addTrip(trip: Trip): Boolean {
        return container.tripCollection.insertOne(trip.toCollection()).wasAcknowledged()
    }

    override suspend fun getTripById(tripId: String): Trip? {
        return container.tripCollection.findOne(TripCollection::isDeleted ne true)?.toEntity()
    }

    override suspend fun getAllTrips(page: Int, limit: Int): List<Trip> {
        return container.tripCollection.find(TripCollection::isDeleted ne true)
            .paginate(page, limit).toList()
            .toEntity()
    }

    override suspend fun getDriverTripsHistory(
        driverId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return container.tripCollection.find(
            and(
                TripCollection::isDeleted ne true,
                TripCollection::driverId eq ObjectId(driverId)
            )
        ).paginate(page, limit).toList().toEntity()
    }

    override suspend fun getClientTripsHistory(
        clientId: String,
        page: Int,
        limit: Int
    ): List<Trip> {
        return container.tripCollection.find(
            and(
                TripCollection::isDeleted ne true,
                TripCollection::clientId eq ObjectId(clientId)
            )
        ).paginate(page, limit).toList().toEntity()
    }

    override suspend fun deleteTrip(tripId: String): Boolean {
        return container.tripCollection.updateOneById(
            id = ObjectId(tripId),
            update = Updates.set(TripCollection::isDeleted.name, true)
        ).isSuccessfullyUpdated()
    }

    override suspend fun approveTrip(tripId: String, taxiId: String, driverId: String): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq ObjectId(tripId),
            ),
            update = Updates.combine(
                Updates.set(TripCollection::taxiId.name, ObjectId(taxiId)),
                Updates.set(TripCollection::driverId.name, ObjectId(driverId)),
                Updates.set(
                    TripCollection::startDate.name, Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault()
                    )
                )
            )
        )?.toEntity()
    }

    override suspend fun finishTrip(tripId: String, driverId: String): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq ObjectId(tripId),
                TripCollection::driverId eq ObjectId(driverId),
            ),
            update = Updates.set(
                TripCollection::endDate.name, Clock.System.now().toLocalDateTime(
                    TimeZone.currentSystemDefault()
                )
            )
        )?.toEntity()
    }

    override suspend fun rateTrip(tripId: String, rate: Double): Trip? {
        return container.tripCollection.findOneAndUpdate(
            filter = and(
                TripCollection::isDeleted ne true,
                TripCollection::id eq ObjectId(tripId),
            ),
            update = Updates.set(TripCollection::rate.name, rate)
        )?.toEntity()
    }
    //endregion
}