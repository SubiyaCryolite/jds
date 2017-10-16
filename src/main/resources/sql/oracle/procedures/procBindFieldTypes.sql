CREATE PROCEDURE procBindFieldTypes(pTypeId IN NUMBER, pTypeName IN NCLOB)
AS
BEGIN
    MERGE INTO JdsRefFieldTypes dest
    USING DUAL ON (pTypeId = TypeId)
    WHEN NOT MATCHED THEN
        INSERT(TypeId, TypeName) VALUES(pTypeId, pTypeName)
    WHEN MATCHED THEN
        UPDATE SET TypeName = pTypeName;
END procBindFieldTypes;