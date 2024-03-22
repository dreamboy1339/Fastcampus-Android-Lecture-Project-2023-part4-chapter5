package kr.co.fastcampus.part4.part5_7.service

import kr.co.fastcampus.part4.part5_7.model.Repo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {
    @GET("users/{user}/repos")
    suspend fun listRepos(
        @Path("user") user: String,
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc"
    ): List<Repo>
}