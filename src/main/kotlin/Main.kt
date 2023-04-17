import java.util.Scanner

val scanner = Scanner(System.`in`)
const val WORD_REGEX = "[a-zA-Z]+"

fun main() {
    val planner = MealPlanner()
    planner.menu()
}
