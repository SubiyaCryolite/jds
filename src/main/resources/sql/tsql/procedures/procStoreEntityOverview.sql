CREATE PROCEDURE procStoreEntityOverview(@EntityGuid NVARCHAR(48), @ParentEntityGuid NVARCHAR(48), @DateCreated DATETIME, @DateModified DATETIME, @EntityId BIGINT)
AS
BEGIN
	MERGE JdsStoreEntityOverview AS dest
	USING (VALUES (@EntityGuid,@ParentEntityGuid,@DateCreated,@DateModified,@EntityId)) AS src([EntityGuid],[ParentEntityGuid],[DateCreated],[DateModified],[EntityId])
	ON (src.EntityGuid = dest.EntityGuid)
	WHEN MATCHED THEN
		UPDATE SET dest.[EntityId] = src.[EntityId]
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [ParentEntityGuid], [DateCreated], [DateModified], [EntityId]) VALUES(src.EntityGuid, src.ParentEntityGuid, src.DateCreated, src.DateModified, src.EntityId);
END