CREATE PROCEDURE procRefEntities(@EntityId BIGINT, @EntityName NVARCHAR(MAX))
AS
BEGIN
    MERGE JdsEntities AS dest
    USING (VALUES (@EntityId,@EntityName)) AS src([EntityId],[EntityName])
    ON (src.EntityId = dest.EntityId)
    WHEN NOT MATCHED THEN
        INSERT([EntityId], [EntityName]) VALUES(src.EntityId, src.EntityName)
    WHEN MATCHED THEN
        UPDATE SET dest.[EntityName] = src.[EntityName];
END