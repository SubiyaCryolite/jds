CREATE PROCEDURE procBindFieldTypes(@TypeId BIGINT, @TypeName NVARCHAR(MAX))
AS
BEGIN
    MERGE JdsRefFieldTypes AS dest
    USING (VALUES (@TypeId,@TypeName)) AS src([TypeId],[TypeName])
    ON (src.TypeId = dest.TypeId)
    WHEN NOT MATCHED THEN
        INSERT([TypeId], [TypeName]) VALUES(src.TypeId, src.TypeName)
    WHEN MATCHED THEN
        UPDATE SET dest.[TypeName] = src.[TypeName];
END