package com.you4me.you4me.models

data class CloudinaryVideoUploadResponse(
    val access_mode: String,
    val asset_id: String,
    val bytes: Int,
    val created_at: String,
    val etag: String,
    val format: String,
    val height: Int,
    val public_id: String,
    val resource_type: String,
    val secure_url: String,
    val signature: String,
    val type: String,
    val url: String,
    val version: Int,
    val width: Int
) {
    companion object {
        fun from(map: Map<Any?, Any?>) = object {
            val access_mode by map
            val asset_id by map
            val bytes by map
            val created_at by map
            val etag by map
            val format by map
            val height by map
            val public_id by map
            val resource_type by map
            val secure_url by map
            val signature by map
            val type by map
            val url by map
            val version by map
            val width by map

            val data = CloudinaryVideoUploadResponse(
                access_mode.toString(),
                asset_id.toString(),
                bytes.toString().toInt(),
                created_at.toString(),
                etag.toString(),
                format.toString(),
                height.toString().toInt(),
                public_id.toString(),
                resource_type.toString(),
                secure_url.toString(),
                signature.toString(),
                type.toString(),
                url.toString(),
                version.toString().toInt(),
                width.toString().toInt()
            )
        }.data
    }
}