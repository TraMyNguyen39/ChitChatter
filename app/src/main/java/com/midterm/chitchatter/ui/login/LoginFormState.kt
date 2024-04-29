package com.midterm.chitchatter.ui.login

data class LoginFormState (
    val isCorrect: Boolean = false,
    val emailError: Int? = null,
    val passwordError: Int? = null
)