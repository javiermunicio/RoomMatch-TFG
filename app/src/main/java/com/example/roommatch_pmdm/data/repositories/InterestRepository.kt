package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.Interest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InterestRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("interests")

    // Guardar interés (idempotente: un usuario solo puede mostrar interés una vez por anuncio)
    suspend fun addInterest(interest: Interest): Result<Unit> {
        return try {
            val id = "${interest.interestedUserId}_${interest.postId}"
            collection.document(id).set(interest.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar interés (el usuario puede retirar su interés)
    suspend fun removeInterest(userId: String, postId: String): Result<Unit> {
        return try {
            val id = "${userId}_${postId}"
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Comprobar si el usuario ya ha mostrado interés en este anuncio
    suspend fun hasInterest(userId: String, postId: String): Boolean {
        return try {
            val id = "${userId}_${postId}"
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    // Escuchar en tiempo real el número de interesados en un anuncio
    fun getInterestCountFlow(postId: String): Flow<Int> = callbackFlow {
        val listener = collection
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snap?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    // Obtener lista de interesados en un anuncio (para el dueño)
    fun getInterestedUsersFlow(postId: String): Flow<List<Interest>> = callbackFlow {
        val listener = collection
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { it.toObject(Interest::class.java) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }
}