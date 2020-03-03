/*
 * This file is generated by jOOQ.
 */
package online.kheops.auth_server.generated.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import online.kheops.auth_server.generated.Indexes;
import online.kheops.auth_server.generated.Keys;
import online.kheops.auth_server.generated.Public;
import online.kheops.auth_server.generated.tables.records.WebhookAttemptsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WebhookAttempts extends TableImpl<WebhookAttemptsRecord> {

    private static final long serialVersionUID = 80997957;

    /**
     * The reference instance of <code>public.webhook_attempts</code>
     */
    public static final WebhookAttempts WEBHOOK_ATTEMPTS = new WebhookAttempts();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WebhookAttemptsRecord> getRecordType() {
        return WebhookAttemptsRecord.class;
    }

    /**
     * The column <code>public.webhook_attempts.pk</code>.
     */
    public final TableField<WebhookAttemptsRecord, Long> PK = createField("pk", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.webhook_attempts.status</code>.
     */
    public final TableField<WebhookAttemptsRecord, Long> STATUS = createField("status", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.webhook_attempts.time</code>.
     */
    public final TableField<WebhookAttemptsRecord, Timestamp> TIME = createField("time", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>public.webhook_attempts.webhook_trigger_fk</code>.
     */
    public final TableField<WebhookAttemptsRecord, Long> WEBHOOK_TRIGGER_FK = createField("webhook_trigger_fk", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.webhook_attempts.attempt</code>.
     */
    public final TableField<WebhookAttemptsRecord, Long> ATTEMPT = createField("attempt", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.webhook_attempts</code> table reference
     */
    public WebhookAttempts() {
        this(DSL.name("webhook_attempts"), null);
    }

    /**
     * Create an aliased <code>public.webhook_attempts</code> table reference
     */
    public WebhookAttempts(String alias) {
        this(DSL.name(alias), WEBHOOK_ATTEMPTS);
    }

    /**
     * Create an aliased <code>public.webhook_attempts</code> table reference
     */
    public WebhookAttempts(Name alias) {
        this(alias, WEBHOOK_ATTEMPTS);
    }

    private WebhookAttempts(Name alias, Table<WebhookAttemptsRecord> aliased) {
        this(alias, aliased, null);
    }

    private WebhookAttempts(Name alias, Table<WebhookAttemptsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> WebhookAttempts(Table<O> child, ForeignKey<O, WebhookAttemptsRecord> key) {
        super(child, key, WEBHOOK_ATTEMPTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.WEBHOOK_ATTEMPT_PK, Indexes.WEBHOOK_ATTEMPTS_WEBHOOK_TRIGGER_FK_INDEX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<WebhookAttemptsRecord> getPrimaryKey() {
        return Keys.WEBHOOK_ATTEMPT_PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<WebhookAttemptsRecord>> getKeys() {
        return Arrays.<UniqueKey<WebhookAttemptsRecord>>asList(Keys.WEBHOOK_ATTEMPT_PK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<WebhookAttemptsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<WebhookAttemptsRecord, ?>>asList(Keys.WEBHOOK_ATTEMPTS__WEBHOOK_ATTEMPTS_WEBHOOK_TRIGGERS_FK_FKEY);
    }

    public WebhookTriggers webhookTriggers() {
        return new WebhookTriggers(this, Keys.WEBHOOK_ATTEMPTS__WEBHOOK_ATTEMPTS_WEBHOOK_TRIGGERS_FK_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttempts as(String alias) {
        return new WebhookAttempts(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebhookAttempts as(Name alias) {
        return new WebhookAttempts(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public WebhookAttempts rename(String name) {
        return new WebhookAttempts(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public WebhookAttempts rename(Name name) {
        return new WebhookAttempts(name, null);
    }
}
