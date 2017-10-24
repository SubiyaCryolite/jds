CREATE TABLE JdsStoreLong(
	FieldId     BIGINT,
	Uuid  VARCHAR(48),
	Value       BIGINT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);