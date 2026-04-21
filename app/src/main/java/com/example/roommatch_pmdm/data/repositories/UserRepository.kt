package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserIfNotExists(userId: String, email: String, username: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.toObject(User::class.java)!!)
            } else {
                val newUser = User(
                    id = userId,
                    username = username,
                    email = email,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                usersCollection.document(userId).set(newUser).await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}