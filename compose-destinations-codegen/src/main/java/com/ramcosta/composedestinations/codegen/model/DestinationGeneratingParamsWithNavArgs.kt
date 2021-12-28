package com.ramcosta.composedestinations.codegen.model

data class DestinationGeneratingParamsWithNavArgs(
    val navArgs: List<Parameter>,
    val destinationGeneratingParams: DestinationGeneratingParams
) : DestinationGeneratingParams by destinationGeneratingParams