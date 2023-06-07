package com.infinitepower.newquiz.comparison_quiz.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.infinitepower.newquiz.core.game.ComparisonQuizCore
import com.infinitepower.newquiz.data.worker.UpdateGlobalEventDataWorker
import com.infinitepower.newquiz.domain.repository.comparison_quiz.ComparisonQuizRepository
import com.infinitepower.newquiz.model.comparison_quiz.ComparisonMode
import com.infinitepower.newquiz.model.comparison_quiz.ComparisonQuizCategory
import com.infinitepower.newquiz.model.global_event.GameEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComparisonQuizViewModel @Inject constructor(
    private val comparisonQuizCore: ComparisonQuizCore,
    private val savedStateHandle: SavedStateHandle,
    private val comparisonQuizRepository: ComparisonQuizRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ComparisonQuizUiState())
    val uiState = _uiState.asStateFlow()

    init {
        comparisonQuizCore
            .quizDataFlow
            .onEach { data ->
                _uiState.update { currentState ->
                    if (data.currentPosition > currentState.highestPosition) {
                        comparisonQuizRepository.saveHighestPosition(data.currentPosition)
                    }

                    if (data.isGameOver) {
                        viewModelScope.launch(Dispatchers.IO) {
                            UpdateGlobalEventDataWorker.enqueueWork(
                                workManager = workManager,
                                GameEvent.ComparisonQuiz.PlayAndGetScore(data.currentPosition)
                            )
                        }
                    }

                    currentState.copy(
                        currentQuestion = data.currentQuestion,
                        gameDescription = data.questionDescription,
                        currentPosition = data.currentPosition,
                        isGameOver = data.isGameOver,
                        gameCategory = getCategory()
                    )
                }
            }.launchIn(viewModelScope)

        comparisonQuizRepository
            .getHighestPosition()
            .onEach { res ->
                _uiState.update { currentState ->
                    currentState.copy(highestPosition = res.data ?: 0)
                }
            }.launchIn(viewModelScope)

        // Start game
        viewModelScope.launch(Dispatchers.IO) {
            comparisonQuizCore.initializeGame(
                initializationData = ComparisonQuizCore.InitializationData(
                    category = getCategory(),
                    comparisonMode = getComparisonMode()
                )
            )

            launch {
                UpdateGlobalEventDataWorker.enqueueWork(
                    workManager = workManager,
                    GameEvent.ComparisonQuiz.PlayWithComparisonMode(getComparisonMode()),
                    GameEvent.ComparisonQuiz.PlayQuizWithCategory(getCategory().id)
                )
            }
        }
    }

    fun onEvent(event: ComparisonQuizUiEvent) {
        when (event) {
            is ComparisonQuizUiEvent.OnAnswerClick -> {
                comparisonQuizCore.onAnswerClicked(event.item)
            }
        }
    }

    fun getCategory(): ComparisonQuizCategory {
        return savedStateHandle
            .get<ComparisonQuizCategory>(ComparisonQuizListScreenNavArg::category.name)
            ?: throw IllegalArgumentException("Category is null")
    }

    fun getComparisonMode(): ComparisonMode {
        return savedStateHandle
            .get<ComparisonMode>(ComparisonQuizListScreenNavArg::comparisonMode.name)
            ?: throw IllegalArgumentException("Comparison mode is null")
    }
}
