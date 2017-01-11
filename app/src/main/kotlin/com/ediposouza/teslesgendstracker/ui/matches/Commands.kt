package com.ediposouza.teslesgendstracker.ui.matches

import com.ediposouza.teslesgendstracker.data.MatchMode
import com.ediposouza.teslesgendstracker.data.Season

/**
 * Created by EdipoSouza on 1/8/17.
 */
class CmdUpdateMatches

data class CmdFilterMode(val mode: MatchMode)

data class CmdFilterSeason(val season: Season?)

