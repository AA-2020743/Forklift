package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.UserProfileDao
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.domain.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val dao: UserProfileDao) {

    fun observeProfile(): Flow<UserProfile> = dao.observe().map { it ?: UserProfile() }

    suspend fun getProfile(): UserProfile = dao.get() ?: UserProfile().also { dao.upsert(it) }

    suspend fun updateProfile(profile: UserProfile) {
        require(profile.bodyWeightKg.isFinite() && profile.bodyWeightKg > 0.0)
        require(profile.heightCm.isFinite() && profile.heightCm > 0.0)
        require(profile.age > 0)
        require(profile.proteinMinGramsPerKg.isFinite() && profile.proteinMinGramsPerKg >= 0.0)
        require(profile.proteinMaxGramsPerKg.isFinite() &&
            profile.proteinMaxGramsPerKg >= profile.proteinMinGramsPerKg)
        require(profile.fatMinGramsPerKg.isFinite() && profile.fatMinGramsPerKg >= 0.0)
        require(profile.fatMaxGramsPerKg.isFinite() && profile.fatMaxGramsPerKg >= profile.fatMinGramsPerKg)
        require(profile.manualCalorieTarget == null || profile.manualCalorieTarget > 0)
        require(profile.proteinGapHours in 1..12)
        require(profile.proteinDoseGrams.isFinite() && profile.proteinDoseGrams >= 1.0)
        require(profile.weightReminderHour in 0..23 && profile.weightReminderMinute in 0..59)
        require(profile.wakeHour in 0..23 && profile.wakeMinute in 0..59)
        require(profile.sleepHour in 0..23 && profile.sleepMinute in 0..59)
        require(NutritionCalculator.computeBmr(profile).isFinite())
        dao.upsert(profile)
    }
}
