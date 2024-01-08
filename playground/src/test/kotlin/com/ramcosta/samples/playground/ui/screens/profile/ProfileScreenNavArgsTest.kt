package com.ramcosta.samples.playground.ui.screens.profile

import androidx.compose.ui.graphics.Color
import com.ramcosta.samples.playground.ui.screens.navArgs
import com.ramcosta.samples.playground.ui.screens.navargs.toSavedStateHandle
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.Test

class ProfileScreenNavArgsTest {

    @Test
    fun testArgsToHandleToArgsAgain() {
        val initialArgs = ProfileScreenNavArgs(
            id = 3,
            whatever = null,
            groupName = "{groupName}",
            stuff = Stuff.STUFF2,
            things = Things(),
            color = Color.Black,
            valueClass = ValueClassArg("asd")
        )

        MatcherAssert
            .assertThat(
                initialArgs.toSavedStateHandle().navArgs<ProfileScreenNavArgs>(),
                IsEqual(initialArgs)
            )
    }
}