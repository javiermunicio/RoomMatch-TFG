package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.Rooms
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepository(val firestore: FirebaseFirestore) {
    private val roomsCollection = firestore.collection("room")
    //Metodo que Lista las peliculas
    fun list(): Flow<List<Rooms>>{
        return queryForList(
            roomsCollection,
            Rooms::class.java
        )
    }
    //Metodo que inserta una pelicula
    suspend fun save(room: Rooms): Boolean {
        return try {
            roomsCollection.add(room).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //Metodo que elimina una pelicula dado su id
    suspend fun delete(id: String): Boolean {
        return try {
            roomsCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun <T> queryForList(query: Query, clazz: Class<T>): Flow<List<T>> {
        return callbackFlow {

            val listener = query
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val items = snapshots?.documents?.mapNotNull { doc ->
                        doc.toObject(clazz)

                    } ?: emptyList()

                    trySend(items)
                }

            awaitClose() { listener.remove() }
        }
    }
}