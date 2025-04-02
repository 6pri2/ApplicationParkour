package iut.gon.applicationparkour.data.api

import iut.gon.applicationparkour.data.model.AddCompetitorRequest
import iut.gon.applicationparkour.data.model.AddObstacleRequest
import iut.gon.applicationparkour.data.model.Competition
import iut.gon.applicationparkour.data.model.Competitor
import iut.gon.applicationparkour.data.model.CourseUpdateRequest
import iut.gon.applicationparkour.data.model.Courses
import iut.gon.applicationparkour.data.model.CreateCourseRequest
import iut.gon.applicationparkour.data.model.ObstacleCourse
import iut.gon.applicationparkour.data.model.Obstacles
import iut.gon.applicationparkour.data.model.UpdateObstaclePositionRequest
import iut.gon.applicationparkour.ui.screens.Performance
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface de l'API
 */

interface ApiService {
    @GET("competitions")
    suspend fun getCompetitions(@Header("Authorization") token: String): List<Competition>

    @GET("competitors")
    suspend fun getCompetitors(@Header("Authorization") token: String): List<Competitor>

    @GET("courses")
    suspend fun getCourses(@Header("Authorization") token: String): List<Courses>

    @GET("obstacles")
    suspend fun getObstacles(@Header("Authorization") token: String): List<Obstacles>

    @GET("courses/{id}/obstacles")
    suspend fun getCourseObstacles(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): List<ObstacleCourse>

    @GET("courses/{id}/unused_obstacles")
    suspend fun getUnusedObstacles(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): List<Obstacles>

    @GET("competitions/{id}/inscriptions")
    suspend fun getCompetitorsByCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): List<Competitor>

    @GET("competitions/{id}")
    suspend fun getCompetitionDetails(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): Competition

    @GET("competitors")
    suspend fun getAllCompetitors(
        @Header("Authorization") token: String
    ): List<Competitor>

    @GET("competitions/{id}/courses")
    suspend fun getCompetitionCourses(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): List<Courses>

    @GET("performances")
    suspend fun getPerformances(
        @Header("Authorization") token: String
    ): List<Performance>

    @POST("obstacles")
    suspend fun addObstacles(
        @Header("Authorization") token: String,
        @Body obstacles: Obstacles
    ): Obstacles

    @POST("competitors")
    suspend fun addCompetitor(
        @Header("Authorization") token: String,
        @Body competitor: Competitor
    ): Competitor

    @POST("competitions")
    suspend fun addCompetition(
        @Header("Authorization") token: String,
        @Body competition: Competition
    ): Competition



    @POST("competitions/{id}/add_competitor")
    suspend fun addCompetitorToCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int,
        @Body competitorId: AddCompetitorRequest
    ): Response<Unit>

    @POST("courses")
    suspend fun addCourse(
        @Header("Authorization") token: String,
        @Body course: CreateCourseRequest
    ): Courses

    @POST("courses/{courseId}/add_obstacle")
    suspend fun addObstacleToCourse(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Body request: AddObstacleRequest
    ): Response<Unit>

    @POST("courses/{courseId}/update_obstacle_position")
    suspend fun updateObstaclePosition(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Body request: UpdateObstaclePositionRequest
    ): Response<Unit>

    @DELETE("competitors/{id}")
    suspend fun deleteCompetitor(
        @Header("Authorization") token: String,
        @Path("id") competitorId: Int
    ): Response<Unit>

    @DELETE("obstacles/{id}")
    suspend fun deleteObstacles(
        @Header("Authorization") token: String,
        @Path("id") obstaclesId: Int
    ): Response<Unit>

    @DELETE("competitions/{id}")
    suspend fun deleteCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int
    ): Response<Unit>

    @DELETE("competitions/{id}/remove_competitor/{id_competitor}")
    suspend fun removeCompetitorFromCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int,
        @Path("id_competitor") competitorId: Int
    ): Response<Unit>

    @DELETE("courses/{id}")
    suspend fun deleteCourse(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int
    ): Response<Unit>

    @DELETE("courses/{courseId}/remove_obstacle/{obstacleId}")
    suspend fun removeObstacleFromCourse(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: Int,
        @Path("obstacleId") obstacleId: Int
    ): Response<Unit>

    @PUT("competitors/{id}")
    suspend fun updateCompetitor(
        @Header("Authorization") token: String,
        @Path("id") competitorId: Int,
        @Body competitor: Competitor
    ): Competitor

    @PUT("obstacles/{id}")
    suspend fun updateObstacles(
        @Header("Authorization") token: String,
        @Path("id") obstaclesId: Int,
        @Body obstacles: Obstacles
    ): Obstacles

    @PUT("competitions/{id}")
    suspend fun updateCompetition(
        @Header("Authorization") token: String,
        @Path("id") competitionId: Int,
        @Body competition: Competition
    ): Competition

    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Header("Authorization") token: String,
        @Path("id") courseId: Int,
        @Body course: CourseUpdateRequest
    ): Courses

}