package com.midterm.chitchatter.ui.register

data class RegisterFormState(
    val isCorrect: Boolean = false,
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val displayNameError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
)