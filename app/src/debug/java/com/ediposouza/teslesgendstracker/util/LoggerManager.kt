package com.ediposouza.teslesgendstracker.util

import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class LoggerManager : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String {
        val tag = super.createStackElementTag(element)
        val methodName = element.methodName
        val lineNumber = element.lineNumber
        return "$tag $methodName:$lineNumber"
    }

}