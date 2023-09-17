DROP TABLE IF EXISTS USERS, REQUESTS, ITEMS, BOOKINGS, COMMENTS;

CREATE TABLE IF NOT EXISTS USERS (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  EMAIL VARCHAR(512) NOT NULL,
  REQUEST_ID BIGINT,
  CONSTRAINT PK_USERS PRIMARY KEY (ID),
  CONSTRAINT UQ_USERS_EMAIL UNIQUE (EMAIL)
);

CREATE TABLE IF NOT EXISTS REQUESTS (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  DESCRIPTION VARCHAR(5512) NOT NULL,
  REQUESTER_ID BIGINT NOT NULL,
  CREATED TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT PK_REQUESTS PRIMARY KEY (ID),
  CONSTRAINT FK_REQUESTS_USERS FOREIGN KEY (REQUESTER_ID) REFERENCES USERS(ID) ON DELETE CASCADE
);

ALTER TABLE USERS
  ADD CONSTRAINT FK_USERS_REQUESTS FOREIGN KEY (REQUEST_ID) REFERENCES REQUESTS(ID) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS ITEMS (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  OWNER_ID BIGINT NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  DESCRIPTION VARCHAR(5512) NOT NULL,
  IS_AVAILABLE BOOLEAN,
  REQUEST_ID BIGINT,
  CONSTRAINT PK_ITEMS PRIMARY KEY (ID),
  CONSTRAINT FK_ITEMS_OWNER FOREIGN KEY (OWNER_ID) REFERENCES USERS(ID) ON DELETE CASCADE
  ,
  CONSTRAINT FK_ITEMS_REQUEST FOREIGN KEY (REQUEST_ID) REFERENCES REQUESTS(ID) ON DELETE CASCADE
);

  CREATE UNIQUE INDEX IF NOT EXISTS OWNER_ID_ITEM_NAME_DESCRIPTION_INDEX
    ON ITEMS (NAME, DESCRIPTION
);

CREATE TABLE IF NOT EXISTS BOOKINGS (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  START_DATE TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  END_DATE TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  ITEM_ID BIGINT NOT NULL,
  BOOKER_ID BIGINT NOT NULL,
  STATUS VARCHAR(52) NOT NULL,
  CONSTRAINT PK_BOOKINGS PRIMARY KEY (ID),
  CONSTRAINT FK_BOOKINGS_ITEM FOREIGN KEY (ITEM_ID) REFERENCES ITEMS(ID) ON DELETE CASCADE,
  CONSTRAINT FK_BOOKINGS_USER FOREIGN KEY (BOOKER_ID) REFERENCES USERS(ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COMMENTS (
  ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  TEXT VARCHAR(252) NOT NULL,
  ITEM_ID BIGINT NOT NULL,
  AUTHOR_ID BIGINT NOT NULL,
  CREATED TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT PK_COMMENTS PRIMARY KEY (ID),
  CONSTRAINT FK_COMMENTS_ITEM FOREIGN KEY (ITEM_ID) REFERENCES ITEMS(ID) ON DELETE CASCADE,
  CONSTRAINT FK_COMMENTS_USER FOREIGN KEY (AUTHOR_ID) REFERENCES USERS(ID) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS TEXT_ITEM_ID_AUTHOR_ID_INDEX
    ON COMMENTS (TEXT, ITEM_ID, AUTHOR_ID
);