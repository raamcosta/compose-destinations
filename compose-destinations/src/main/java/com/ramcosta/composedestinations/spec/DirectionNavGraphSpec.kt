package com.ramcosta.composedestinations.spec

interface DirectionNavGraphSpec: TypedNavGraphSpec<Unit>, Direction {

    override fun invoke(navArgs: Unit): Direction = this

    operator fun invoke(): Direction = this
}
