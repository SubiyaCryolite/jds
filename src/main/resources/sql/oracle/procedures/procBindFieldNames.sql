CREATE PROCEDURE procBindFieldNames(pFieldId IN NUMBER, pFieldName IN NCLOB)
AS
BEGIN
    MERGE INTO JdsRefFields dest
    USING DUAL ON (pFieldId = FieldId)
    WHEN NOT MATCHED THEN
        INSERT(FieldId, FieldName) VALUES(pFieldId, pFieldName)
    WHEN MATCHED THEN
        UPDATE SET FieldName = pFieldName;
END procBindFieldNames;