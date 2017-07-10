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
CREATE PROCEDURE procStoreEntityInheritance(@EntityGuid NVARCHAR(48), @EntityId BIGINT)
AS
BEGIN
	MERGE JdsStoreEntityInheritance AS dest
	USING (VALUES (@EntityGuid, @EntityId)) AS src([EntityGuid], [EntityId])
	ON (src.EntityGuid = dest.EntityGuid AND src.EntityId = dest.EntityId)
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [EntityId]) VALUES(src.EntityGuid, src.EntityId);
END