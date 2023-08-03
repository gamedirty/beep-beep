package org.thechance.service_identity.domain.usecases.user

import org.thechance.service_identity.domain.entity.User

interface GetDetailedUsersUseCase {
    suspend operator fun invoke(): List<User>
}