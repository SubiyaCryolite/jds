CREATE TABLE JdsStoreBlob(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       BLOB,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)