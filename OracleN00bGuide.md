# Reference point for fellow n00bs

# Create tablespace and temp files
```sql
create tablespace jds_tabspace datafile 'jds_tabspace.dat' size 10M autoextend on;
create temporary tablespace jds_tabspace_temp tempfile 'jds_tabspace_temp.dat' size 5M autoextend on;
```

# Create user
```sql
create user jds identified by jds default tablespace jds_tabspace temporary tablespace jds_tabspace_temp
```

# Assign necessary rights
```sql
grant create session to jds;
grant create table to jds;
grant create view to jds;
grant create procedure to jds;
grant create trigger to jds;
grant unlimited tablespace to jds;
```

# Use this to drop ALL your tables
```sql
select 'drop table '||table_name||' cascade constraints;' from user_tables;
```

# Use this to drop ALL your objects
```sql
select 'drop '||object_type||' '|| object_name || ';' from user_objects where object_type in ('VIEW','PACKAGE','SEQUENCE', 'PROCEDURE', 'FUNCTION', 'INDEX')
```