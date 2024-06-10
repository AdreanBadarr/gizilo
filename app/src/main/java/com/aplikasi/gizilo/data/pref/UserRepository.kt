package com.aplikasi.gizilo.data.pref

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.aplikasi.gizilo.data.api.ApiService
import com.aplikasi.gizilo.data.repository.Result
import com.aplikasi.gizilo.data.response.LoginResponse
import com.aplikasi.gizilo.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun saveAuthToken(token: String) {
        userPreference.saveAuthToken(token)
    }

    fun getAuthToken(): Flow<String?> {
        return userPreference.getAuthToken()
    }

    suspend fun saveLogin(loginSession: Boolean) {
        userPreference.saveLoginSession(loginSession)
    }

    fun getLogin(): Flow<Boolean> {
        return userPreference.getLoginSession()
    }

    suspend fun logout() {
        userPreference.clearData()
    }
    fun register(name:String,
                 email:String,
                 password:String):LiveData<Result<RegisterResponse>> = liveData{
        emit(Result.Loading)
        try {
            val response = apiService.doRegister(name,email,password)
            emit(Result.Success(response))
        }catch (e: HttpException){
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.doLogin(email, password)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun clearDataFactory() {
            instance = null
        }
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference)
            }.also { instance = it }
    }
}