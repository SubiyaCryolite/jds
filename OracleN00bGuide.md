# Reference point for fellow n00bs

# Create tablespace and temp files
```sql
CREATE TABLESPACE jds_tabspace DATAFILE 'jds_tabspace.dat' SIZE 10M AUTOEXTEND ON;
CREATE TEMPORARY TABLESPACE jds_tabspace_temp TEMPFILE 'jds_tabspace_temp.dat' SIZE 5M AUTOEXTEND on;
```

# Create user
```sql
CREATE USER jds IDENTIFIED BY jds DEFAULT TABLESPACE jds_tabspace TEMPORARY TABLESPACE jds_tabspace_temp
```

# Assign necessary rights
```sql
GRANT CREATE SESSION TO jds;
GRANT CREATE TABLE TO jds;
GRANT CREATE VIEW TO jds;
GRANT CREATE PROCEDURE TO jds;
GRANT CREATE TRIGGER TO jds;
GRANT UNLIMITED TABLESPACE TO jds;
```

# Use this to drop ALL your tables
```sql
SELECT 'drop table '||table_name||' cascade constraints;' FROM user_tables;
```

# Use this to drop ALL your objects
```sql
SELECT 'drop '||object_type||' '|| object_name || ';' FROM user_objects WHERE object_type IN ('VIEW','PACKAGE','SEQUENCE', 'PROCEDURE', 'FUNCTION', 'INDEX')
```

# If you password expires, login as admin and run the command
```sql
ALTER USER jds IDENTIFIED BY jds;
```