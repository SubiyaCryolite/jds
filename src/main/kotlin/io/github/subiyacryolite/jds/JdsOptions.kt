package io.github.subiyacryolite.jds

import java.io.Serializable

data class JdsOptions(
        /**
         * A value indicating whether JDS should print internal log information
         */
        var isLoggingOutput: Boolean = false,

        /**
         * Indicates whether JDS is persisting values to the EAV jds_str_* tables
         */
        var isWritingValuesToEavTables: Boolean = true,

        /**
         * Indicates whether JDS is persisting collections to the EAV jds_str_*_col tables
         */
        var isWritingCollectionsToEavTables: Boolean = true,

        /**
         * Indicates if load operations should initialise java primitive types
         */
        var initialisePrimitives: Boolean = true,

        /**
         * Indicates if load operations should initialise java date and time types
         */
        var initialiseDatesAndTimes: Boolean = true,

        /**
         * Indicates if load operations should initialise embedded object and object collections
         */
        var initialiseObjects: Boolean = true,

        /**
         * Indicates if JDS should write to custom reporting tables
         */
        var isWritingToReportingTables: Boolean = true,

        /**
         * Indicates if JDS should write parent to child entity bindings. This is necessary when saving in EAV mode, and can be skipped to save hard-drive space when using nested portable containers persisted in a format such as JSON
         */
        var isWritingEntityBindings: Boolean = true,

        /**
         * Indicates if JDS should delete old data from report tables after every save. For bulk saves you can disable this
         * property and manually call [deleteOldDataFromReportTables][JdsDb.deleteOldDataFromReportTables] from [JdsDb][JdsDb]
         */
        var isDeletingOldDataFromReportTablesAfterSave: Boolean = true,

        /**
         * Indicates if JDS is writing the latest version to the jds_entity_live_version table
         */
        var isWritingLatestEntityVersion: Boolean = true,

        var isWritingToOverviewTable: Boolean = true) : Serializable