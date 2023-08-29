package com.infinitepower.newquiz.model.multi_choice_quiz

import androidx.annotation.Keep
import com.infinitepower.newquiz.model.question.QuestionDifficulty
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Keep
@Serializable
data class MultiChoiceQuestion(
    val id: Int,
    val description: String,
    val imageUrl: String? = null,
    val answers: List<String>,
    val lang: QuestionLanguage,
    val category: MultiChoiceBaseCategory,
    val correctAns: Int,
    val type: MultiChoiceQuestionType,
    val difficulty: QuestionDifficulty
) : java.io.Serializable {
    constructor(
        description: String,
        imageUrl: String? = null,
        answers: List<String>,
        lang: QuestionLanguage,
        category: MultiChoiceBaseCategory,
        correctAns: Int,
        type: MultiChoiceQuestionType,
        difficulty: QuestionDifficulty
    ) : this(
        // Generate an id based in all the fields of the question.
        // Answers are sorted to avoid different ids for the same question with different order of answers.
        // Correct answer is not included because it depends on the order of the answers.
        id = (description + imageUrl + answers.sorted() + lang + category + type + difficulty).hashCode(),
        description = description,
        imageUrl = imageUrl,
        answers = answers,
        lang = lang,
        category = category,
        correctAns = correctAns,
        type = type,
        difficulty = difficulty
    )

    fun toQuestionStep() = MultiChoiceQuestionStep.NotCurrent(this)

    override fun toString(): String = Json.encodeToString(this)

    override fun hashCode(): Int = id

    override fun equals(other: Any?): Boolean {
        if (other !is MultiChoiceQuestion) return false
        return id == other.id
    }
}

/**
 * Generates a random [MultiChoiceQuestion].
 *
 * @param id The id of the question.
 * @param correctAns The index of the correct answer.
 */
fun getBasicMultiChoiceQuestion(
    id: Int = Random.nextInt(),
    correctAns: Int = (0..3).random()
): MultiChoiceQuestion {
    return MultiChoiceQuestion(
        id = id,
        description = "Question description",
        imageUrl = null,
        answers = listOf(
            "Answer 1",
            "Answer 2",
            "Answer 3",
            "Answer 4"
        ),
        lang = QuestionLanguage.EN,
        category = MultiChoiceBaseCategory.Random,
        correctAns = correctAns,
        type = MultiChoiceQuestionType.MULTIPLE,
        difficulty = QuestionDifficulty.Medium
    )
}
