CREATE TABLE JdsStoreFloat(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(48),
	Value       BINARY_FLOAT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)