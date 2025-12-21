package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "game_library.db";
    private static final int DATABASE_VERSION = 2;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_REG_DATE = "registration_date";

    // Таблица игр
    private static final String TABLE_GAMES = "games";
    private static final String COLUMN_GAME_ID = "game_id";
    private static final String COLUMN_GAME_TITLE = "title";
    private static final String COLUMN_GAME_GENRE = "genre";
    private static final String COLUMN_GAME_PLATFORM = "platform";
    private static final String COLUMN_GAME_STATUS = "status";
    private static final String COLUMN_GAME_USER_ID = "user_id";
    private static final String COLUMN_GAME_NOTE = "note";
    private static final String COLUMN_GAME_ADDED_DATE = "added_date";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_NAME + " TEXT NOT NULL,"
            + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_REG_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_GAMES =
            "CREATE TABLE " + TABLE_GAMES + "("
                    + COLUMN_GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_GAME_TITLE + " TEXT NOT NULL,"
                    + COLUMN_GAME_GENRE + " TEXT NOT NULL,"
                    + COLUMN_GAME_PLATFORM + " TEXT NOT NULL,"
                    + COLUMN_GAME_STATUS + " TEXT DEFAULT 'Не начата',"
                    + COLUMN_GAME_USER_ID + " INTEGER NOT NULL,"
                    + COLUMN_GAME_NOTE + " TEXT,"
                    + COLUMN_GAME_ADDED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + COLUMN_GAME_USER_ID + ") REFERENCES "
                    + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_GAMES);
        Log.d(TAG, "База данных создана");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    /**
     * Регистрация нового пользователя
     */
    public long registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }
    
    /**
     * Авторизация пользователя
     */
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
            COLUMN_USER_ID,
            COLUMN_USER_NAME,
            COLUMN_USER_EMAIL,
            COLUMN_REG_DATE
        };
        
        String selection = COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        
        Cursor cursor = db.query(
            TABLE_USERS,
            columns,
            selection,
            selectionArgs,
            null, null, null
        );
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            user.setRegistrationDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REG_DATE)));
            cursor.close();
        }
        
        db.close();
        return user;
    }
    
    /**
     * Проверка существования email
     */
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(
            TABLE_USERS,
            columns,
            selection,
            selectionArgs,
            null, null, null
        );
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    
    /**
     * Получение пользователя по ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
            COLUMN_USER_ID,
            COLUMN_USER_NAME,
            COLUMN_USER_EMAIL,
            COLUMN_REG_DATE
        };
        
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(
            TABLE_USERS,
            columns,
            selection,
            selectionArgs,
            null, null, null
        );
        
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            user.setRegistrationDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REG_DATE)));
            cursor.close();
        }
        
        db.close();
        return user;
    }

    
    /**
     * Добавление игры
     */
    public long addGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GAME_TITLE, game.getTitle());
        values.put(COLUMN_GAME_GENRE, game.getGenre());
        values.put(COLUMN_GAME_PLATFORM, game.getPlatform());
        values.put(COLUMN_GAME_STATUS, game.getStatus());
        values.put(COLUMN_GAME_USER_ID, game.getUserId());
        values.put(COLUMN_GAME_NOTE, game.getNote());
        
        long result = db.insert(TABLE_GAMES, null, values);
        db.close();
        return result;
    }
    
    /**
     * Получение всех игр пользователя
     */
    public List<Game> getUserGames(int userId) {
        List<Game> gameList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] columns = {
            COLUMN_GAME_ID,
            COLUMN_GAME_TITLE,
            COLUMN_GAME_GENRE,
            COLUMN_GAME_PLATFORM,
            COLUMN_GAME_STATUS,
            COLUMN_GAME_USER_ID,
                COLUMN_GAME_NOTE,
            COLUMN_GAME_ADDED_DATE
        };
        
        String selection = COLUMN_GAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = COLUMN_GAME_ADDED_DATE + " DESC";
        
        Cursor cursor = db.query(
            TABLE_GAMES,
            columns,
            selection,
            selectionArgs,
            null, null, orderBy
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Game game = new Game();
                game.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAME_ID)));
                game.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_TITLE)));
                game.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_GENRE)));
                game.setPlatform(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_PLATFORM)));
                game.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_STATUS)));
                game.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAME_USER_ID)));
                game.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_NOTE)));
                game.setAddedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_ADDED_DATE)));
                gameList.add(game);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return gameList;
    }
    
    /**
     * Обновление статуса игры
     */
    public int updateGameStatus(int gameId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GAME_STATUS, status);
        
        String whereClause = COLUMN_GAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(gameId)};
        
        int result = db.update(TABLE_GAMES, values, whereClause, whereArgs);
        db.close();
        return result;
    }
    
    /**
     * Удаление игры
     */
    public int deleteGame(int gameId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_GAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(gameId)};
        
        int result = db.delete(TABLE_GAMES, whereClause, whereArgs);
        db.close();
        return result;
    }
    
    /**
     * Получение игры по ID
     */
    public Game getGameById(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
            COLUMN_GAME_ID,
            COLUMN_GAME_TITLE,
            COLUMN_GAME_GENRE,
            COLUMN_GAME_PLATFORM,
            COLUMN_GAME_STATUS,
            COLUMN_GAME_USER_ID,
                COLUMN_GAME_NOTE,
            COLUMN_GAME_ADDED_DATE
        };
        
        String selection = COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.query(
            TABLE_GAMES,
            columns,
            selection,
            selectionArgs,
            null, null, null
        );
        
        Game game = null;
        if (cursor != null && cursor.moveToFirst()) {
            game = new Game();
            game.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAME_ID)));
            game.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_TITLE)));
            game.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_GENRE)));
            game.setPlatform(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_PLATFORM)));
            game.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_STATUS)));
            game.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAME_USER_ID)));
            game.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_NOTE)));
            game.setAddedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GAME_ADDED_DATE)));
            cursor.close();
        }
        
        db.close();
        return game;
    }
}