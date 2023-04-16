import entity.Meal

class MealPlanner {
    private val meals: MutableList<Meal> = mutableListOf()

    fun addMeal() {
        println("Which meal do you want to add (breakfast, lunch, dinner)?")
        val category = scanner.nextLine()
        println("Input the meal's name:")
        val name = scanner.nextLine()
        println("Input the ingredients:")
        val ingredients = scanner.nextLine().split(", ")
        val meal = Meal(category, name, ingredients)
        meals.add(meal)
        println("\n$meal\nThe meal has been added!")
    }
}
