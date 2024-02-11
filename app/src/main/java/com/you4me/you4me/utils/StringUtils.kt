package com.you4me.you4me.utils

import android.util.Patterns

fun CharSequence?.validateEmail() = !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun CharSequence?.validatePassword() = !isNullOrBlank() && this.length >= 3