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

    companion object {
        const val TOKEN = "Bearer 1ofD5tbAoC0Xd0TCMcQG3U214MqUo7JzUWrQFWt1ugPuiiDmwQCImm9Giw7fwR0Y"
    }

    @GET("competitions")
    suspend fun getCompetitions(@Header("Authorization") token: String = TOKEN): List<Competition>

    @GET("competitors")
    suspend fun getCompetitors(@Header("Authorization") token: String = TOKEN): List<Competitor>

    @GET("courses")
    suspend fun getCourses(@Header("Authorization") token: String = TOKEN): List<Courses>

    @GET("obstacles")
    suspend fun getObstacles(@Header("Authorization") token: String = TOKEN): List<Obstacles>

    @GET("courses/{id}/obstacles")
    suspend fun getCourseObstacles(
        @Path("id") courseId: Int,
        @Header("Authorization") token: String = TOKEN

    ): List<ObstacleCourse>

    @GET("courses/{id}/unused_obstacles")
    suspend fun getUnusedObstacles(
        @Path("id") courseId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): List<Obstacles>

    @GET("competitions/{id}/inscriptions")
    suspend fun getCompetitorsByCompetition(
        @Path("id") competitionId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): List<Competitor>

    @GET("competitions/{id}")
    suspend fun getCompetitionDetails(
        @Path("id") competitionId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): Competition

    @GET("competitors")
    suspend fun getAllCompetitors(
        @Header("Authorization") token:  String = TOKEN
    ): List<Competitor>

    @GET("competitions/{id}/courses")
    suspend fun getCompetitionCourses(
        @Path("id") competitionId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): List<Courses>

    @POST("obstacles")
    suspend fun addObstacles(
        @Body obstacles: Obstacles,
        @Header("Authorization") token:  String = TOKEN,

    ): Obstacles

    @POST("competitors")
    suspend fun addCompetitor(
        @Body competitor: Competitor,
        @Header("Authorization") token:  String = TOKEN

    ): Competitor

    @POST("competitions")
    suspend fun addCompetition(
        @Body competition: Competition,
        @Header("Authorization") token:  String = TOKEN

    ): Competition



    @POST("competitions/{id}/add_competitor")
    suspend fun addCompetitorToCompetition(
        @Path("id") competitionId: Int,
        @Body competitorId: AddCompetitorRequest,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @POST("courses")
    suspend fun addCourse(
        @Body course: CreateCourseRequest,
        @Header("Authorization") token:  String = TOKEN

    ): Courses

    @POST("courses/{courseId}/add_obstacle")
    suspend fun addObstacleToCourse(
        @Path("courseId") courseId: Int,
        @Body request: AddObstacleRequest,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @POST("courses/{courseId}/update_obstacle_position")
    suspend fun updateObstaclePosition(
        @Path("courseId") courseId: Int,
        @Body request: UpdateObstaclePositionRequest,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @DELETE("competitors/{id}")
    suspend fun deleteCompetitor(
        @Path("id") competitorId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @DELETE("obstacles/{id}")
    suspend fun deleteObstacles(
        @Path("id") obstaclesId: Int,
        @Header("Authorization") token:  String = TOKEN
    ): Response<Unit>

    @DELETE("competitions/{id}")
    suspend fun deleteCompetition(
        @Path("id") competitionId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @DELETE("competitions/{id}/remove_competitor/{id_competitor}")
    suspend fun removeCompetitorFromCompetition(
        @Path("id") competitionId: Int,
        @Path("id_competitor") competitorId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @DELETE("courses/{id}")
    suspend fun deleteCourse(
        @Path("id") courseId: Int,
        @Header("Authorization") token:  String = TOKEN

    ): Response<Unit>

    @DELETE("courses/{courseId}/remove_obstacle/{obstacleId}")
    suspend fun removeObstacleFromCourse(
        @Path("courseId") courseId: Int,
        @Path("obstacleId") obstacleId: Int,
        @Header("Authorization") token: String = TOKEN

    ): Response<Unit>

    @PUT("competitors/{id}")
    suspend fun updateCompetitor(
        @Path("id") competitorId: Int,
        @Body competitor: Competitor,
        @Header("Authorization") token: String = TOKEN
    ): Competitor

    @PUT("obstacles/{id}")
    suspend fun updateObstacles(
        @Path("id") obstaclesId: Int,
        @Body obstacles: Obstacles,
        @Header("Authorization") token: String = TOKEN
    ): Obstacles

    @PUT("competitions/{id}")
    suspend fun updateCompetition(
        @Path("id") competitionId: Int,
        @Body competition: Competition,
        @Header("Authorization") token: String = TOKEN

    ): Competition

    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Path("id") courseId: Int,
        @Body course: CourseUpdateRequest,
        @Header("Authorization") token: String = TOKEN

    ): Courses

}