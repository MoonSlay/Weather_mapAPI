package ph.edu.auf.apidiscussion.api.models


import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("1h")
    val h: Double
)