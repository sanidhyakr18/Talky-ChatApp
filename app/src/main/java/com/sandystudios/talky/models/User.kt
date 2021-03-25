package com.sandystudios.talky.models

const val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/chatapp-aef60.appspot.com/o/default%2Fdefault_avatar.png?alt=media&token=13c043d3-622b-48dc-a9a7-995fc6c9a021"

data class User(
    val name: String,
    val imageUrl: String,
    val thumbImage: String,
    val deviceToken: String,
    val about: String,
    val online: Boolean,
    val uid: String
) {
    /** Empty [Constructor] for Firebase */
    constructor() : this("", "", "", "", "", false, "")

    constructor(name: String, imageUrl: String, thumbImage: String, uid: String, about: String) :
            this(
                name,
                imageUrl,
                thumbImage,
                "",
                uid = uid,
                about = about,
                online = false
            )

    constructor(name: String, uid: String, about: String) :
            this(
                name,
                defaultImageUrl,
                defaultImageUrl,
                "",
                uid = uid,
                about = about,
                online = false
            )
    }