CREATE PROCEDURE procJdsRefEntityOverview(@EntityGuid NVARCHAR(48), @EntityId BIGINT, @DateCreated DATETIME, @DateModified DATETIME)
AS
BEGIN
	MERGE JdsRefEntityOverview AS dest
	USING (VALUES (@EntityGuid,  @EntityId, @DateCreated, @DateModified)) AS src([EntityGuid],  [EntityId], [DateCreated], [DateModified])
	ON (src.EntityGuid = dest.EntityGuid AND src.EntityId = dest.EntityId)
	WHEN MATCHED THEN
		UPDATE SET dest.[DateCreated] = src.[DateCreated], dest.[DateModified] = src.[DateModified]
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [EntityId], [DateCreated], [DateModified])   VALUES(src.EntityGuid, src.EntityId, src.DateCreated, src.DateModified);
END