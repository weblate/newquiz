package com.infinitepower.newquiz.settings_presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinitepower.newquiz.core.analytics.logging.CoreLoggingAnalytics
import com.infinitepower.newquiz.domain.repository.user.auth.AuthUserRepository
import com.infinitepower.newquiz.domain.repository.wordle.daily.DailyWordleRepository
import com.infinitepower.newquiz.settings_presentation.data.SettingsScreenPageData
import com.infinitepower.newquiz.settings_presentation.model.ScreenKey
import com.infinitepower.newquiz.translation_dynamic_feature.TranslatorUtil
import com.infinitepower.newquiz.translation_dynamic_feature.TranslatorUtil.TranslatorModelState
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
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dailyWordleRepository: DailyWordleRepository,
    private val translatorUtil: TranslatorUtil,
    private val authUserRepository: AuthUserRepository,
    private val coreLoggingAnalytics: CoreLoggingAnalytics
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle
            .getStateFlow(
                key = SettingsScreenNavArgs::screenKey.name,
                initialValue = SettingsScreenPageData.MainPage.key.value
            ).onEach { key ->
                _uiState.update { currentState ->
                    currentState.copy(screenKey = ScreenKey(key))
                }
            }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { currentState ->
                val state = if (translatorUtil.isModelDownloaded()) {
                    TranslatorModelState.Downloaded
                } else TranslatorModelState.None

                currentState.copy(translationModelState = state)
            }
        }

        authUserRepository
            .isSignedInFlow
            .onEach { isSignedIn ->
                _uiState.update { currentState ->
                    currentState.copy(userIsSignedIn = isSignedIn)
                }
            }.launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsScreenUiEvent) {
        when (event) {
            is SettingsScreenUiEvent.DeleteTranslationModel -> viewModelScope.launch(Dispatchers.IO) {
                translatorUtil.deleteModel()
            }
            is SettingsScreenUiEvent.DownloadTranslationModel -> downloadTranslationModel()
            is SettingsScreenUiEvent.ClearWordleCalendarItems -> clearWordleCalendarItems()
            is SettingsScreenUiEvent.SignOut -> authUserRepository.signOut()
            is SettingsScreenUiEvent.EnableLoggingAnalytics -> coreLoggingAnalytics.enableLoggingAnalytics(event.enabled)
        }
    }

    private fun clearWordleCalendarItems() = viewModelScope.launch(Dispatchers.IO)  {
        dailyWordleRepository.clearAllCalendarItems()
    }

    private fun downloadTranslationModel() = viewModelScope.launch(Dispatchers.IO)  {
        translatorUtil
            .downloadModel()
            .collect { res ->
                _uiState.update { currentState ->
                    currentState.copy(translationModelState = res.data ?: TranslatorModelState.None)
                }
            }
    }
}