package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.DatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: The base provider class that {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
 * extend when generated.
 */
public abstract class BaseContentProvider extends ContentProvider {

    protected Class<? extends DatabaseHolder> moduleClass;

    /**
     * Converts the column into a {@link Property}. This exists since the property method is static and cannot
     * be referenced easily.
     */
    public interface PropertyConverter {
        IProperty fromName(String columnName);
    }

    protected BaseContentProvider() {}

    protected BaseContentProvider(Class<? extends DatabaseHolder> databaseHolderClass) {
        this.moduleClass = databaseHolderClass;
    }

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        // If this is a module, then we need to initialize the module as part
        // of the creation process. We can assume the framework has been general
        // framework has been initialized.
        if (moduleClass != null) {
            FlowManager.initModule(moduleClass);
        }

        return true;
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values) {
        final int[] count = {0};
        TransactionManager.transact(getDatabase().getWritableDatabase(), new Runnable() {
            @Override
            public void run() {
                for (ContentValues contentValues : values) {
                    count[0] += bulkInsert(uri, contentValues);
                }
            }
        });
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return count[0];
    }

    protected abstract String getDatabaseName();

    protected abstract int bulkInsert(Uri uri, ContentValues contentValues);

    protected BaseDatabaseDefinition getDatabase() {
        if (database == null) {
            database = FlowManager.getDatabase(getDatabaseName());
        }
        return database;
    }
}
