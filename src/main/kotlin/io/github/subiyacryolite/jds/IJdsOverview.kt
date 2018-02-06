/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds;

import java.time.LocalDateTime;

interface IJdsOverview {

    var entityId: Long

    /**
     * The unique identifier of this record
     */
    var uuid: String

    /**
     * A unique flag to identify where this record was modified e.g ip address, terminal identifier, any other id
     */
    var uuidLocation: String

    /**
     * A value indicating the version (number of edits) of this record at a location
     */
    var uuidLocationVersion: Int

    /**
     * A composite key for this unique record at different locations and for each version
     */
    val compositeKey: String

    /**
     * A run-time only value that indicates the parent of this JdsEntity. Its value is populated at Save and at Load.
     */
    var parentUuid: String?

    /**
     * A run-time only value that indicates the parent of this JdsEntity. Its value is populated at Save and at Load.
     */
    var parentCompositeKey: String?

    /**
     * A flag indicating if this record is live or deprecated
     */
    var live: Boolean

    /**
     * A flag indicating the version of this [JdsEntity]
     */
    var version: Long

    /**
     * Indicates when this record was last edited
     */
    var lastEdit: LocalDateTime
}
