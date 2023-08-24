package presentation.meals

sealed class MealsScreenUIEffect {

    object Back : MealsScreenUIEffect()


    object NavigateToAddMeal : MealsScreenUIEffect()

}
