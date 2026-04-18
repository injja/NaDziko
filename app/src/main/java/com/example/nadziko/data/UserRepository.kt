package com.example.nadziko.data

import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(
        username: String,
        passwordHash: String
    ) {
        val user = User(
            username = username,
            passwordHash = passwordHash,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(
            user.copy(updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteUserById(id: Int) {
        userDao.deleteUserById(id)
    }
}