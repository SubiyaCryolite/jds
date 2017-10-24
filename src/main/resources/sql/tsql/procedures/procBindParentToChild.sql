CREATE PROCEDURE procBindParentToChild(@ParentEntityCode BIGINT, @ChildEntityCode BIGINT)
AS
BEGIN
    MERGE JdsEntityInheritance AS dest
    USING (VALUES (@ParentEntityCode,@ChildEntityCode)) AS src([ParentEntityCode],[ChildEntityCode])
    ON (src.ParentEntityCode = dest.ParentEntityCode AND src.ChildEntityCode = dest.ChildEntityCode)
    WHEN NOT MATCHED THEN
        INSERT([ParentEntityCode], [ChildEntityCode]) VALUES(src.ParentEntityCode, src.ChildEntityCode);
END