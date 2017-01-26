package com.ediposouza.teslesgendstracker.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by ediposouza on 25/01/17.
 */
enum class PatchType {

    BALANCE,
    REWARD,
    SET,
    UNKNOWN;

    companion object {

        fun of(value: String): PatchType {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

class PatchChange(

        val attr: String,
        val set: String,
        val shortName: String

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PatchChange> = object : Parcelable.Creator<PatchChange> {
            override fun createFromParcel(source: Parcel): PatchChange = PatchChange(source)
            override fun newArray(size: Int): Array<PatchChange?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(attr)
        dest?.writeString(set)
        dest?.writeString(shortName)
    }

    override fun describeContents(): Int = 0

    fun newImageBitmap(context: Context, nextPatchUuid: String): Bitmap {
        val cardName = shortName + "_" + nextPatchUuid
        return Card.getCardImageBitmap(context, set.capitalize(), attr.capitalize(), cardName) {
            Card.getCardImageBitmap(context, set.capitalize(), attr.capitalize(), shortName)
        }
    }

    fun oldImageBitmap(context: Context, patchUuid: String): Bitmap {
        val cardName = shortName + "_" + patchUuid
        return Card.getCardImageBitmap(context, set.capitalize(), attr.capitalize(), cardName)
    }

}

data class Patch(

        val uuidDate: String,
        val date: LocalDate,
        val desc: String,
        val type: PatchType,
        val changes: List<PatchChange>

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Patch> = object : Parcelable.Creator<Patch> {
            override fun createFromParcel(source: Parcel): Patch = Patch(source)
            override fun newArray(size: Int): Array<Patch?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), LocalDate.ofEpochDay(source.readLong()),
            source.readString(), PatchType.values()[source.readInt()],
            ArrayList<PatchChange>().apply { source.readList(this, PatchChange::class.java.classLoader) })

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(uuidDate)
        dest?.writeLong(date.toEpochDay())
        dest?.writeString(desc)
        dest?.writeInt(type.ordinal)
        dest?.writeList(changes)
    }

    override fun describeContents() = 0

}