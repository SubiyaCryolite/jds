CREATE PROCEDURE procStoreEntityInheritance(@EntityGuid NVARCHAR(48), @EntityId BIGINT)
AS
BEGIN
	MERGE JdsStoreEntityInheritance AS dest
	USING (VALUES (@EntityGuid, @EntityId)) AS src([EntityGuid], [EntityId])
	ON (src.EntityGuid = dest.EntityGuid AND src.EntityId = dest.EntityId)
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [EntityId]) VALUES(src.EntityGuid, src.EntityId);
END