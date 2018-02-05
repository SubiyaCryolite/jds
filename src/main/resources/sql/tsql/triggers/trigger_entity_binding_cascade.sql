--Based on https://www.mssqltips.com/sqlservertip/2733/solving-the-sql-server-multiple-cascade-path-issue-with-a-trigger/
CREATE TRIGGER trigger_entity_binding_cascade
  ON jds_entity_overview
  INSTEAD OF DELETE
AS
  BEGIN
    SET NOCOUNT ON;
    DELETE FROM jds_entity_binding
    WHERE child_composite_key IN (SELECT composite_key
                                  FROM DELETED)
    DELETE FROM jds_entity_binding
    WHERE parent_composite_key IN (SELECT composite_key
                                   FROM DELETED)
    DELETE FROM jds_entity_overview
    WHERE composite_key IN (SELECT composite_key
                            FROM DELETED)
  END