CREATE TABLE JdsStoreDouble(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(48),
	Value       BINARY_DOUBLE,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)