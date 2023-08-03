package org.thechance.service_restaurant.usecase.meal

import io.ktor.server.plugins.*
import org.koin.core.annotation.Single
import org.thechance.service_restaurant.entity.Meal
import org.thechance.service_restaurant.data.gateway.MealGateway

@Single
class GetMealByIdUseCaseImp(private val mealGateway: MealGateway) : GetMealByIdUseCase {
    override suspend fun invoke(id: String): Meal = mealGateway.getMealById(id) ?: throw NotFoundException()

}