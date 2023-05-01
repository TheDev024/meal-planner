import entity.Meal
import entity.MealRow
import query.InsertQuery
import query.SelectQuery
import query.TableQuery

class MealPlanner {
    private val manager = DataBaseManager("meals.db")

    init {
        manager.open()

        val tables = listOf(
            TableQuery(
                "meal",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "name TEXT",
                    "category INGREDIENT"
                )
            ),
            TableQuery(
                "ingredient",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "name TEXT"
                )
            ),
            TableQuery(
                "meal_ingredients",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "meal_id INTEGER",
                    "ingredient_id INTEGER"
                )
            )
        )

        tables.forEach { manager.createTable(it) }

        manager.close()
    }

    fun menu() {
        while (true) {
            manager.open()

            when (getInput(
                "What would you like to do (add, show, exit)?",
                listOf("add", "show", "exit"),
                errorMessage = ""
            )) {
                "add" -> add()

                "show" -> show()

                "exit" -> {
                    manager.close()
                    println("Bye!")
                    break
                }
            }

            manager.close()
        }
    }

    private fun show() {
        val dataSet = manager.select(
            SelectQuery(
                "meal",
                columns = listOf(
                    "meal.id",
                    "meal.name as 'meal'",
                    "meal.category as category",
                    "ingredient.name as 'ingredient'"
                ),
                join = listOf(
                    "meal_ingredients ON meal.id = meal_ingredients.meal_id",
                    "ingredient ON ingredient.id = meal_ingredients.ingredient_id"
                )
            )
        )
        val rows = mutableListOf<MealRow>()
        while (dataSet.next()) {
            rows.add(
                MealRow(
                    dataSet.getInt("id"),
                    dataSet.getString("meal"),
                    dataSet.getInt("category"),
                    dataSet.getString("ingredient")
                )
            )
        }

        if (rows.isEmpty()) println("No meals saved. Add a meal first.")
        else {
            val groupedRows = rows.groupBy { it.id }
            val meals = groupedRows.map { it.value }.map {
                val name = it.first().name
                val category =
                    Categories.values().find { category -> category.ordinal == it.first().category }!!.name.lowercase()
                Meal(
                    name,
                    category,
                    it.map { row -> row.ingredient }
                )
            }
            println(meals.joinToString("\n\n", "\n", "\n"))
        }
    }

    private fun add() {
        val categoryText = getInput(
            "Which meal do you want to add (breakfast, lunch, dinner)?",
            listOf("breakfast", "lunch", "dinner"),
            errorMessage = "Wrong meal category! Choose from: breakfast, lunch, dinner."
        )

        val category = when (categoryText) {
            "breakfast" -> Categories.BREAKFAST.ordinal

            "lunch" -> Categories.LUNCH.ordinal

            else -> Categories.DINNER.ordinal
        }

        val name = getInput(
            "Input the meal's name:",
            "^[a-zA-Z\\s]+\$".toRegex(),
            true,
            "Wrong format. Use letters only!"
        )
        val ingredients = getInput(
            "Input the ingredients:",
            "(\\s*$WORD_REGEX\\s*)+(,(\\s*$WORD_REGEX\\s*)+)*".toRegex(),
            caseSensitive = true,
            errorMessage = "Wrong format. Use letters only!"
        ).split("\\s*,\\s*".toRegex())

        manager.insert(
            InsertQuery(
                "meal",
                listOf(listOf(name, category)),
                listOf("name", "category")
            )
        )

        var dataSet = manager.select(
            SelectQuery(
                "meal",
                listOf("id"),
                where = "name = '$name'"
            )
        )
        dataSet.next()
        val mealId = dataSet.getInt("id")

        ingredients.forEach {
            manager.insert(
                InsertQuery(
                    "ingredient",
                    listOf(
                        listOf(it)
                    ),
                    listOf("name")
                )
            )

            dataSet = manager.select(
                SelectQuery(
                    "ingredient",
                    listOf("id"),
                    where = "name = '$it'"
                )
            )

            dataSet.next()

            val ingredientId = dataSet.getInt("id")

            manager.insert(
                InsertQuery(
                    "meal_ingredients",
                    listOf(
                        listOf(mealId, ingredientId)
                    ), listOf("meal_id", "ingredient_id")
                )
            )
        }

        println("The meal has been added!")
    }
}
