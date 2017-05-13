package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import com.ediposouza.teslesgendstracker.ui.base.BaseParcelable
import com.ediposouza.teslesgendstracker.ui.base.read
import com.ediposouza.teslesgendstracker.ui.base.write

/**
 * Created by ediposouza on 25/01/17.
 */
enum class MatchMode {

    RANKED,
    CASUAL,
    ARENA

}

data class MatchDeck(

        val name: String? = null,
        val cls: DeckClass,
        val type: DeckType,
        val deck: String? = null,
        val version: String? = null

) : BaseParcelable {

    companion object {
        @JvmField val CREATOR = BaseParcelable.generateCreator {
            MatchDeck(it.read(), DeckClass.values()[it.readInt()], DeckType.values()[it.readInt()],
                    it.read(), it.read())
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.write(name)
        dest?.write(cls.ordinal)
        dest?.write(type.ordinal)
        dest?.write(deck)
        dest?.write(version)
    }
}

data class Match(

        val uuid: String,
        val first: Boolean,
        val player: MatchDeck,
        val opponent: MatchDeck,
        val mode: MatchMode,
        val season: String,
        val rank: Int,
        val legend: Boolean,
        val win: Boolean

) : BaseParcelable {

    companion object {
        @JvmField val CREATOR = BaseParcelable.generateCreator {
            Match(it.read(), 1 == it.readInt(), it.read(), it.read(), MatchMode.values()[it.read()],
                    it.read(), it.read(), 1 == it.readInt(), 1 == it.readInt())
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.write(uuid)
        dest?.writeInt(if (first) 1 else 0)
        dest?.write(player)
        dest?.write(opponent)
        dest?.write(mode.ordinal)
        dest?.write(season)
        dest?.write(rank)
        dest?.writeInt(if (legend) 1 else 0)
        dest?.writeInt(if (win) 1 else 0)
    }
}