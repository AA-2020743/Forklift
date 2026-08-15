package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.MealTemplate
import com.caloriecalc.app.data.local.entity.MealTemplateItem
import kotlinx.coroutines.flow.Flow

/** A template's name plus how many ingredients it has, for a list row — cheaper than loading
 * every template's full item list just to show a count. */
data class MealTemplateSummary(val id: Long, val name: String, val itemCount: Int)

@Dao
interface MealTemplateDao {

    @Insert
    suspend fun insertTemplate(template: MealTemplate): Long

    @Insert
    suspend fun insertItems(items: List<MealTemplateItem>)

    @Query(
        """
        SELECT t.id AS id, t.name AS name, COUNT(i.id) AS itemCount
        FROM meal_templates t LEFT JOIN meal_template_items i ON i.templateId = t.id
        GROUP BY t.id ORDER BY t.name ASC
        """
    )
    fun observeSummaries(): Flow<List<MealTemplateSummary>>

    @Query("SELECT * FROM meal_template_items WHERE templateId = :templateId")
    suspend fun getItems(templateId: Long): List<MealTemplateItem>

    @Query("DELETE FROM meal_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)
}
