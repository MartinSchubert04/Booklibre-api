package com.BookLibre.fetcher

import com.BookLibre.domain.BookTypeAverageRating
import com.BookLibre.domain.CatalogHealth
import com.BookLibre.domain.ConversionRow
import com.BookLibre.domain.Leaderboard
import com.BookLibre.service.MetricService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class MetricFetcher(private val metricService: MetricService) {

    @DgsQuery
    fun leaderboard(): Leaderboard = metricService.leaderboard()

    @DgsQuery
    fun conversionRate(): List<ConversionRow> = metricService.conversionRate()

    @DgsQuery
    fun catalogHealth(): CatalogHealth = metricService.catalogHealth()

    @DgsQuery
    fun activityFeed(): List<Any> = metricService.activityFeed()

    @DgsQuery
    fun averageRatingByBookType(): List<BookTypeAverageRating> = metricService.averageRatingByBookType()

}