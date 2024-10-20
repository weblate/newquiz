package com.infinitepower.newquiz.multi_choice_quiz.saved_questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.infinitepower.newquiz.core.analytics.logging.multi_choice_quiz.MultiChoiceQuizLoggingAnalytics
import com.infinitepower.newquiz.data.worker.multichoicequiz.DownloadMultiChoiceQuestionsWorker
import com.infinitepower.newquiz.domain.repository.multi_choice_quiz.saved_questions.SavedMultiChoiceQuestionsRepository
import com.infinitepower.newquiz.model.multi_choice_quiz.MultiChoiceQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedMultiChoiceQuestionsViewModel @Inject constructor(
    private val savedQuestionsRepository: SavedMultiChoiceQuestionsRepository,
    private val multiChoiceQuizLoggingAnalytics: MultiChoiceQuizLoggingAnalytics,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavedMultiChoiceQuestionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        savedQuestionsRepository
            .getFlowQuestions()
            .onEach { questions ->
                _uiState.update { currentState ->
                    currentState.copy(questions = questions)
                }
            }.launchIn(viewModelScope)
    }

    fun onEvent(event: SavedMultiChoiceQuestionsUiEvent) {
        when (event) {
            is SavedMultiChoiceQuestionsUiEvent.SelectQuestion -> selectQuestion(event.question)
            is SavedMultiChoiceQuestionsUiEvent.SelectAll -> selectAllQuestions()
            is SavedMultiChoiceQuestionsUiEvent.DeleteAllSelected -> deleteAllSelected()
            is SavedMultiChoiceQuestionsUiEvent.DownloadQuestions -> downloadQuestions()
        }
    }

    private fun selectQuestion(question: MultiChoiceQuestion) {
        _uiState.update { currentState ->
            val selectedQuestions = if (question in currentState.selectedQuestions) {
                currentState.selectedQuestions - question
            } else {
                currentState.selectedQuestions + question
            }

            currentState.copy(selectedQuestions = selectedQuestions)
        }
    }

    private fun selectAllQuestions() {
        _uiState.update { currentState ->
            val selectedQuestions = if (currentState.selectedQuestions.isEmpty()) {
                currentState.questions
            } else emptyList()

            currentState.copy(selectedQuestions = selectedQuestions)
        }
    }

    private fun downloadQuestions() {
        multiChoiceQuizLoggingAnalytics.logDownloadQuestions()

        val downloadQuestionsRequest = OneTimeWorkRequestBuilder<DownloadMultiChoiceQuestionsWorker>()
            .setConstraints(
                Constraints(
                    requiredNetworkType = NetworkType.CONNECTED
                )
            ).build()

        workManager.enqueue(downloadQuestionsRequest)
    }

    private fun deleteAllSelected() = viewModelScope.launch(Dispatchers.IO) {
        val allSelectedQuestions = uiState.first().selectedQuestions
        savedQuestionsRepository.deleteAllSelected(allSelectedQuestions)
    }
}