package com.example.myaccountapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 4; // 更新版本号为4
    private static final String DB_NAME = "account_daily.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表，包含分类字段、年月日字段以及照片字段
        String sql = "CREATE TABLE account (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // 主键
                "Title VARCHAR(20), " + // 标题
                "Date VARCHAR(20), " + // 日期
                "Money VARCHAR(20), " + // 金额
                "Category VARCHAR(10) DEFAULT '支出', " + // 分类：收入或支出，默认值为“支出”
                "Year INTEGER, " + // 年份
                "Month INTEGER, " + // 月份
                "Day INTEGER, " + // 日
                "MonthBudget DOUBLE DEFAULT 0.0, " + // 每月预算，默认值为0
                "Photo BLOB" + // 存储照片，BLOB类型
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 处理数据库升级
        if (oldVersion < 2) {
            // 在版本1升级到版本2时，添加Category字段
            String sql = "ALTER TABLE account ADD COLUMN Category VARCHAR(10) DEFAULT '支出';";
            db.execSQL(sql);
        }
        if (oldVersion < 3) {
            // 在版本2升级到版本3时，添加年月日字段，并添加MonthBudget字段
            String sql1 = "ALTER TABLE account ADD COLUMN Year INTEGER;";
            String sql2 = "ALTER TABLE account ADD COLUMN Month INTEGER;";
            String sql3 = "ALTER TABLE account ADD COLUMN Day INTEGER;";
            String sql4 = "ALTER TABLE account ADD COLUMN MonthBudget DOUBLE DEFAULT 0.0;";
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.execSQL(sql3);
            db.execSQL(sql4);
        }
        if (oldVersion < 4) {
            // 在版本3升级到版本4时，添加照片字段
            String sql = "ALTER TABLE account ADD COLUMN Photo BLOB;";
            db.execSQL(sql);
        }
    }
}
