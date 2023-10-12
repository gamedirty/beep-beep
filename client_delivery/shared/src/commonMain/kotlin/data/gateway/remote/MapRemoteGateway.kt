package data.gateway.remote

import data.remote.mapper.toTripEntity
import data.remote.model.BaseResponse
import data.remote.model.LocationDto
import data.remote.model.TripDto
import domain.entity.Trip
import domain.gateway.remote.IMapRemoteGateway
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.put
import io.ktor.http.Parameters
import io.ktor.util.InternalAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MapRemoteGateway(client: HttpClient) : IMapRemoteGateway,
    BaseRemoteGateway(client = client) {
    override suspend fun getOrders(): Flow<Trip> {
        return client.tryToExecuteWebSocket<BaseResponse<Trip>>("/trip/delivery")
            .map { baseResponse ->
                if (baseResponse.isSuccess) {
                    baseResponse.value
                        ?: throw IllegalStateException("Value is null in successful response")
                } else {
                    throw Exception()
                }
            }
    }

    override suspend fun sendLocation(location: LocationDto, tripId: String) {
        client.tryToExecuteWebSocket<Trip>("/location/sender/$tripId")
    }

    @OptIn(InternalAPI::class)
    override suspend fun acceptOrder(taxiId: String, tripId: String, driverId: String): Trip {
        val result = tryToExecute<BaseResponse<TripDto>> {
            val formData = FormDataContent(Parameters.build {
                append("tripId", tripId)
                append("taxiId", taxiId)
                append("driverId", driverId)
            })
            put("/trip/approve") {
                body = formData
            }
        }.value ?: throw Exception()

        return result.toTripEntity()
    }

    @OptIn(InternalAPI::class)
    override suspend fun updateOrderAsReceived(tripId: String, driverId: String): Trip {
        val result = tryToExecute<BaseResponse<TripDto>> {
            val formData = FormDataContent(Parameters.build {
                append("tripId", tripId)
                append("driverId", driverId)
            })
            put("/trip/received") {
                body = formData
            }
        }.value ?: throw Exception()

        return result.toTripEntity()
    }

    @OptIn(InternalAPI::class)
    override suspend fun updateOrderAsDelivered(tripId: String, driverId: String): Trip {
         val result = tryToExecute<BaseResponse<TripDto>> {
            val formData = FormDataContent(Parameters.build {
                append("tripId", tripId)
                append("driverId", driverId)
            })
            put("/trip/finish") {
                body = formData
            }
        }.value ?: throw Exception()

        return result.toTripEntity()
    }

}