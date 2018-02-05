--Based on https://www.mssqltips.com/sqlservertip/2733/solving-the-sql-server-multiple-cascade-path-issue-with-a-trigger/
CREATE TRIGGER trigger_entity_binding_cascade
  ON jds_entity_overview
  INSTEAD OF DELETE
AS
  BEGIN
    SET NOCOUNT ON;
    DELETE FROM jds_entity_binding
    WHERE child_uuid IN (SELECT uuid
                         FROM DELETED)
    DELETE FROM jds_entity_binding
    WHERE parent_uuid IN (SELECT uuid
                          FROM DELETED)
    DELETE FROM jds_entity_overview
    WHERE uuid IN (SELECT uuid
                   FROM DELETED)
  END