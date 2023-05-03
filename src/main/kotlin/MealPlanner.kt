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
                listOf("add", "show", "exit", "plan"),
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
        val categoryId = getInput(
            "Choose category(1-3):\n1. Breakfast\n2. Lunch\n3. Dinner",
            "[1-3]".toRegex(),
            errorMessage = "Wrong meal category! Make a choice in (1-3)!"
        ).toInt()

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
        val categoryId = getInput(
            "Which category do you want to print(1-3)?:\n1. Breakfast\n2. Lunch\n3. Dinner",
            "[1-3]".toRegex(),
            errorMessage = "Wrong meal category! Make a choice in (1-3)!"
        ).toInt()

        val category = Category.values().find { it.ordinal + 1 == categoryId }!!

        val dataSet = manager.select(
            SelectQuery(
                "meal",
                columns = listOf(
                    "meal.meal",
                    "ingredient.ingredient"
                ),
                join = listOf(
                    "category ON meal.category_id = category.id",
                    "ingredients ON ingredients.meal_id = meal.id",
                    "ingredient ON ingredient.id = ingredients.ingredient_id"
                ),
                where = "category_id = $categoryId"
            )
        )

        val rows = mutableListOf<MealRow>()
        while (dataSet.next()) {
            rows.add(
                MealRow(
                    dataSet.getString("meal"),
                    dataSet.getString("ingredient")
                )
            )
        }

        if (rows.isEmpty()) println("No saved meals found.") else {
            println("Category: $category")
            val groupedRows = rows.groupBy { it.meal }
            groupedRows.forEach { (meal, ingredients) ->
                println("\nName: $meal")
                println(ingredients.joinToString("\n") { it.ingredient.toCapitalCase() })
            }
        }
    }

    private fun plan() {
        val weekdays = Weekday.values()
        val categories = Category.values()
        manager.delete("plan")

        weekdays.forEach { weekday ->
            val weekdayId = weekday.ordinal + 1
            println(weekday.name.lowercase().toCapitalCase())
            categories.forEach { category ->
                val categoryId = category.ordinal + 1
                val dataSet = manager.select(
                    SelectQuery(
                        "meal",
                        listOf("id", "meal"),
                        where = "category_id = $categoryId",
                    )
                )
                val meals = mutableListOf<Pair<Int, String>>()
                while (dataSet.next()) meals.add(Pair(dataSet.getInt("id"), dataSet.getString("meal")))
                println(meals.joinToString("\n") { meal -> "${meal.first}. ${meal.second}" })
                val mealId = getInput(
                    "Choose $category for $weekday from the list above",
                    meals.map { it.first.toString() },
                    errorMessage = "Please choose from the list above..."
                ).toInt()
                manager.insert(
                    InsertQuery(
                        "plan",
                        listOf(
                            listOf(weekdayId, mealId)
                        ),
                        listOf("weekday_id", "meal_id")
                    )
                )
            }
        }

        println("The weekly plan is ready.")
        val dataSet = manager.select(
            SelectQuery(
                "plan",
                listOf("weekday.weekday", "category.category", "meal.meal"),
                join = listOf(
                    "weekday on weekday.id = plan.weekday_id",
                    "meal on meal.id = plan.meal_id",
                    "category on category.id = meal.category_id"
                )
            )
        )

        val rows = mutableListOf<Triple<String, String, String>>()
        while (dataSet.next()) rows.add(
            Triple(
                dataSet.getString("weekday"),
                dataSet.getString("category"),
                dataSet.getString("meal")
            )
        )

        val days = rows.groupBy({ row -> row.first }) { row ->
            Pair(row.second, row.third)
        }

        days.forEach { (day, meal) ->
            println(day.toCapitalCase())
            println(meal.joinToString("\n") { "${it.first.toCapitalCase()}: ${it.second}" })
        }
    }
}
