CREATE TABLE JdsStoreLong(
	FieldId     BIGINT,
	Uuid  VARCHAR(96),
	Value       BIGINT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);