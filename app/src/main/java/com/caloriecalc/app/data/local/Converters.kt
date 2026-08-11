package com.caloriecalc.app.data.local

import androidx.room.TypeConverter
import com.caloriecalc.app.data.local.entity.FoodSource
import com.caloriecalc.app.domain.ActivityLevel
import com.caloriecalc.app.domain.Goal
import com.caloriecalc.app.domain.MealType
import com.caloriecalc.app.domain.MuscleGroup
import com.caloriecalc.app.domain.MuscleSubGroup
import com.caloriecalc.app.domain.Sex

class Converters {

    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)

    @TypeConverter
    fun fromFoodSource(value: FoodSource): String = value.name

    @TypeConverter
    fun toFoodSource(value: String): FoodSource = FoodSource.valueOf(value)

    @TypeConverter
    fun fromSex(value: Sex): String = value.name

    @TypeConverter
    fun toSex(value: String): Sex = Sex.valueOf(value)

    @TypeConverter
    fun fromActivityLevel(value: ActivityLevel): String = value.name

    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel = ActivityLevel.valueOf(value)

    @TypeConverter
    fun fromGoal(value: Goal): String = value.name

    @TypeConverter
    fun toGoal(value: String): Goal = Goal.valueOf(value)

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun fromMuscleSubGroupSet(value: Set<MuscleSubGroup>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toMuscleSubGroupSet(value: String): Set<MuscleSubGroup> =
        if (value.isBlank()) emptySet()
        else value.split(",").map { MuscleSubGroup.valueOf(it) }.toSet()
}
