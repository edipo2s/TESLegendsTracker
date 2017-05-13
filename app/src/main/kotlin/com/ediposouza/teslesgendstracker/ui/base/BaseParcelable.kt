package com.ediposouza.teslesgendstracker.ui.base

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ediposouza on 19/04/17.
 */
interface BaseParcelable : Parcelable {

    companion object {
        fun <T> generateCreator(create: (source: Parcel) -> T): Parcelable.Creator<T> = object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T = create(source)

            override fun newArray(size: Int): Array<out T>? = newArray(size)
        }

    }

    override fun describeContents(): Int = 0

}

inline fun <reified T> Parcel.read(): T = readValue(T::class.javaClass.classLoader) as T

fun Parcel.write(vararg values: Any?) = values.forEach { writeValue(it) }