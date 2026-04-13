package com.vgroups.gymbuddy.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val quotes = listOf(
        "The only bad workout is the one that didn't happen.",
        "Muscle is built with sweat and consistency.",
        "Your body can stand almost anything. It’s your mind that you have to convince.",
        "Strive for progress, not perfection.",
        "Discipline is doing what needs to be done, even if you don't want to do it.",
        "Success starts with self-discipline.",
        "The hard part isn't getting your body in shape. The hard part is getting your mind in shape."
    )

    private val _quote = MutableStateFlow(quotes.random())
    val quote: StateFlow<String> = _quote

    fun refreshQuote() {
        _quote.value = quotes.random()
    }
}
