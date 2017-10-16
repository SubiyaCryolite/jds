CREATE TABLE JdsStoreFloat(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       BINARY_FLOAT,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)