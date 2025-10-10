package com.example.codeforces.api

import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.BlogEntry
import com.example.codeforces.models.Comment
import com.example.codeforces.models.Contest
import com.example.codeforces.models.ContestStandingsResponse
import com.example.codeforces.models.ProblemSetResponse
import com.example.codeforces.models.RatingChange
import com.example.codeforces.models.Submission
import com.example.codeforces.models.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApi {

    // ----- Blog Methods -----
    @GET("blogEntry.comments")
    fun getBlogComments(
        @Query("blogEntryId") blogEntryId: Int
    ): Call<ApiResponse<List<Comment>>>

    @GET("blogEntry.view")
    fun getBlogEntry(
        @Query("blogEntryId") blogEntryId: Int
    ): Call<ApiResponse<BlogEntry>>

    // ----- Contest Methods -----
    @GET("contest.list")
    fun getContestList(
        @Query("gym") gym: Boolean = false,
        @Query("groupCode") groupCode: String? = null
    ): Call<ApiResponse<List<Contest>>>

    @GET("contest.ratingChanges")
    fun getContestRatingChanges(
        @Query("contestId") contestId: Int
    ): Call<ApiResponse<List<RatingChange>>>

    @GET("contest.standings")
    fun getContestStandings(
        @Query("contestId") contestId: Int,
        @Query("asManager") asManager: Boolean = false,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 10,
        @Query("handles") handles: String? = null,
        @Query("room") room: Int? = null,
        @Query("showUnofficial") showUnofficial: Boolean = false,
        @Query("participantTypes") participantTypes: String? = null
    ): Call<ApiResponse<ContestStandingsResponse>>

    @GET("contest.status")
    fun getContestStatus(
        @Query("contestId") contestId: Int,
        @Query("handle") handle: String? = null,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 50,
        @Query("includeSources") includeSources: Boolean = false
    ): Call<ApiResponse<List<Submission>>>

    // ----- Problemset Methods -----
    @GET("problemset.problems")
    fun getProblemSet(
        @Query("tags") tags: String? = null,
        @Query("problemsetName") problemsetName: String? = null
    ): Call<ApiResponse<ProblemSetResponse>>

    @GET("problemset.recentStatus")
    fun getRecentProblemStatus(
        @Query("count") count: Int,
        @Query("problemsetName") problemsetName: String? = null
    ): Call<ApiResponse<List<Submission>>>


    // ----- User Methods -----
    @GET("user.blogEntries")
    fun getUserBlogEntries(
        @Query("handle") handle: String
    ): Call<ApiResponse<List<BlogEntry>>>

    @GET("user.friends")
    fun getUserFriends(
        @Query("onlyOnline") onlyOnline: Boolean = false
    ): Call<ApiResponse<List<String>>>

    @GET("user.info")
    fun getUserInfo(
        @Query("handles") handles: String,
        @Query("checkHistoricHandles") checkHistoricHandles: Boolean = true
    ): Call<ApiResponse<List<User>>>

    @GET("user.ratedList")
    fun getUserRatedList(
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("includeRetired") includeRetired: Boolean = false,
        @Query("contestId") contestId: Int? = null
    ): Call<ApiResponse<List<User>>>

    @GET("user.rating")
    fun getUserRating(
        @Query("handle") handle: String
    ): Call<ApiResponse<List<RatingChange>>>

    @GET("user.status")
    fun getUserSubmissions(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 50,
        @Query("includeSources") includeSources: Boolean = false
    ): Call<ApiResponse<List<Submission>>>
}
