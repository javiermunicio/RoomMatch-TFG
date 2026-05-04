package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditRoomPostViewModel(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val collection = firestore.collection("roomPosts")

    private val _roomPost = MutableStateFlow(RoomPost())
    val roomPost: StateFlow<RoomPost> = _roomPost

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    // Carga el post existente desde Firestore
    fun loadPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = collection.document(postId).get().await()
                doc.toObject(RoomPost::class.java)?.copy(id = doc.id)?.let {
                    _roomPost.value = it
                }
            } catch (e: Exception) {
                _validationError.value = "No se pudo cargar el anuncio"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setTitle(v: String)         { _roomPost.value = _roomPost.value.copy(title = v) }
    fun setDescription(v: String)   { _roomPost.value = _roomPost.value.copy(description = v) }
    fun setAddress(v: String)       { _roomPost.value = _roomPost.value.copy(address = v) }
    fun setCity(v: String)          { _roomPost.value = _roomPost.value.copy(city = v) }
    fun setPrice(v: Long)           { _roomPost.value = _roomPost.value.copy(price = v) }
    fun setRoommates(v: Int)        { _roomPost.value = _roomPost.value.copy(roommates = v) }
    fun setAvailableFrom(v: String) { _roomPost.value = _roomPost.value.copy(availableFrom = v) }

    fun save() {
        val post = _roomPost.value

        if (post.title.isBlank()) { _validationError.value = "El título es obligatorio"; return }
        if (post.city.isBlank())  { _validationError.value = "La ciudad es obligatoria"; return }
        if (post.price <= 0)      { _validationError.value = "El precio debe ser mayor que 0"; return }
        if (post.address.isBlank()) { _validationError.value = "La dirección es obligatoria"; return }
        if (post.availableFrom.isBlank()) { _validationError.value = "La fecha de disponibilidad es obligatoria"; return }
        val dateRegex = Regex("""^\d{2}/\d{2}/\d{4}$""")
        if (!dateRegex.matches(post.availableFrom)) {
            _validationError.value = "Formato de fecha incorrecto (DD/MM/YYYY)"; return
        }

        _validationError.value = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                collection.document(post.id).set(post).await()
                _isSaved.value = true
            } catch (e: Exception) {
                _validationError.value = "Error al guardar los cambios"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSaved() { _isSaved.value = false }
}