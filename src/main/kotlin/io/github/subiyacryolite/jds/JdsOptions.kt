package io.github.subiyacryolite.jds

class JdsOptions {
    /**
     * A value indicating whether JDS should print internal log information
     */
    var isPrintingOutput: Boolean = false

    /**
     * Indicate whether JDS is persisting to the primary data tables
     */
    var isWritingToPrimaryDataTables = true

    /**
     * Indicate if JDS should write data to overview fields
     */
    var isWritingOverviewFields = true

    /**
     * Indicate if JDS should write array types to the DB
     */
    var isWritingArrayValues = true

    /**
     * Indicates if load operations should initialise java primitive types
     */
    var initialisePrimitives = true

    /**
     * Indicates if load operations should initialise java date and time types
     */
    var initialiseDatesAndTimes = true

    /**
     * Indicates if load operations should initialise embedded object and object collections
     */
    var initialiseObjects = true

    /**
     * Indicates if JDS should write to custom reporting tables
     */
    var isWritingToReportingTables = true

    /**
     * Indicates if JDS should write parent to child entity bindings. This is nexessary when saving in EAV mode, and can be skipped to save hard-drive space when using nested portable containers persisted in a format such as JSON
     */
    var isWritingEntityBindings = true
}