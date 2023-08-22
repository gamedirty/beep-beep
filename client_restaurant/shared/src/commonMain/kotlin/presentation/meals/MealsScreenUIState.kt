package presentation.meals

data class MealsScreenUIState(
    val cousin: List<CousinUIState> = emptyList(),
    val meals: List<MealUIState> = emptyList(),
    val selectedCousin: CousinUIState? = CousinUIState("", "All"),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
){
    data class CousinUIState(
        val id: String,
        val name: String,
        val isSelected: Boolean = false
    )
    data class  MealUIState(
        val id: String,
        val name: String,
        val price: Double,
        val imageUrl: String,
    )
}
