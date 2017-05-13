package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageView
import com.ediposouza.teslesgendstracker.ui.base.BaseParcelable
import com.ediposouza.teslesgendstracker.ui.base.read
import com.ediposouza.teslesgendstracker.ui.base.write
import org.threeten.bp.LocalDate

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
        val shortName: String,
        val change: String

) : BaseParcelable {

    companion object {
        @JvmField val CREATOR = BaseParcelable.generateCreator {
            PatchChange(it.read(), it.read(), it.read(), it.read())
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.write(attr)
        dest?.write(set)
        dest?.write(shortName)
        dest?.write(change)
    }

    fun newImageBitmap(view: ImageView, nextPatchUuid: String) {
        val cardName = shortName + "_" + nextPatchUuid
        Card.loadCardImageInto(view, set.capitalize(), attr.capitalize(), cardName) {
            view.post {
                Card.loadCardImageInto(view, set.capitalize(), attr.capitalize(), shortName)
            }
        }
    }

    fun oldImageBitmap(view: ImageView, patchUuid: String) {
        val cardName = shortName + "_" + patchUuid
        Card.loadCardImageInto(view, set.capitalize(), attr.capitalize(), cardName)
    }

}

data class Patch(

        val uuidDate: String,
        val date: LocalDate,
        val desc: String,
        val legendsDeck: String,
        val type: PatchType,
        val changes: List<PatchChange>

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Patch> = object : Parcelable.Creator<Patch> {
            override fun createFromParcel(source: Parcel): Patch = Patch(source)
            override fun newArray(size: Int): Array<Patch?> = arrayOfNulls(size)
        }

        val DUMMY = Patch("", LocalDate.now(), "", "", PatchType.UNKNOWN, listOf())
    }

    constructor(source: Parcel) : this(source.readString(), LocalDate.ofEpochDay(source.readLong()),
            source.readString(), source.readString(), PatchType.values()[source.readInt()],
            mutableListOf<PatchChange>().apply { source.readList(this, PatchChange::class.java.classLoader) })

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(uuidDate)
        dest?.writeLong(date.toEpochDay())
        dest?.writeString(desc)
        dest?.writeString(legendsDeck)
        dest?.writeInt(type.ordinal)
        dest?.writeList(changes)
    }

    override fun describeContents() = 0

}