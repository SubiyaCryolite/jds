CREATE PROCEDURE procStoreEntityInheritance(@Uuid NVARCHAR(48), @EntityId BIGINT)
AS
BEGIN
	MERGE JdsEntityInstance AS dest
	USING (VALUES (@Uuid, @EntityId)) AS src([Uuid], [EntityId])
	ON (src.Uuid = dest.Uuid AND src.EntityId = dest.EntityId)
	WHEN NOT MATCHED THEN
		INSERT([Uuid], [EntityId]) VALUES(src.Uuid, src.EntityId);
END