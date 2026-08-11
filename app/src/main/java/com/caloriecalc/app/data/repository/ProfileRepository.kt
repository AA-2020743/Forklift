package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.UserProfileDao
import com.caloriecalc.app.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val dao: UserProfileDao) {

    fun observeProfile(): Flow<UserProfile> = dao.observe().map { it ?: UserProfile() }

    suspend fun getProfile(): UserProfile = dao.get() ?: UserProfile().also { dao.upsert(it) }

    suspend fun updateProfile(profile: UserProfile) = dao.upsert(profile)
}
