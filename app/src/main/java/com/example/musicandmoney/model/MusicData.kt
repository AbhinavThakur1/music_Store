package com.example.musicandmoney.model


import android.os.Parcel
import android.os.Parcelable

data class MusicData(
    val path : String,
    val title : String,
    val duration: String,
    val artistName : String,
    val fileName : String
    ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(title)
        parcel.writeString(duration)
        parcel.writeString(artistName)
        parcel.writeString(fileName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MusicData> {
        override fun createFromParcel(parcel: Parcel): MusicData {
            return MusicData(parcel)
        }

        override fun newArray(size: Int): Array<MusicData?> {
            return arrayOfNulls(size)
        }
    }
}

