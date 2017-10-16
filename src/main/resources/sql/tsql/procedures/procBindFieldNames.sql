CREATE PROCEDURE procBindFieldNames(@FieldId BIGINT, @FieldName NVARCHAR(MAX))
AS
BEGIN
    MERGE JdsRefFields AS dest
    USING (VALUES (@FieldId,@FieldName)) AS src([FieldId],[FieldName])
    ON (src.FieldId = dest.FieldId)
    WHEN NOT MATCHED THEN
        INSERT([FieldId], [FieldName]) VALUES(src.FieldId, src.FieldName)
    WHEN MATCHED THEN
        UPDATE SET dest.[FieldName] = src.[FieldName];
END