package com.you4me.you4me.models

data class GetTokenResponse(
    val token: String
)

data class RequestPasswordResetResponse(
    val message: String
)

data class VerifyCodeResponse(
    val reset_token: String
)

data class ResetPasswordResponse(
    val message: String
)