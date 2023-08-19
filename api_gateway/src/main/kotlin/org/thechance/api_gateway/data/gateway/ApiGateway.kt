package org.thechance.api_gateway.data.gateway

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import org.koin.core.annotation.Single
import org.thechance.api_gateway.data.mappers.toManagedUser
import org.thechance.api_gateway.data.model.*
import org.thechance.api_gateway.data.model.identity.AddressResource
import org.thechance.api_gateway.data.model.identity.PermissionResource
import org.thechance.api_gateway.data.model.identity.UserManagementResource
import org.thechance.api_gateway.data.model.identity.UserResource
import org.thechance.api_gateway.endpoints.IApiGateway
import org.thechance.api_gateway.plugins.TokenType
import org.thechance.api_gateway.util.APIS
import java.util.*


@Single(binds = [IApiGateway::class])
class ApiGateway(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val resourcesGateway: IResourcesGateway,
    private val tokenManagementService: ITokenService
) : IApiGateway {


    // region identity
    override suspend fun createUser(
        fullName: String,
        username: String,
        password: String,
        email: String,
        locale: Locale
    ): Boolean {
        return tryToExecute<Boolean>(APIS.IDENTITY_API, locale) {
            submitForm("/user",
                formParameters = parameters {
                    append("fullName", fullName)
                    append("username", username)
                    append("password", password)
                    append("email", email)
                }
            )
        }

    }

    override suspend fun loginUser(
        userName: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
        locale: Locale
    ): UserTokens {
        tryToExecute<Boolean>(APIS.IDENTITY_API, locale) {
            submitForm("/user/login",
                formParameters = parameters {
                    append("username", userName)
                    append("password", password)
                }
            )
        }

        val user = getUserByUsername(userName)

        return generateUserTokens(user.id, user.permissions.map { it.id }, tokenConfiguration)
    }


    override suspend fun getUsers(
        page: Int,
        limit: Int,
        searchTerm: String,
        locale: Locale
    ): List<UserManagementResource> {
        return tryToExecute<List<UserManagementResource>>(APIS.IDENTITY_API, locale) {
            get("/users") {
                parameter("page", page)
                parameter("limit", limit)
                parameter("searchTerm", searchTerm)
            }
        }
    }

    override suspend fun getUserByUsername(username: String): UserManagement {
        return tryToExecute<UserManagementResource>(APIS.IDENTITY_API) {
            get("user/get-user") {
                parameter("username", username)
            }
        }.toManagedUser()
    }

    override suspend fun getUserById(id: String): UserResource {
        TODO("Not yet implemented")
    }


    override suspend fun deleteUser(id: String, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }


    override suspend fun deleteAddress(id: String, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }


    override suspend fun getAddress(id: String, locale: Locale): AddressResource {
        TODO("Not yet implemented")
    }

    override suspend fun getUserAddresses(userId: String, locale: Locale): List<AddressResource> {
        TODO("Not yet implemented")
    }

    override suspend fun subtractFromWallet(userId: String, amount: Double, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getWalletBalance(userId: String, locale: Locale): Double {
        TODO("Not yet implemented")
    }

    override suspend fun addToWallet(userId: String, amount: Double, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getPermission(permissionId: Int, locale: Locale): PermissionResource {
        TODO("Not yet implemented")
    }

    override suspend fun deletePermission(permissionId: Int, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getListOfPermission(locale: Locale): List<PermissionResource> {
        TODO("Not yet implemented")
    }

    override suspend fun addPermissionToUser(userId: String, permissionId: Int, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removePermissionFromUser(userId: String, permissionId: Int, locale: Locale): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getUserPermissions(userId: String, locale: Locale): List<PermissionResource> {
        TODO("Not yet implemented")
    }

    // endregion

    override suspend fun generateUserTokens(
        userId: String,
        userPermissions: List<Int>,
        tokenConfiguration: TokenConfiguration,
    ): UserTokens {

        val accessTokenExpirationDate = getExpirationDate(tokenConfiguration.accessTokenExpirationTimestamp)
        val refreshTokenExpirationDate = getExpirationDate(tokenConfiguration.refreshTokenExpirationTimestamp)

        val refreshToken = generateUserToken(userId, userPermissions, tokenConfiguration, TokenType.REFRESH_TOKEN)
        val accessToken = generateUserToken(userId, userPermissions, tokenConfiguration, TokenType.ACCESS_TOKEN)

        return UserTokens(accessTokenExpirationDate.time, refreshTokenExpirationDate.time, accessToken, refreshToken)
    }

    private suspend fun getExpirationDate(timestamp: Long): Date {
        return Date(System.currentTimeMillis() + timestamp)
    }

    private suspend fun generateUserToken(
        userId: String,
        userPermissions: List<Int>,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ): String {
        val userIdClaim = TokenClaim("userId", userId)
        val claims = userPermissions.map { TokenClaim("role", it.toString()) }
        val accessTokenClaim = TokenClaim("tokenType", tokenType.name)
        return tokenManagementService.generateToken(
            tokenConfiguration,
            userIdClaim,
            *claims.toTypedArray(),
            accessTokenClaim
        )
    }

    private suspend inline fun <reified T> tryToExecute(
        api: APIS,
        locale: Locale = Locale.ENGLISH,
        method: HttpClient.() -> HttpResponse
    ): T {
        attributes.put(AttributeKey("API"), api.value)
        val response = client.method()
        if (response.status.isSuccess()) {
            return response.body<T>()
        } else {
            val errorResponse = response.body<List<Int>>()
            val errorMessages = errorResponse.map {
                resourcesGateway.getLocalizedResponseMessage(it, locale = locale)
            }
            throw MultiLocalizedMessageException(errorMessages)
        }
    }
}
