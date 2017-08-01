/*
*    Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
*    1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
*    2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*    3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*    Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
*    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
*    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*    OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
*    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
BEGIN;
    CREATE TABLE JdsStoreOldFieldValues (
      EntityGuid         NVARCHAR(48),
      FieldId            BIGINT,
      DateOfModification DATETIME DEFAULT GETDATE(),
      Sequence           INTEGER,
      TextValue          NVARCHAR(MAX),
      IntegerValue       INTEGER,
      FloatValue         REAL,
      DoubleValue        FLOAT,
      LongValue          INTEGER,
      DateTimeValue      DATETIME,
      BlobValue          VARBINARY(MAX)
    );
    CREATE NONCLUSTERED INDEX IntegerValues  ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, IntegerValue);
    CREATE NONCLUSTERED INDEX FloatValues    ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, FloatValue);
    CREATE NONCLUSTERED INDEX DoubleValues   ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DoubleValue);
    CREATE NONCLUSTERED INDEX LongValues     ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, LongValue);
    CREATE NONCLUSTERED INDEX DateTimeValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence, DateTimeValue);
    CREATE NONCLUSTERED INDEX TextBlobValues ON JdsStoreOldFieldValues(EntityGuid, FieldId, Sequence);
END;