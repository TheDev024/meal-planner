import entity.Meal

class MealPlanner {
    private val meals: MutableList<Meal> = mutableListOf()

    fun menu() {
        while (true) {
            when (getInput(
                "What would you like to do (add, show, exit)?",
                listOf("add", "show", "exit"),
                errorMessage = ""
            )) {
                "add" -> add()

                "show" -> show()

                "exit" -> {
                    println("Bye!")
                    break
                }
            }
        }
    }

    private fun show() {
        println(
            if (meals.isEmpty()) "No meals saved. Add a meal first."
            else meals.joinToString("\n\n", "\n", "\n")
        )
    }

    private fun add() {
        val category = getInput(
            "Which meal do you want to add (breakfast, lunch, dinner)?",
            listOf("breakfast", "lunch", "dinner"),
            errorMessage = "Wrong meal category! Choose from: breakfast, lunch, dinner."
        )
        val name = getInput(
            "Input the meal's name:",
            "^[a-zA-Z\\s]+\$".toRegex(),
            true,
            "Wrong format. Use letters only!"
        )
        println()
        val ingredients = getInput(
            "Input the ingredients:",
            "(\\s*$WORD_REGEX\\s*)+(,(\\s*$WORD_REGEX\\s*)+)*".toRegex(),
            caseSensitive = true,
            errorMessage = "Wrong format. Use letters only!"
        ).split("\\s*,\\s*".toRegex())
        val meal = Meal(category, name, ingredients)
        meals.add(meal)
        println("The meal has been added!")
    }
}
