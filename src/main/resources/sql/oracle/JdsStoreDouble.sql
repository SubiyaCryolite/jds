CREATE TABLE JdsStoreDouble(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(96),
	Value       BINARY_DOUBLE,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)