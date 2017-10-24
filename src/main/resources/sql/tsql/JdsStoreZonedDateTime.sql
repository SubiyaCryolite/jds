CREATE TABLE JdsStoreZonedDateTime(
	FieldId         BIGINT,
	Uuid      NVARCHAR(48) NOT NULL,
	Value           DATETIMEOFFSET(7),
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreZonedDateTime_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);