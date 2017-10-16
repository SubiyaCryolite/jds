CREATE PROCEDURE procBindFieldNames(pFieldId IN NUMBER, pFieldName IN NCLOB, pFieldDescription IN NCLOB)
AS
BEGIN
    MERGE INTO JdsRefFields dest
    USING DUAL ON (pFieldId = FieldId)
    WHEN NOT MATCHED THEN
        INSERT(FieldId, FieldName, FieldDescription) VALUES(pFieldId, pFieldName, pFieldDescription)
    WHEN MATCHED THEN
        UPDATE SET FieldName = pFieldName, FieldDescription = pFieldDescription;
END procBindFieldNames;