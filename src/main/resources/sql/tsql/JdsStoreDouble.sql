CREATE TABLE JdsStoreDouble(
	FieldId         BIGINT,
	Uuid      NVARCHAR(96) NOT NULL,
	Value           FLOAT,
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreDouble_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);