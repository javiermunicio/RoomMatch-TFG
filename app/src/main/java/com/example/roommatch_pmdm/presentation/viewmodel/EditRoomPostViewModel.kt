package com.example.roommatch_pmdm.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.remote.StorageRepository
import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.GetRoomPostByIdUseCase
import com.example.roommatch_pmdm.domain.usecase.UpdateRoomPostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditRoomPostViewModel(
    private val authRepository: AuthRepository,
    private val getRoomPostByIdUseCase: GetRoomPostByIdUseCase,
    private val updateRoomPostUseCase: UpdateRoomPostUseCase,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _roomPost = MutableStateFlow(RoomPost())
    val roomPost: StateFlow<RoomPost> = _roomPost

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    private val _newImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val newImageUris: StateFlow<List<Uri>> = _newImageUris

    private val _uploadProgress = MutableStateFlow<String?>(null)
    val uploadProgress: StateFlow<String?> = _uploadProgress

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getRoomPostByIdUseCase(postId).fold(
                onSuccess = { _roomPost.value = it },
                onFailure = { _validationError.value = "No se pudo cargar el anuncio" }
            )
            _isLoading.value = false
        }
    }

    fun setTitle(v: String)         { _roomPost.value = _roomPost.value.copy(title = v) }
    fun setDescription(v: String)   { _roomPost.value = _roomPost.value.copy(description = v) }
    fun setAddress(v: String)       { _roomPost.value = _roomPost.value.copy(address = v) }
    fun setCity(v: String)          { _roomPost.value = _roomPost.value.copy(city = v) }
    fun setPrice(v: Long)           { _roomPost.value = _roomPost.value.copy(price = v) }
    fun setRoommates(v: Int)        { _roomPost.value = _roomPost.value.copy(roommates = v) }
    fun setAvailableFrom(v: String) { _roomPost.value = _roomPost.value.copy(availableFrom = v) }

    private fun totalImageCount() = _roomPost.value.images.size + _newImageUris.value.size

    fun addImages(uris: List<Uri>) {
        val remaining = 5 - totalImageCount()
        if (remaining <= 0) return
        _newImageUris.value = _newImageUris.value + uris.take(remaining)
    }

    fun removeExistingImage(url: String) {
        _roomPost.value = _roomPost.value.copy(
            images = _roomPost.value.images.filter { it != url }
        )
    }

    fun removeNewImage(uri: Uri) {
        _newImageUris.value = _newImageUris.value.filter { it != uri }
    }

    fun save() {
        val post = _roomPost.value

        if (post.title.isBlank())         { _validationError.value = "El título es obligatorio"; return }
        if (post.city.isBlank())          { _validationError.value = "La ciudad es obligatoria"; return }
        if (post.price <= 0)              { _validationError.value = "El precio debe ser mayor que 0"; return }
        if (post.address.isBlank())       { _validationError.value = "La dirección es obligatoria"; return }
        if (post.availableFrom.isBlank()) { _validationError.value = "La fecha de disponibilidad es obligatoria"; return }
        val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
        if (!dateRegex.matches(post.availableFrom)) {
            _validationError.value = "Formato de fecha incorrecto (DD/MM/YYYY)"; return
        }

        _validationError.value = null

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Subir las imágenes nuevas a Cloudinary
            val newUrls = mutableListOf<String>()
            val uris = _newImageUris.value
            uris.forEachIndexed { index, uri ->
                _uploadProgress.value = "Subiendo imagen ${index + 1} de ${uris.size}…"
                storageRepository.uploadProfileImage(uri).fold(
                    onSuccess = { url -> newUrls.add(url) },
                    onFailure = { /* imagen fallida: la saltamos */ }
                )
            }
            _uploadProgress.value = null

            // 2. Combinar URLs existentes (no eliminadas) + nuevas subidas
            val updatedPost = post.copy(images = post.images + newUrls)

            // 3. Guardar a través del repositorio
            val success = updateRoomPostUseCase(updatedPost)
            if (success) {
                _isSaved.value = true
            } else {
                _validationError.value = "Error al guardar los cambios"
            }

            _isLoading.value = false
        }
    }

    fun clearSaved() { _isSaved.value = false }
}