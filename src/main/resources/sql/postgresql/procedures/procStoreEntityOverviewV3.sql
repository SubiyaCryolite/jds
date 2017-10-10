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
CREATE FUNCTION procStoreEntityOverviewV3(pEntityGuid VARCHAR(48), pDateCreated TIMESTAMP, pDateModified TIMESTAMP, pLive BOOLEAN, pVersion BIGINT)
RETURNS VOID AS $$
BEGIN
	INSERT INTO JdsStoreEntityOverview(EntityGuid, DateCreated, DateModified, Live, Version)
    VALUES (pEntityGuid, pDateCreated, pDateModified, pLive, pVersion)
    ON CONFLICT (EntityGuid) DO UPDATE SET DateModified = pDateModified, Live = pLive, Version = pVersion;
END;
$$ LANGUAGE plpgsql;