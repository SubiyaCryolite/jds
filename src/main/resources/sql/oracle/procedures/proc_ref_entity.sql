CREATE PROCEDURE proc_ref_entity(pid IN NUMBER, pcaption IN NCLOB)
AS
  BEGIN
    MERGE INTO jds_ref_entity dest
    USING DUAL
    ON (pid = id)
    WHEN NOT MATCHED THEN
      INSERT (id, caption) VALUES (pid, pcaption)
    WHEN MATCHED THEN
      UPDATE SET caption = pcaption;
  END proc_ref_entity;