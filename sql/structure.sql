CREATE TABLE "users" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "name" varchar(80) NOT NULL,
    "lastname" varchar(80) NOT NULL,
    "email" varchar(100) UNIQUE NOT NULL,
    "password" varchar(500) NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp
);
CREATE INDEX "users_index_0" ON "users" ("id");
CREATE SEQUENCE IF NOT EXISTS users_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 2
    OWNED BY public.users.id;


CREATE TABLE "product" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "name" varchar(100) NOT NULL,
    "description" varchar(255) NOT NULL,
    "price" numeric(8,2) NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "product" ADD CONSTRAINT "fk_user_id_product_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "product" ADD CONSTRAINT "fk_user_id_product_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "product" ADD CONSTRAINT "fk_user_id_product_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "producto_index_0" ON "product" ("id");
CREATE SEQUENCE IF NOT EXISTS product_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.product.id;


CREATE TABLE "sale_status" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "status" varchar(100) NOT NULL,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "sale_status" ADD CONSTRAINT "fk_user_id_sale_status_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_status" ADD CONSTRAINT "fk_user_id_sale_status_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_status" ADD CONSTRAINT "fk_user_id_sale_status_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "sale_status_index_0" ON "sale_status" ("id");
CREATE SEQUENCE IF NOT EXISTS sale_status_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.sale_status.id;


CREATE TABLE "sale" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "date" timestamp,
    "total" numeric(8,2),
    "status" bigint NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "sale" ADD CONSTRAINT "fk_status_id_sale_status" FOREIGN KEY ("status") REFERENCES "sale_status" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale" ADD CONSTRAINT "fk_user_id_sale_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale" ADD CONSTRAINT "fk_user_id_sale_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale" ADD CONSTRAINT "fk_user_id_sale_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "sale_index_0" ON "sale" ("id");
CREATE SEQUENCE IF NOT EXISTS sale_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.sale.id;


CREATE TABLE "entry_status" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "status" varchar(100) NOT NULL,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "entry_status" ADD CONSTRAINT "fk_user_id_entry_status_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_status" ADD CONSTRAINT "fk_user_id_entry_status_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_status" ADD CONSTRAINT "fk_user_id_entry_status_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "entry_status_index_0" ON "entry_status" ("id");
CREATE SEQUENCE IF NOT EXISTS entry_status_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.entry_status.id;


CREATE TABLE "entry" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "date" timestamp,
    "total" numeric(8,2),
    "is_initial" boolean,
    "status" bigint NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "entry" ADD CONSTRAINT "fk_status_id_entry_status" FOREIGN KEY ("status") REFERENCES "entry_status" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry" ADD CONSTRAINT "fk_user_id_entry_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry" ADD CONSTRAINT "fk_user_id_entry_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry" ADD CONSTRAINT "fk_user_id_entry_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "entry_index_0" ON "entry" ("id");
CREATE SEQUENCE IF NOT EXISTS entry_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.entry.id;


CREATE TABLE "sale_detail" (
    "id" bigint UNIQUE NOT NULL,
    "sale_id" bigint NOT NULL,
    "product_id" bigint NOT NULL,
    "price_sale" numeric(8,2) NOT NULL,
    "quantity" integer NOT NULL,
    "subtotal" numeric(8,2) NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint,
    PRIMARY KEY ("id", "sale_id", "product_id")
);
ALTER TABLE "sale_detail" ADD CONSTRAINT "fk_product_id_sale_detail" FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_detail" ADD CONSTRAINT "fk_sale_id_sale_detail" FOREIGN KEY ("sale_id") REFERENCES "sale" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_detail" ADD CONSTRAINT "fk_user_id_sale_detail_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_detail" ADD CONSTRAINT "fk_user_id_sale_detail_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "sale_detail" ADD CONSTRAINT "fk_user_id_sale_detail_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "sale_detail_index_0" ON "sale_detail" ("id");
CREATE SEQUENCE IF NOT EXISTS sale_detail_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.sale_detail.id;


CREATE TABLE "entry_detail" (
    "id" bigint UNIQUE NOT NULL,
    "entry_id" bigint NOT NULL,
    "product_id" bigint NOT NULL,
    "price_entry" numeric(8,2) NOT NULL,
    "quantity" integer NOT NULL,
    "subtotal" numeric(8,2) NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint,
    PRIMARY KEY ("id", "entry_id", "product_id")
);
ALTER TABLE "entry_detail" ADD CONSTRAINT "fk_product_id_entry_detail" FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_detail" ADD CONSTRAINT "fk_entry_id_entry_detail" FOREIGN KEY ("entry_id") REFERENCES "entry" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_detail" ADD CONSTRAINT "fk_user_id_entry_detail_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_detail" ADD CONSTRAINT "fk_user_id_entry_detail_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "entry_detail" ADD CONSTRAINT "fk_user_id_entry_detail_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "entry_detail_index_0" ON "entry_detail" ("id");
CREATE SEQUENCE IF NOT EXISTS entry_detail_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 1
    OWNED BY public.entry_detail.id;


CREATE TABLE "roles" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "name" varchar(25) NOT NULL,
    "description" varchar(255) NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "roles" ADD CONSTRAINT "fk_user_id_roles_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "roles" ADD CONSTRAINT "fk_user_id_roles_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "roles" ADD CONSTRAINT "fk_user_id_roles_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "roles_index_0" ON "roles" ("id");
CREATE SEQUENCE IF NOT EXISTS roles_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 4
    OWNED BY public.roles.id;


CREATE TABLE "users_roles" (
    "id" bigint UNIQUE PRIMARY KEY NOT NULL,
    "user_id" bigint NOT NULL,
    "role_id" bigint NOT NULL,
    "active" boolean NOT NULL DEFAULT true,
    "date_created" timestamp NOT NULL,
    "date_updated" timestamp,
    "date_deleted" timestamp,
    "user_created" bigint NOT NULL,
    "user_updated" bigint,
    "user_deleted" bigint
);
ALTER TABLE "users_roles" ADD CONSTRAINT "fk_user_roles_user_id_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "users_roles" ADD CONSTRAINT "fk_user_roles_role_id_roles" FOREIGN KEY ("role_id") REFERENCES "roles" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "users_roles" ADD CONSTRAINT "fk_user_id_user_roles_created" FOREIGN KEY ("user_created") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "users_roles" ADD CONSTRAINT "fk_user_id_user_roles_updated" FOREIGN KEY ("user_updated") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "users_roles" ADD CONSTRAINT "fk_user_id_user_roles_deleted" FOREIGN KEY ("user_deleted") REFERENCES "users" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
CREATE INDEX "users_roles_index_0" ON "users_roles" ("id");
CREATE SEQUENCE IF NOT EXISTS users_roles_sequence
    AS bigint
    INCREMENT BY 1
    START WITH 2
    OWNED BY public.users_roles.id;
