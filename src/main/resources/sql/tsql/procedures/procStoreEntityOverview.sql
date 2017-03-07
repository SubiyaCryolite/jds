CREATE PROCEDURE procStoreEntityOverview(@EntityGuid NVARCHAR(48), @DateCreated DATETIME, @DateModified DATETIME, @EntityId BIGINT)
AS
BEGIN
	MERGE JdsStoreEntityOverview AS dest
	USING (VALUES (@EntityGuid,@DateCreated,@DateModified,@EntityId)) AS src([EntityGuid],[DateCreated],[DateModified],[EntityId])
	ON (src.EntityGuid = dest.EntityGuid)
	WHEN MATCHED THEN
		UPDATE SET dest.[EntityId] = src.[EntityId]
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [DateCreated], [DateModified], [EntityId]) VALUES(src.EntityGuid, src.DateCreated, src.DateModified, src.EntityId);
END