package com.midterm.chitchatter.ui.edit_profile

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class EditProfileViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contact = MutableLiveData<Account?>()
    val contact: LiveData<Account?> = _contact

//    private val _profileFormState = MutableLiveData<ProfileFormState>()
//    val profileFormState: LiveData<ProfileFormState> = _profileFormState

    private val _updateMessage = MutableLiveData<Int?>()
    val updateMessage: LiveData<Int?> = _updateMessage
    fun loadAccountInfo(email: String) {
        viewModelScope.launch {
            val account =
                (repository as Repository.RemoteRepository).getContactDetail(email)
            _contact.postValue(account)
        }
    }
    fun checkFormState(displayName: String, birthDate: String) : Boolean {
        if (displayName.isBlank()) {
            return false
        }
        if (birthDate.isNotBlank()) {
            if (!isValidDate(birthDate)) {
                return false
            }
        }
        return true
    }

    fun updateAvatar(
        email: String,
        fileName: String
    ) {
        viewModelScope.launch {
            val account = Account(
                email = email,
                imageUrl = fileName,
                token = ChitChatterUtils.token
            )
            val isSuccessful =
                (repository as Repository.RemoteRepository).updateAvatar(account)
            if (isSuccessful) {
                _updateMessage.postValue(R.string.txt_update_avatar_success)
            } else {
                _updateMessage.postValue(R.string.unknown_error)
            }
        }
    }
    fun updateProfile(
        email: String,
        displayName: String,
        birthDate: String,
        gender: String
    ) {
        viewModelScope.launch {
            val account = Account(
                email = email,
                name = displayName,
                birthday = birthDate,
                gender = gender,
                token = ChitChatterUtils.token
            )
            val isSuccessful =
                (repository as Repository.RemoteRepository).updateAccount(account)
            if (isSuccessful) {
                _updateMessage.postValue(R.string.txt_update_profile_success)
            } else {
                _updateMessage.postValue(R.string.unknown_error)
            }
        }
    }

    fun resetState() {
        _updateMessage.postValue(null)
    }

    @SuppressLint("SimpleDateFormat")
    fun isValidDate(birthDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        dateFormat.isLenient = false
        return try {
            val date = dateFormat.parse(birthDate)
            val calendar = Calendar.getInstance().apply { time = date!! }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val birthYear = calendar.get(Calendar.YEAR)
            birthYear in 1901..<currentYear
        } catch (e: Exception) {
            false
        }

    }
}

class EditProfileViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not EditProfileViewModel")
    }
}