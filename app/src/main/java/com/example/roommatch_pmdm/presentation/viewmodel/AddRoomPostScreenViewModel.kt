package com.example.roommatch_pmdm.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.remote.StorageRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.AddRoomPostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddRoomPostViewModel(
    private val addRoomPostUseCase: AddRoomPostUseCase,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _roomPost = MutableStateFlow(RoomPost())
    val roomPost: StateFlow<RoomPost> = _roomPost.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris.asStateFlow()

    private val _uploadProgress = MutableStateFlow<String?>(null)
    val uploadProgress: StateFlow<String?> = _uploadProgress.asStateFlow()

    fun setTitle(v: String)         { _roomPost.value = _roomPost.value.copy(title = v) }
    fun setDescription(v: String)   { _roomPost.value = _roomPost.value.copy(description = v) }
    fun setAddress(v: String)       { _roomPost.value = _roomPost.value.copy(address = v) }
    fun setCity(v: String)          { _roomPost.value = _roomPost.value.copy(city = v) }
    fun setPrice(v: Long)           { _roomPost.value = _roomPost.value.copy(price = v) }
    fun setRoommates(v: Int)        { _roomPost.value = _roomPost.value.copy(roommates = v) }
    fun setAvailableFrom(v: String) { _roomPost.value = _roomPost.value.copy(availableFrom = v) }

    fun addImages(uris: List<Uri>) {
        val current = _selectedImageUris.value.toMutableList()
        val remaining = 5 - current.size
        current.addAll(uris.take(remaining))
        _selectedImageUris.value = current
    }

    fun removeImage(uri: Uri) {
        _selectedImageUris.value = _selectedImageUris.value.filter { it != uri }
    }

    fun reset() {
        _roomPost.value = RoomPost()
        _selectedImageUris.value = emptyList()
        _validationError.value = null
        _uploadProgress.value = null
    }

    fun save() {
        val post = _roomPost.value

        if (post.title.isBlank()) {
            _validationError.value = "El título es obligatorio"; return
        }
        if (post.city.isBlank()) {
            _validationError.value = "La ciudad es obligatoria"; return
        }
        if (post.price <= 0) {
            _validationError.value = "El precio debe ser mayor que 0"; return
        }
        if (post.address.isBlank()) {
            _validationError.value = "La dirección es obligatoria"; return
        }
        if (post.availableFrom.isBlank()) {
            _validationError.value = "La fecha de disponibilidad es obligatoria"; return
        }
        val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
        if (!dateRegex.matches(post.availableFrom)) {
            _validationError.value = "Formato de fecha incorrecto (DD/MM/YYYY)"; return
        }

        _validationError.value = null
        val currentUser = authRepository.currentUser ?: return

        viewModelScope.launch {
            _isLoading.value = true

            val uploadedUrls = mutableListOf<String>()
            val uris = _selectedImageUris.value
            uris.forEachIndexed { index, uri ->
                _uploadProgress.value = "Subiendo imagen ${index + 1} de ${uris.size}…"
                storageRepository.uploadProfileImage(uri).fold(
                    onSuccess = { url -> uploadedUrls.add(url) },
                    onFailure = { /* imagen fallida: la saltamos */ }
                )
            }
            _uploadProgress.value = null

            val fullPost = post.copy(
                ownerId   = currentUser.uid,
                ownerName = currentUser.email ?: "",
                images    = uploadedUrls,
                createdAt = System.currentTimeMillis()
            )
            if (addRoomPostUseCase(fullPost)) {
                _isSaved.value = true
            } else {
                _validationError.value = "Error al publicar el anuncio"
            }

            _isLoading.value = false
        }
    }

    fun clearValidationError() { _validationError.value = null }
    fun clearSaved()           { _isSaved.value = false }
}