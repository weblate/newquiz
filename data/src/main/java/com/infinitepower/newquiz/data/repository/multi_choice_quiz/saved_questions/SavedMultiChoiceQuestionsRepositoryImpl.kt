package com.infinitepower.newquiz.data.repository.multi_choice_quiz.saved_questions

import com.infinitepower.newquiz.domain.repository.multi_choice_quiz.saved_questions.SavedMultiChoiceQuestionsDao
import com.infinitepower.newquiz.domain.repository.multi_choice_quiz.saved_questions.SavedMultiChoiceQuestionsRepository
import com.infinitepower.newquiz.model.multi_choice_quiz.MultiChoiceQuestion
import com.infinitepower.newquiz.model.multi_choice_quiz.MultiChoiceQuestionEntity
import com.infinitepower.newquiz.model.multi_choice_quiz.saved.SortSavedQuestionsBy
import com.infinitepower.newquiz.model.multi_choice_quiz.toQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedMultiChoiceQuestionsRepositoryImpl @Inject constructor(
    private val savedQuestionsDao: SavedMultiChoiceQuestionsDao
) : SavedMultiChoiceQuestionsRepository {
    override suspend fun insertQuestions(questions: List<MultiChoiceQuestion>) {
        val questionsEntity = questions.map(MultiChoiceQuestion::toEntity)
        savedQuestionsDao.insertQuestions(questionsEntity)
    }

    override suspend fun insertQuestions(vararg questions: MultiChoiceQuestion) {
        val questionsEntity = questions.map(MultiChoiceQuestion::toEntity)
        savedQuestionsDao.insertQuestions(questionsEntity)
    }

    override fun getFlowQuestions(
        sortBy: SortSavedQuestionsBy
    ): Flow<List<MultiChoiceQuestion>> {
        val questionsFlow = when (sortBy) {
            SortSavedQuestionsBy.BY_DEFAULT -> savedQuestionsDao.getFlowQuestions()
            SortSavedQuestionsBy.BY_DESCRIPTION -> savedQuestionsDao.getFlowQuestionsSortedByDescription()
            SortSavedQuestionsBy.BY_CATEGORY -> savedQuestionsDao.getFlowQuestionsSortedByCategory()
        }

        return questionsFlow.map { flowQuestions -> flowQuestions.map(MultiChoiceQuestionEntity::toQuestion) }
    }

    override suspend fun getQuestions(): List<MultiChoiceQuestion> = savedQuestionsDao
        .getQuestions()
        .map(MultiChoiceQuestionEntity::toQuestion)

    override suspend fun deleteAllSelected(questions: List<MultiChoiceQuestion>) {
        val questionsEntity = questions.map(MultiChoiceQuestion::toEntity)
        savedQuestionsDao.deleteAll(questionsEntity)
    }
}