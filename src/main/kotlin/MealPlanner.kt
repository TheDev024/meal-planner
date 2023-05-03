import entity.*
import enums.*
import org.sqlite.SQLiteException
import query.*

class MealPlanner {
    private val manager = DataBaseManager("meals.db")

    init {
        manager.open()

        val tables = listOf(
            TableQuery(
                "meal",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "meal VARCHAR(50) NOT NULL",
                    "category_id INTEGER",
                    "UNIQUE (meal, category_id)",
                    "CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE ON UPDATE CASCADE"
                )
            ),
            TableQuery("category", listOf("id INTEGER PRIMARY KEY", "category VARCHAR(15) NOT NULL UNIQUE")),
            TableQuery(
                "ingredient",
                listOf("id INTEGER PRIMARY KEY AUTOINCREMENT", "ingredient VARCHAR(50) NOT NULL UNIQUE")
            ),
            TableQuery(
                "ingredients",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "meal_id INTEGER",
                    "ingredient_id INTEGER",
                    "CONSTRAINT fk_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE ON UPDATE CASCADE",
                    "CONSTRAINT fk_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE ON UPDATE CASCADE"
                )
            ),
            TableQuery("weekday", listOf("id INTEGER PRIMARY KEY", "weekday VARCHAR(10) NOT NULL UNIQUE")),
            TableQuery(
                "plan",
                listOf(
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "weekday_id INTEGER",
                    "meal_id INTEGER",
                    "CONSTRAINT fk_weekday FOREIGN KEY (weekday_id) REFERENCES ingredients(id) ON DELETE CASCADE ON UPDATE CASCADE",
                    "CONSTRAINT fk_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE ON UPDATE CASCADE"
                )
            )
        )

        Category.values().forEach { category ->
            try {
                manager.insert(InsertQuery("category", listOf(listOf(category.ordinal + 1, category.name.lowercase()))))
            } catch (_: SQLiteException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Weekday.values().forEach { weekday ->
            try {
                manager.insert(InsertQuery("weekday", listOf(listOf(weekday.ordinal + 1, weekday.name.lowercase()))))
            } catch (_: SQLiteException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tables.forEach { manager.createTable(it) }

        manager.close()
    }

    fun menu() {
        while (true) {
            manager.open()

            when (getInput(
                "What would you like to do (add, show, plan, exit)?",
                listOf("add", "show", "exit"),
                errorMessage = ""
            )) {
                "add" -> add()

                "show" -> show()

                "plan" -> plan()

                "exit" -> {
                    manager.close()
                    println("Bye!")
                    break
                }
            }

            manager.close()
        }
    }

    private fun add() {
        val category = getInput(
            "Which meal do you want to add (breakfast, lunch, dinner)?",
            listOf("breakfast", "lunch", "dinner"),
            errorMessage = "Wrong meal category! Choose from: breakfast, lunch, dinner."
        )

        val categoryId = Category.valueOf(category.uppercase()).ordinal + 1

        val meal = getInput(
            "Input the meal's name:", "^[a-zA-Z\\s]+\$".toRegex(), true, "Wrong format. Use letters only!"
        )

        try {
            manager.insert(
                InsertQuery("meal", listOf(listOf(meal, categoryId)), listOf("meal", "category_id"))
            )
        } catch (e: SQLiteException) {
            println("The meal has already been added!")
            return
        }

        val ingredients = getInput(
            "Input the ingredients:",
            "(\\s*$WORD_REGEX\\s*)+(,(\\s*$WORD_REGEX\\s*)+)*".toRegex(),
            caseSensitive = true,
            errorMessage = "Wrong format. Use letters only!"
        ).split("\\s*,\\s*".toRegex())

        var dataSet = manager.select(
            SelectQuery("meal", listOf("id"), where = "meal = '$meal' AND category_id = $categoryId")
        )
        dataSet.next()
        val mealId = dataSet.getInt("id")

        ingredients.forEach { ingredient ->
            try {
                manager.insert(InsertQuery("ingredient", listOf(listOf(ingredient)), listOf("ingredient")))
            } catch (_: Exception) {
            }

            dataSet = manager.select(SelectQuery("ingredient", listOf("id"), where = "ingredient = '$ingredient'"))
            dataSet.next()

            val ingredientId = dataSet.getInt("id")

            manager.insert(
                InsertQuery(
                    "ingredients",
                    listOf(
                        listOf(mealId, ingredientId)
                    ), listOf("meal_id", "ingredient_id")
                )
            )
        }

        println("The meal has been added!")
    }

    private fun show() {
        val input = getInput(
            "Which category do you want to print (breakfast, lunch, dinner)?",
            listOf("breakfast", "lunch", "dinner"),
            errorMessage = "Wrong meal category! Choose from: breakfast, lunch, dinner."
        )

        val category = Category.valueOf(input.uppercase())

        val dataSet = manager.select(
            SelectQuery(
                "meal",
                columns = listOf(
                    "meal.id",
                    "meal.name as 'meal'",
                    "ingredient.name as 'ingredient'"
                ),
                join = listOf(
                    "meal_ingredients ON meal.id = meal_ingredients.meal_id",
                    "ingredient ON ingredient.id = meal_ingredients.ingredient_id"
                ),
                where = "category = ${category.ordinal}"
            )
        )
        val rows = mutableListOf<MealRow>()
        while (dataSet.next()) {
            rows.add(
                MealRow(
                    dataSet.getInt("id"),
                    dataSet.getString("meal"),
                    dataSet.getString("ingredient")
                )
            )
        }

        if (rows.isEmpty()) println("No meals saved found.")
        else {
            val groupedRows = rows.groupBy { it.id }
            val meals = groupedRows.map { it.value }.map {
                val name = it.first().name
                Meal(
                    name,
                    category.name.lowercase(),
                    it.map { row -> row.ingredient }
                )
            }
            println(meals.joinToString("\n\n", "Category: ${input.lowercase()}\n\n", "\n"))
        }
    }

    private fun plan() {
        TODO("Not yet implemented")
    }
}
