CREATE TABLE transaction (
datetime char(12), -- Dates will be recorded as mmddyyhhmmss
action_type char(8) NOT NULL,
action_amount decimal(7,2) NOT NULL,
memo varchar(64),
primary key (datetime),
CHECK (datetime LIKE "[0-1][0-9][0-3][0-9][0-9][0-9][0-9][0-9][0-5][0-9][0-5][0-9]"));
