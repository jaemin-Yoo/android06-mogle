package com.wakeup.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wakeup.data.database.entity.GlobeEntity
import com.wakeup.data.database.entity.MomentEntity
import com.wakeup.data.database.entity.MomentPictureEntity
import com.wakeup.data.database.entity.PictureEntity

@Dao
interface MomentDao {
    @Query("SELECT * FROM moment " +
            "WHERE mainAddress LIKE '%' || :query || '%' " +
            "or detailAddress LIKE '%' || :query || '%' " +
            "or content LIKE '%' || :query || '%'")
    fun getMoments(query: String): PagingSource<Int, MomentEntity>

    @Query(
        "SELECT * FROM picture WHERE id IN" +
                "(SELECT picture_id FROM moment_picture WHERE moment_id = :momentId)"
    )
    suspend fun getPictures(momentId: Long): List<PictureEntity>

    @Query(
        "SELECT * FROM globe WHERE id IN " +
                "(SELECT globe_id FROM moment_globe WHERE moment_id = :momentId)"
    )
    suspend fun getGlobes(momentId: Long): List<GlobeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMoment(moment: MomentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePicture(picture: List<PictureEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMomentPicture(momentPictures: List<MomentPictureEntity>)
}