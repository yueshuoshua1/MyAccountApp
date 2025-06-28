package com.example.myaccountapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditCostActivity extends AppCompatActivity {

    private EditText etTitle, etMoney;
    private DatePicker ed_date;
    private RadioGroup rgCategory;
    private RadioButton rbIncome, rbExpense;
    private Button btnSave, btnSelectImage;
    private ImageView ivImage;
    private DBHelper dbHelper;

    private String recordId; // 当前编辑的记录ID
    private Uri imageUri; // 图片URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cost);

        initView();

        // 获取传递的记录ID
        recordId = getIntent().getStringExtra("record_id");
        if (recordId != null) {
            loadRecordData(recordId);
        }

        btnSave.setOnClickListener(v -> saveChanges());
        btnSelectImage.setOnClickListener(v -> openImagePicker());
    }

    // 初始化控件
    private void initView() {
        etTitle = findViewById(R.id.et_title);       // 标题输入框
        etMoney = findViewById(R.id.et_money);       // 金额输入框
        ed_date = findViewById(R.id.dp_date);        // 日期选择器，改为 DatePicker
        rgCategory = findViewById(R.id.rg_category); // 分类单选组
        rbIncome = findViewById(R.id.rb_income);     // 收入按钮
        rbExpense = findViewById(R.id.rb_expense);   // 支出按钮
        btnSave = findViewById(R.id.btn_save);       // 保存按钮
        btnSelectImage = findViewById(R.id.btn_select_image); // 选择图片按钮
        ivImage = findViewById(R.id.iv_image);       // 显示图片
        dbHelper = new DBHelper(this);               // 数据库帮助类实例
    }

    // 加载记录数据
    private void loadRecordData(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("account", null, "_id=?", new String[]{id}, null, null, null);

        if (cursor.moveToFirst()) {
            etTitle.setText(cursor.getString(cursor.getColumnIndex("Title")));
            etMoney.setText(cursor.getString(cursor.getColumnIndex("Money")));

            // 解析日期为年、月、日
            String[] dateParts = cursor.getString(cursor.getColumnIndex("Date")).split("-");
            if (dateParts.length == 3) {
                ed_date.updateDate(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2])
                );
            }

            String category = cursor.getString(cursor.getColumnIndex("Category"));
            if ("收入".equals(category)) {
                rbIncome.setChecked(true);
            } else if ("支出".equals(category)) {
                rbExpense.setChecked(true);
            }

            // 加载图片（如果有）
            byte[] imageByteArray = cursor.getBlob(cursor.getColumnIndex("Photo"));
            if (imageByteArray != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                ivImage.setImageBitmap(bitmap);
            }
        }
        cursor.close();
        db.close();
    }

    // 打开图库选择图片
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            ivImage.setImageURI(imageUri);
        }
    }

    // 保存修改
    private void saveChanges() {
        String title = etTitle.getText().toString().trim();
        String money = etMoney.getText().toString().trim();

        // 获取日期
        String date = ed_date.getYear() + "-" + (ed_date.getMonth() + 1) + "-" + ed_date.getDayOfMonth();

        // 获取分类
        String category = rbIncome.isChecked() ? "收入" : "支出";

        if (title.isEmpty() || money.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        // 将图片转换为字节数组
        byte[] imageByteArray = null;
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                imageByteArray = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Title", title);
        values.put("Date", date);
        values.put("Money", money);
        values.put("Category", category);
        if (imageByteArray != null) {
            values.put("Photo", imageByteArray); // 保存图片
        }

        int rows = db.update("account", values, "_id=?", new String[]{recordId});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "记录更新成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // 返回成功标志
            finish(); // 关闭当前页面
        } else {
            Toast.makeText(this, "记录更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void backButton2(View view) {
        // 返回到 MainActivity
        Intent intent = new Intent(EditCostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
